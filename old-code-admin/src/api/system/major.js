import request from '@/utils/request'

// 查询专业列表
export function listdiscipline(query) {
  return request({
    url: '/system/discipline/list',
    method: 'get',
    params: query
  })
}

// 查询专业详细
export function getdiscipline(postId) {
  return request({
    url: '/system/discipline/' + postId,
    method: 'get'
  })
}

// 新增专业
export function adddiscipline(data) {
  return request({
    url: '/system/discipline',
    method: 'post',
    data: data
  })
}

// 修改专业
export function updatediscipline(data) {
  return request({
    url: '/system/discipline',
    method: 'put',
    data: data
  })
}

// 删除专业
export function deldiscipline(postId) {
  return request({
    url: '/system/discipline/' + postId,
    method: 'delete'
  })
}
