import request from '@/utils/request'

export function listReviewObject(query) {
  return request({
    url: '/competition/review/object/list',
    method: 'get',
    params: query
  })
}

export function getReviewObject(id) {
  return request({
    url: `/competition/review/object/${id}`,
    method: 'get'
  })
}

export function addReviewObject(data) {
  return request({
    url: '/competition/review/object',
    method: 'post',
    data
  })
}

export function updateReviewObject(id, data) {
  return request({
    url: `/competition/review/object/${id}`,
    method: 'put',
    data
  })
}

export function delReviewObject(id) {
  return request({
    url: `/competition/review/object/${id}`,
    method: 'delete'
  })
}

export function importPreview(data) {
  return request({
    url: '/competition/review/object/import-preview',
    method: 'post',
    data
  })
}

export function importFromBusiness(data) {
  return request({
    url: '/competition/review/object/import-from-business',
    method: 'post',
    data
  })
}

export function syncFileTaskMaterials(data) {
  return request({
    url: '/competition/review/object/sync-file-task-materials',
    method: 'post',
    data
  })
}

export function listObjectMembers(id) {
  return request({
    url: `/competition/review/object/${id}/members`,
    method: 'get'
  })
}

export function listObjectPermissions(id) {
  return request({
    url: `/competition/review/object/${id}/permissions`,
    method: 'get'
  })
}

export function listObjectCertificates(id) {
  return request({
    url: `/competition/review/object/${id}/certificates`,
    method: 'get'
  })
}

export function listObjectExternalRefs(id) {
  return request({
    url: `/competition/review/object/${id}/external-refs`,
    method: 'get'
  })
}

export function resolveCertificate(query) {
  return request({
    url: '/competition/review/object/certificate/resolve',
    method: 'get',
    params: query
  })
}
