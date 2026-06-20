<script setup lang="ts">
defineProps<{
  message: string
}>()
</script>

<template>
  <div class="state-panel error" role="alert">
    {{ message }}
  </div>
</template>
