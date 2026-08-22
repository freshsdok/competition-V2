<template>
  <view class="login-container">
    <view class="top-actions">
      <button class="back-btn" @click="handleBack">返回</button>
    </view>

    <!-- Logo -->
    <view class="logo-wrapper">
      <view class="logo-circle">
		    <image src="https://www.ksup.cn/statics/wxApp/logo.png" mode="widthFix"></image>
      </view>
    </view>

    <!-- 登录按钮 -->
    <view class="login-btn-wrapper">
      <!-- 未勾选协议时显示普通按钮 -->
      <button 
        v-if="!agreed"
        class="login-btn" 
        @click="onLoginClick"
      >
        获取手机号一键登录
      </button>
      <!-- 勾选协议后显示获取手机号按钮 -->
      <button 
        v-else
        class="login-btn" 
        open-type="getPhoneNumber"
        @getphonenumber="onGetPhoneNumber"
      >
        获取手机号一键登录
      </button>
      <button class="cancel-login-btn" @click="handleSkipLogin">暂不登录，继续浏览</button>
    </view>

    <!-- 协议勾选 -->
    <view class="agreement-wrapper">
      <view class="checkbox" @click="toggleAgree">
        <view class="checkbox-inner" :class="{ 'checked': agreed }">
          <text v-if="agreed" class="check-icon">✓</text>
        </view>
      </view>
      <text class="agreement-text">阅读并同意</text>
      <text class="agreement-link" @click="showAgreement('service')">《用户服务协议》</text>
      <text class="agreement-text">和</text>
      <text class="agreement-link" @click="showAgreement('privacy')">《隐私政策》</text>
    </view>

  </view>
</template>

<script setup>
import { ref, getCurrentInstance } from "vue"
import { onLoad } from "@dcloudio/uni-app"
import { getToken, setToken } from '@/utils/auth'
import { wxPhoneLogin } from '@/api/login'

const { proxy } = getCurrentInstance()
const agreed = ref(false)

// 切换同意状态
function toggleAgree() {
  agreed.value = !agreed.value
}

// 普通按钮点击（未勾选协议时）
function onLoginClick() {
  uni.showToast({
    title: '请先阅读并同意用户服务协议和隐私政策',
    icon: 'none',
    duration: 2000
  })
}

// 获取手机号回调（勾选协议后）
function onGetPhoneNumber(e) {
  // 检查获取手机号是否成功
  console.log('获取手机号响应:', e.detail)
  if (e.detail.errMsg === 'getPhoneNumber:fail user deny' || e.detail.errMsg === 'getPhoneNumber:fail:user deny'){
    uni.showToast({
      title: '已取消授权，可继续浏览',
      icon: 'none'
    })
    return
  }
  if (e.detail.errMsg !== 'getPhoneNumber:ok') {
    uni.showToast({
      title: '获取手机号失败',
      icon: 'none'
    })
    return
  }
  
  proxy.$modal.loading("登录中，请耐心等待...")
  
  // 获取微信登录code
  uni.login({
    provider: 'weixin',
    success: (loginRes) => {
      if (loginRes.code) {
        // 组装登录参数
        const loginData = {
          encryptedData: e.detail.encryptedData,
          iv: e.detail.iv,
          code: loginRes.code
        }
        console.log('登录响应:请求参数', loginData)
        // 调用微信登录接口
        wxPhoneLogin(loginData).then(res => {
          proxy.$modal.closeLoading()
          console.log('登录响应:1111', res)
          if (res.code === 200) {
            // 保存token
            setToken(res.data.token)
            proxy.$modal.msgSuccess('登录成功')
            // 检查是否有跳转目标
            const redirectUrl = uni.getStorageSync('redirect_after_login')
            setTimeout(() => {
              if (redirectUrl) {
                // 清除存储的跳转目标
                uni.removeStorageSync('redirect_after_login')
                // 跳转到目标页面
                uni.redirectTo({ url: redirectUrl })
              } else {
                // 没有目标则去首页
                uni.switchTab({
                  url: '/pages/index/index'
                })
              }
            }, 500)
          } else {
            proxy.$modal.msgError(res.msg || '登录失败')
          }
        }).catch(err => {
          proxy.$modal.closeLoading()
          proxy.$modal.msgError(err.msg || '登录失败，请重试')
        })
      } else {
        proxy.$modal.closeLoading()
        uni.showToast({
          title: '获取微信code失败',
          icon: 'none'
        })
      }
    },
    fail: () => {
      proxy.$modal.closeLoading()
      uni.showToast({
        title: '微信登录api调用失败',
        icon: 'none'
      })
    }
  })
}

// 显示协议
function showAgreement(type) {
  uni.navigateTo({
    url: `/pages/agreement/index?type=${type}`
  })
}

function clearLoginRedirect() {
  uni.removeStorageSync('redirect_after_login')
}

function handleBack() {
  clearLoginRedirect()
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
    return
  }
  uni.switchTab({ url: '/pages/mine/index' })
}

function handleSkipLogin() {
  clearLoginRedirect()
  uni.switchTab({ url: '/pages/index/index' })
}

onLoad(() => {
  // 如果已登录，通过拦截器会自动跳转到首页
  // 这里不需要额外处理
})
</script>

<style lang="scss" scoped>
page {
  background-color: #ffffff;
}

.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx 60rpx;
  background-color: #ffffff;
  box-sizing: border-box;
}

.top-actions {
  position: fixed;
  left: 32rpx;
  top: calc(24rpx + constant(safe-area-inset-top));
  top: calc(24rpx + env(safe-area-inset-top));
  z-index: 10;
}

.back-btn {
  min-width: 128rpx;
  height: 64rpx;
  line-height: 64rpx;
  padding: 0 28rpx;
  border-radius: 32rpx;
  background: #f3f6ff;
  color: $theme-color;
  font-size: 28rpx;
  border: 2rpx solid rgba(49, 105, 248, 0.18);

  &::after {
    border: none;
  }
}

.logo-wrapper {
  margin-bottom: 120rpx;
}

.logo-circle {
  width: 160rpx;
  height: 160rpx;
  background: linear-gradient(135deg, $theme-color 0%, $theme-color-dark 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(49, 105, 248, 0.3);
}

.logo-text {
  color: #ffffff;
  font-size: 56rpx;
  font-weight: bold;
  letter-spacing: 2rpx;
}

.login-btn-wrapper {
  width: 100%;
  margin-bottom: 40rpx;
}

.login-btn,
.cancel-login-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  font-size: 32rpx;
  border-radius: 48rpx;
  border: none;

  &::after {
    border: none;
  }

  &:active {
    opacity: 0.9;
  }
}

.login-btn {
  background: linear-gradient(135deg, $theme-color 0%, $theme-color-dark 100%);
  color: #ffffff;
  box-shadow: 0 8rpx 24rpx rgba(49, 105, 248, 0.3);
}

.cancel-login-btn {
  margin-top: 24rpx;
  background: #ffffff;
  color: $theme-color;
  border: 2rpx solid $theme-color;
  box-shadow: none;
}

.agreement-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  padding: 0 60rpx;
  margin-top: 40rpx;
}

.checkbox {
  width: 36rpx;
  height: 36rpx;
  margin-right: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.checkbox-inner {
  width: 32rpx;
  height: 32rpx;
  border: 2rpx solid #999;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;

  &.checked {
    background-color: $theme-color;
    border-color: $theme-color;
  }
}

.check-icon {
  color: #ffffff;
  font-size: 20rpx;
  font-weight: bold;
}

.agreement-text {
  font-size: 26rpx;
  color: #666;
}

.agreement-link {
  font-size: 26rpx;
  color: $theme-color;
}
</style>
