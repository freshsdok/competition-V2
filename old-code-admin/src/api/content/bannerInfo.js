import request from '@/utils/request'

// 查询banner图管理列表
export function listBannerInfo(query) {
  return request({
    url: '/content/bannerInfo/list',
    method: 'get',
    params: query
  })
}

// 查询banner图管理详细
export function getBannerInfo(id) {
  return request({
    url: '/content/bannerInfo/' + id,
    method: 'get'
  })
}

// 新增banner图管理
export function addBannerInfo(data) {
  return request({
    url: '/content/bannerInfo',
    method: 'post',
    data: data
  })
}

// 修改banner图管理
export function updateBannerInfo(data) {
  return request({
    url: '/content/bannerInfo',
    method: 'put',
    data: data
  })
}

// 删除banner图管理
export function delBannerInfo(id) {
  return request({
    url: '/content/bannerInfo/' + id,
    method: 'delete'
  })
}
