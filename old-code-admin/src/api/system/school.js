import request from '@/utils/request'

// 查询院校列表
export function listschool(query) {
  return request({
    url: '/system/school/list',
    method: 'get',
    params: query
  })
}

// 查询院校详细
export function getschool(postId) {
  return request({
    url: '/system/school/' + postId,
    method: 'get'
  })
}

// 新增院校444
export function addschool(data) {
  return request({
    url: '/system/school',
    method: 'post',
    data: data
  })
}

// 修改院校
export function updateschool(data) {
  return request({
    url: '/system/school',
    method: 'put',
    data: data
  })
}

// 删除院校
export function delschool(postId) {
  return request({
    url: '/system/school/' + postId,
    method: 'delete'
  })
}
