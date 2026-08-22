import request from '@/utils/request'

export function listReviewRound(query) {
  return request({
    url: '/competition/review/round/list',
    method: 'get',
    params: query
  })
}

export function getReviewRound(id) {
  return request({
    url: `/competition/review/round/${id}`,
    method: 'get'
  })
}

export function addReviewRound(data) {
  return request({
    url: '/competition/review/round',
    method: 'post',
    data
  })
}

export function updateReviewRound(id, data) {
  return request({
    url: `/competition/review/round/${id}`,
    method: 'put',
    data
  })
}

export function delReviewRound(id) {
  return request({
    url: `/competition/review/round/${id}`,
    method: 'delete'
  })
}

export function bindReviewRoundRule(roundId, ruleId) {
  return request({
    url: `/competition/review/round/${roundId}/bind-rule`,
    method: 'post',
    data: { ruleId }
  })
}
