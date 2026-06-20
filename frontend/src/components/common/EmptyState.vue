<script setup lang="ts">
defineProps<{
  message: string
}>()
</script>

<template>
  <div class="state-panel empty">
    {{ message }}
  </div>
</template>
