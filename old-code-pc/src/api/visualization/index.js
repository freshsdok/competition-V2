import request from '@/utils/request'

// 查询页面数据
export function contentPagePC(params) {
  return request({
    url: `/content/page/pc`,
    method: 'get',
    params: params
  })
}
// 查询页面数据
export function contentPagePcById(id) {
  return request({
    url: `/content/page/pc/${id}`,
    method: 'get'
  })
}

// 查询用户赛事详情
export function getUserCompetitionDetailInfoById(params) {
  return request({
    url: `/competition/userCompetition/pc/getUserCompetitionDetailInfoById`,
    method: 'get',
    params: params
  })
}

// 分享赛事
export function shareCompetition(params) {
  return request({
    url: `/competition/userCompetition/shareCompetition`,
    method: 'get',
    params: params
  })
}

// 收藏
export function addCollect(data) {
  return request({
    url: `/competition/collect/saveUserCollect`,
    method: 'post',
    data: data
  })
}

// 删除收藏
export function removeCollect(data) {
  return request({
    url: `/competition/collect/removeUserCollect`,
    method: 'post',
    data: data
  })
}

// 校验是否收藏
export function checkCollect(data) {
  return request({
    url: `/competition/collect/checkCollect`,
    method: 'get',
    params: data
  })
}

//获取团队成员  
export function getTeamMemberList(data) {
  return request({
    url: `/competition/userCompetition/getTeamMemberList`,
    method: 'get',
    params: data
  })
}

// 查询教师列表
export function teacherList(params) {
  return request({
    url: `/system/personalCenter/teacherList`,
    method: 'get',
    params: params
  })
}

// 保存团队负责人信息
export function saveTeamManagerInfo(data) {
  return request({
    url: `/competition/userCompetition/saveTeamManagerInfo`,
    method: 'post',
    data: data
  })
}

// 修改团队负责人信息
export function updateTeamManagerInfo(data) {
  return request({
    url: `/competition/userCompetition/updateTeamManagerInfo`,
    method: 'post',
    data: data
  })
}

// 查询团队列表（个人也可以）
export function userTeamList(data) {
  return request({
    url: `/competition/userCompetition/teamList`,
    method: 'post',
    data: data
  })
}

// 查询赛事团队列表-所有
export function getAllTeamList(params) {
  return request({
    url: `/competition/userCompetition/getCompetitionTeamList`,
    method: 'get',
    params: params
  })
}

// 可视化页面
export function visualizationPageMobile(apiurl,method = 'get',params) {
  let sendApiurl = getUrl(apiurl,params)
  return request({
    url: sendApiurl,
    method: method
  })
}

export function getUrl(apiurl,params) {
  if (!params) return apiurl;
  const hasQuery = apiurl.includes('?');
  const separator = hasQuery ? '&' : '?';
  
  const queryString = Object.keys(params)
    .map(key => {
      if (params[key] === undefined || params[key] === null) {
        return '';
      }
      return `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`;
    })
    .filter(Boolean)
    .join('&');
  
  return queryString ? `${apiurl}${separator}${queryString}` : apiurl;
}


// 申请加入团队
export function applyJoinTeam(data) {
  return request({
    url: `/competition/userCompetition/applyJoinTeam`,
    method: 'post',
    data: data
  })
}

// 加入团队
export function userJoinTeaList(params) {
  return request({
    url: `/competition/userCompetition/userJoinTeam`,
    method: 'get',
    params: params
  })
}


// 同意加入团队
export function agreeJoinTeam(data) {
  return request({
    url: `/competition/userCompetition/agreeJoinTeam`,
    method: 'post',
    data: data
  })
}


// 报名提交申请
export function userSaveApplyCompetitionInfo(data) {
  return request({
    url: `/competition/userCompetition/userSaveApplyCompetitionInfo`,
    method: 'post',
    data: data
  })
}

// 查询用户赛事报名状态
export function checkCompetitionApplyStatusByUser(data) {
  return request({
    url: `/competition/userCompetition/checkCompetitionApplyStatusByUser`,
    method: 'post',
    data: data
  })
}


// 作品上传文件接口
export function uploadCompetitionWorks(competitionId,params) {
  return request({
    url: `/competition/competitionWorks/uploadCompetitionWorks/${competitionId}`,
    method: 'get',
    params: params
  })
}

// 保存作品
export function saveCompetitionWorks(data) {
  return request({
    url: `/competition/competitionWorks/saveCompetitionWorks`,
    method: 'post',
    data: data
  })
}

// 更新作品
export function updateCompetitionWorks(data) {
  return request({
    url: `/competition/competitionWorks/updateCompetitionWorks`,
    method: 'post',
    data: data
  })
}

// 咨询详情
export function getNewsInfo(newsId) {
  return request({
    url: `/content/newsInfo/public/${newsId}`,
    method: 'get'
  })
}

// 通知公告详情
export function getNoticeInfo(noticeId) {
  return request({
    url: `/content/noticeInfo/public/${noticeId}`,
    method: 'get'
  })
}

// 查询赛事赛道列表
export function getCompetitionTrackList(params) {
  return request({
    url: `/competition/competitionTrackInfo/list`,
    method: 'get',
    params: params
  })
}

export function getPublicCompetitionTrackList(competitionSeriesId) {
  return request({
    url: `/competition/userCompetition/selectCompetitionTrackInfoByCompetitionSeriesId/${competitionSeriesId}`,
    method: 'get'
  })
}
