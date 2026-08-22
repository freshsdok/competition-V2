import request from '@/utils/request'

export function listSceneResourceScheduleScope(query) {
  return request({
    url: '/competition/sceneResourceScheduleScope/list',
    method: 'get',
    params: query
  })
}

export function addSceneResourceScheduleScope(data) {
  return request({
    url: '/competition/sceneResourceScheduleScope/add',
    method: 'post',
    data
  })
}

export function removeSceneResourceScheduleScope(data) {
  return request({
    url: '/competition/sceneResourceScheduleScope/remove',
    method: 'post',
    data
  })
}

export function ensureSceneResourceScheduleScope(data) {
  return request({
    url: '/competition/sceneResourceScheduleScope/ensure',
    method: 'post',
    data
  })
}

export function batchEnsureSceneResourceScheduleScope(data) {
  return request({
    url: '/competition/sceneResourceScheduleScope/batchEnsure',
    method: 'post',
    data
  })
}
