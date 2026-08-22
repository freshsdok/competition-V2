<template>
  <!-- 水印层 - 使用 fixed 定位，不随滚动移动 -->
  <div ref="watermarkRef" class="pdf-watermark" :style="watermarkStyle">
    <div
      v-for="(item, index) in watermarkItems"
      :key="index"
      class="watermark-item"
      :style="getItemStyle(item)"
    >
      {{ text }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  // 水印文字
  text: {
    type: String,
    default: '内部资料 请勿外传'
  },
  // 字体大小
  fontSize: {
    type: Number,
    default: 14
  },
  // 字体颜色
  color: {
    type: String,
    default: 'rgba(0, 0, 0, 0.2)'
  },
  // 旋转角度
  rotate: {
    type: Number,
    default: 30
  },
  // 水平间距
  gapX: {
    type: Number,
    default: 250
  },
  // 垂直间距
  gapY: {
    type: Number,
    default: 200
  },
  // 是否开启防护
  protect: {
    type: Boolean,
    default: true
  }
})

const watermarkRef = ref(null)
let observer = null
let checkTimer = null

// 水印样式 - 使用 absolute 定位，只覆盖父容器（PDF区域）
const watermarkStyle = computed(() => ({
  position: 'absolute',
  top: 0,
  left: '20px',      // 左侧留出 padding 空间
  right: '30px',     // 右侧留出滚动条空间
  bottom: 0,
  overflow: 'hidden',
  pointerEvents: 'none',
  zIndex: 9999,
  userSelect: 'none'
}))

// 生成水印网格 - 根据父容器尺寸计算
const watermarkItems = computed(() => {
  const items = []

  // 获取父容器尺寸（如果拿不到则使用窗口尺寸）
  const parentWidth = watermarkRef.value?.parentElement?.clientWidth || window.innerWidth
  const parentHeight = watermarkRef.value?.parentElement?.clientHeight || window.innerHeight

  const rows = Math.ceil(parentHeight / props.gapY) + 2
  const cols = Math.ceil(parentWidth / props.gapX) + 2

  for (let i = 0; i < rows; i++) {
    for (let j = 0; j < cols; j++) {
      items.push({
        row: i,
        col: j,
        offsetX: (i % 2) * (props.gapX / 2) // 交错排列
      })
    }
  }
  return items
})

// 单个水印项样式
function getItemStyle(item) {
  return {
    position: 'absolute',
    left: `${item.col * props.gapX + item.offsetX}px`,
    top: `${item.row * props.gapY}px`,
    fontSize: `${props.fontSize}px`,
    color: props.color,
    transform: `rotate(${props.rotate}deg)`,
    whiteSpace: 'nowrap',
    fontWeight: '500'
  }
}

// 检测并恢复水印
function checkAndRestore() {
  if (!watermarkRef.value || !props.protect) return

  // 检查水印是否被删除或隐藏
  const isRemoved = !document.body.contains(watermarkRef.value)
  const isHidden = watermarkRef.value.style.display === 'none'
  const styles = window.getComputedStyle(watermarkRef.value)
  const isInvisible = styles.opacity === '0' || styles.visibility === 'hidden'

  if (isRemoved || isHidden || isInvisible) {
    restoreWatermark()
  }
}

// 恢复水印
function restoreWatermark() {
  if (!watermarkRef.value) return

  // 重置样式
  watermarkRef.value.style.display = ''
  watermarkRef.value.style.opacity = '1'
  watermarkRef.value.style.visibility = 'visible'

  // 如果节点被删除，需要重新插入
  if (!document.body.contains(watermarkRef.value)) {
    location.reload() // 极端情况刷新页面
  }
}

// 启动防护
function startProtection() {
  if (!props.protect) return

  // 1. MutationObserver 监控
  if ('MutationObserver' in window) {
    observer = new MutationObserver((mutations) => {
      // 使用 requestAnimationFrame 节流
      requestAnimationFrame(() => {
        checkAndRestore()
      })
    })

    // 只监控水印父节点
    if (watermarkRef.value?.parentElement) {
      observer.observe(watermarkRef.value.parentElement, {
        childList: true,
        subtree: false
      })
    }
  }

  // 2. 定时检测（页面可见时才执行）
  startIntervalCheck()

  // 3. 监听页面可见性
  document.addEventListener('visibilitychange', handleVisibilityChange)

  // 4. 监听窗口大小变化，重新计算水印
  window.addEventListener('resize', handleResize)
}

// 窗口大小变化处理
function handleResize() {
  // 重新渲染水印（通过修改 key 或强制更新）
  if (watermarkRef.value) {
    watermarkRef.value.style.display = 'none'
    requestAnimationFrame(() => {
      if (watermarkRef.value) {
        watermarkRef.value.style.display = ''
      }
    })
  }
}

// 定时检测
function startIntervalCheck() {
  if (document.visibilityState === 'hidden') return

  // 使用更长的检测间隔（30秒），减少资源占用
  checkTimer = setInterval(() => {
    // 页面空闲时才执行检测
    if ('requestIdleCallback' in window) {
      requestIdleCallback(checkAndRestore, { timeout: 5000 })
    } else {
      checkAndRestore()
    }
  }, 30000) // 30秒间隔，减少频繁检测带来的性能开销
}

// 页面可见性变化处理
function handleVisibilityChange() {
  if (document.visibilityState === 'hidden') {
    clearInterval(checkTimer)
  } else {
    startIntervalCheck()
  }
}

// 停止防护
function stopProtection() {
  if (observer) {
    observer.disconnect()
    observer = null
  }
  clearInterval(checkTimer)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener('resize', handleResize)
}

onMounted(() => {
  if (props.protect) {
    startProtection()
  }
})

onBeforeUnmount(() => {
  stopProtection()
})
</script>

<style scoped>
.pdf-watermark {
  /* 防止被选中 */
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;

  /* 防止被拖拽 */
  -webkit-user-drag: none;
  user-drag: none;
}

.watermark-item {
  /* 防止文字被复制 */
  -webkit-touch-callout: none;
  pointer-events: none;
}
</style>
