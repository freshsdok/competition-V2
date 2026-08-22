<template>
<div class="model-page" :style="detail.style">
  <div @click="handleClick">{{ detail.content }}</div>
</div>
</template>
<script setup>
const props = defineProps({
  // 数据
  info: {
    type: Object,
    default: {},
  }
})
// 点击事件
function handleClick() {
  if (detail.href) {
    window.open(detail.href, '_blank')
  }
}
// 状态管理
let detail = $ref({})
watch(() => props.info, (newVal) => {
  if (newVal) {
    detail = newVal
  }
}, { immediate: true }) // 立即执行，处理初始值
// 超链接组件

</script>
<style lang="scss" scoped>
.model-page {
  width: 100%;
  overflow: hidden;
  .alink {
    width: 100%;
  }
  .underline {
    text-decoration: underline;
  }
}
</style>