import request from "@/utils/request";

export function orderlist(params) {
  return request({
    url: "/system/order/list",
    method: "get",
    params,
  });
}
export function orderchak(id) {
  return request({
    url: `/system/order/${id}`,
    method: "get"
  });
}

export function cancelOrder(id) {
  return request({
    url: `/system/order/cancelOrder/${id}`,
    method: "get"
  });
}
// 转账证明审核
export function proofAudit(params) {
  return request({
    url: `/system/order/proofAudit`,
    method: "get",
    params
  });
}
// 发票列表
export function invoicelist(data,pam,psise) {
  return request({
    url: `/system/invoice/list?pageNum=${pam}&pageSize=${psise}`,
    method: "post",
    data
  });
}
// 发票申请
export function invoiceapply(data) {
  return request({
    url: `/system/invoice/apply`,
    method: "post",
    data
  });
}
// 失败发票重试
export function invoicereInvoice(params) {
  return request({
    url: `/system/invoice/reInvoice`,
    method: "get",
    params:params
  });
}
// 发票结果查询
export function queryInvoiceResult(data) {
  return request({
    url: `/system/invoice/queryInvoiceResult`,
    method: "post",
    data
  });
}


// 收款公司的名称
export function merchantParamConfigmerSelect() {
  return request({
    url: `/system/merchantParamConfig/merSelect`,
    method: "get"
  });
}
// 大赛项目的名称
export function getCommodityNameLists() {
  return request({
    url: `/system/order/commodityNameList`,
    method: "get"
  });
}