<template>
  <div class="app">
      <div :class="['app-glb-container flex-1',routerName]">
        <TheHeader />
        <RouterView />
      </div>
      <TheFooter />
      <RightBar />
  </div>
</template>
<script setup>
import RightBar from './rightBar.vue'
import { RouterView } from 'vue-router'
import TheHeader from '@/components/layout/TheHeader.vue'
import TheFooter from '@/components/layout/TheFooter.vue'
import { useRoute  } from 'vue-router'

let route = useRoute()
let routerName = $computed(() => {
  return route?.name
})
</script>
<style>
</style>
