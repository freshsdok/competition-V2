<template>
  <div class="app">
      <RouterView />
      <RightBar />
  </div>
</template>
<script setup>
import RightBar from './rightBar.vue'
import { RouterView } from 'vue-router'
</script>
<style>
</style>
