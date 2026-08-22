import request from '@/utils/request'
// ==================== 晋级管理 ====================

// 查询晋级列表
export function getPromotionList(query) {
  return request({
    url: '/competition/promotedInfo/list',
    method: 'get',
    params: query
  })
}

// 获取晋级详情
export function getPromotionDetail(promotedId) {
  return request({
    url: `/competition/promotedInfo/getDetailInfo/${promotedId}`,
    method: 'get'
  })
}

// 导入晋级名单（新建/重新导入）
export function importPromotion(data) {
  return request({
    url: '/competition/promotedApplyInfo/import',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 修改赛事晋级信息（费用、报名时间、提示语等）
export function editCompetitionPromotedInfo(data) {
  return request({
    url: '/competition/promotedInfo/editCompetitionPromotedInfo',
    method: 'post',
    data
  })
}

// 查询晋级名单列表
export function getPromotionDetailsList(params) {
  return request({
    url: '/competition/promotedApplyInfo/list',
    method: 'get',
    params: params
  })
}

// 删除晋级公示
export function deletePromotion(promotedId) {
  return request({
    url: `/competition/promotedInfo/remove/${promotedId}`,
    method: 'get'
  })
}

// 编辑晋级名单明细
export function editPromotionDetails(data) {
  return request({
    url: '/competition/promotedApplyInfo',
    method: 'put',
    data
  })
}

// 导出晋级名单明细
export function exportPromotionDetails(data) {
  return request({
    url: '/competition/promotedApplyInfo/export',
    method: 'post',
    data
  })
}

// 删除晋级名单明细
export function removePromotionDetail(competitionSeriesId, teamCodes) {
  return request({
    url: `/competition/promotedApplyInfo/${competitionSeriesId}/${teamCodes}`,
    method: 'delete'
  })
}
