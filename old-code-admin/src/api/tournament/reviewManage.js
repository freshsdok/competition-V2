import request from '@/utils/request'

// ==================== 评审任务相关 ====================

// 查询评审任务列表
export function getReviewTaskList(query) {
  return request({
    url: '/system/reviewTaskInfo/list',
    method: 'get',
    params: query
  })
}


// 查询省份下拉列表
export function getProvinceOptions() {
  return request({
    url: '/system/school/selectAllProvince',
    method: 'get'
  })
}

// 查询任务分组下拉列表
export function getTaskGroupOptions() {
  return request({
    url: '/system/reviewTaskAllotGroup/list',
    method: 'get'
  })
}

// 查询分组详情
export function getGroupDetail(reviewGroupId) {
  return request({
    url: `/system/reviewTaskAllotGroup/getReviewTaskAllotGroup/${reviewGroupId}`,
    method: 'get'
  })
}

// 移除分组关系（单个/批量）
export function removeGroupRelation(data) {
  return request({
    url: '/system/reviewTaskAllotGroup/remove/groupRelation',
    method: 'post',
    data
  })
}

// 批量设置截止时间
export function batchSetDeadline(data) {
  return request({
    url: '/system/reviewTaskInfo/saveReviewTaskInfo',
    method: 'post',
    data
  })
}

// 批量设置评审备注（使用通用保存接口）
export function batchSetRemark(data) {
  return request({
    url: '/system/reviewTaskInfo/saveReviewTaskInfo',
    method: 'post',
    data
  })
}

// 批量设置参考文档
export function batchSetDoc(data) {
  return request({
    url: '/system/reviewTaskInfo/saveReviewTaskInfo',
    method: 'post',
    data
  })
}

// 批量移除专家
export function batchRemoveExpert(data) {
  return Promise.resolve({ code: 200, msg: '移除成功' })
}

// 确认分配
export function confirmAssignApi(data) {
  return request({
    url: '/system/reviewTaskInfo/saveSpecialistReviewTaskInfo',
    method: 'post',
    data
  })
}

// 创建任务分组
export function createTaskGroup(data) {
  return request({
    url: '/system/reviewTaskAllotGroup/addReviewTaskAllotGroup',
    method: 'post',
    data
  })
}

// ==================== 评审专家相关 ====================

// 查询评审专家列表
export function getExpertList(data = {}) {
  return request({
    url: '/system/reviewSpecialistGroupInfo/getSpecialistInfo',
    method: 'post',
    data
  })
}

// 查询专家分组列表
export function getExpertGroupList(params) {
  return request({
    url: '/system/reviewSpecialistGroupInfo/list',
    method: 'get',
    params: params
  })
}

// 创建专家分组
export function createExpertGroup(data) {
  return request({
    url: '/system/reviewSpecialistGroupInfo/addReviewSpecialistGroupInfo',
    method: 'post',
    data
  })
}

// 删除专家分组
export function deleteExpertGroup(groupIds) {
  return request({
    url: `/system/reviewSpecialistGroupInfo/remove/${groupIds}`,
    method: 'get'
  })
}

// 更新专家分组
export function updateExpertGroup(data) {
  return request({
    url: '/system/reviewSpecialistGroupInfo/editReviewSpecialistGroupInfo',
    method: 'post',
    data
  })
}

// 批量分配专家组
export function batchAssignExpertGroup(data) {
  return Promise.resolve({ code: 200, msg: '分配成功' })
}

// 从任务中移除专家
export function removeSpecialistFromTask(data) {
  return request({
    url: '/system/reviewSpecialistGroupInfo/remove/specialist',
    method: 'post',
    data
  })
}

// 查询我的评审任务列表
export function getMyReviewTaskList(query) {
  return request({
    url: '/system/reviewTaskInfo/getExpertList',
    method: 'get',
    params: query
  })
}

// 查询评审任务详情
export function getReviewTaskDetail(params) {
  return request({
    url: '/system/reviewTaskInfo/getTaskInfoByProcessedId',
    method: 'get',
    params
  })
}

// 提交评审
export function submitReview(data) {
  return request({
    url: '/system/processRelation',
    method: 'put',
    data: data
  })
}

// ==================== 审阅备注相关 ====================

// 查询审阅备注列表
export function getNotesList(params) {
  return request({
    url: '/system/notes/getList',
    method: 'get',
    params
  })
}

// 新增审阅备注
export function addNotes(data) {
  return request({
    url: '/system/notes',
    method: 'post',
    data
  })
}

// 删除审阅备注
export function deleteNotes(fileId, ids) {
  return request({
    url: `/system/notes/${fileId}/${ids}`,
    method: 'delete'
  })
}


export function getPreviewUrl(params) {
  return request({
    url: '/file/oss/previewUrl',
    method: 'get',
    params
  })
}

// 保存阅读页码
export function saveLastPage(data) {
  return request({
    url: '/system/processRelation/lastPage',
    method: 'post',
    data
  })
}
