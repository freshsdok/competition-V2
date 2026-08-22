import request from '@/utils/request'

export function listSceneResourceReservation(query) {
  return request({
    url: '/competition/sceneResourceReservation/list',
    method: 'get',
    params: query
  })
}

export function getSceneResourceReservation(reservationId) {
  return request({
    url: `/competition/sceneResourceReservation/${reservationId}`,
    method: 'get'
  })
}
