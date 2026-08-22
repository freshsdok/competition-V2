import request from '@/utils/request'

export function listMySceneNotice() {
  return request({
    url: '/competition/sceneNotice/myList',
    method: 'get'
  })
}

