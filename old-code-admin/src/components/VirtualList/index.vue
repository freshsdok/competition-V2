<template>
  <RecycleScroller
    ref="scrollerRef"
    class="virtual-list"
    :class="{ 'virtual-list--bordered': bordered }"
    :items="items"
    :item-size="itemSize"
    :key-field="keyField"
    :buffer="buffer"
    v-bind="$attrs"
    @scroll="onScroll"
  >
    <template #default="{ item, index, active }">
      <div
        class="virtual-list-item"
        :class="[
          itemClass,
          { 'is-active': active, 'is-selected': isSelected(item) }
        ]"
        :style="{ height: itemSize + 'px' }"
        @click="handleItemClick(item, index)"
      >
        <slot :item="item" :index="index" :active="active" />
      </div>
    </template>
    <template #before>
      <slot name="before" />
    </template>
    <template #after>
      <slot name="after" />
    </template>
    <template #empty>
      <slot name="empty">
        <div class="virtual-list-empty">暂无数据</div>
      </slot>
    </template>
  </RecycleScroller>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  // 数据列表
  items: {
    type: Array,
    default: () => []
  },
  // 每项高度（固定高度模式）
  itemSize: {
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
  }
})

const emit = defineEmits(['item-click', 'scroll', 'update:scrollTop'])

const scrollerRef = ref(null)

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

// 监听数据变化，滚动到顶部
watch(() => props.items, () => {
  scrollToItem(0)
}, { deep: true })

// 暴露方法
defineExpose({
  scrollToItem,
  scrollToPosition,
  getScroller: () => scrollerRef.value
})
</script>

<style scoped lang="scss">
.virtual-list {
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

.virtual-list--bordered {
  :deep(.virtual-list-item) {
    border-bottom: 1px solid #ebeef5;
    
    &:last-child {
      border-bottom: none;
    }
  }
}

.virtual-list-item {
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

.virtual-list-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100px;
  color: #909399;
  font-size: 14px;
}

// 滚动条样式
.virtual-list {
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
