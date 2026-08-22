import request from '@/utils/request'

// 查询报名管理列表
export function competitionApplyList(query,data) {
  return request({
    url: '/competition/competitionApply/list',
    method: 'post',
    data: data,
    params: query
  })
}


// 查询团队管理列表
export function teamManagerList(query,data) {
  return request({
    url: '/competition/teamManager/list',
    method: 'post',
    data: data,
    params: query
  })
}

// 更新团队管理信息
export function updateTeamManagerInfo(data) {
  return request({
    url: '/competition/teamManager/updateTeamManagerInfo',
    method: 'post',
    data: data
  })
}

// 查权限配置接口
export function getCompetitionLogList(params) {
  return request({
    url: `/competition/log//list`,
    method: "get",
    params: params,
  });
}

// 查权限配置接口
export function getSysSenderMessageLogList(params) {
  return request({
    url: `/system/sysSenderMessageLog/list`,
    method: "get",
    params: params,
  });
}

// 导出报名管理列表
export function exportCompetitionApplyList(data) {
  return request({
    url: '/competition/competitionApply/export',
    method: 'post',
    data: data
  })
}
