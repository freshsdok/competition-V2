import request from '@/utils/request'

// 查询错误日志列表
export function listErrorLog(query) {
  return request({
    url: '/system/errorlog/list',
    method: 'get',
    params: query
  })
}

// 查询错误日志详细
export function getErrorLog(errorId) {
  return request({
    url: '/system/errorlog/' + errorId,
    method: 'get'
  })
}

// 处理错误日志
export function handleErrorLog(data) {
  return request({
    url: '/system/errorlog/handle',
    method: 'put',
    data: data
  })
}

// 忽略错误日志
export function ignoreErrorLog(data) {
  return request({
    url: '/system/errorlog/ignore',
    method: 'put',
    data: data
  })
}

// 删除错误日志
export function delErrorLog(errorIds) {
  return request({
    url: '/system/errorlog/' + errorIds,
    method: 'delete'
  })
}

// 清空错误日志
export function cleanErrorLog() {
  return request({
    url: '/system/errorlog/clean',
    method: 'delete'
  })
}

// 导出错误日志
export function exportErrorLog(query) {
  return request({
    url: '/system/errorlog/export',
    method: 'post',
    params: query
  })
}
