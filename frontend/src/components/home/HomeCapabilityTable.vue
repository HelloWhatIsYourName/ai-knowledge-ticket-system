<script setup lang="ts">
const rows = [
  ['AI 问答', '快速获得可追溯答案', 'RAG chat API, citation list'],
  ['工单流转', '复杂问题进入处理队列', 'ticket, ticket_comment'],
  ['知识管理', '维护材料并生成向量', 'Oracle Vector, embedding provider'],
  ['权限管理', '管理员和普通用户隔离', 'RBAC menus and permissions'],
  ['统计分析', '看见服务质量和热点', 'admin statistics APIs']
]
</script>

<template>
  <section id="trust" class="home-capability" aria-labelledby="capability-title">
    <div class="home-section-heading">
      <h2 id="capability-title">可信的系统边界</h2>
      <p>首页展示能力，功能区承接真实后端契约。</p>
    </div>

    <div class="home-table-wrap">
      <table class="home-capability-table">
        <thead>
          <tr>
            <th>Capability</th>
            <th>User Value</th>
            <th>System Contract</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in rows" :key="row[0]">
            <th scope="row">{{ row[0] }}</th>
            <td>{{ row[1] }}</td>
            <td>{{ row[2] }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>
