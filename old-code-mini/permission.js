import { getToken } from '@/utils/auth'

// 登录页面
const loginPage = "/pages/login"

// 首页（登录后默认跳转页面）
const indexPage = "/pages/index/index"
  
// 页面白名单（不需要登录）
const whiteList = [
  '/pages/login',
  '/pages/index/index',
  '/pages/match/index',
  '/pages/news/index',
  '/pages/mine/index',
  '/pages/agreement/index',
  '/pages/notice/detail',
  '/pages/notice/list'
]

// 检查地址白名单
function checkWhite(url) {
  const path = url.split('?')[0]
  return whiteList.indexOf(path) !== -1
}

// 页面跳转验证拦截器
let list = ["navigateTo", "redirectTo", "reLaunch", "switchTab"]
list.forEach(item => {
  uni.addInterceptor(item, {
    invoke(to) {
      // 获取目标路径（去掉参数）
      const targetPath = to.url.split('?')[0]
      
      if (getToken()) {
        // 已登录状态
        if (targetPath === loginPage) {
          // 已登录用户访问登录页，重定向到首页
          uni.reLaunch({ url: indexPage })
          return false
        }
        return true
      } else {
        // 未登录状态
        if (checkWhite(to.url)) {
          // 白名单页面允许访问
          return true
        }
        // 非白名单页面，跳转到登录页
        uni.reLaunch({ url: loginPage })
        return false
      }
    },
    fail(err) {
      console.log(err)
    }
  })
})
