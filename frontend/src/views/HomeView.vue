<script setup lang="ts">
import HomeCapabilityTable from '../components/home/HomeCapabilityTable.vue'
import HomeFooter from '../components/home/HomeFooter.vue'
import HomeHero from '../components/home/HomeHero.vue'
import HomeNarrative from '../components/home/HomeNarrative.vue'
import HomeScenarioDropdowns from '../components/home/HomeScenarioDropdowns.vue'
</script>

<template>
  <div class="home-page">
    <HomeHero />
    <HomeNarrative />
    <HomeCapabilityTable />
    <HomeScenarioDropdowns />
    <HomeFooter />
  </div>
</template>
