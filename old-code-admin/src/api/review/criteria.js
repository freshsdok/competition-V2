import request from '@/utils/request'

export function listReviewCriteria(query) {
  return request({
    url: '/competition/review/criteria/list',
    method: 'get',
    params: query
  })
}

export function getReviewCriteria(id) {
  return request({
    url: `/competition/review/criteria/${id}`,
    method: 'get'
  })
}

export function addReviewCriteria(data) {
  return request({
    url: '/competition/review/criteria',
    method: 'post',
    data
  })
}

export function updateReviewCriteria(id, data) {
  return request({
    url: `/competition/review/criteria/${id}`,
    method: 'put',
    data
  })
}

export function delReviewCriteria(id) {
  return request({
    url: `/competition/review/criteria/${id}`,
    method: 'delete'
  })
}
