import request from '@/utils/request'

/** V1只请求一次性V2入口，不在浏览器传递用户ID、金额、银行卡或发票资料。 */
export function createPayoutTransitionEntry() {
  return request({
    url: '/system/v2PayoutTransition/entry',
    method: 'post',
  })
}
