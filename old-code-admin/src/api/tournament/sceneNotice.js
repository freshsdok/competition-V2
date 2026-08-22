import request from '@/utils/request'

function encodeUtf8Base64(value) {
  const text = value || ''
  if (typeof TextEncoder === 'undefined') {
    return window.btoa(unescape(encodeURIComponent(text)))
  }
  const bytes = new TextEncoder().encode(text)
  const chunkSize = 0x8000
  let binary = ''
  for (let offset = 0; offset < bytes.length; offset += chunkSize) {
    binary += String.fromCharCode(...bytes.subarray(offset, offset + chunkSize))
  }
  return window.btoa(binary)
}

function encodeNoticeContent(data) {
  const payload = { ...data }
  if (typeof payload.content === 'string') {
    payload.contentBase64 = encodeUtf8Base64(payload.content)
    delete payload.content
  }
  return payload
}

export function listSceneNotice(params) {
  return request({
    url: '/competition/sceneNotice/list',
    method: 'get',
    params
  })
}

export function getSceneNotice(noticeId) {
  return request({
    url: `/competition/sceneNotice/${noticeId}`,
    method: 'get'
  })
}

export function addSceneNotice(data) {
  return request({
    url: '/competition/sceneNotice',
    method: 'post',
    data: encodeNoticeContent(data)
  })
}

export function updateSceneNotice(data) {
  return request({
    url: '/competition/sceneNotice',
    method: 'put',
    data: encodeNoticeContent(data)
  })
}

export function delSceneNotice(noticeIds) {
  return request({
    url: `/competition/sceneNotice/${noticeIds}`,
    method: 'delete'
  })
}

export function changeSceneNoticeStatus(data) {
  return request({
    url: '/competition/sceneNotice/changeStatus',
    method: 'put',
    data
  })
}

export function publishSceneNotice(noticeId) {
  return request({
    url: `/competition/sceneNotice/publish/${noticeId}`,
    method: 'post'
  })
}
