import request from '@/utils/request'

export function listMySubmission(query) {
  return request({
    url: '/competition/review/submission/my-list',
    method: 'get',
    params: query
  })
}

export function getSubmissionDetail(objectId) {
  return request({
    url: `/competition/review/submission/${objectId}`,
    method: 'get'
  })
}

export function saveSubmissionDraft(objectId, data) {
  return request({
    url: `/competition/review/submission/${objectId}/draft`,
    method: 'put',
    data
  })
}

export function addSubmissionMaterial(objectId, data) {
  return request({
    url: `/competition/review/submission/${objectId}/material`,
    method: 'post',
    data
  })
}

export function listSubmissionMaterials(objectId) {
  return request({
    url: `/competition/review/submission/${objectId}/materials`,
    method: 'get'
  })
}

export function deleteSubmissionMaterial(materialId) {
  return request({
    url: `/competition/review/submission/material/${materialId}`,
    method: 'delete'
  })
}

export function submitSubmission(objectId) {
  return request({
    url: `/competition/review/submission/${objectId}/submit`,
    method: 'post'
  })
}

export function withdrawSubmission(objectId, data) {
  return request({
    url: `/competition/review/submission/${objectId}/withdraw-request`,
    method: 'post',
    data
  })
}

export function approveWithdraw(objectId, data) {
  return request({
    url: `/competition/review/submission/${objectId}/withdraw-approve`,
    method: 'post',
    data
  })
}

export function rejectWithdraw(objectId, data) {
  return request({
    url: `/competition/review/submission/${objectId}/withdraw-reject`,
    method: 'post',
    data
  })
}

export function getSubmissionResult(objectId) {
  return request({
    url: `/competition/review/submission/${objectId}/result`,
    method: 'get'
  })
}
