<template>
  <div>
    <!-- 根据后台管理可视化出来的页面 -->
    <visualization pageUrl="home" />
    <V2CredentialDiscovery />
  </div>
</template>

<script setup>
import visualization from '@/components/visualization/index.vue'
import V2CredentialDiscovery from '@/components/V2CredentialDiscovery.vue'
onMounted(() => {
});
</script>

<style>
</style>
