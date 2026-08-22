import request from '@/utils/request'

// 查询课程信息列表
export function listCourseInfo(query) {
  return request({
    url: '/course/courseInfo/list',
    method: 'get',
    params: query
  })
}

// 查询课程信息详细
export function getCourseInfo(courseId) {
  return request({
    url: '/course/courseInfo/' + courseId,
    method: 'get'
  })
}

// 新增课程信息
export function addCourseInfo(data) {
  return request({
    url: '/course/courseInfo',
    method: 'post',
    data: data
  })
}

// 修改课程信息
export function updateCourseInfo(data) {
  return request({
    url: '/course/courseInfo',
    method: 'put',
    data: data
  })
}

// 删除课程信息
export function delCourseInfo(courseId) {
  return request({
    url: '/course/courseInfo/' + courseId,
    method: 'delete'
  })
}

// 导出课程信息
export function exportCourseInfo(query) {
  return request({
    url: '/course/courseInfo/export',
    method: 'post',
    params: query
  })
}

// 修改课程审核状态
export function updateCourseStatus(data) {
  return request({
    url: '/course/courseInfo/updateStatus',
    method: 'put',
    data: {
      pageId: data.pageId || data.businessId,
      checkStatus: data.checkStatus
    }
  })
}

