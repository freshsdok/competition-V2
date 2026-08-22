import request from '@/utils/request'

// ==================== 签到二维码管理 ====================

// 查询签到二维码列表
export function getSignInQrCodeList(query) {
  return request({
    url: '/wxApp/wxQcCodeConfig/list',
    method: 'get',
    params: query
  })
}

// 新增签到二维码
export function addSignInQrCode(data) {
  return request({
    url: '/wxApp/wxQcCodeConfig',
    method: 'post',
    data
  })
}

// 修改签到二维码
export function updateSignInQrCode(data) {
  return request({
    url: '/system/signInQrCode/edit',
    method: 'post',
    data
  })
}

// 删除签到二维码
export function deleteSignInQrCode(codeConfigIds) {
  return request({
    url: `/wxApp/wxQcCodeConfig/${codeConfigIds}`,
    method: 'delete'
  })
}

// 查询签到二维码详情
export function getSignInQrCodeDetail(id) {
  return request({
    url: `/system/signInQrCode/getInfo/${id}`,
    method: 'get'
  })
}

// ==================== 用户组设置 ====================
// 注：查询用户组列表接口复用 @/api/fileTask 中的 systemUserGroupMangerList

// 保存用户组设置
export function saveUserGroupSetting(data) {
  return request({
    url: '/wxApp/wxQcCodeConfig',
    method: 'put',
    data
  })
}

// ==================== 提示语配置 ====================

// 查询提示语配置
export function getPromptConfig(id) {
  return request({
    url: `/system/signInQrCode/promptConfig/${id}`,
    method: 'get'
  })
}

// ==================== 二维码管理 ====================

// 查询二维码列表
export function getQrCodeList(query) {
  return request({
    url: '/wxApp/wxQcCodeRecord/list',
    method: 'get',
    params: query
  })
}

// 获取二维码base64
export function getCodeBase64(recordId) {
  return request({
    url: `/wxApp/wxQcCodeRecord/codeBase/${recordId}`,
    method: 'get'
  })
}