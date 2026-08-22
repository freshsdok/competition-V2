import request from '@/utils/request'

export function listReviewResult(query) {
  return request({
    url: '/competition/review/result/list',
    method: 'get',
    params: query
  })
}

export function generateReviewResult(data) {
  return request({
    url: '/competition/review/result/generate',
    method: 'post',
    data
  })
}

export function updateResultConclusion(id, data) {
  return request({
    url: `/competition/review/result/${id}/conclusion`,
    method: 'put',
    data
  })
}

export function publishReviewResult(id, data) {
  return request({
    url: `/competition/review/result/${id}/publish`,
    method: 'post',
    data
  })
}

export function revokeReviewResult(id, data) {
  return request({
    url: `/competition/review/result/${id}/revoke`,
    method: 'post',
    data
  })
}

export function listReviewResultRecords(query) {
  return request({
    url: '/competition/review/result/records',
    method: 'get',
    params: query
  })
}

export function listReviewScoreDetails(recordId) {
  return request({
    url: `/competition/review/result/record/${recordId}/details`,
    method: 'get'
  })
}
