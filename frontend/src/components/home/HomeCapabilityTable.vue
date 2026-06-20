<script setup lang="ts">
const rows = [
  {
    name: 'AI 问答',
    objective: '快速回答重复问题，并保留答案引用。',
    criteria: 'RAG chat API, knowledge chunks, citation list'
  },
  {
    name: '工单流转',
    objective: '复杂问题进入处理队列，回复和内部备注都有记录。',
    criteria: 'ticket, ticket_comment, status transition'
  },
  {
    name: '知识管理',
    objective: '维护资料、切分内容并生成向量。',
    criteria: 'Oracle Vector, embedding provider, source metadata'
  },
  {
    name: '权限管理',
    objective: '普通用户、客服和管理员看到不同操作边界。',
    criteria: 'RBAC menus, route guards, backend permissions'
  },
  {
    name: '统计分析',
    objective: '看见热门问题、分类分布和处理效率。',
    criteria: 'admin statistics APIs, dashboard charts'
  }
]
</script>

<template>
  <section id="trust" class="component component--tablethreecols" data-component="tablethreecols" aria-labelledby="capability-title">
    <div class="container --full">
      <div class="waves" aria-hidden="true"></div>

      <div class="columns">
        <div class="item">
          <div class="label">能力矩阵</div>
          <div class="wysiwyg text --medium --bold">
            <p>首页展示的是可验证的产品边界，功能区承接真实后端契约。</p>
          </div>
        </div>
        <div class="item">
          <div id="capability-title" class="title display --2xlarge --bold --fill">
            可信的<br />
            系统边界
          </div>
        </div>
      </div>

      <div class="table" role="table" aria-label="Capability matrix">
        <div class="head" role="rowgroup">
          <div class="row" role="row">
            <div class="cell" role="columnheader">
              <div class="label">能力</div>
            </div>
            <div class="cell" role="columnheader">
              <div class="label">用户价值</div>
            </div>
            <div class="cell" role="columnheader">
              <div class="label">系统契约</div>
            </div>
          </div>
        </div>
        <div class="body" role="rowgroup">
          <div v-for="row in rows" :key="row.name" class="row" role="row">
            <div class="cell" role="cell">
              <div class="title display --small --bold">{{ row.name }}</div>
            </div>
            <div class="cell" role="cell">
              <div data-mobile>
                <div class="label">用户价值</div>
              </div>
              <div class="wysiwyg text --medium --bold">{{ row.objective }}</div>
            </div>
            <div class="cell" role="cell">
              <div data-mobile>
                <div class="label">系统契约</div>
              </div>
              <div class="wysiwyg text --medium">{{ row.criteria }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
