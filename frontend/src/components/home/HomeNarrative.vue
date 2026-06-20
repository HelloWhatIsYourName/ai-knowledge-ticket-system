<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

const steps = [
  {
    title: '用户问题',
    body: '用户先提出自然语言问题，系统保留问题上下文和身份信息。'
  },
  {
    title: '知识检索',
    body: '后端基于 Oracle Vector 找到相关知识片段，保留引用来源。'
  },
  {
    title: 'AI 回答',
    body: '模型生成可追溯回答，用户能看到答案依据。'
  },
  {
    title: '转入工单',
    body: '未解决或需要人工介入的问题进入工单流转。'
  },
  {
    title: '管理统计',
    body: '管理员查看热门问题、分类分布和处理效率。'
  }
]

const sectionRef = ref<HTMLElement | null>(null)
let observer: IntersectionObserver | undefined

onMounted(() => {
  if (!sectionRef.value || !('IntersectionObserver' in window)) {
    return
  }

  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible')
        }
      })
    },
    { threshold: 0.2 }
  )

  sectionRef.value.querySelectorAll('[data-reveal]').forEach((element) => {
    observer?.observe(element)
  })
})

onBeforeUnmount(() => {
  observer?.disconnect()
})
</script>

<template>
  <section id="workflow" ref="sectionRef" class="home-narrative" aria-labelledby="workflow-title">
    <div class="home-section-heading" data-reveal>
      <h2 id="workflow-title">先回答，再流转</h2>
      <p>从用户问题到管理统计，一条链路闭环。</p>
    </div>

    <div class="home-narrative-grid">
      <article v-for="step in steps" :key="step.title" class="home-narrative-card" data-reveal>
        <span>{{ step.title }}</span>
        <p>{{ step.body }}</p>
      </article>
    </div>

    <div class="home-proof-band" data-reveal>
      <h3>知识库可追溯</h3>
      <p>回答不是孤立文本，它连接引用、相似度、工单记录和权限上下文。</p>
    </div>

    <div class="home-proof-band muted" data-reveal>
      <h3>工单闭环</h3>
      <p>回复、内部备注、状态流转和处理人都进入同一条服务记录。</p>
    </div>

    <div class="home-proof-band" data-reveal>
      <h3>管理端看得见</h3>
      <p>服务质量、知识命中情况和问题热点可以被统计，而不是停留在聊天记录里。</p>
    </div>
  </section>
</template>
