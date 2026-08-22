import request from '@/utils/request'

// 查询课程推荐信息列表
export function listRecommendInfo(query) {
  return request({
    url: '/course/recommendInfo/list',
    method: 'get',
    params: query
  })
}

// 查询课程推荐信息详细
export function getRecommendInfo(recommendId) {
  return request({
    url: '/course/recommendInfo/' + recommendId,
    method: 'get'
  })
}

// 新增课程推荐信息
export function addRecommendInfo(data) {
  return request({
    url: '/course/recommendInfo',
    method: 'post',
    data: data
  })
}

// 修改课程推荐信息
export function updateRecommendInfo(data) {
  return request({
    url: '/course/recommendInfo',
    method: 'put',
    data: data
  })
}

// 删除课程推荐信息
export function delRecommendInfo(recommendId) {
  return request({
    url: '/course/recommendInfo/' + recommendId,
    method: 'delete'
  })
}

// 导出课程推荐信息
export function exportRecommendInfo(query) {
  return request({
    url: '/course/recommendInfo/export',
    method: 'post',
    params: query
  })
}


