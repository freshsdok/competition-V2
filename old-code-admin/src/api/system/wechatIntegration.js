import request from '@/utils/request'

// 查询微信集成列表
export function listWechatIntegration(query) {
  return request({
    url: '/system/wechatIntegration/list',
    method: 'get',
    params: query
  })
}

// 查询微信集成详细
export function getWechatIntegration(id) {
  return request({
    url: '/system/wechatIntegration/' + id,
    method: 'get'
  })
}

// 新增微信集成
export function addWechatIntegration(data) {
  return request({
    url: '/system/wechatIntegration',
    method: 'post',
    data: data
  })
}

// 修改微信集成
export function updateWechatIntegration(data) {
  return request({
    url: '/system/wechatIntegration',
    method: 'put',
    data: data
  })
}

// 删除微信集成
export function delWechatIntegration(ids) {
  return request({
    url: '/system/wechatIntegration/' + ids,
    method: 'delete'
  })
}

// 重置查询条件
export function resetWechatIntegration() {
  return request({
    url: '/system/wechatIntegration/reset',
    method: 'post'
  })
}
