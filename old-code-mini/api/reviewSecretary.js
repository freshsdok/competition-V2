import request from '@/utils/request'

export function listMySecretarySessions(params, silent = false) {
  return request({
    url: '/competition/review/secretary/session/my-list',
    method: 'get',
    params,
    headers: {
      isSilent: silent
    }
  })
}

export function getSecretarySession(sessionId) {
  return request({
    url: `/competition/review/secretary/session/${sessionId}`,
    method: 'get'
  })
}

export function listSecretarySessionObjects(sessionId) {
  return request({
    url: `/competition/review/secretary/session/${sessionId}/objects`,
    method: 'get'
  })
}

export function resolveReviewCertificate(params) {
  return request({
    url: '/competition/review/object/certificate/resolve',
    method: 'get',
    params
  })
}

export function setSecretaryCurrentObject(sessionId, data) {
  return request({
    url: `/competition/review/secretary/session/${sessionId}/current-object`,
    method: 'post',
    data
  })
}

export function nextSecretaryObject(sessionId) {
  return request({
    url: `/competition/review/secretary/session/${sessionId}/next-object`,
    method: 'post'
  })
}

export function updateSecretarySessionObjectStatus(sessionObjectId, data) {
  return request({
    url: `/competition/review/secretary/session-object/${sessionObjectId}/status`,
    method: 'post',
    data
  })
}
