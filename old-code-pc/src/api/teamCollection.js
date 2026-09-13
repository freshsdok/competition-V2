import request from '@/utils/request'

const base = '/system/teamCollection'
const command = (id, action, version) => request({
  url: `${base}/${encodeURIComponent(String(id))}/${action}`,
  method: 'post', data: { version }, headers: { repeatSubmit: false },
})
export const getMyTeamCollections = () => request({ url: `${base}/mine`, method: 'get' })
export const startTeamCollection = (id, version) => command(id, 'start', version)
export const confirmTeamCollection = (id, version) => command(id, 'confirm', version)
export const releaseTeamCollection = (id, version) => command(id, 'release', version)
