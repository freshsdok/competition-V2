<template>
  <div class="global-page">
    <!-- 根据后台管理可视化出来的页面 -->
    <visualization  pageUrl="event/browse"/>
  </div>
</template>

<script setup>
import visualization from '@/components/visualization/index.vue'
onMounted(() => {
});
</script>

<style scoped lang="scss">
.global-page{
  :deep(.ds_visualization_page){
    .model-page{
      background: #ffffff;
    }
  }
}
</style>