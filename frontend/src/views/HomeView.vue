<script setup lang="ts">
import HomeCapabilityTable from '../components/home/HomeCapabilityTable.vue'
import HomeFooter from '../components/home/HomeFooter.vue'
import HomeHero from '../components/home/HomeHero.vue'
import HomeMetrics from '../components/home/HomeMetrics.vue'
import HomeNarrative from '../components/home/HomeNarrative.vue'
</script>

<template>
  <div class="home-page">
    <HomeHero />
    <HomeMetrics />
    <HomeNarrative />
    <HomeCapabilityTable />
    <HomeFooter />
  </div>
</template>
