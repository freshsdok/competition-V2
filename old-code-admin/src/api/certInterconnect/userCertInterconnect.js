import request from '@/utils/request'

// 用户赛证互通申请列表
export function getUserCertInterconnectList(params) {
  return request({
    url: '/competition/competition/competitionCertExchangeApply/list',
    method: 'get',
    params
  })
}

// 异步导出用户赛证互通申请信息
export function exportUserCertInterconnect(params) {
  return request({
    url: '/competition/competition/competitionCertExchangeApply/export',
    method: 'post',
    data: params
  })
}