import request from "@/utils/request";

export function saveApplyCompetitionData(data) {
  return request({
    url: "/competition/userCompetition/saveApplyCompetitionData",
    method: "post",
    data,
  });
}

// 我的赛事列表
export function getTeamCompetitionInfo(params) {
  return request({
    url: "/competition/userCompetition/getTeamCompetitionInfo",
    method: "get",
    params,
  });
}

// 删除赛事
export function deleteCompetition(data) {
  return request({
    url: "/competition/userCompetition/deleteTeam",
    method: "post",
    data,
  });
}

// 结算
export function submitSettlement(data) {
  return request({
    url: "/competition/userCompetition/settlement",
    method: "post",
    data,
  });
}

// 确认页面
export function getConfirmOrder(data) {
  return request({
    url: "/competition/userCompetition/confirmOrder",
    method: "post",
    data,
  });
}