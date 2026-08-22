import request from '@/utils/request'

// 获取签到结果（静默处理，不报错）
export function getSignResult(data) {
  return request({
    url: `/wxApp/wxAuth/wx-scanCode`,
    method: 'post',
    data: data,
    headers: {
      isSilent: true
    }
  })
}


// 获取签到规则，承诺
export function getSignRule(data) {
  return request({
    url: `/wxApp/wxAuth/wx-ruler`,
    method: 'post',
    data: data
  })
}

// 重试签到
export function retrySign(data) {
  return request({
    url: `/wxApp/wxAuth/wx-retry`,
    method: 'post',
    data: data
  })
}

// 现场证件扫码核验，返回扫码操作矩阵
export function scanSceneCredential(data) {
  return request({
    url: '/competition/sceneVerify/scan',
    method: 'post',
    data
  })
}

// 现场证件扫码确认操作
export function confirmSceneCredential(data) {
  return request({
    url: '/competition/sceneVerify/confirm',
    method: 'post',
    data
  })
}
