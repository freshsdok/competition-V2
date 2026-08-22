import request from '@/utils/request'

// 查询组件库信息列表
export function listContent(query) {
  return request({
    url: '/content/subassembly/list',
    method: 'get',
    params: query
  })
}
// 查询组件库信息详细
export function subassemblyGetList(query) {
  return request({
    url: '/content/subassembly/getList',
    method: 'get',
    params: query
  })
}
// 查询组件库信息详细
export function getContent(componentId) {
  return request({
    url: '/content/subassembly/' + componentId,
    method: 'get'
  })
}

// 新增组件库信息
export function addContent(data) {
  return request({
    url: '/content/subassembly',
    method: 'post',
    data: data
  })
}

// 修改组件库信息
export function updateContent(data) {
  return request({
    url: '/content/subassembly',
    method: 'put',
    data: data
  })
}

// 删除组件库信息
export function delContent(componentId) {
  return request({
    url: '/content/subassembly/' + componentId,
    method: 'delete'
  })
}
