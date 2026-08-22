import request from '@/utils/request'

export function listSceneResourceSlot(query) {
  return request({
    url: '/competition/sceneResourceSlot/list',
    method: 'get',
    params: query
  })
}

export function getSceneResourceSlot(slotId) {
  return request({
    url: `/competition/sceneResourceSlot/${slotId}`,
    method: 'get'
  })
}

export function addSceneResourceSlot(data) {
  return request({
    url: '/competition/sceneResourceSlot',
    method: 'post',
    data
  })
}

export function batchGenerateSceneResourceSlot(data) {
  return request({
    url: '/competition/sceneResourceSlot/batch',
    method: 'post',
    data
  })
}

export function updateSceneResourceSlot(data) {
  return request({
    url: '/competition/sceneResourceSlot',
    method: 'put',
    data
  })
}

export function delSceneResourceSlot(slotIds) {
  return request({
    url: `/competition/sceneResourceSlot/${slotIds}`,
    method: 'delete'
  })
}

export function changeSceneResourceSlotStatus(data) {
  return request({
    url: '/competition/sceneResourceSlot/changeStatus',
    method: 'post',
    data
  })
}
