import request from '@/utils/request'

// 查询内容文件列表
export function listContentFile(query) {
  return request({
    url: '/content/contentFile/list',
    method: 'get',
    params: query
  })
}

// 查询内容文件详细
export function getContentFile(fileId) {
  return request({
    url: '/content/contentFile/' + fileId,
    method: 'get'
  })
}

// 根据栏目ID获取文件列表（前端页面使用）
export function getFileListByColumnId(columnId) {
  return request({
    url: '/content/contentFile/getByColumnId/' + columnId,
    method: 'get'
  })
}

// 新增内容文件
export function addContentFile(data) {
  return request({
    url: '/content/contentFile/add',
    method: 'post',
    data: data
  })
}

// 修改内容文件
export function updateContentFile(data) {
  return request({
    url: '/content/contentFile',
    method: 'put',
    data: data
  })
}

// 删除内容文件
export function delContentFile(fileId) {
  return request({
    url: '/content/contentFile/' + fileId,
    method: 'delete'
  })
}

// 批量删除内容文件
export function delContentFileBatch(fileIds) {
  return request({
    url: '/content/contentFile/' + fileIds,
    method: 'delete'
  })
}

// 上传文件
export function uploadFile(formData) {
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData
  })
}

// 根据栏目ID获取文件（前端用户直接下载使用，无需权限验证）
export function getFileByColumnId(columnId) {
  return request({
    url: '/content/contentFile/getFileByColumnId/' + columnId,
    method: 'get'
  })
}

// 上传文件
export function updateFileItem(formData) {
  return request({
    url: '/content/contentFile',
    method: 'put',
    data: formData
  })
}