<template>
    <div class="information">
        <!-- 根据后台管理可视化出来的页面 -->
        <visualization pageUrl="information" />
    </div>
</template>
<script setup>
import visualization from '@/components/visualization/index.vue'
</script>
<style scoped lang="scss">
.information{
  background-color: #F5F5F5 ;
}
</style>