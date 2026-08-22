import request from '@/utils/request'

// ==================== 获奖公示管理 ====================

// 查询获奖公示列表
export function getAwardPublicityList(query) {
  return request({
    url: '/competition/publicity/list',
    method: 'get',
    params: query
  })
}

// 获取公示详情
export function getAwardPublicityDetail(id) {
  return request({
    url: `/competition/publicity/${id}`,
    method: 'get'
  })
}

// 导入获奖公示（新建/重新导入）
export function importAwardPublicity(data) {
  return request({
    url: '/competition/publicity/importData',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 设置公示过期时间
export function setExpireTime(data) {
  return request({
    url: '/competition/publicity',
    method: 'put',
    data
  })
}

// 设置公示提示信息
export function setHintText(data) {
  return request({
    url: '/competition/publicity/tipInfo',
    method: 'put',
    data
  })
}

// 查询获奖名单列表
export function getAwardDetailsList(params) {
  return request({
    url: '/competition/details/awardDetailsList',
    method: 'get',
    params: params
  })
}

// 删除获奖公示
export function deleteAwardPublicity(ids) {
  return request({
    url: `/competition/publicity/${ids}`,
    method: 'delete'
  })
}

// 编辑获奖名单明细
export function editAwardDetails(data) {
  return request({
    url: '/competition/details/editAwardDetails',
    method: 'post',
    data
  })
}

// 导出获奖名单明细
export function exportAwardDetails(data) {
  return request({
    url: '/competition/details/export',
    method: 'post',
    data
  })
}

// 删除获奖名单明细
export function removeAwardDetail(id) {
  return request({
    url: `/competition/details/remove/${id}`,
    method: 'get'
  })
}
