import { defineStore } from 'pinia'
import { ref } from 'vue'
import storage from '@/utils/storage'
import constant from '@/utils/constant'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { wxPhoneLogin, wxInfo } from '@/api/login'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const isLogin = ref(!!getToken())
  const userInfo = ref(null)

  const SET_TOKEN = (val) => {
    token.value = val
    isLogin.value = !!val
  }

  const SET_USER_INFO = (val) => {
    userInfo.value = val
  }

  // 微信登录
  const loginAction = (loginData) => {
    return new Promise((resolve, reject) => {
      wxPhoneLogin(loginData).then(res => {
        if (res.token) {
          setToken(res.token)
          SET_TOKEN(res.token)
        }
        resolve(res)
      }).catch(error => {
        reject(error)
      })
    })
  }

  // 获取用户信息
  // forceRefresh: true 强制刷新，false 有缓存则使用缓存
  const getUserInfoAction = (forceRefresh = false) => {
    return new Promise((resolve, reject) => {
      // 非强制刷新且已有用户信息，直接返回缓存
      if (!forceRefresh && userInfo.value) {
        resolve(userInfo.value)
        return
      }
      
      // 调用接口获取
      wxInfo().then(res => {
        if (res.code === 200 && res.data) {
          SET_USER_INFO(res.data)
          resolve(res.data)
        } else {
          reject(res.msg || '获取用户信息失败')
        }
      }).catch(error => {
        reject(error)
      })
    })
  }

  // 退出系统
  const logOutAction = () => {
    return new Promise((resolve) => {
      SET_TOKEN('')
      SET_USER_INFO(null)
      removeToken()
      storage.clean()
      resolve()
    })
  }

  return {
    token,
    isLogin,
    userInfo,
    SET_TOKEN,
    SET_USER_INFO,
    login: loginAction,
    getUserInfo: getUserInfoAction,
    logOut: logOutAction
  }
})
