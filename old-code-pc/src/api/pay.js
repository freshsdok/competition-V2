import request from "@/utils/request";
//注册
export function getPaymentUrl(data) {
  return request({
    url: "/system/order/getPaymentUrl",
    method: "post",
    data,
  });
}

// 重新生成支付链接
export function regeneratePaymentUrl(id) {
  return request({
    url: `/system/order/regeneratePaymentUrl/${id}`,
    method: "get",
  });
}

//获取订单信息
export function getOrderDetail(id) {
  return request({
    url: `/system/order/personal/${id}`,
    method: "get",

  });
}

// 修改支付方式
//获取订单信息
export function updatePayMethod(params) {
  return request({
    url: `/system/order/updatePayMethod`,
    method: "get",
    params

  });
}

//获取线下支付银行信息
export function getOfflineBankInfo(id) {
  return request({
    url: `/system/order/getOfflineBankInfo/${id}`,
    method: "get",

  });
}