import request from '@/utils/request'

// 查询意见反馈列表
export function listSuggBack(query) {
  return request({
    url: '/system/suggBack/list',
    method: 'get',
    params: query
  })
}

// 查询意见反馈详细
export function getSuggBack(suggBackId) {
  return request({
    url: '/system/suggBack/' + suggBackId,
    method: 'get'
  })
}

// 新增意见反馈
export function addSuggBack(data) {
  return request({
    url: '/system/suggBack',
    method: 'post',
    data: data
  })
}

// 修改意见反馈
export function updateSuggBack(data) {
  return request({
    url: '/system/suggBack',
    method: 'put',
    data: data
  })
}

// 删除意见反馈
export function delSuggBack(suggBackIds) {
  return request({
    url: '/system/suggBack/' + suggBackIds,
    method: 'delete'
  })
}

// 回复意见反馈
export function replySuggBack(data) {
  return request({
    url: '/system/suggBack/reply',
    method: 'put',
    data: data
  })
}

// 转交意见反馈
export function transferSuggBack(data) {
  return request({
    url: '/system/suggBack/transfer',
    method: 'put',
    data: data
  })
}

// 修改意见反馈处理状态
export function changeStatusSuggBack(data) {
  return request({
    url: '/system/suggBack/changeStatus',
    method: 'put',
    data: data
  })
}
