import request from '@/utils/request'

// 查询审计日志列表
export function listAuditLog(query) {
  return request({
    url: '/system/auditlog/list',
    method: 'get',
    params: query
  })
}

// 查询审计日志详细
export function getAuditLog(auditId) {
  return request({
    url: '/system/auditlog/' + auditId,
    method: 'get'
  })
}

// 审计日志
export function auditAuditLog(data) {
  return request({
    url: '/system/auditlog/audit',
    method: 'put',
    data: data
  })
}

// 忽略审计日志
export function ignoreAuditLog(data) {
  return request({
    url: '/system/auditlog/ignore',
    method: 'put',
    data: data
  })
}

// 删除审计日志
export function delAuditLog(auditIds) {
  return request({
    url: '/system/auditlog/' + auditIds,
    method: 'delete'
  })
}

// 清空审计日志
export function cleanAuditLog() {
  return request({
    url: '/system/auditlog/clean',
    method: 'delete'
  })
}

// 导出审计日志
export function exportAuditLog(query) {
  return request({
    url: '/system/auditlog/export',
    method: 'post',
    params: query
  })
}

// 统计审计日志（按审计类型）
export function statisticsByType() {
  return request({
    url: '/system/auditlog/statistics/byType',
    method: 'get'
  })
}

// 统计审计日志（按风险级别）
export function statisticsByRiskLevel() {
  return request({
    url: '/system/auditlog/statistics/byRiskLevel',
    method: 'get'
  })
}

// 统计异常行为
export function statisticsAbnormalBehavior() {
  return request({
    url: '/system/auditlog/statistics/abnormalBehavior',
    method: 'get'
  })
}
