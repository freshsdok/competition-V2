import request from '@/utils/request'

export function listReviewSession(query) {
  return request({
    url: '/competition/review/session/list',
    method: 'get',
    params: query
  })
}

export function getReviewSession(id) {
  return request({
    url: `/competition/review/session/${id}`,
    method: 'get'
  })
}

export function addReviewSession(data) {
  return request({
    url: '/competition/review/session',
    method: 'post',
    data
  })
}

export function updateReviewSession(id, data) {
  return request({
    url: `/competition/review/session/${id}`,
    method: 'put',
    data
  })
}

export function delReviewSession(id) {
  return request({
    url: `/competition/review/session/${id}`,
    method: 'delete'
  })
}

export function getCurrentReviewObject(sessionId) {
  return request({
    url: `/competition/review/session/${sessionId}/current-object`,
    method: 'get'
  })
}

export function setCurrentReviewObject(sessionId, data) {
  return request({
    url: `/competition/review/session/${sessionId}/current-object`,
    method: 'post',
    data
  })
}

export function addReviewSessionObject(data) {
  return request({
    url: '/competition/review/session/object',
    method: 'post',
    data
  })
}

export function updateReviewSessionObject(id, data) {
  return request({
    url: `/competition/review/session/object/${id}`,
    method: 'put',
    data
  })
}

export function delReviewSessionObject(id) {
  return request({
    url: `/competition/review/session/object/${id}`,
    method: 'delete'
  })
}

export function listReviewSessionObject(query) {
  return request({
    url: '/competition/review/session/object/list',
    method: 'get',
    params: query
  })
}

export function listReviewSessionEventLog(query) {
  return request({
    url: '/competition/review/session/event-log/list',
    method: 'get',
    params: query
  })
}
