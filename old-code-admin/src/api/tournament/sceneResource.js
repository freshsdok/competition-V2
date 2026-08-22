import request from '@/utils/request'

export function listSceneResource(query) {
  return request({
    url: '/competition/sceneResource/list',
    method: 'get',
    params: query
  })
}

export function getSceneResource(resourceId) {
  return request({
    url: `/competition/sceneResource/${resourceId}`,
    method: 'get'
  })
}

export function addSceneResource(data) {
  return request({
    url: '/competition/sceneResource',
    method: 'post',
    data
  })
}

export function updateSceneResource(data) {
  return request({
    url: '/competition/sceneResource',
    method: 'put',
    data
  })
}

export function delSceneResource(resourceIds) {
  return request({
    url: `/competition/sceneResource/${resourceIds}`,
    method: 'delete'
  })
}

export function changeSceneResourceStatus(data) {
  return request({
    url: '/competition/sceneResource/changeStatus',
    method: 'post',
    data
  })
}
