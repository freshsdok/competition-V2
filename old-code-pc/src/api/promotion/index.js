import request from "@/utils/request";

// 获取晋级公示Tab列表
export function getPromotedInfoPcList() {
  return request({
    url: "/competition/promotedInfo/pcList",
    method: "get",
  });
}

// 获取获奖公示Tab列表
export function getAwardPublicityTabs() {
  return request({
    url: "/competition/awardDetailsUser/awardPublicityList",
    method: "get",
  });
}

// 获取晋级申请信息列表
export function getPromotedApplyInfoPcList(params) {
  return request({
    url: "/competition/promotedApplyInfo/pcList",
    method: "get",
    params,
  });
}

// 根据Tab id查询获奖公示明细列表
export function getAwardDetailsList(params) {
  return request({
    url: "/competition/awardDetailsUser/awardDetailsList",
    method: "get",
    params,
  });
}

// 编辑保存晋级申请信息
export function updatePromotedApplyInfoPcEdit(data) {
  return request({
    url: "/competition/promotedApplyInfo/pcEdit",
    method: "put",
    data,
  });
}

// 晋级报名
export function promotedApplyInfoPcApply(data) {
  return request({
    url: "/competition/promotedApplyInfo/pcApply",
    method: "post",
    data,
  });
}

// 编辑保存获奖公示明细列表
export function updateAwardDetailsList(data) {
  return request({
    url: "/competition/awardDetailsUser/updateAwardDetailsList",
    method: "post",
    data,
  });
}
