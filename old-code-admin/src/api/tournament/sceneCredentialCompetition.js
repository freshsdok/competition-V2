import request from '@/utils/request'

export function listCompetitionSceneCredential(query) {
  return request({
    url: '/competition/sceneCredential/competitionList',
    method: 'get',
    params: query
  })
}

export function getCompetitionSceneCredential(credentialId) {
  return request({
    url: `/competition/sceneCredential/${credentialId}`,
    method: 'get'
  })
}

export function competitionDirectIssue(data) {
  return request({
    url: '/competition/sceneCredential/competitionDirectIssue',
    method: 'post',
    data
  })
}

export function updateCompetitionSceneCredential(data) {
  return request({
    url: '/competition/sceneCredential',
    method: 'put',
    data
  })
}

export function delCompetitionSceneCredential(credentialIds) {
  return request({
    url: `/competition/sceneCredential/${credentialIds}`,
    method: 'delete'
  })
}
