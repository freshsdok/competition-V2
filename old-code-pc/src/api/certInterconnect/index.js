import request from "@/utils/request";

// 获取证书互通规则列表
export function getCertInterconnectRuleList(params) {
  return request({
    url: "/competition/competition/competitionCertExchangeRule/getList",
    method: "get",
    params
  });
}

// 获取证书互通规则详情
export function getCertInterconnectRule(ruleId) {
  return request({
    url: `/competition/user/competitionCertExchangeRule/queryUserCertExchangeApplyDetail/${ruleId}`,
    method: "get",
  });
}

// 计算金额 
export function getCertInterconnectAmount(data) {
  return request({
    url: `/competition/user/competitionCertExchangeRule/queryUserCertExchangeApplyDetail`,
    method: "post",
    data
  });
}

// 下单 
export function orderCertInterconnect(data) {
  return request({
    url: `/competition/user/competitionCertExchangeRule/saveUserCertExchangeApply`,
    method: "post",
    data
  });
}

// 下单 检查是否可以支付
export function saveUserCertExchangeApplyCheck(data) {
  return request({
    url: `/competition/user/competitionCertExchangeRule/saveUserCertExchangeApplyCheck`,
    method: "post",
    data
  });
}

// 获取证书互通规则详情
export function getCertInterconnectRuleNoAuth(ruleId) {
  return request({
    url: `/competition/user/competitionCertExchangeRule/queryUserCertExchangeApplyDetailNoAuth/${ruleId}`,
    method: "get",
  });
}

export function getCertInterconnectApplyDetailNoAuthList(data) {
  return request({
    url: `/competition/user/competitionCertExchangeRule/list`,
    method: "post",
    data
  });
}

// 获取大赛证书列表
export function getCompetitionCertificateList(data) {
  return request({
    url: `/competition/user/competitionCertExchangeRule/getCompetitionCertificateList`,
    method: "post",
    data
  });
}

// 获取当前团队报名负责人可打包的学生证书统计
export function getGuidedCertificateSummary() {
  return request({
    url: `/competition/user/competitionCertExchangeRule/certificate/guidedSummary`,
    method: "get",
  });
}

// 获取当前团队报名负责人名下学生证书的图片地址
export function getGuidedCertificatePictures() {
  return request({
    url: `/competition/user/competitionCertExchangeRule/certificate/guidedPackage`,
    method: "post",
  });
}
