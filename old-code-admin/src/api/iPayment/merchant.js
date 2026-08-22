import request from "@/utils/request";
// 商户配置列表
export function merchantParamConfiglist(params) {
  return request({
    url: "/system/merchantParamConfig/list",
    method: "get",
    params,
  });
}
// 商户配置详情
export function merchantParamConfigid(id) {
  return request({
    url: `/system/merchantParamConfig/${id}`,
    method: "get"
  });
}
// 删除商户配置
export function deletemerchantParamConfig(id) {
  return request({
    url: `/system/merchantParamConfig/${id}`,
    method: "delete"
  });
}
// 新增商户配置列表
export function addmerchantParamConfig(data) {
  return request({
    url: "/system/merchantParamConfig",
    method: "post",
    data,
  });
}

// 修改商户配置列表
export function updatamerchantParamConfig(data) {
  return request({
    url: "/system/merchantParamConfig",
    method: "put",
    data,
  });
}
// 商户配置修改状态
export function merchantParamConfigidchangeStatus(id,params) {
  return request({
    url: `/system/merchantParamConfig/changeStatus/${id}`,
    method: "get",
    params
  });
}

// 商户配置修改状态
export function getSecondList(params) {
  return request({
    url: `/system/invoice/getSecondList`,
    method: "get",
    params
  });
}