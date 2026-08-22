<template>
  <div class="text-ellipsis-wrapper">
    <el-tooltip
      v-if="isOverflow && showTooltip"
      :content="text"
      placement="top"
      effect="dark"
    >
      <div
        ref="contentEl"
        class="text-ellipsis"
        :style="lineClampStyle"
      >{{ text }}</div>
    </el-tooltip>
    <div
      v-else
      ref="contentEl"
      class="text-ellipsis"
      :style="lineClampStyle"
    >{{ text }}</div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted, nextTick } from 'vue'

const props = defineProps({
  text: { type: String, default: '' },
  lines: { type: Number, default: 1 },
  showTooltip: { type: Boolean, default: true }
})

const contentEl = ref(null)
const isOverflow = ref(false)

const lineClampStyle = computed(() => ({
  display: '-webkit-box',
  WebkitBoxOrient: 'vertical',
  WebkitLineClamp: props.lines,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'normal',
  maxHeight: `${props.lines * 1.5}em`,
  lineHeight: '1.5em',
  cursor: 'default'
}))

const checkOverflow = () => {
  const el = contentEl.value
  if (!el) return
  // 真实文本行高度判断
  isOverflow.value = el.scrollHeight > el.clientHeight + 1
}

watch(() => props.text, async () => {
  await nextTick()
  checkOverflow()
})

watch(() => props.lines, async () => {
  await nextTick()
  checkOverflow()
})

onMounted(() => {
  nextTick(checkOverflow)
})
</script>

<style scoped lang="scss">
.text-ellipsis-wrapper {
  width: 100%;
}
.text-ellipsis {
  width: 100%;
  overflow: hidden;
}
</style>