import request from '@/utils/request'

export function listReviewActivity(query) {
  return request({
    url: '/competition/review/activity/list',
    method: 'get',
    params: query
  })
}

export function getReviewActivity(id) {
  return request({
    url: `/competition/review/activity/${id}`,
    method: 'get'
  })
}

export function addReviewActivity(data) {
  return request({
    url: '/competition/review/activity',
    method: 'post',
    data
  })
}

export function updateReviewActivity(id, data) {
  return request({
    url: `/competition/review/activity/${id}`,
    method: 'put',
    data
  })
}

export function delReviewActivity(id) {
  return request({
    url: `/competition/review/activity/${id}`,
    method: 'delete'
  })
}

export function closeSubmission(activityId) {
  return request({
    url: `/competition/review/activity/${activityId}/close-submission`,
    method: 'post'
  })
}
