import request from "@/utils/request";

// 查询赛事列表
export function listCompetition(query) {
  return request({
    url: "/competition/competitionManager/list",
    method: "get",
    params: query,
  });
}

// 查询赛事详细
export function getCompetition(data) {
  return request({
    url: "/competition/competitionManager/getCompetitionDetailInfoById",
    method: "get",
    params: data,
  });
}
//  获取当前赛事处于阶段
export function queryNowCompetitionStageConfig(data) {
  return request({
    url: "/competition/competitionManager/queryNowCompetitionStageConfig",
    method: "get",
    params: data,
  });
}

// 新增赛事
export function saveCompetitionInfo(data) {
  return request({
    url: "/competition/competitionManager/saveCompetitionInfo",
    method: "post",
    data: data,
  });
}

// 修改赛事
export function updateCompetitionInfo(data) {
  return request({
    url: "/competition/competitionManager/updateCompetitionInfo",
    method: "post",
    data: data,
  });
}

// 删除赛事
export function removeCompetitionMainInfo(data) {
  return request({
    url: "/competition/competitionManager/removeCompetitionMainInfo",
    method: "post",
    data: data,
  });
}
// 修改赛事状态
export function updateCompetitionInfoStatus(data) {
  return request({
    url: "/competition/competitionManager/updateCompetitionInfoStatus",
    method: "post",
    data: data,
  });
}

//修改赛事晋级分数 晋级人数接口
export function saveUserGradeCompetitionStageConfig(data) {
  return request({
    url: "/competition/userGradeInfo/saveUserGradeCompetitionStageConfig",
    method: "post",
    data: data,
  });
}

// 查询赛事列表
export function createAdvanceUserGradeInfo(params) {
  return request({
    url: `/competition/userGradeInfo/createAdvanceUserGradeInfo`,
    method: "get",
    params: params,
  });
}

//保存晋级最终成绩 
export function saveAdvanceUserGradeInfo(data) {
  return request({
    url: "/competition/userGradeInfo/saveAdvanceUserGradeInfo",
    method: "post",
    data: data,
  });
}

// 查询赛事赛道配置列表
export function listCompetitionTracks(params) {
  return request({
    url: "/competition/competitionTrackInfo/list",
    method: "get",
    params: params,
  });
}

// 查询赛事赛道配置详情
export function getCompetitionTrackConfig(id) {
  return request({
    url: `/competition/competitionTrackConfig/getCompetitionTrackConfigInfo/${id}`,
    method: "get",
  });
}

// 保存赛道配置
export function saveCompetitionTrack(data) {
  return request({
    url: "/competition/competitionTrackInfo/saveCompetitionTrackInfo",
    method: "post",
    data: data,
  });
}

// 保存赛道配置规则
export function saveCompetitionTrackRule(data) {
  return request({
    url: "/competition/competitionTrackConfig/saveCompetitionTrackRuleConfig",
    method: "post",
    data: data,
  });
}

// 删除赛道配置
export function deleteCompetitionTrack(id) {
  return request({
    url: `/competition/competitionTrackConfig/removeCompetitionTrackConfig/${id}`,
    method: "get",
  });
}

// 删除赛道配置列表
export function deleteCompetitionListById(id) {
  return request({
    url: `/competition/competitionTrackInfo/removeCompetitionTrack/${id}`,
    method: "get",
  });
}

// 查赛事列表下拉
export function getCompetitionPullDownList(params) {
  return request({
    url: "/competition/competitionManager/pullDownList",
    method: "get",
    params: params,
  });
}

// 查赛事列表下拉
export function getCheckPackage(params) {
  return request({
    url: "/competition/checkPackage/getList",
    method: "get",
    params: params,
  });
}

// 查权限配置接口
export function getCompetitionOperationConfig(competitionSeriesId,params) {
  return request({
    url: `/competition/competitionOperationConfig/operationConfigList/${competitionSeriesId}`,
    method: "get",
    params: params,
  });
}
// 新增权限配置接口
export function saveCompetitionOperationConfig(data) {
  return request({
    url: "/competition/competitionOperationConfig/saveCompetitionOperationConfig",
    method: "post",
    data: data,
  });
}
// 修改权限配置接口
export function updateCompetitionOperationConfig(data) {
  return request({
    url: "/competition/competitionOperationConfig/updateCompetitionOperationConfig",
    method: "post",
    data: data,
  });
}

// 签到信息分页查询
export function getSignInInfoList(params) {
  return request({
    url: `/wxApp/wxSignInInfo/list`,
    method: "get",
    params: params,
  });
}

// 签到信息导出列表
export function exportSignInInfoList(params) {
  return request({
    url: `/wxApp/wxSignInInfo/export`,
    method: "post",
    data: params,
  });
}