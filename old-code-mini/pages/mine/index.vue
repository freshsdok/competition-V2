<template>
  <view class="container">
    <!-- 用户信息卡片 -->
    <view class="user-card">
      <view class="avatar-section">
        <div class="avatar-container">
          <image class="avatar" 
                  :src="userInfo?.avatar || '/static/images/common/avatar.png'" 
                  mode="aspectFill" />
        </div>
        <view class="user-info">
          <text class="sub-text">
            {{ isLogin ? `欢迎您,${userInfo?.userName || '用户'}` : '登录后体验更多功能' }}
          </text>
          <text v-if="isLogin && userInfo?.phone" class="phone-text">{{ userInfo.phone }}</text>
          <text v-if="isLogin" class="auth-tag" :class="{ verified: userInfo?.authStatus == 5 }">
            {{ userInfo?.authStatus == 5 ? '已实名认证' : '未实名认证' }}
          </text>
        </view>
      </view>
    </view>

    <view v-if="isLogin" class="personal-services">
      <view class="service-title">个人服务</view>
      <view class="service-grid">
        <view class="service-item" @click="goToPersonal('/pages-personal/order/index')">
          <view class="service-icon order-icon">单</view>
          <text class="service-name">我的订单</text>
        </view>
        <view class="service-item" @click="goToPersonal('/pages-personal/invoice/index')">
          <view class="service-icon invoice-icon">票</view>
          <text class="service-name">开票记录</text>
        </view>
        <view class="service-item" @click="goToPersonal('/pages-personal/team/index')">
          <view class="service-icon team-icon">队</view>
          <text class="service-name">我的团队</text>
        </view>
        <view class="service-item" @click="goToPersonal('/pages-personal/certificate/index')">
          <view class="service-icon cert-icon">证</view>
          <text class="service-name">我的证书</text>
        </view>
      </view>
    </view>

    <!-- 退出登录按钮 -->
    <view class="menu-section" v-if="isLogin">
      <view class="menu-item" @click="goToMyFile">
        <text>我的文件</text>
        <view class="menu-right">
          <text v-if="fileUnreadCount" class="menu-badge">{{ fileUnreadCount > 99 ? '99+' : fileUnreadCount }}</text>
          <text class="arrow">›</text>
        </view>
      </view>
      <view class="menu-item" @click="goToAwardPublicity">
        <text>获奖公示</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="goToMyCredential">
        <text>现场证件</text>
        <text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="goToSceneResource">
        <text>设备预约</text>
        <text class="arrow">›</text>
      </view>
      <view v-if="showSecretaryConsole" class="menu-item" @click="goToSecretaryConsole">
        <text>秘书现场控制台</text>
        <text class="arrow">›</text>
      </view>
    </view>

    <view class="logout-section" v-if="isLogin">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>

    <!-- 未登录状态 -->
    <view class="guest-section" v-else>
      <view class="guest-card">
        <view class="guest-title">登录后可使用个人服务</view>
        <view class="guest-desc">可查看现场证件、订单、团队等赛事服务，也可以暂不登录继续浏览。</view>
        <button class="login-btn" @click="goToLogin">登录/注册</button>
        <button class="skip-btn" @click="goHome">暂不登录，返回首页</button>
      </view>
    </view>
  </view>
  <custom-tabbar />
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from "@dcloudio/uni-app"
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/modules/user'
import { listMySecretarySessions } from '@/api/reviewSecretary'
import { getMyFileUnreadCount } from '@/api/myFile'

const userStore = useUserStore()
const isLogin = ref(false)
const showSecretaryConsole = ref(false)
const fileUnreadCount = ref(0)

onShow(async () => {
  const loginStatus = await checkLoginStatus()
  // 获取用户信息（不传参数，有缓存则使用缓存）
  if (loginStatus) {
    getUserInfo(true)
    refreshSecretaryConsoleVisible()
    refreshFileUnreadCount()
  } else {
    showSecretaryConsole.value = false
    fileUnreadCount.value = 0
  }
})

const userInfo = ref(null)
function getUserInfo(forceRefresh = false) {
  userStore.getUserInfo(forceRefresh).then(res => {
    console.log('用户信息:', res)
    userInfo.value = res  
    if (hasSecretaryPermission(res)) {
      showSecretaryConsole.value = true
    }
  }).catch(err => {
    console.log('获取用户信息失败:', err)
  })
}

async function refreshSecretaryConsoleVisible() {
  try {
    const res = await listMySecretarySessions({}, true)
    const rows = getResponseList(res)
    showSecretaryConsole.value = rows.length > 0 || hasSecretaryPermission(userInfo.value)
  } catch (error) {
    showSecretaryConsole.value = hasSecretaryPermission(userInfo.value)
  }
}

async function refreshFileUnreadCount() {
  try {
    const res = await getMyFileUnreadCount()
    fileUnreadCount.value = Number(res?.data || 0)
  } catch (error) {
    fileUnreadCount.value = 0
  }
}

// 检查登录状态
function checkLoginStatus() {
  const token = getToken()
  isLogin.value = !!token
  return token ? true : false
}

// 退出登录
function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    confirmColor: '#3169F8',
    success: (res) => {
      if (res.confirm) {
        userStore.logOut().then(() => {
          isLogin.value = false
          userInfo.value = null
          showSecretaryConsole.value = false
          fileUnreadCount.value = 0
          uni.showToast({
            title: '已退出登录',
            icon: 'success'
          })
        })
      }
    }
  })
}

// 去登录
function goToLogin() {
  uni.navigateTo({ url: '/pages/login' })
}

function goHome() {
  uni.switchTab({ url: '/pages/index/index' })
}

function goToSceneResource() {
  uni.navigateTo({ url: '/pages/scene-resource/index' })
}

function goToMyCredential() {
  uni.navigateTo({ url: '/pages/my-credential/index' })
}

function goToAwardPublicity() {
  uni.navigateTo({ url: '/pages-personal/award-publicity/index' })
}

function goToMyFile() {
  uni.navigateTo({ url: '/pages-personal/file/index' })
}

function goToPersonal(url) {
  uni.navigateTo({ url })
}

function goToSecretaryConsole() {
  uni.navigateTo({ url: '/pages/review-secretary/index' })
}

function getResponseList(res) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res?.rows)) return res.rows
  if (Array.isArray(res?.data?.rows)) return res.data.rows
  return []
}

function hasSecretaryPermission(info) {
  const permissions = info?.permissions || []
  const roles = info?.roles || []
  return permissions.includes('*:*:*')
    || permissions.includes('competition:review:secretary:query')
    || roles.includes('admin')
    || roles.includes('review_secretary')
    || roles.includes('secretary')
}
</script>

<style lang="scss" scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(180deg, #3169F8 0%, #ffffff 40%);
}

// 用户卡片
.user-card {
  padding: 70rpx 40rpx 52rpx;
  // background: linear-gradient(135deg, #3169F8 0%, #ffffff 100%);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 30rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.welcome-text {
  font-size: 36rpx;
  color: #fff;
  font-weight: 600;
}

.sub-text {
  font-size: 30rpx;
  color: #fff;
  font-weight: 600;
}

.phone-text {
  font-size: 25rpx;
  color: rgba(255, 255, 255, 0.85);
}

.auth-tag {
  align-self: flex-start;
  padding: 4rpx 14rpx;
  border-radius: 18rpx;
  font-size: 21rpx;
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
}

.auth-tag.verified {
  color: #17663a;
  background: #d9f7e7;
}

.personal-services {
  margin: 0 40rpx 28rpx;
  padding: 28rpx 24rpx 24rpx;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 10rpx 30rpx rgba(23, 56, 125, 0.1);
}

.service-title {
  margin: 0 8rpx 22rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2937;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8rpx;
}

.service-item {
  min-width: 0;
  padding: 18rpx 4rpx 12rpx;
  border-radius: 18rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.service-icon {
  width: 66rpx;
  height: 66rpx;
  line-height: 66rpx;
  text-align: center;
  border-radius: 18rpx;
  font-size: 27rpx;
  font-weight: 700;
}

.order-icon { color: #3169f8; background: #e7eeff; }
.invoice-icon { color: #ff8a1f; background: #fff1e2; }
.team-icon { color: #10a36b; background: #e1f7ee; }
.cert-icon { color: #8b5cf6; background: #efe8ff; }

.service-name {
  margin-top: 14rpx;
  font-size: 23rpx;
  font-weight: 600;
  color: #25314a;
  white-space: nowrap;
}

// 按钮区域
.menu-section {
  padding: 0 40rpx 20rpx;
}

.menu-item {
  height: 96rpx;
  padding: 0 28rpx;
  margin-bottom: 20rpx;
  border-radius: 16rpx;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 30rpx;
  color: #1f2937;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.arrow {
  color: #9ca3af;
  font-size: 44rpx;
}

.menu-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.menu-badge {
  min-width: 34rpx;
  height: 34rpx;
  padding: 0 8rpx;
  border-radius: 18rpx;
  color: #fff;
  background: #ef5350;
  font-size: 19rpx;
  line-height: 34rpx;
  text-align: center;
  box-sizing: border-box;
}

.logout-section,
.guest-section {
  padding: 42rpx 40rpx 150rpx;
}

.guest-card {
  padding: 40rpx 32rpx;
  border-radius: 20rpx;
  background: #fff;
  box-shadow: 0 8rpx 24rpx rgba(15, 23, 42, 0.06);
}

.guest-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #1f2937;
  text-align: center;
}

.guest-desc {
  margin-top: 16rpx;
  font-size: 26rpx;
  line-height: 1.6;
  color: #6b7280;
  text-align: center;
}

.logout-btn,
.login-btn,
.skip-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 32rpx;
  border: none;
}

.logout-btn {
  background-color: #fff;
  color: #3169F8;
  border: 2rpx solid #3169F8;
}

.login-btn {
  background: linear-gradient(135deg, #3169F8 0%, #5B8FF9 100%);
  color: #fff;
  margin-top: 40rpx;
}

.skip-btn {
  margin-top: 24rpx;
  background: #fff;
  color: #3169F8;
  border: 2rpx solid #3169F8;
}
</style>
