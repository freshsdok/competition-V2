import request from '@/utils/request'
import config from '@/config'

export function listPersonalInvoices(data = {}, params = {}) {
  return request({ url: '/system/invoice/personalList', method: 'post', data, params })
}

export function listInvoiceCandidates(data = {}) {
  return request({ url: '/system/invoice/queryTeamAndUserByOrderId', method: 'post', data })
}

export function getInvoiceAmount(data) {
  return request({ url: '/system/invoice/queryInvoiceAmount', method: 'post', data })
}

export function listInvoiceTitles(params = {}) {
  return request({ url: '/system/invoicePerInfo/selectInvoicePerInfo', method: 'get', params })
}

export function applyInvoice(data) {
  return request({ url: '/system/invoice/applyNew', method: 'post', data })
}

export function refreshInvoiceResult(data) {
  return request({ url: '/system/invoice/queryInvoiceResult', method: 'post', data })
}

export function getInvoicePdfUrl(id) {
  return `${config.baseUrl}/system/invoice/personal/pdf/${id}`
}
