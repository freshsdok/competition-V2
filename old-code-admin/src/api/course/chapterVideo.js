import request from '@/utils/request'

// 查询章节信息列表（通过课程信息接口查询，然后获取章节列表）
export function listChapterInfo(query) {
  return request({
    url: '/course/courseInfo/list',
    method: 'get',
    params: query
  })
}

// 查询章节信息详细（通过课程ID查询，返回课程及其章节信息）
export function getChapterInfo(courseId) {
  return request({
    url: '/course/courseInfo/' + courseId,
    method: 'get'
  })
}

// 新增章节信息（通过课程信息接口，需要包含章节列表）
export function addChapterInfo(data) {
  return request({
    url: '/course/courseInfo',
    method: 'post',
    data: data
  })
}

// 修改章节信息
export function updateChapterInfo(data) {
  return request({
    url: '/course/courseInfo',
    method: 'put',
    data: data
  })
}

// 删除章节信息（逻辑删除，通过更新课程信息）
export function delChapterInfo(chapterId) {
  return request({
    url: '/course/chapterVideo/' + chapterId,
    method: 'delete'
  })
}

// 根据章节ID查询视频列表
export function getVideoListByChapterId(chapterId) {
  return request({
    url: '/course/chapterVideo/getInfoByChapterId/' + chapterId,
    method: 'get'
  })
}

// 新增章节视频信息
export function addChapterVideo(data) {
  return request({
    url: '/course/chapterVideo',
    method: 'post',
    data: data
  })
}

// 修改章节视频信息
export function updateChapterVideo(data) {
  return request({
    url: '/course/chapterVideo',
    method: 'put',
    data: data
  })
}

// 删除章节视频信息
// videoIds: 视频ID数组，可以是单个ID或数组，多个ID用逗号分隔
// chapterId: 章节ID
export function delChapterVideo(videoIds, chapterId) {
  // 如果videoIds是数组，转换为逗号分隔的字符串
  const videoIdsStr = Array.isArray(videoIds) ? videoIds.join(',') : videoIds
  return request({
    url: '/course/chapterVideo/' + videoIdsStr + '/' + chapterId,
    method: 'delete'
  })
}

// 导出章节视频信息
export function exportChapterVideo(query) {
  return request({
    url: '/course/chapterVideo/export',
    method: 'post',
    params: query
  })
}

// 上传视频文件
export function uploadVideo(data) {
  return request({
    url: '/file/uploadVideo',
    method: 'post',
    data: data
  })
}
