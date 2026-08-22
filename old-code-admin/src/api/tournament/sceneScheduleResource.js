import request from '@/utils/request'

export function listSceneScheduleResource(query) {
  return request({
    url: '/competition/sceneScheduleResource/list',
    method: 'get',
    params: query
  })
}

export function getSceneScheduleResource(scheduleResourceId) {
  return request({
    url: `/competition/sceneScheduleResource/${scheduleResourceId}`,
    method: 'get'
  })
}

export function addSceneScheduleResource(data) {
  return request({
    url: '/competition/sceneScheduleResource',
    method: 'post',
    data
  })
}

export function updateSceneScheduleResource(data) {
  return request({
    url: '/competition/sceneScheduleResource',
    method: 'put',
    data
  })
}

export function delSceneScheduleResource(scheduleResourceIds) {
  return request({
    url: `/competition/sceneScheduleResource/${scheduleResourceIds}`,
    method: 'delete'
  })
}

export function changeSceneScheduleResourceBookingStatus(data) {
  return request({
    url: '/competition/sceneScheduleResource/changeBookingStatus',
    method: 'post',
    data
  })
}
