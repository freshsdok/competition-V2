<template>
  <view class="tab-bar-wrapper">
    <view class="tab-bar">
      <view
        v-for="(item, index) in list"
        :key="index"
        class="tab-bar-item"
        :class="{ 'tab-bar-item-center': index === 1 }"
        @click="switchTab(index, item)"
      >
        <template v-if="index === 1" class="center-btn">
          <image class="center-icon" :src="item.centerIconPath" />
          <text class="tab-bar-text"> {{ item.text }} </text>
        </template>
        <template v-else>
          <image
            class="tab-bar-icon"
            :src="current === index ? item.selectedIconPath : item.iconPath"
          />
          <text
            class="tab-bar-text"
            :class="{ 'tab-bar-text-active': current === index }"
          >
            {{ item.text }}
          </text>
        </template>
      </view>
    </view>
    <!-- 底部安全区域白色背景占位 -->
    <view class="safe-area-placeholder"></view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getToken } from '@/utils/auth'

const list = [
  {
    pagePath: '/pages/index/index',
    iconPath: '/static/images/tabbar/1.png',
    selectedIconPath: '/static/images/tabbar/6.png',
    text: '首页',
  },
  {
    centerIconPath: '/static/images/tabbar/center.png',
    text: '扫一扫',
  },
  {
    pagePath: '/pages/mine/index',
    iconPath: '/static/images/tabbar/5.png',
    selectedIconPath: '/static/images/tabbar/10.png',
    text: '我的',
  },
]

// 使用 computed 自动根据当前页面路径计算 current
const current = computed(() => {
  const pages = getCurrentPages()
  if (pages.length === 0) return 0

  const currentPage = pages[pages.length - 1]
  const currentPath = '/' + currentPage.route

  // 找到匹配的 tab 索引
  const index = list.findIndex((item) => item.pagePath === currentPath)
  return index !== -1 ? index : 0
})

function switchTab(index, item) {
  // 点击扫一扫，检查登录状态
  if (index === 1) {
    const token = getToken()
    if (!token) {
      uni.showModal({
        title: '提示',
        content: '登录后即可使用扫码签到功能，是否前往登录？',
        confirmText: '去登录',
        confirmColor: '#3169F8',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            uni.navigateTo({
              url: '/pages/login',
            })
          }
        },
      })
      return
    }
    startScan()
    return
  }

  const url = item.pagePath
  uni.switchTab({ url })
}

function startScan() {
  uni.scanCode({
    onlyFromCamera: false,
    scanType: ['qrCode', 'barCode', 'wxCode'],
    success: (res) => {
      console.log('扫码成功1:', res)
      const path = res.path || ''
      const normalizedPath = path.charAt(0) === '/' ? path.slice(1) : path
      const rawResult = res.result || path || ''
      if (normalizedPath && normalizedPath.indexOf('pages/') === 0) {
        uni.navigateTo({
          url: `/${normalizedPath}`,
        })
        return
      }
      if (!rawResult) {
        uni.showToast({ title: '无效的二维码', icon: 'none' })
        return
      }
      uni.navigateTo({
        url: `/pages/scan/result?scene=${encodeURIComponent(rawResult)}`,
      })
    },
    fail: (err) => {
      console.log('扫码取消或失败:', err)
    },
  })
}
</script>

<style lang="scss" scoped>
.tab-bar-wrapper {
  z-index: 999999;
}
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #ffffff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  box-sizing: border-box;
  z-index: 999999;
}

.tab-bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  height: 100rpx;
  flex: 1;
}
.tab-bar-item-center {
  position: relative;
  .center-icon {
    width: 110rpx;
    height: 110rpx;
    flex-shrink: 0;
    position: absolute;
    bottom: 30rpx;
  }
  .tab-bar-text {
    position: absolute;
    bottom: 0;
  }
}

.tab-bar-icon {
  width: 48rpx;
  height: 48rpx;
  margin-bottom: 6rpx;
}

.tab-bar-text {
  font-size: 24rpx;
  color: #999999;
}

.tab-bar-text-active {
  color: #3169f8;
}

.safe-area-placeholder {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: constant(safe-area-inset-bottom);
  height: env(safe-area-inset-bottom);
  background-color: #ffffff;
  z-index: 999998;
}
</style>
