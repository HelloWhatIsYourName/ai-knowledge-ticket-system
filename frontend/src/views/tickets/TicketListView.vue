<script setup lang="ts">
import { onMounted, ref } from 'vue'
import EmptyState from '../../components/common/EmptyState.vue'
import ErrorState from '../../components/common/ErrorState.vue'
import LoadingState from '../../components/common/LoadingState.vue'
import { listMyTickets, type TicketPriority, type TicketStatus, type TicketSummary } from '../../api/tickets'

const tickets = ref<TicketSummary[]>([])
const loading = ref(true)
const error = ref('')

const statusLabel: Record<TicketStatus, string> = {
  PENDING: '待处理',
  PROCESSING: '处理中',
  RESOLVED: '已解决',
  CLOSED: '已关闭'
}

const priorityLabel: Record<TicketPriority, string> = {
  LOW: '低',
  MEDIUM: '中',
  HIGH: '高',
  URGENT: '紧急'
}

function formatStatus(status: TicketStatus) {
  return statusLabel[status] ?? status
}

function formatPriority(priority?: TicketPriority | null) {
  return priority ? priorityLabel[priority] : '未定'
}

function formatSource(source?: string) {
  return source === 'AI_SESSION' ? 'AI 会话' : '人工创建'
}

function formatDate(value?: string) {
  if (!value) {
    return '未记录'
  }

  return value.replace('T', ' ').slice(0, 16)
}

onMounted(async () => {
  try {
    tickets.value = await listMyTickets()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '工单加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="ticket-list-view">
    <header class="workspace-page-header">
      <div>
        <p>Ticket workspace</p>
        <h3>我的工单</h3>
      </div>
      <span>从 AI 会话转入的问题会在这里继续跟踪</span>
    </header>

    <LoadingState v-if="loading" message="正在加载工单" />
    <ErrorState v-else-if="error" :message="error" />
    <EmptyState v-else-if="tickets.length === 0" message="暂无工单" />

    <section v-else class="ticket-table" aria-label="我的工单列表">
      <div class="ticket-row ticket-row-head">
        <span>编号</span>
        <span>标题</span>
        <span>状态</span>
        <span>优先级</span>
        <span>来源</span>
        <span>创建时间</span>
      </div>
      <article v-for="ticket in tickets" :key="ticket.id" class="ticket-row">
        <RouterLink class="ticket-no" :to="`/app/tickets/${ticket.id}`">{{ ticket.ticketNo }}</RouterLink>
        <span>
          <strong>{{ ticket.title }}</strong>
          <small>{{ ticket.transferReason || '暂无转人工原因' }}</small>
        </span>
        <span>
          <mark class="ticket-chip">{{ formatStatus(ticket.status) }}</mark>
        </span>
        <span>{{ formatPriority(ticket.priority) }}</span>
        <span>{{ formatSource(ticket.source) }}</span>
        <span>{{ formatDate(ticket.createdAt) }}</span>
      </article>
    </section>
  </section>
</template>
