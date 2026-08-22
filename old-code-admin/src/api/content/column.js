import request from '@/utils/request'

// 查询内容栏目列表
export function listContentColumn(query) {
  return request({
    url: '/content/contentColumn/list',
    method: 'get',
    params: query
  })
}

// 查询内容栏目详细
export function getContentColumn(columnId) {
  return request({
    url: '/content/contentColumn/' + columnId,
    method: 'get'
  })
}

// 根据菜单ID获取栏目信息（前端页面使用）
export function getColumnByMenuId(menuId) {
  return request({
    url: '/content/contentColumn/getByMenuId/' + menuId,
    method: 'get'
  })
}

// 获取栏目树形结构
export function getColumnTree(query) {
  return request({
    url: '/content/contentColumn/tree',
    method: 'get',
    params: query
  })
}

// 新增内容栏目
export function addContentColumn(data) {
  return request({
    url: '/content/contentColumn',
    method: 'post',
    data: data
  })
}

// 修改内容栏目
export function updateContentColumn(data) {
  return request({
    url: '/content/contentColumn',
    method: 'put',
    data: data
  })
}

// 删除内容栏目
export function delContentColumn(columnId) {
  return request({
    url: '/content/contentColumn/' + columnId,
    method: 'delete'
  })
}

// 批量删除内容栏目
export function delContentColumnBatch(columnIds) {
  return request({
    url: '/content/contentColumn/' + columnIds,
    method: 'delete'
  })
}
