import request from "@/utils/requestts";
//团队列表
export function teamList(data,params) {
  return request({
    url: "/competition/userCompetition/teamList?pageNum="+params.pageNum+"&pageSize="+params.pageSize,
    method: "post",
    data,
  });
}
//团队列表
export function getUserCompetitionApplyInfo(data) {
  return request({
    url: "/competition/userCompetition/getUserCompetitionApplyInfo",
    method: "post",
    data,
  });
}
//团队列表
export function changeCompetitionApplyInfo(data) {
  return request({
    url: "/competition/userCompetition/changeCompetitionApplyInfo",
    method: "post",
    data,
  });
}
//团队列表
export function selectCompetitionTrackConfigInfo(params) {
  return request({
    url: `/competition/userCompetition/selectCompetitionTrackConfigInfo`,
    method: "get",
    params
  });
}
//获取赛事操作配置信息
export function selectCompetitionOperationConfigInfo(competitionSeriesId) {
  return request({
    url: `/competition/userCompetition/selectCompetitionOperationConfigInfo/${competitionSeriesId}`,
    method: "get",
  });
}
// 获取发起流程id
export function workflowstartProcess(params) {
  return request({
    url: `/workflow/process/startProcess`,
    method: "get",
    params
  });
}
// 校验是否有变更权限
export function checkChangeOperator(params) {
  return request({
    url: `/competition/userCompetition/checkChangeOperator`,
    method: "get",
    params
  });
}

//校验修改信息
export function checkChangeCompetitionApplyInfo(data) {
  return request({
    url: "/competition/userCompetition/checkChangeCompetitionApplyInfo",
    method: "post",
    data,
  });
}

//退费
export function createPayOrderByTeamChange(data) {
  return request({
    url: "/system/order/createPayOrderByTeamChange",
    method: "post",
    data,
  });
}

// 更新报名顺序
export function updateApplyInfoSequence(params) {
  return request({
    url: "/competition/userCompetition/updateApplyInfoSequence",
    method: "get",
    params: params,
  });
}
