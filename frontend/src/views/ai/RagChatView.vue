<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import EmptyState from '../../components/common/EmptyState.vue'
import ErrorState from '../../components/common/ErrorState.vue'
import LoadingState from '../../components/common/LoadingState.vue'
import {
  askQuestion,
  askQuestionStream,
  listSessionMessages,
  listSessions,
  type AskQuestionRequest,
  type AiMessage,
  type AiSession,
  type RagAnswerResponse
} from '../../api/ragChat'
import {
  createTicketFromAiSession,
  listTicketCategories,
  type TicketCategory,
  type TicketPriority,
  type TicketSummary
} from '../../api/tickets'

const sessions = ref<AiSession[]>([])
const messages = ref<AiMessage[]>([])
const categories = ref<TicketCategory[]>([])
const selectedSessionId = ref<number | undefined>()
const question = ref('')
const topK = ref(4)
const answer = ref<RagAnswerResponse | null>(null)
const loadingSessions = ref(true)
const asking = ref(false)
const transferring = ref(false)
const error = ref('')
const transferMessage = ref('')
const ticketTitle = ref('')
const ticketDescription = ref('')
const ticketCategoryId = ref<number | undefined>()
const ticketPriority = ref<TicketPriority>('MEDIUM')

const confidenceText = computed(() => {
  if (!answer.value) {
    return '0%'
  }

  return `${Math.round(answer.value.confidence * 100)}%`
})

const canTransfer = computed(() => Boolean(answer.value?.sessionId && ticketTitle.value && ticketDescription.value))

async function loadInitialData() {
  loadingSessions.value = true

  try {
    sessions.value = await listSessions()
  } catch {
    sessions.value = []
  } finally {
    loadingSessions.value = false
  }

  try {
    categories.value = await listTicketCategories()
  } catch {
    categories.value = []
  }
}

async function selectSession(sessionId: number) {
  selectedSessionId.value = sessionId
  answer.value = null
  transferMessage.value = ''

  try {
    messages.value = await listSessionMessages(sessionId)
  } catch {
    messages.value = []
  }
}

async function submitQuestion() {
  const normalizedQuestion = question.value.trim()

  if (!normalizedQuestion || asking.value) {
    return
  }

  asking.value = true
  error.value = ''
  transferMessage.value = ''

  try {
    const request: AskQuestionRequest = {
      question: normalizedQuestion,
      sessionId: selectedSessionId.value,
      topK: topK.value
    }
    const result = await askQuestionWithStreamFallback(request)

    applyAnswer(result, normalizedQuestion)
    await refreshSessions()
  } catch {
    error.value = '问答请求失败，请稍后重试。'
  } finally {
    asking.value = false
  }
}

async function askQuestionWithStreamFallback(request: AskQuestionRequest): Promise<RagAnswerResponse> {
  let streamedAnswer = ''

  try {
    return await askQuestionStream(request, {
      onToken: (token) => {
        streamedAnswer += token
        answer.value = {
          sessionId: selectedSessionId.value ?? 0,
          userMessageId: 0,
          assistantMessageId: 0,
          answer: streamedAnswer,
          canAnswer: true,
          confidence: 0,
          transferSuggested: false,
          transferReason: null,
          citations: []
        }
      }
    })
  } catch {
    return askQuestion(request)
  }
}

function applyAnswer(result: RagAnswerResponse, normalizedQuestion: string) {
  answer.value = result
  selectedSessionId.value = result.sessionId
  ticketTitle.value = normalizedQuestion.slice(0, 80)
  ticketDescription.value = `${normalizedQuestion}\n\nAI 回答：${result.answer}`
  ticketPriority.value = result.transferSuggested ? 'HIGH' : 'MEDIUM'
}

async function refreshSessions() {
  try {
    sessions.value = await listSessions()
  } catch {
    sessions.value = []
  }
}

async function submitTransfer() {
  if (!answer.value || !canTransfer.value || transferring.value) {
    return
  }

  transferring.value = true
  transferMessage.value = ''

  try {
    const ticket: TicketSummary = await createTicketFromAiSession({
      sessionId: answer.value.sessionId,
      assistantMessageId: answer.value.assistantMessageId,
      title: ticketTitle.value.trim(),
      description: ticketDescription.value.trim(),
      categoryId: ticketCategoryId.value,
      priority: ticketPriority.value,
      transferReason: answer.value.transferReason ?? undefined
    })

    transferMessage.value = `已创建工单 ${ticket.ticketNo}`
  } catch {
    transferMessage.value = '转工单失败，请稍后重试。'
  } finally {
    transferring.value = false
  }
}

onMounted(loadInitialData)
</script>

<template>
  <section class="rag-workspace">
    <header class="workspace-page-header">
      <div>
        <p>AI workspace</p>
        <h3>AI 问答工作台</h3>
      </div>
      <span>知识库回答 · 引用追溯 · 转人工闭环</span>
    </header>

    <div class="rag-layout">
      <aside class="rag-panel rag-session-panel">
        <div class="panel-heading">
          <span>历史会话</span>
          <strong>{{ sessions.length }}</strong>
        </div>
        <LoadingState v-if="loadingSessions" message="正在加载会话" />
        <EmptyState v-else-if="sessions.length === 0" message="暂无历史会话" />
        <button
          v-for="session in sessions"
          v-else
          :key="session.id"
          class="session-item"
          :class="{ 'is-active': session.id === selectedSessionId }"
          type="button"
          @click="selectSession(session.id)"
        >
          <strong>{{ session.title || '未命名会话' }}</strong>
          <span>{{ session.lastQuestion || '暂无最近问题' }}</span>
        </button>
      </aside>

      <main class="rag-panel rag-main-panel">
        <form class="question-form" @submit.prevent="submitQuestion">
          <label for="rag-question">输入用户问题</label>
          <textarea
            id="rag-question"
            v-model="question"
            data-testid="question-input"
            rows="5"
            placeholder="例如：用户无法登录后台，应该如何处理？"
          />
          <div class="question-actions">
            <label>
              检索数量
              <select v-model.number="topK">
                <option :value="3">3</option>
                <option :value="4">4</option>
                <option :value="6">6</option>
              </select>
            </label>
            <button data-testid="ask-button" type="button" :disabled="asking || !question.trim()" @click="submitQuestion">
              {{ asking ? '生成中...' : '发送问题' }}
            </button>
          </div>
        </form>

        <ErrorState v-if="error" :message="error" />
        <EmptyState
          v-else-if="!answer && messages.length === 0"
          message="输入用户问题后，系统会检索知识库并返回带引用的回答。"
        />

        <section v-if="answer" class="answer-panel">
          <div class="answer-meta">
            <span :class="{ 'is-muted': !answer.canAnswer }">{{ answer.canAnswer ? '可回答' : '建议转人工' }}</span>
            <span>置信度 {{ confidenceText }}</span>
            <span v-if="answer.transferSuggested">建议转为工单</span>
          </div>
          <p>{{ answer.answer }}</p>
          <p v-if="answer.transferReason" class="transfer-reason">{{ answer.transferReason }}</p>
        </section>

        <section class="citation-section">
          <div class="panel-heading">
            <span>引用来源</span>
            <strong>{{ answer?.citations.length ?? 0 }}</strong>
          </div>
          <EmptyState v-if="!answer || answer.citations.length === 0" message="暂无引用来源" />
          <article v-for="citation in answer?.citations" v-else :key="citation.citationIndex" class="citation-card">
            <span>#{{ citation.citationIndex }}</span>
            <h4>{{ citation.sourceTitle }}</h4>
            <p>{{ citation.snippet }}</p>
          </article>
        </section>
      </main>

      <aside class="rag-panel transfer-panel">
        <div class="panel-heading">
          <span>转为工单</span>
        </div>
        <p class="panel-copy">当 AI 置信度不足或用户仍需人工处理时，将当前会话转入工单流转。</p>
        <form class="transfer-form" @submit.prevent="submitTransfer">
          <label>
            标题
            <input v-model="ticketTitle" type="text" placeholder="简短描述问题" />
          </label>
          <label>
            描述
            <textarea v-model="ticketDescription" rows="6" placeholder="补充用户问题、AI 回答和人工处理线索" />
          </label>
          <label>
            分类
            <select v-model.number="ticketCategoryId">
              <option :value="undefined">暂不选择</option>
              <option v-for="category in categories" :key="category.id" :value="category.id">
                {{ category.name }}
              </option>
            </select>
          </label>
          <label>
            优先级
            <select v-model="ticketPriority">
              <option value="LOW">低</option>
              <option value="MEDIUM">中</option>
              <option value="HIGH">高</option>
              <option value="URGENT">紧急</option>
            </select>
          </label>
          <button type="submit" :disabled="!canTransfer || transferring">
            {{ transferring ? '创建中...' : '创建工单' }}
          </button>
        </form>
        <p v-if="transferMessage" class="transfer-message">{{ transferMessage }}</p>
      </aside>
    </div>
  </section>
</template>
