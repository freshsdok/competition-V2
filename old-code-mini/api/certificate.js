import request from '@/utils/request'

export function listPersonalCertificates(data = {}) {
  return request({
    url: '/competition/user/competitionCertExchangeRule/list',
    method: 'post',
    data
  })
}
