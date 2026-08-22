<template>
  <div class="app">
      <RouterView />
  </div>
</template>
<script setup>
import { RouterView } from 'vue-router'
import { useCounterStore } from "@/stores/index";
const counterStore = useCounterStore();

counterStore.getApiMenu()
</script>


<style>

</style>
