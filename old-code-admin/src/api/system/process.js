import request from "@/utils/request";

// 审核流程配置列表
export function auditlist(params) {
  return request({
    url: "/system/audit/list",
    method: "get",
    params: params,
  });
}

// 新增流程配置
export function audit(data) {
  return request({
    url: "/system/audit",
    method: "post",
    data: data,
  });
}
// 获取系统审核配置详细信息
export function auditroleId(roleId) {
  return request({
    url: "/system/audit/" + roleId,
    method: "get",
  });
}
// 获取提交人审核人
export function taskgetUser(type) {
  return request({
    url: "/system/task/getUser/" + type,
    method: "get",
  });
}
//  修改流程配置
export function updataaudit(data) {
  return request({
    url: "/system/audit",
    method: "put",
    data: data,
  });
}

//  修改流程配置状态
export function enableOrDeactivate(data) {
  return request({
    url: "/system/audit/enableOrDeactivate",
    method: "put",
    data: data,
  });
}
// 删除流程配置
export function delaudit(roleId) {
  return request({
    url: "/system/audit/" + roleId,
    method: "delete",
  });
}

export function tasklist(params) {
  return request({
    url: "/system/task/list",
    method: "get",
    params: params,
  });
}
// 已完成列表
export function taskfinish(params) {
  return request({
    url: "/system/task/finish",
    method: "get",
    params: params,
  });
}

// 查看详情
export function taskroleId(params) {
  return request({
    url: `/system/task/${params}`,
    method: "get",
  });
}
//  审核操作
export function taskaudit(data) {
  return request({
    url: "/system/task/audit",
    method: "put",
    data: data,
  });
}
//  视频审核操作
export function taskVideoAudit(data) {
  return request({
    url: "/system/task/videoAudit",
    method: "put",
    data: data,
  });
}
// 审核流程配置列表
export function auditcopy(auditId) {
  return request({
    url: `/system/audit/copy/${auditId}`,
    method: "get",
  });
}

// 机构角色筛选用户
export function getUserList(orgId, roleId) {
  return request({
    url: `/system/user/getUserList/${orgId}/${roleId}`,
    method: "get",
  });
}

// 身份认证查询
export function identityInfolist(params) {
  return request({
    url: "/system/identityInfo/list",
    method: "get",
    params: params,
  });
}

// 身份认证详情信息
export function getIdentityInfoDetail(authId) {
  return request({
    url: `/system/identityInfo/getIdentityInfoDetail/${authId} `,
    method: "get"
  });
}

// 学校查询
export function schoollist(params) {
  return request({
    url: "/system/school/pc/list",
    method: "get",
    params:params,
  });
}

// 带队老师
export function personalCentergetTeachers(params) {
  return request({
    url: "/system/personalCenter/pc/getTeachers",
    method: "get",
    params:params,
  });
}

export function getTaskPic(taskId) {
  return request({
    url: "/system/task/pic/" + taskId,
    method: "get",
  });
}
// 批量审核put参考审核接口
export function sendTaskAudit(data) {
  return request({
    url: "/system/task/audits",
    method: "put",
    data: data,
  });
}
