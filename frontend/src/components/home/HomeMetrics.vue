<script setup lang="ts">
const metrics = ['RAG 问答', '向量检索', '工单闭环', 'RBAC 权限', '管理统计']
</script>

<template>
  <section id="product" class="home-metrics" aria-label="Product capabilities">
    <div v-for="metric in metrics" :key="metric" class="home-metric">
      {{ metric }}
    </div>
  </section>
</template>
