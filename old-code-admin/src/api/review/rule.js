import request from '@/utils/request'

export function listReviewRule(query) {
  return request({
    url: '/competition/review/rule/list',
    method: 'get',
    params: query
  })
}

export function getReviewRule(id) {
  return request({
    url: `/competition/review/rule/${id}`,
    method: 'get'
  })
}

export function addReviewRule(data) {
  return request({
    url: '/competition/review/rule',
    method: 'post',
    data
  })
}

export function updateReviewRule(id, data) {
  return request({
    url: `/competition/review/rule/${id}`,
    method: 'put',
    data
  })
}

export function delReviewRule(id) {
  return request({
    url: `/competition/review/rule/${id}`,
    method: 'delete'
  })
}

export function validateReviewRule(id) {
  return request({
    url: `/competition/review/rule/${id}/validate`,
    method: 'post'
  })
}

export function enableReviewRule(id) {
  return request({
    url: `/competition/review/rule/${id}/enable`,
    method: 'post'
  })
}

export function disableReviewRule(id) {
  return request({
    url: `/competition/review/rule/${id}/disable`,
    method: 'post'
  })
}

export function copyReviewRule(id) {
  return request({
    url: `/competition/review/rule/${id}/copy`,
    method: 'post'
  })
}
