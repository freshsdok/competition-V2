import request from '@/utils/request'

// 查询课程分类列表
export function listClassify(query) {
  return request({
    url: '/course/classify/list',
    method: 'get',
    params: query
  })
}
// 查询课程分类分级下拉框
export function listClassifyGetList(query) {
  return request({
    url: '/course/classify/getList',
    method: 'get',
    params: query
  })
}


// 查询课程分类详细
export function getClassify(classifyId) {
  return request({
    url: '/course/classify/' + classifyId,
    method: 'get'
  })
}

// 新增课程分类
export function addClassify(data) {
  return request({
    url: '/course/classify',
    method: 'post',
    data: data
  })
}

// 修改课程分类
export function updateClassify(data) {
  return request({
    url: '/course/classify',
    method: 'put',
    data: data
  })
}

// 删除课程分类
export function delClassify(classifyId) {
  return request({
    url: '/course/classify/' + classifyId,
    method: 'delete'
  })
}