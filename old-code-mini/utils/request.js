import config from '@/config'
import { getToken } from '@/utils/auth'
import errorCode from '@/utils/errorCode'
import { useUserStore } from '@/store/modules/user'
import { toast, showConfirm, tansParams } from '@/utils/common'

let timeout = 20000
const baseUrl = config.baseUrl
let loginExpiredPromptVisible = false

const OLD_FILE_ORIGIN = 'https://dtcup.dtxiaotangren.com'
const NEW_FILE_ORIGIN = 'https://www.ksup.cn'

/**
 * 递归替换接口响应中的旧静态资源域名。
 * 同时兼容普通字符串、数组、嵌套对象以及包含资源地址的富文本内容。
 */
function replaceFileOrigin(value) {
  if (typeof value === 'string') {
    return value.split(OLD_FILE_ORIGIN).join(NEW_FILE_ORIGIN)
  }

  if (Array.isArray(value)) {
    return value.map(replaceFileOrigin)
  }

  if (value !== null && typeof value === 'object') {
    Object.keys(value).forEach(key => {
      value[key] = replaceFileOrigin(value[key])
    })
  }

  return value
}

const request = config => {
  // 是否需要设置 token
  const isToken = (config.headers || {}).isToken === false
  // 是否静默处理（不报错）
  const isSilent = (config.headers || {}).isSilent === true
  config.header = config.header || {}
  if (getToken() && !isToken) {
    config.header['Authorization'] = 'Bearer ' + getToken()
  }
  // get请求映射params参数
  if (config.params) {
    let url = config.url + '?' + tansParams(config.params)
    url = url.slice(0, -1)
    config.url = url
  }
  return new Promise((resolve, reject) => {
    uni.request({
        method: config.method || 'get',
        timeout: config.timeout ||  timeout,
        url: config.baseUrl || baseUrl + config.url,
        data: config.data,
        header: {
          ...config.header,
          tjPlatformType: 'miniProgram'
        },
        dataType: 'json'
      }).then(response => {
        console.log('request111111111111111111', response)
        const res = response
        res.data = replaceFileOrigin(res.data)
        const code = res.data.code || 200
        const msg = errorCode[code] || res.data.msg || errorCode['default']
        if (code === 401) {
          if (!loginExpiredPromptVisible && !isSilent) {
            loginExpiredPromptVisible = true
            showConfirm('登录状态已过期，您可以继续留在该页面，或者重新登录?').then(res => {
              if (res.confirm) {
                useUserStore().logOut().then(() => {
                  uni.reLaunch({ url: '/pages/login' })
                })
              }
            }).then(() => {
              loginExpiredPromptVisible = false
            }, () => {
              loginExpiredPromptVisible = false
            })
          }
          reject(401)
          return
        } else if (code === 500) {
          if (!isSilent) toast(msg)
          reject('500')
          return
        } else if (code === 5008) {
          reject(res.data)
          return
        } else if (code !== 200) {
          if (!isSilent) toast(msg)
          reject(code)
          return
        }
        resolve(res.data)
      })
      .catch(error => {
        console.log('catchcatchcatchcatch error', error)
        let message = error.message || error.errMsg || '未知错误'
        if (message === 'Network Error') {
          message = '后端接口连接异常'
        } else if (message && message.includes && message.includes('timeout')) {
          message = '系统接口请求超时'
        } else if (message && message.includes && message.includes('Request failed with status code')) {
          message = '系统接口' + message.slice(-3) + '异常'
        }
        if (!isSilent) toast(message)
        reject(error)
      })
  })
}

export default request
