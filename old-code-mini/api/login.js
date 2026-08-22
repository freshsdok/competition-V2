import request from '@/utils/request'

// 微信手机号登录
export function wxPhoneLogin(data) {
  return request({
    url: '/wxApp/wxAuth/wx-login',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

// 获取用户信息
export function wxInfo(data) {
  return request({
    url: '/wxApp/wxAuth/wx-info',
    method: 'post',
    data: data,
    headers: {
      isSilent: true
    }
  })
}
