<script setup>
  import config from './config'
  import { useConfigStore } from '@/store'
  import { getToken } from '@/utils/auth'
  import { onLaunch,onShow } from '@dcloudio/uni-app'

  // 记录上次检查时间，防止重复触发
  let lastCheckTime = 0
  const CHECK_INTERVAL = 5000 // 5秒内不重复检查

  onLaunch(() => {
    initConfig()
    // 启动时开始监听网络
    startNetworkListener()
  })

  onShow(() => {
    setTimeout(() => {
      checkPendingSignIn('onShow')
    }, 1000)
  })

  // 监听网络状态变化
  function startNetworkListener() {
    uni.onNetworkStatusChange((res) => {
      if (res.isConnected) {
        // 网络已连接，检查是否有 pending 签到需要重试
        console.log('网络已恢复，检查 pending 签到...')
        checkPendingSignIn('network')
      }
    })
  }

  // 检查是否有待确认的签到记录
  function checkPendingSignIn(source) {
    // 防抖：5秒内不重复检查
    const now = Date.now()
    if (now - lastCheckTime < CHECK_INTERVAL) {
      console.log(`[${source}] 5秒内已检查过，跳过`)
      return
    }
    lastCheckTime = now

    // 未登录用户不检查 pending 签到
    const token = getToken()
    if (!token) {
      console.log(`[${source}] 用户未登录，跳过 pending 签到检查`)
      return
    }

    const pendingRecord = uni.getStorageSync('pending_sign_in')
    if (pendingRecord && pendingRecord.status === 'pending' && pendingRecord.rid) {
      // 获取当前页面路径
      const pages = getCurrentPages()
      const currentPage = pages[pages.length - 1]
      const currentRoute = currentPage ? currentPage.route : ''
      
      // 如果当前已经在 result 页面，则不重复跳转
      if (currentRoute && currentRoute.includes('pages/scan/result')) {
        console.log(`[${source}] 已在 result 页面，跳过跳转`)
        return
      }
      
      console.log(`[${source}] 跳转到 result 页面重试签到`)
      // 跳转到签到结果页面继续重试（使用 scene 参数格式）
      uni.navigateTo({
        url: `/pages/scan/result?scene=rid_${pendingRecord.rid}`
      })
    }
  }

  // 初始化应用配置
  function initConfig() {
    useConfigStore().setConfig(config)
  }
</script>

<style lang="scss">

  @import '@/static/scss/vue-quill.snow.prod.scss';
  @import '@/static/scss/index.scss'
  
</style>
