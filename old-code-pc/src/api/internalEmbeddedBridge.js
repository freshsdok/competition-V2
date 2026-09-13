import request from '@/utils/request'

// V1浏览器只请求当前登录人的固定目标入口；userId、V2地址和HMAC均由V1服务端掌握。
export const discoverEmbeddedCredentials = () => request({ url: '/system/v2EmbeddedBridge/discovery', method: 'get' })
export const enterEmbeddedSettlement = (teamId, version) => request({
  url: '/system/v2EmbeddedBridge/settlement', method: 'post', data: { teamId, version },
  headers: { repeatSubmit: false },
})
export const enterEmbeddedCredential = (offeringId) => request({
  url: `/system/v2EmbeddedBridge/credential/${encodeURIComponent(String(offeringId))}`,
  method: 'post',
})
