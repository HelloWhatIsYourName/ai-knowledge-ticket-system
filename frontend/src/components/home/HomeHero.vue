<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { gsap } from 'gsap'

const heroRef = ref<HTMLElement | null>(null)
let context: gsap.Context | undefined

onMounted(() => {
  const reduceMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
  if (!heroRef.value || reduceMotion) {
    return
  }

  context = gsap.context(() => {
    const timeline = gsap.timeline({ defaults: { ease: 'expo.out' } })
    timeline
      .fromTo('.home-red-visual', { scale: 0.86, autoAlpha: 0 }, { scale: 1, autoAlpha: 1, duration: 1.2 })
      .fromTo('.home-red-plane', { y: 44, rotate: -8, autoAlpha: 0 }, { y: 0, rotate: 0, autoAlpha: 1, duration: 1.1, stagger: 0.08 }, '-=0.75')
      .fromTo('.home-red-line', { scaleX: 0, autoAlpha: 0 }, { scaleX: 1, autoAlpha: 1, duration: 0.8, stagger: 0.06 }, '-=0.75')

    gsap.to('.home-red-plane.is-primary', {
      y: -18,
      rotate: 1.2,
      duration: 4.8,
      ease: 'sine.inOut',
      repeat: -1,
      yoyo: true
    })
    gsap.to('.home-red-plane.is-secondary', {
      y: 16,
      rotate: -1.4,
      duration: 5.6,
      ease: 'sine.inOut',
      repeat: -1,
      yoyo: true
    })
  }, heroRef.value)
})

onBeforeUnmount(() => {
  context?.revert()
})
</script>

<template>
  <section
    ref="heroRef"
    class="component component--herosolutions home-solution-hero"
    data-component="herosolutions"
    aria-labelledby="home-hero-title"
  >
    <header class="home-nav" aria-label="Public navigation">
      <RouterLink class="home-brand" to="/">AI Knowledge Ticket</RouterLink>
      <nav class="home-nav-links">
        <a href="#workflow">Workflow</a>
        <a href="#trust">Capability</a>
        <a href="#scenarios">Scenarios</a>
      </nav>
      <RouterLink class="home-nav-login" to="/login">Login</RouterLink>
    </header>

    <div class="container --full">
      <div id="herosolutions-svg" class="image --color home-red-visual" data-motion="red-hero" aria-hidden="true">
        <div class="home-red-plane is-primary"></div>
        <div class="home-red-plane is-secondary"></div>
        <div class="home-red-plane is-tertiary"></div>
        <div class="home-red-line line-a"></div>
        <div class="home-red-line line-b"></div>
        <div class="home-red-line line-c"></div>
      </div>

      <h2 id="home-hero-title" class="title display --3xlarge">
        <span>AI 知识库问答与</span>
        <span>工单协同处理平台</span>
      </h2>

      <div class="text-block-left">
        <div class="wysiwyg text --large --bold">
          <p>先用知识库回答问题，再把复杂问题流转成可追踪工单。适合需要沉淀知识、控制权限并复盘服务质量的团队。</p>
        </div>
        <div class="home-hero-actions" aria-label="Homepage actions">
          <RouterLink class="button --primary" to="/login">
            <span>进入系统</span>
          </RouterLink>
          <a class="link --small" href="#workflow">
            <span>查看流程</span>
          </a>
        </div>
      </div>

      <div class="details" aria-label="Product facts">
        <div class="group">
          <div class="title text --xsmall --uppercase">核心链路</div>
          <div class="wysiwyg text --bold --xsmall --na">RAG + Ticket</div>
        </div>
        <div class="group">
          <div class="title text --xsmall --uppercase">知识检索</div>
          <div class="wysiwyg text --bold --xsmall --na">Oracle Vector</div>
        </div>
        <div class="group">
          <div class="title text --xsmall --uppercase">回复记录</div>
          <div class="wysiwyg text --bold --xsmall --na">ticket_comment</div>
        </div>
        <div class="group">
          <div class="title text --xsmall --uppercase">权限</div>
          <div class="wysiwyg text --bold --xsmall --na">RBAC</div>
        </div>
        <div class="group">
          <div class="title text --xsmall --uppercase">统计</div>
          <div class="wysiwyg text --bold --xsmall --na">Admin APIs</div>
        </div>
      </div>
    </div>
  </section>
</template>
