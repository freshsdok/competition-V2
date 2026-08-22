import request from '@/utils/request'

// 查询数据源信息列表
export function listSource(query) {
  return request({
    url: '/content/questions/list',
    method: 'get',
    params: query
  })
}

// 查询数据源信息详细
export function getSource(dataId) {
  return request({
    url: '/content/questions/' + dataId,
    method: 'get'
  })
}

// 新增数据源信息
export function addSource(data) {
  return request({
    url: '/content/questions',
    method: 'post',
    data: data
  })
}

// 修改数据源信息
export function updateSource(data) {
  return request({
    url: '/content/questions',
    method: 'put',
    data: data
  })
}

// 删除数据源信息
export function delSource(dataId) {
  return request({
    url: '/content/questions/' + dataId,
    method: 'delete'
  })
}
