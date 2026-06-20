<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'

gsap.registerPlugin(ScrollTrigger)

const sectionRef = ref<HTMLElement | null>(null)
let context: gsap.Context | undefined

const gallery = [
  { src: '/home-assets/gallery-answer.jpg', alt: 'Knowledge answer workspace', ratio: '1' },
  { src: '/home-assets/gallery-routing.jpg', alt: 'Ticket routing workspace', ratio: '1.5' },
  { src: '/home-assets/gallery-knowledge.jpg', alt: 'Knowledge source workspace', ratio: '0.8' },
  { src: '/home-assets/gallery-admin.jpg', alt: 'Admin review workspace', ratio: '0.8135593220339' }
]

onMounted(() => {
  const reduceMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
  if (!sectionRef.value || reduceMotion) {
    return
  }

  context = gsap.context(() => {
    gsap.fromTo(
      '.component--gallerysticky .item',
      { autoAlpha: 0, y: 34 },
      {
        autoAlpha: 1,
        y: 0,
        duration: 0.9,
        ease: 'power3.out',
        stagger: 0.08,
        scrollTrigger: {
          trigger: sectionRef.value,
          start: 'top 72%',
          once: true
        }
      }
    )
  }, sectionRef.value)
})

onBeforeUnmount(() => {
  context?.revert()
})
</script>

<template>
  <section
    id="workflow"
    ref="sectionRef"
    class="component component--gallerysticky small"
    data-component="gallerysticky"
    aria-labelledby="workflow-title"
  >
    <div class="container --full">
      <div class="columns">
        <div v-for="(image, index) in gallery" :key="image.src" :class="['item', `item-${index}`]">
          <div class="image" :style="{ aspectRatio: image.ratio }">
            <img :src="image.src" :alt="image.alt" loading="lazy" />
          </div>
        </div>
      </div>

      <div class="text-block-left">
        <div id="workflow-title" class="title display --large --fill">先回答，再流转</div>
        <div class="wysiwyg text --large --bold">
          <p>用户问题先进入知识库检索，系统生成带引用的回答。未解决的问题再进入工单，由客服补充回复或内部备注。</p>
        </div>
        <div class="actions">
          <div class="link">
            <div class="text">用户问题</div>
          </div>
          <div class="link">
            <div class="text">知识检索</div>
          </div>
          <div class="link">
            <div class="text">AI 回答</div>
          </div>
          <div class="link">
            <div class="text">转入工单</div>
          </div>
        </div>
        <div class="actions secondary">
          <div class="link">
            <div class="text">知识库可追溯</div>
          </div>
          <div class="link">
            <div class="text">工单闭环</div>
          </div>
          <div class="link">
            <div class="text">管理端看得见</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
