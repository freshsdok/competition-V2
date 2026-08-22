import request from '@/utils/request'

// 用户证书信息列表
export function getUserCertList(params) {
  return request({
    url: '/competition/competition/userCertificate/list',
    method: 'get',
    params
  })
}

// 用户证书信息
export function getUserCertInfo(data) {
  return request({
    url: `/competition/competition/userCertificate/getCertInfo`,
    method: 'post',
    data
  })
}

// 颁发用户证书信息
export function issueUserCerts(data) {
  return request({
    url: `/competition/competition/userCertificate/batchSaveUserCertificate`,
    method: 'post',
    data
  })
}

// 修改用户证书信息
export function updateUserCertInfo(data) {
  return request({
    url: `/competition/competition/userCertificate`,
    method: 'put',
    data
  })
}


// 用户源证书 
export function getUserCertificateOriginList(params) {
  return request({
    url: "/competition/competition/userCertificateOrigin/list",
    method: "get",
    params,
  });
}

// 异步导出用户证书信息
export function exportUserCert(params) {
  return request({
    url: '/competition/competition/userCertificate/export',
    method: 'post',
    data: params
  })
}

// 删除用户证书
export function delUserCert(data) {
  return request({
    url: `/competition/competition/userCertificate/remove`,
    method: 'post',
    data
  })
}