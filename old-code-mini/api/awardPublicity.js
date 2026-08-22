import request from '@/utils/request'

export function listAwardPublicities() {
  return request({
    url: '/competition/awardDetailsUser/awardPublicityList',
    method: 'get'
  })
}

export function listAwardDetails(params = {}) {
  return request({
    url: '/competition/awardDetailsUser/awardDetailsList',
    method: 'get',
    params
  })
}
