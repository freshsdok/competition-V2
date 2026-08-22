import request from '@/utils/request'

/* 新增审核任务 business 发起审核
{
  "businessId": , //业务id
  "auditType": "" //审核类型
}
*/
export function systemTask(data) {
  return request({
    url: '/system/task',
    method: 'post',
    data: data
  })
}
