import request from "@/utils/request";

// 赛证互通列表
export function getCertExchangeRuleList(params) {
  return request({
    url: "/competition/competition/competitionCertExchangeRule/list",
    method: "get",
    params,
  });
}

// 赛证互通信息
export function getCertExchangeRuleInfo(ruleId) {
  return request({
    url: `/competition/competition/competitionCertExchangeRule/${ruleId}`,
    method: "get",
  });
}

// 新增赛证互通规则
export function addCertExchangeRule(data) {
  return request({
    url: "/competition/competition/competitionCertExchangeRule/saveCompetitionCertExchangeRule",
    method: "post",
    data,
  });
}

// 修改赛证互通规则
export function updateCertExchangeRule(data) {
  return request({
    url: "/competition/competition/competitionCertExchangeRule/updateCompetitionCertExchangeRule",
    method: "post",
    data,
  });
}

// 修改赛证互通规则列表信息
export function updateCertExchangeRuleInfo(data) {
  return request({
    url: `/competition/competition/competitionCertExchangeRule/updateCompetitionCertExchangeRuleMain`,
    method: "post",
    data
  });
}

// 删除赛证互通规则
export function deleteCertExchangeRule(ruleId) {
  return request({
    url: `/competition/competition/competitionCertExchangeRule/remove/${ruleId}`,
    method: "get",
  });
}


