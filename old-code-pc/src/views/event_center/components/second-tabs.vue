<template>
  <div class="second-tabs-wrapper">
    <el-tabs 
      v-model="localActiveTab" 
      @tab-click="handleTabClick"
      class="second-tabs"
    >
      <el-tab-pane 
        v-for="tab in tabs" 
        :key="tab.id"
        :label="tab.name" 
        :name="tab.id"
      >
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

// Props 定义
const props = defineProps({
  // 二级选项卡配置数组
  tabs: {
    type: Array,
    required: true,
    default: () => []
  },
  // 当前激活的二级选项卡ID
  activeTab: {
    type: String,
    required: true,
    default: ''
  }
})

// Emits 定义
const emit = defineEmits([
  // 二级选项卡切换事件
  'tab-change',
  'update:activeTab',
])

// 本地激活的选项卡，用于双向绑定
const localActiveTab = ref(props.activeTab)

// 监听外部activeTab变化，更新本地状态
watch(() => props.activeTab, (newVal) => {
  localActiveTab.value = newVal
})

// 监听本地activeTab变化，通知父组件
watch(localActiveTab, (newVal) => {
  emit('update:activeTab', newVal)
  emit('tab-change', newVal)
})

// 处理选项卡点击
const handleTabClick = (tab) => {
  localActiveTab.value = tab.props.name
}
</script>

<style scoped lang="scss">
// 样式变量
$active-color: #3B72FF;
$border-color: #CECECE;
$bg-color: #FFFFFF;
$bg-color-active: #F6F9FF;
$text-primary: #333333;
$transition-speed: 0.3s;

// 二级选项卡容器
.second-tabs-wrapper {
  box-sizing: border-box;
}

// 二级选项卡样式
.second-tabs {
  :deep(.el-tabs__item) {
    width: 250px;
    height: 64px;
    font-size: 20px;
    color: $text-primary;
    background: $bg-color;
    border-radius: 12px;
    border: 1px solid $border-color;
    padding: 0;
    margin-right: 20px;
    transition: all $transition-speed ease;
    &:hover {
      border-color: $active-color;
      background: $bg-color-active;
    }

    &.is-active {
      border-color: $active-color;
      background: $bg-color-active;
    }
  }
  :deep(.el-tabs__active-bar) {
    display: none;
  }
  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }
  :deep(.el-tabs__header) {
    margin-bottom: 45px;
  }
}

</style>