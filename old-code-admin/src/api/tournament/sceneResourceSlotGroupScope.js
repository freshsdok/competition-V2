import request from '@/utils/request'

export function listSceneResourceSlotGroupScopeBySlot(query) {
  return request({
    url: '/competition/sceneResourceSlotGroupScope/listBySlot',
    method: 'get',
    params: query
  })
}

export function replaceSceneResourceSlotGroupScope(data) {
  return request({
    url: '/competition/sceneResourceSlotGroupScope/replace',
    method: 'post',
    data
  })
}

export function batchReplaceSceneResourceSlotGroupScope(data) {
  return request({
    url: '/competition/sceneResourceSlotGroupScope/batchReplace',
    method: 'post',
    data
  })
}

export function listSceneResourceSlotGroupOptions(query) {
  return request({
    url: '/competition/sceneResourceSlotGroupScope/groupOptions',
    method: 'get',
    params: query
  })
}
