import request from '@/utils/request'

export function listPersonalTeams(data = {}, params = {}) {
  return request({ url: '/competition/userCompetition/teamList', method: 'post', data, params })
}

export function getPersonalTeam(data) {
  return request({ url: '/competition/userCompetition/getUserCompetitionApplyInfo', method: 'post', data })
}
