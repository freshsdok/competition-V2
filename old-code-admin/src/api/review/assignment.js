import request from '@/utils/request'

export function listReviewAssignment(query) {
  return request({
    url: '/competition/review/assignment/list',
    method: 'get',
    params: query
  })
}

export function getReviewAssignment(id) {
  return request({
    url: `/competition/review/assignment/${id}`,
    method: 'get'
  })
}

export function addReviewAssignment(data) {
  return request({
    url: '/competition/review/assignment',
    method: 'post',
    data
  })
}

export function batchAssignReviewAssignment(data) {
  return request({
    url: '/competition/review/assignment/batch',
    method: 'post',
    data
  })
}

export function updateReviewAssignment(id, data) {
  return request({
    url: `/competition/review/assignment/${id}`,
    method: 'put',
    data
  })
}

export function delReviewAssignment(id) {
  return request({
    url: `/competition/review/assignment/${id}`,
    method: 'delete'
  })
}
