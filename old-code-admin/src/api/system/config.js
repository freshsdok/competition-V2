import request from '@/utils/request'

// 查询参数列表
export function listConfig(query) {
  return request({
    url: '/system/config/list',
    method: 'get',
    params: query
  })
}

// 查询参数详细
export function getConfig(configId) {
  return request({
    url: '/system/config/' + configId,
    method: 'get'
  })
}

// 根据参数键名查询参数值
export function getConfigKey(configKey) {
  return request({
    url: '/system/config/configKey/' + configKey,
    method: 'get'
  })
}

// 新增参数配置
export function addConfig(data) {
  return request({
    url: '/system/config',
    method: 'post',
    data: data
  })
}

// 修改参数配置
export function updateConfig(data) {
  return request({
    url: '/system/config',
    method: 'put',
    data: data
  })
}

// 删除参数配置
export function delConfig(configId) {
  return request({
    url: '/system/config/' + configId,
    method: 'delete'
  })
}

// 刷新参数缓存
export function refreshCache() {
  return request({
    url: '/system/config/refreshCache',
    method: 'delete'
  })
}


// 评分列表
export function competitionWorkslist(params) {
  return request({
    url: '/competition/competitionWorks/list',
    method: 'get',
    params: params
  })
}
//  作品详情
export function competitionWorksworksId(worksId) {
  return request({
    url:`/competition/competitionWorks/${worksId} `,
    method: 'get'
  })
}


// 打分
export function updateCompetitionWorks(data) {
  return request({
    url: '/competition/competitionWorks/updateCompetitionWorks',
    method: 'post',
    data: data
  })
}
// 返回提取码
export function saveCompetitionWorkLinkInfo(data) {
  return request({
    url: '/competition/competitionWorkLink/saveCompetitionWorkLinkInfo',
    method: 'post',
    data: data
  })
}
// 通过提取码获取列表
export function getSpecialistList(params) {
  return request({
    url: '/competition/competitionWorks/getSpecialistList',
    method: 'get',
    params:params
  })
}

// 打分
export function updateCompetitionWorksScore(data) {
  return request({
    url: '/competition/competitionWorks/updateCompetitionWorksScore',
    method: 'post',
    data
  })
}

//  作品详情
export function getLinkCompetitionWorksInfo(worksId) {
  return request({
    url:`/competition/competitionWorks/getLinkCompetitionWorksInfo/${worksId} `,
    method: 'get'
  })
}


//  链接打分列表
export function competitionWorkLinklist(params) {
  return request({
    url:`/competition/competitionWorkLink/list`,
    method: 'get',
    params:params
  })
}

// 链接打分时间修改
export function updateCompetitionWorkLinkInfo(data) {
  return request({
    url: '/competition/competitionWorkLink/updateCompetitionWorkLinkInfo',
    method: 'post',
    data
  })
}
