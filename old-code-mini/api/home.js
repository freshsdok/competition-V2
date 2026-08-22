import request from '@/utils/request'

// 获取轮播图列表
export function getBannerList(params) {
  return request({
    url: '/content/bannerInfo/pc/list',
    headers: {
      isToken: false
    },
    method: 'get',
    params: params
  })
}

// 获取公告列表
export function getNoticeList(params = {}) {
  return request({
    url: '/content/noticeInfo/getList',
    headers: {
      isToken: false
    },
    method: 'get',
    params: params
  })
}

// 通知公告详情
export function getNoticeDetail(id) {
  return request({
    url: `/content/noticeInfo/public/${id}`,
    headers: {
      isToken: false
    },
    method: 'get'
  })
}

