// 全屏页面配置
// 集中管理全屏页面的路由配置，用于控制：
// 1. 隐藏侧边栏和顶部导航 (permission.js)
// 2. 不加入 Tags-Views (TagsView/index.vue)

// 全屏页面 - 路由 name 列表（精确匹配）
export const fullScreenNames = ['ReviewTaskDetail']

// 全屏页面 - 路由 path 包含关键字列表（模糊匹配）
export const fullScreenPathKeywords = ['/reviewTaskDetail']

/**
 * 判断路由是否为全屏页面
 * @param {Object} route - 路由对象
 * @returns {Boolean}
 */
export function isFullScreenRoute(route) {
  if (!route) return false
  return fullScreenNames.includes(route.name) ||
    fullScreenPathKeywords.some(keyword => route.path?.includes(keyword))
}
