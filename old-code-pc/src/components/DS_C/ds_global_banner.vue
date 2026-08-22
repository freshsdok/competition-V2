<template>
<div class="ds_global_banner" :style="detail.style">
    <el-carousel 
        v-if="bannerList && bannerList.length > 0" 
        :height="detail.height"
        :arrow="bannerList.length > 1 ? 'hover' : 'never'"
        :indicator-position="bannerList.length > 1 ? '' : 'none'"
        :autoplay="detail.autoPlay ? true : false"
        :loop="bannerList.length > 1 && detail.loop ? true : false"
        :interval="detail.interval">
        <el-carousel-item v-for="item in bannerList" :key="item.id">
          <div class="carousel-image-container">
            <el-image :src="item.bannerUrl"
                      fit="cover"
                      class="w-full h-full"></el-image>
          </div>
        </el-carousel-item>
    </el-carousel>
    <template v-else>
      <!-- 骨架屏，用于数据加载时显示 -->
      <el-skeleton 
        animated 
        :style="{ width: '100%', height: detail?.height || '400px' }">
        <template #template>
          <el-skeleton-item
            variant="image"/>
        </template>
      </el-skeleton>
    </template>
</div>
</template>
<script setup>
import { visualizationPageMobile } from '@/api/visualization'
const props = defineProps({
  // 数据
  info: {
    type: Object,
    default: {},
  }
})

// 状态管理
let detail = $ref({})
let bannerList = $ref([])
// API调用函数
const getDsApi = async (detailData) => {
  visualizationPageMobile(detailData.dataSourceUrl).then(res => { 
    if (res.code === 200) {
      bannerList = res.data || []
    } else {
      console.warn('轮播图数据获取失败:', res)
      bannerList = []
    }
  })
}

// 监听props变化
watch(() => props.info, (newVal) => {
  if (newVal) {
    detail = newVal
    if (!detail || !detail.dataSourceUrl) {
      return
    }
    getDsApi(detail)
  }
}, { immediate: true }) // 立即执行，处理初始值

</script>
<style lang="scss" scoped>
.ds_global_banner {
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}
/* 为轮播图片容器添加缩放动画，通过嵌套元素避免与滑动动画冲突 */
:deep(.el-carousel__item.is-active) .carousel-image-container {
  animation: slideUpBackground 5s ease-out;
}

.carousel-image-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

@keyframes slideUpBackground {
  0% {
    transform: scale(1);
  }
  100% {
    transform: scale(1.1);
  }
}
.banner-error {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  width: 100%;
}

.error-content {
  text-align: center;
}

:deep(.el-skeleton) {
  background-color: #f5f7fa;
  border-radius: 4px;
}

.banner-skeleton {
  width: 100%;
  overflow: hidden;
}

.image-skeleton-container {
  width: 100%;
}

:deep(.el-skeleton__image) {
  width: 100% !important;
  height: auto !important;
  background-color: #f5f7fa;
  border-radius: 4px;
}

:deep(.el-carousel__indicators) {
  .el-carousel__indicator{
    .el-carousel__button{
      width: 30px;
      height: 8px;
      background: #FFFFFF;
      border-radius: 10px;
      opacity: 0.5;
    }
  }
  .is-active{
    .el-carousel__button{
      opacity: 1;
    }
  }
}
</style>