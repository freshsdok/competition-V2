import request from '@/utils/request'

// 查询列表
export function sponsoringEnterpriseList(query,data) {
  return request({
    url: '/competition/sponsoringEnterprise/list',
    method: 'post',
    data: data,
    params: query
  })
}
// 保存赞助企业信息
export function saveSponsoringEnterpriseInfo(data) {
  return request({
    url: '/competition/sponsoringEnterprise/saveSponsoringEnterpriseInfo',
    method: 'post',
    data: data
  })
}

// 更新赞助企业信息
export function updateSponsoringEnterpriseInfo(data) {
  return request({
    url: '/competition/sponsoringEnterprise/updateSponsoringEnterpriseInfo',
    method: 'post',
    data: data
  })
}

// 查询赞助企业详情
export function getSponsoringEnterpriseDetailInfo(id,{}) {
  return request({
    url: `/competition/sponsoringEnterprise/getSponsoringEnterpriseDetailInfo/${id}`,
    method: 'get',
    params: query
  })
}

// 删除赞助企业信息
export function removeSponsoringEnterpriseInfo(id,query) {
  return request({
    url: `/competition/sponsoringEnterprise/removeSponsoringEnterpriseInfo/${id}`,
    method: 'get',
    params: query
  })
}
