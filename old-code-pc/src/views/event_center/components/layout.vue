<template>
  <div class="event-layout">
    <!-- 左侧区域 -->
    <div class="layout-left" v-if="showLeft">
      <slot name="user-info">
        <div class="page-content-left-user flex justify-start align-center">
          <img alt="" :src="userInfo?.avatar" v-if="userInfo?.avatar" class="page-content-left-user-avatar">
          <img alt="" src="@/assets/images/avatar.png" v-else class="page-content-left-user-avatar">
          <div class="page-content-left-user-name flex flex-col justify-center items-center">
            <div class="use-name flex justify-center items-center">{{ userInfo?.nickName }}</div>
          </div>
        </div>
      </slot>
      
      <!-- 一级选项卡导航 -->
      <div class="nav-tabs">
        <div 
          v-for="tab in tabs" 
          :key="tab.id"
          class="nav-tab-item"
          :class="{ 'active': localActiveTab == tab.id }"
          @click="handleTabClick(tab.id)"
        >
          {{ tab.name }}
        </div>
      </div>
    </div>
    
    <!-- 右侧区域 -->
    <div class="layout-right">
      <!-- 顶部标题栏 -->
      <div class="top-bar">
        <div class="top-bar-content">
          <span class="top-bar-title">{{ getCurrentTabName() }}</span>
          <div class="top-bar-line"></div>
        </div>
      </div>
      
      <!-- 内容区域 -->
      <div class="content-section">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

// Props 定义
const props = defineProps({
  // 一级选项卡配置
  tabs: {
    type: Array,
    required: true,
    default: () => []
  },
  // 当前激活的一级选项卡
  activeTab: {
    type: String,
    required: true,
    default: ''
  },
  // 用户信息（可选，用于默认用户信息展示）
  userInfo: {
    type: Object,
    default: () => ({})
  },
  showLeft: {
    type: Boolean,
    default: true
  }
})

// Emits 定义
const emit = defineEmits([
  // 一级选项卡切换事件
  'tab-change',
  'update:activeTab'
])

// 本地响应式变量，用于管理激活的选项卡
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

// 获取当前选中的一级选项卡名称
const getCurrentTabName = () => {
  const currentTab = props.tabs.find(tab => tab.id === localActiveTab.value)
  return currentTab ? currentTab.name : ''
}

// 处理一级选项卡点击
const handleTabClick = (tabId) => {
  localActiveTab.value = tabId
}
</script>

<style scoped lang="scss">
// 布局变量
$left-width: 300px;
$border-color: #E4E4E4;
$active-color: #3169F8;
$hover-color: #83A5FB;
$bg-color: #FFFFFF;
$text-primary: #333333;
$text-secondary: #999999;
$transition-speed: 0.3s;

// 主布局容器
.event-layout {
  display: flex;
  background: $bg-color;
  border: 1px solid $border-color;
  min-height: 600px;
  margin-bottom: 45px;
  box-sizing: border-box;
  width: 100%;
  overflow: hidden;
}

// 左侧区域
.layout-left {
  width: $left-width;
  flex-shrink: 0;
  border-right: 1px solid $border-color;
  display: flex;
  flex-direction: column;

  // 用户信息区域
  .page-content-left-user{
    padding: 20px 30px;
    box-sizing: border-box;
    .page-content-left-user-avatar{
      width: 80px;
      height: 80px;
      border-radius: 50%;
      object-fit: cover;
    }
    .page-content-left-user-name{
      padding: 3px 0;
      margin-left: 15px;
      height: 80px;
      .use-name{
        font-size: 20px;
        color: #333333;
        @include ellipsis(1);
        overflow: hidden;
        word-break: break-all;
        font-weight: bold;
      }

    }
  }

  // 导航选项卡
  .nav-tabs {
    flex: 1;
    padding: 0;
    .nav-tab-item {
      height: 60px;
      line-height: 60px;
      font-size: 20px;
      color: $text-primary;
      background: $bg-color;
      text-align: center;
      cursor: pointer;
      transition: all $transition-speed ease;

      &:hover {
        background: $hover-color;
        color: $bg-color;
        font-weight: 500;
      }

      &.active {
        background: $hover-color;
        color: $bg-color;
        font-weight: 500;
      }
    }
  }
}

// 右侧区域
.layout-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  width: 100%;
  overflow: hidden;
  // 顶部标题栏
  .top-bar {
    box-sizing: border-box;
    border-bottom: 2px solid $border-color;
    padding: 0 45px 0px;
    height: 75px;
    background: $bg-color;

    .top-bar-content {
      display: flex;
      align-items: center;
      height: 100%;
      width: fit-content;
      position: relative;

      .top-bar-title {
        font-size: 20px;
        color: $text-primary;
        font-weight: 400;
        margin-top: 5px;
      }

      .top-bar-line {
        position: absolute;
        left: 0;
        bottom: -1px;
        height: 3px;
        width: 100%;
        background: $active-color;
        transition: width $transition-speed ease;
      }
    }
  }

  // 内容区域
  .content-section {
    flex: 1;
    padding: 45px 45px 70px 45px;
    box-sizing: border-box;
    overflow-y: auto;
    background: $bg-color;
    width: 100%;
    overflow: hidden;
  }
}

</style>