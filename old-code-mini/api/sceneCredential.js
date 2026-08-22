import request from '@/utils/request'

export function listMySceneCredential() {
  return request({
    url: '/competition/userCompetition/sceneCredential/myList',
    method: 'get'
  })
}
