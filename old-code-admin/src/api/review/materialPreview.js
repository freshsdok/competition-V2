import request from '@/utils/request'

export function getOssPreviewUrl(fileKey) {
  return request({
    url: '/file/oss/previewUrl',
    method: 'get',
    params: { fileKey }
  })
}

export function isOssFileUrl(url) {
  if (!url) {
    return false
  }
  const lower = String(url).trim().toLowerCase()
  return lower.includes('.aliyuncs.com/')
    || lower.includes('.oss-')
    || lower.includes('oss.ksup.cn')
}

export function normalizeDirectFileUrl(url) {
  if (!url) {
    return ''
  }
  const value = String(url).trim()
  const normalized = /^https?:\/\//i.test(value) || value.startsWith('/')
    ? value
    : `/${value}`
  return encodeURI(normalized)
}
