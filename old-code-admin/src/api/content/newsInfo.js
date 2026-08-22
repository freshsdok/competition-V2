import request from '@/utils/request'

// 查询资讯信息列表
export function listNewsInfo(query) {
  return request({
    url: '/content/newsInfo/list',
    method: 'get',
    params: query
  })
}

// 查询资讯信息详细
export function getNewsInfo(newsId) {
  return request({
    url: '/content/newsInfo/' + newsId,
    method: 'get'
  })
}

// 新增资讯信息
export function addNewsInfo(data) {
  return request({
    url: '/content/newsInfo',
    method: 'post',
    data: data
  })
}

// 修改资讯信息
export function updateNewsInfo(data) {
  return request({
    url: '/content/newsInfo',
    method: 'put',
    data: data
  })
}

// 删除资讯信息
export function delNewsInfo(newsId) {
  return request({
    url: '/content/newsInfo/' + newsId,
    method: 'delete'
  })
}

// 发布资讯
export function publishNews(newsId) {
  return request({
    url: '/content/newsInfo/publish/' + newsId,
    method: 'put'
  })
}

// 下架资讯
export function offlineNews(newsId) {
  return request({
    url: '/content/newsInfo/offline/' + newsId,
    method: 'put'
  })
}

// 提交审核
export function submitAudit(newsId) {
  return request({
    url: '/content/newsInfo/submitAudit/' + newsId,
    method: 'put'
  })
}

// 增加阅读量
export function increaseReading(newsId) {
  return request({
    url: '/content/newsInfo/increaseReading/' + newsId,
    method: 'get'
  })
}

// 增加点赞数
export function increaseLikes(newsId) {
  return request({
    url: '/content/newsInfo/increaseLikes/' + newsId,
    method: 'post'
  })
}
