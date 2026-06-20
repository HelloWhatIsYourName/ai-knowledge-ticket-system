<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, type ComponentPublicInstance } from 'vue'
import { gsap } from 'gsap'

const scenarios = [
  {
    title: '用户',
    body: '用自然语言提问，先得到带引用的知识库回答。回答不充分时，可以一键转成工单继续处理。',
    facts: [
      ['入口', '问答页'],
      ['结果', '引用答案'],
      ['升级', '创建工单']
    ]
  },
  {
    title: '客服',
    body: '处理工单时可以对外回复，也可以写内部备注。所有沟通都沉淀在 ticket_comment 里。',
    facts: [
      ['入口', '工单队列'],
      ['动作', '回复/备注'],
      ['记录', '时间线']
    ]
  },
  {
    title: '管理员',
    body: '查看分类、处理效率和知识命中情况，判断哪些问题需要补知识库，哪些流程需要优化。',
    facts: [
      ['入口', '仪表盘'],
      ['指标', '热点/效率'],
      ['权限', 'RBAC']
    ]
  }
]

const activeIndex = ref(0)
const bodyRefs = ref<HTMLElement[]>([])

function setBodyRef(element: Element | ComponentPublicInstance | null, index: number) {
  if (element instanceof HTMLElement) {
    bodyRefs.value[index] = element
  }
}

function setInitialHeights() {
  bodyRefs.value.forEach((body, index) => {
    gsap.set(body, { height: index === activeIndex.value ? 'auto' : 0 })
  })
}

function toggle(index: number) {
  if (index === activeIndex.value) {
    return
  }

  const previous = bodyRefs.value[activeIndex.value]
  const next = bodyRefs.value[index]
  activeIndex.value = index

  if (previous) {
    gsap.to(previous, { height: 0, duration: 0.45, ease: 'power3.out' })
  }

  if (next) {
    gsap.fromTo(next, { height: 0 }, { height: 'auto', duration: 0.55, ease: 'power3.out' })
  }
}

onMounted(async () => {
  await nextTick()
  setInitialHeights()
})

onBeforeUnmount(() => {
  bodyRefs.value.forEach((body) => gsap.killTweensOf(body))
})
</script>

<template>
  <section
    id="scenarios"
    class="component component--dropdownssticky"
    data-component="dropdownssticky"
    aria-labelledby="scenario-title"
  >
    <div class="container --medium">
      <div class="columns --0">
        <div class="item">
          <div id="scenario-title" class="label">用户场景</div>
        </div>
        <div class="item" data-desktop>
          <div class="label">处理策略</div>
        </div>
      </div>

      <div class="dropdowns">
        <article
          v-for="(scenario, index) in scenarios"
          :key="scenario.title"
          :class="['dropdown', { 'is-open': activeIndex === index }]"
        >
          <button class="head" type="button" :aria-expanded="activeIndex === index" @click="toggle(index)">
            <div class="columns top">
              <div class="item">
                <div class="wysiwyg display --large --na">
                  <p>{{ scenario.title }}</p>
                </div>
              </div>
              <div class="item last" data-desktop>
                <div class="group">
                  <div class="wysiwyg text --medium">
                    <p>{{ scenario.body }}</p>
                  </div>
                </div>
              </div>
            </div>
            <div :class="['icon --medium --arrow-down', { '--opened': activeIndex === index }]">
              <span></span>
            </div>
          </button>

          <div :ref="(element) => setBodyRef(element, index)" class="body">
            <div class="columns bottom">
              <div class="item" data-mobile>
                <div class="group">
                  <div class="wysiwyg text --medium --na">
                    <p>{{ scenario.body }}</p>
                  </div>
                </div>
              </div>
              <div class="flex">
                <div v-for="fact in scenario.facts" :key="fact[0]" class="item">
                  <div class="wysiwyg text --xsmall --uppercase --na">{{ fact[0] }}</div>
                  <div class="wysiwyg text --xsmall --na">{{ fact[1] }}</div>
                </div>
              </div>
            </div>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>
