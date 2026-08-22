import request from '@/utils/request'

export function listReviewPanel(query) {
  return request({
    url: '/competition/review/panel/list',
    method: 'get',
    params: query
  })
}

export function addReviewPanel(data) {
  return request({
    url: '/competition/review/panel',
    method: 'post',
    data
  })
}

export function updateReviewPanel(id, data) {
  return request({
    url: `/competition/review/panel/${id}`,
    method: 'put',
    data
  })
}

export function delReviewPanel(id) {
  return request({
    url: `/competition/review/panel/${id}`,
    method: 'delete'
  })
}

export function listReviewPanelMember(query) {
  return request({
    url: '/competition/review/panel-member/list',
    method: 'get',
    params: query
  })
}

export function addReviewPanelMember(data) {
  return request({
    url: '/competition/review/panel-member',
    method: 'post',
    data
  })
}

export function delReviewPanelMember(id) {
  return request({
    url: `/competition/review/panel-member/${id}`,
    method: 'delete'
  })
}
