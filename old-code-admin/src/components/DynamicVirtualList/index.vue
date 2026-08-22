<template>
  <DynamicScroller
    ref="scrollerRef"
    class="dynamic-virtual-list"
    :class="{ 'dynamic-virtual-list--bordered': bordered }"
    :items="items"
    :min-item-size="minItemSize"
    :key-field="keyField"
    :buffer="buffer"
    v-bind="$attrs"
    @scroll="onScroll"
  >
    <template #default="{ item, index, active }">
      <DynamicScrollerItem
        :item="item"
        :active="active"
        :size-dependencies="getSizeDependencies(item)"
        :data-index="index"
      >
        <div
          class="dynamic-virtual-list-item"
          :class="[
            itemClass,
            { 'is-active': active, 'is-selected': isSelected(item) }
          ]"
          @click="handleItemClick(item, index)"
        >
          <slot :item="item" :index="index" :active="active" />
        </div>
      </DynamicScrollerItem>
    </template>
    <template #before>
      <slot name="before" />
    </template>
    <template #after>
      <slot name="after" />
    </template>
    <template #empty>
      <slot name="empty">
        <div class="dynamic-virtual-list-empty">暂无数据</div>
      </slot>
    </template>
  </DynamicScroller>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  // 数据列表
  items: {
    type: Array,
    default: () => []
  },
  // 最小项高度
  minItemSize: {
    type: Number,
    default: 60
  },
  // 唯一标识字段
  keyField: {
    type: String,
    default: 'id'
  },
  // 缓冲区大小
  buffer: {
    type: Number,
    default: 5
  },
  // 列表项自定义class
  itemClass: {
    type: String,
    default: ''
  },
  // 是否显示边框
  bordered: {
    type: Boolean,
    default: true
  },
  // 选中项的key值（单选）或key数组（多选）
  selectedKeys: {
    type: [String, Number, Array],
    default: null
  },
  // 选中判断字段
  selectedField: {
    type: String,
    default: 'id'
  },
  // 高度依赖的字段数组（当这些字段变化时，重新计算高度）
  sizeDependencies: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['item-click', 'scroll', 'update:scrollTop'])

const scrollerRef = ref(null)

// 获取高度依赖数据
function getSizeDependencies(item) {
  // 如果指定了sizeDependencies，使用指定字段
  if (props.sizeDependencies.length > 0) {
    return props.sizeDependencies.map(field => {
      const value = item[field]
      // 如果是数组，返回长度；否则返回值本身
      return Array.isArray(value) ? value.length : value
    })
  }
  // 默认返回空数组，让组件自动计算
  return []
}

// 判断项是否被选中
function isSelected(item) {
  if (!props.selectedKeys) return false
  const key = item[props.selectedField]
  if (Array.isArray(props.selectedKeys)) {
    return props.selectedKeys.includes(key)
  }
  return props.selectedKeys === key
}

// 点击项
function handleItemClick(item, index) {
  emit('item-click', item, index)
}

// 滚动事件
function onScroll(event) {
  emit('scroll', event)
  emit('update:scrollTop', event.target.scrollTop)
}

// 滚动到指定项
function scrollToItem(index) {
  nextTick(() => {
    scrollerRef.value?.scrollToItem(index)
  })
}

// 滚动到指定位置
function scrollToPosition(position) {
  nextTick(() => {
    scrollerRef.value?.$el.scrollTo({ top: position, behavior: 'auto' })
  })
}

// 强制重新计算所有项高度
function forceUpdate() {
  nextTick(() => {
    scrollerRef.value?.forceUpdate()
  })
}

// 监听数据变化，滚动到顶部
watch(() => props.items, () => {
  scrollToItem(0)
}, { deep: true })

// 暴露方法
defineExpose({
  scrollToItem,
  scrollToPosition,
  forceUpdate,
  getScroller: () => scrollerRef.value
})
</script>

<style scoped lang="scss">
.dynamic-virtual-list {
  height: 100%;
  overflow-y: auto;
  
  :deep(.vue-recycle-scroller) {
    position: relative;
    height: 100%;
  }
  
  :deep(.vue-recycle-scroller__item-wrapper) {
    position: relative;
    width: 100%;
    height: 100%;
  }
  
  :deep(.vue-recycle-scroller__item-view) {
    position: absolute;
    left: 0;
    right: 0;
    will-change: transform;
  }
}

.dynamic-virtual-list--bordered {
  :deep(.dynamic-virtual-list-item) {
    border-bottom: 1px solid #ebeef5;
    
    &:last-child {
      border-bottom: none;
    }
  }
}

.dynamic-virtual-list-item {
  box-sizing: border-box;
  width: 100%;
  overflow: hidden;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: #f5f7fa;
  }
  
  &.is-selected {
    background-color: #ecf5ff;
  }
}

.dynamic-virtual-list-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100px;
  color: #909399;
  font-size: 14px;
}

// 滚动条样式
.dynamic-virtual-list {
  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #c0c4cc;
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-track {
    background: #f5f7fa;
  }
}
</style>
