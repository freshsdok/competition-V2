import request from '@/utils/request'



// 查询已评分列表
export function competitionWorkslist(params) {
  return request({
    url: `/competition/competitionWorks/list`,
    method: 'get',
    params: params
  })
}

// 查询奖项设置
export function competitionAwardsConfiglist(params) {
  return request({
    url: `/competition/competitionAwardsConfig/list`,
    method: 'get',
    params: params
  })
}
// 新增奖项设置
export function addCompetitionAwardsConfig(data) {
  return request({
    url: `/competition/competitionAwardsConfig/addCompetitionAwardsConfig`,
    method: 'post',
    data
  })
}
// 修改奖项设置
export function updateCompetitionAwardsConfig(data) {
  return request({
    url: `/competition/competitionAwardsConfig/updateCompetitionAwardsConfig`,
    method: 'post',
    data
  })
}