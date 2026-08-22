import request from '@/utils/request'

export function listMyReviewActivityRounds() {
  return request({
    url: '/competition/review/my-review/activity-rounds',
    method: 'get'
  })
}

export function listMyReview(query) {
  return request({
    url: '/competition/review/my-review/list',
    method: 'get',
    params: query
  })
}

export function getMyReviewCurrentObject(sessionId) {
  return request({
    url: `/competition/review/my-review/session/${sessionId}/current-object`,
    method: 'get'
  })
}

export function getMyReviewDetail(assignmentId) {
  return request({
    url: `/competition/review/my-review/${assignmentId}`,
    method: 'get'
  })
}

export function getMyReviewCriteria(assignmentId) {
  return request({
    url: `/competition/review/my-review/${assignmentId}/criteria`,
    method: 'get'
  })
}

export function saveMyReviewDraft(assignmentId, data) {
  return request({
    url: `/competition/review/my-review/${assignmentId}/draft`,
    method: 'post',
    data
  })
}

export function submitMyReview(assignmentId, data) {
  return request({
    url: `/competition/review/my-review/${assignmentId}/submit`,
    method: 'post',
    data
  })
}
