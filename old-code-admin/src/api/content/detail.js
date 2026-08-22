import request from '@/utils/request'

// 查询内容详情列表
export function listContentDetail(query) {
  return request({
    url: '/content/contentDetail/list',
    method: 'get',
    params: query
  })
}

// 查询内容详情详细
export function getContentDetail(detailId) {
  return request({
    url: '/content/contentDetail/' + detailId,
    method: 'get'
  })
}

// 根据栏目ID获取详情（前端页面使用）
export function getDetailByColumnId(columnId) {
  return request({
    url: '/content/contentDetail/getByColumnId/' + columnId,
    method: 'get'
  })
}

// 新增内容详情
export function addContentDetail(data) {
  return request({
    url: '/content/contentDetail',
    method: 'post',
    data: data
  })
}

// 修改内容详情
export function updateContentDetail(data) {
  return request({
    url: '/content/contentDetail',
    method: 'put',
    data: data
  })
}

// 删除内容详情
export function delContentDetail(detailId) {
  return request({
    url: '/content/contentDetail/' + detailId,
    method: 'delete'
  })
}

// 批量删除内容详情
export function delContentDetailBatch(detailIds) {
  return request({
    url: '/content/contentDetail/' + detailIds,
    method: 'delete'
  })
}
