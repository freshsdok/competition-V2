import request from '@/utils/request'

// ==================== 赛场安排 ====================

export function listSceneSchedule(query) {
  return request({
    url: '/competition/sceneSchedule/list',
    method: 'get',
    params: query
  })
}

export function getSceneSchedule(scheduleId) {
  return request({
    url: `/competition/sceneSchedule/${scheduleId}`,
    method: 'get'
  })
}

export function addSceneSchedule(data) {
  return request({
    url: '/competition/sceneSchedule',
    method: 'post',
    data
  })
}

export function updateSceneSchedule(data) {
  return request({
    url: '/competition/sceneSchedule',
    method: 'put',
    data
  })
}

export function delSceneSchedule(scheduleIds) {
  return request({
    url: `/competition/sceneSchedule/${scheduleIds}`,
    method: 'delete'
  })
}

export function matchSceneSchedule(scheduleId) {
  return request({
    url: `/competition/sceneSchedule/match/${scheduleId}`,
    method: 'post'
  })
}

// ==================== 安排对象 ====================

export function listSceneTarget(query) {
  return request({
    url: '/competition/sceneSchedule/target/list',
    method: 'get',
    params: query
  })
}

export function addSceneTarget(data) {
  return request({
    url: '/competition/sceneSchedule/target',
    method: 'post',
    data
  })
}

export function updateSceneTarget(data) {
  return request({
    url: '/competition/sceneSchedule/target',
    method: 'put',
    data
  })
}

export function delSceneTarget(targetIds) {
  return request({
    url: `/competition/sceneSchedule/target/${targetIds}`,
    method: 'delete'
  })
}

export function bindSceneReviewObjects(scheduleId, data) {
  return request({
    url: `/competition/scene/schedule/${scheduleId}/targets/review-objects`,
    method: 'post',
    data
  })
}

export function bindSceneTeams(scheduleId, data) {
  return request({
    url: `/competition/scene/schedule/${scheduleId}/targets/teams`,
    method: 'post',
    data
  })
}

export function bindScenePersons(scheduleId, data) {
  return request({
    url: `/competition/scene/schedule/${scheduleId}/targets/persons`,
    method: 'post',
    data
  })
}

export function addSceneManualTarget(scheduleId, data) {
  return request({
    url: `/competition/scene/schedule/${scheduleId}/targets/manual`,
    method: 'post',
    data
  })
}

export function saveSceneTargetSequence(scheduleId, data) {
  return request({
    url: `/competition/scene/schedule/${scheduleId}/targets/sequence`,
    method: 'post',
    data
  })
}

export function autoGenerateSceneTargetSequence(scheduleId, data) {
  return request({
    url: `/competition/scene/schedule/${scheduleId}/targets/sequence/auto-generate`,
    method: 'post',
    data
  })
}

export function sortSceneTargetSequenceByNames(scheduleId, data) {
  return request({
    url: `/competition/scene/schedule/${scheduleId}/targets/sequence/by-name`,
    method: 'post',
    data
  })
}

export function syncSceneTargetsToReviewSession(scheduleId, data) {
  return request({
    url: `/competition/scene/schedule/${scheduleId}/targets/sync-review-session`,
    method: 'post',
    data
  })
}

// ==================== 证件列表 ====================

export function listSceneCredential(query) {
  return request({
    url: '/competition/sceneCredential/list',
    method: 'get',
    params: query
  })
}

export function generateSceneCredential(data) {
  return request({
    url: '/competition/sceneCredential/generate',
    method: 'post',
    data
  })
}

export function updateSceneCredential(data) {
  return request({
    url: '/competition/sceneCredential',
    method: 'put',
    data
  })
}

export function delSceneCredential(credentialIds) {
  return request({
    url: `/competition/sceneCredential/${credentialIds}`,
    method: 'delete'
  })
}

// ==================== 操作流水 ====================

export function listSceneVerifyLog(query) {
  return request({
    url: '/competition/sceneVerify/log/list',
    method: 'get',
    params: query
  })
}
