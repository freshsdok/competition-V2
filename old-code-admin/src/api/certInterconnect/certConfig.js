import request from "@/utils/request";

// 查询证书配置信息列表
export function getCertConfigList(params) {
  return request({
    url: "/competition/competition/certConfigInfo/list",
    method: "get",
    params,
  });
}

// 查询证书配置信息
export function getCertConfigInfo(id) {
  return request({
    url: `/competition/competition/certConfigInfo/getCertConfigInfo/${id}`,
    method: "get",
  });
}

// 添加证书配置信息
export function addCertConfigInfo(data) {
  return request({
    url: `/competition/competition/certConfigInfo/addCertConfigInfo`,
    method: "post",
    data,
  });
}

// 修改证书配置信息
export function updateCertConfigInfo(data) {
  return request({
    url: `/competition/competition/certConfigInfo/updateCertConfigInfo`,
    method: "post",
    data,
  });
}

// 删除证书配置信息
export function delCertConfig(ids) {
  return request({
    url: `/competition/competition/certConfigInfo/${ids}`,
    method: "delete",
  });
}

// 导出证书配置信息
export function exportCertConfig(data) {
  return request({
    url: `/competition/competition/certConfigInfo/export`,
    method: "post",
    data,
  });
}

// 赛事列表
export function getSelectCompetitionList(params) {
  return request({
    url: `/competition/competitionManager/selectAllCompetitionDetailInfo`,
    method: "get",
    params,
  });
}

// 证书管理员列表
export function getSelectCertManageUserList() {
  return request({
    url: `/system/user/group/list`,
    method: "get",
    params: {
      userType: "0",
    },
  });
}

// 证书颁发机构列表
export function getSelectOrganizationList() {
  return request({
    url: `/competition/competition/certOrgInfo/list`,
    method: "get",
    params: {
      userType: "0",
    },
  });
}

// 可颁发证书列表人员列表信息
export function getCompetitionApplyList(params) {
  return request({
    url: `/competition/competition/certConfigInfo/cert/getCompetitionApplyInfo`,
    method: "post",
    params: params.pagination,
    data: params.params,
  });
}

// 选定候选人列表
export function getCandidateCertList(params) {
  return request({
    url: `/competition/competition/candidateCertInfo/list`,
    method: "get",
    params,
  });
}

// 更新候选人列表
export function updateCandidateCertList(data) {
  return request({
    url: `/competition/competition/candidateCertInfo/saveCandidateCertInfo`,
    method: "post",
    data,
  });
}
// 更新候选人列表
export function batchInsertCandidateCertInfo(certConfigId,data) {
  return request({
    url: `/competition/competition/candidateCertInfo/batchInsertCandidateCertInfo/${certConfigId}`,
    method: "post",
    data,
  });
}

// 候选人导入
export function importCandidateCertInfo(formData, params) {
  return request({
    url: `/competition/competition/candidateCertInfo/importCandidateCertInfo`,
    method: "post",
    data: formData,
    params,
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });
}



// 导出选定候选人列表
export function exportCandidateCertList(query) {
  return request({
    url: '/competition/competition/candidateCertInfo/export',
    method: 'post',
    data: query
  })
}

// 拉取获奖数据
export function pullAwardData(data) {
  return request({
    url: `/competition/competition/candidateCertInfo/insertCandidateCertInfoFromAwards`,
    method: 'post',
    data
  })
}

