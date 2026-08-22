import request from '@/utils/request'

export function listMyFileTasks(params = {}) {
  return request({ url: '/system/fileDistributeUserTask/list', method: 'get', params })
}

export function getMyFileUnreadCount() {
  return request({ url: '/system/fileDistributeUserTask/unReadCount', method: 'get' })
}

export function getSystemTime() {
  return request({ url: '/system/fileDistributeUserTask/getSystemDate', method: 'get' })
}

export function markFileTaskRead(fileTaskId) {
  return request({ url: '/system/fileDistributeUserTask/fileTaskReadRecord', method: 'get', params: { fileTaskId } })
}

export function listFileTaskNotifications(taskId) {
  return request({ url: `/system/fileDistributeUserTask/${taskId}/notifications`, method: 'get' })
}

export function getFileTaskNotificationDetail(taskId, notificationId) {
  return request({ url: `/system/fileDistributeUserTask/${taskId}/notifications/${notificationId}`, method: 'get' })
}

export function saveMyFileUploadRecord(data) {
  return request({ url: '/system/fileDistributeUserTask/saveFileUploadRecordUser', method: 'post', data })
}

export function saveMyFileTaskSubmission(data) {
  return request({ url: '/system/fileDistributeUserTask/saveFileUploadManagerUser', method: 'post', data })
}

export function recordMyFileDownload(data) {
  return request({ url: '/system/downLoadRecord', method: 'post', data })
}

export function getFilePresignedUrl(fileKey) {
  return request({ url: '/file/oss/presignedUrl', method: 'get', params: { fileKey } })
}
