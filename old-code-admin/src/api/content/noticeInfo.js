import request from '@/utils/request'

// 查询通知公告信息列表
export function listNoticeInfo(query) {
  return request({
    url: '/content/noticeInfo/list',
    method: 'get',
    params: query
  })
}

// 查询通知公告信息详细
export function getNoticeInfo(noticeId) {
  return request({
    url: '/content/noticeInfo/' + noticeId,
    method: 'get'
  })
}

// 新增通知公告信息
export function addNoticeInfo(data) {
  return request({
    url: '/content/noticeInfo',
    method: 'post',
    data: data
  })
}

// 修改通知公告信息
export function updateNoticeInfo(data) {
  return request({
    url: '/content/noticeInfo',
    method: 'put',
    data: data
  })
}

// 删除通知公告信息
export function delNoticeInfo(noticeId) {
  return request({
    url: '/content/noticeInfo/' + noticeId,
    method: 'delete'
  })
}

// 发布通知公告
export function publishNotice(noticeId) {
  return request({
    url: '/content/noticeInfo/publish/' + noticeId,
    method: 'put'
  })
}

// 下架通知公告
export function offlineNotice(noticeId) {
  return request({
    url: '/content/noticeInfo/offline/' + noticeId,
    method: 'put'
  })
}

// 提交审核
export function submitAudit(noticeId) {
  return request({
    url: '/content/noticeInfo/submitAudit/' + noticeId,
    method: 'put'
  })
}


