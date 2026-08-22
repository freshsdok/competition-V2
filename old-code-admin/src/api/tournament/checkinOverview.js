import request from '@/utils/request'

export function getCheckinOverviewStatistics(query) {
  return request({
    url: '/competition/scene/checkin-overview/statistics',
    method: 'get',
    params: query
  })
}

export function listCheckinOverviewSchedules(query) {
  return request({
    url: '/competition/scene/checkin-overview/schedules',
    method: 'get',
    params: query
  })
}

export function getCheckinOverviewSchedule(scheduleId) {
  return request({
    url: `/competition/scene/checkin-overview/schedules/${scheduleId}`,
    method: 'get'
  })
}

export function listCheckinOverviewPersons(scheduleId, query) {
  return request({
    url: `/competition/scene/checkin-overview/schedules/${scheduleId}/persons`,
    method: 'get',
    params: query
  })
}
