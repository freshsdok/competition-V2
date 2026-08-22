import request from "@/utils/request";

// 获取用户/管理员
export function systemUserGroupList(params) {
  return request({
    url: `/system/user/group/list`,
    method: 'get',
    params: params
  })
}

// 获取用户/管理员
export function selectAllCompetitionDetailInfo(params) {
  return request({
    url: `/competition/competitionManager/selectAllCompetitionDetailInfoForUserGroup`,
    method: 'get',
    params: params
  })
}

// 新增用户/管理员
export function systemUserGroupAdd(data) {
  return request({
    url: '/system/userGroup',
    method: 'post',
    data: data
  })
}

// 修改用户组
export function systemUserGroupUpdate(data) {
  return request({
    url: '/system/userGroup',
    method: 'put',
    data: data
  })
}

// 用户组列表
export function systemUserGroupMangerList(params) {
  return request({
    url: '/system/userGroup/list',
    method: 'get',
    params: params
  })
}

// 删除用户组
export function systemUserGroupDelete(ids) {
  return request({
    url: '/system/userGroup/' + ids,
    method: 'delete'
  })
}

// 用户组详情
export function systemUserGroupDetail(id) {
  return request({
    url: '/system/userGroup/' + id,
    method: 'get'
  })
}

// 文件上传日志
export function fileUploadRecordList(params) {
  return request({
    url: '/system/fileUploadRecord/list',
    method: 'get',
    params: params
  })
}

// 文件上传管理
export function fileUploadManagerList(params) {
  return request({
    url: '/system/fileUploadManager/list',
    method: 'get',
    params: params
  })
}

// 导出文件
export function exportManageExportFiles(params) {
  return request({
    url: '/system/fileUploadRecord/exportFiles',
    method: 'get',
    params: params
  })
}

export function exportLogExportExecl(data) {
  return request({
    url: '/system/fileUploadRecord/export',
    method: 'post',
    data: data
  })
}

// 上传文件管理导出excel
export function exportManageExportExecl(data) {
  return request({
    url: '/system/fileUploadManager/export',
    method: 'post',
    data: data
  })
}

// 上传文件管理导出   get请求  筛选导出文件
export function exportManageExportFileFilter(params) {
  return request({
    url: '/system/fileUploadManager/exportFiles',
    method: 'get',
    params: params
  })
}

// post请求 选择导出文件   入参：id数组
export function exportManageExportFileSelect(data) {
  return request({
    url: '/system/fileUploadManager/selectExportFiles',
    method: 'post',
    data: data
  })
}

export function exportPresignedUrl(params) {
  return request({
    url: '/file/oss/presignedUrl',
    method: 'get',
    params: params
  })
}

export function getOssClientKey(params) {
  return request({
    url: '/file/oss/temporaryVoucher',
    method: 'get',
    params: params
  })
}