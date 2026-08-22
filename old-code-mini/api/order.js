import request from '@/utils/request'

export function listPersonalOrders(params) {
  return request({ url: '/system/order/personalList', method: 'get', params })
}

export function getPersonalOrderStatusCount(params = {}) {
  return request({ url: '/system/order/perStatusCount', method: 'get', params })
}

export function getPersonalOrder(id) {
  return request({ url: `/system/order/personal/${id}`, method: 'get' })
}

export function cancelPersonalOrder(id) {
  return request({ url: `/system/order/cancelOrder/${id}`, method: 'get' })
}

export function cancelPersonalRepaymentOrder(id) {
  return request({ url: `/system/order/cancelRepaymentOrder/${id}`, method: 'get' })
}
