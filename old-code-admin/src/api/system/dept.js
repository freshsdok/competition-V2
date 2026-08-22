import request from "@/utils/request";

// 查询机构列表
export function listDept(query) {
  return request({
    url: "/system/org/list",
    method: "get",
    params: query,
  });
}
// 这个没有api权限校验
export function listDeptNoAuth(query) {
  return request({
    url: "/system/org/lists",
    method: "get",
    params: query,
  });
}

// 查询机构列表（排除节点）
export function listDeptExcludeChild(deptId) {
  return request({
    url: "/system/org/list/exclude/" + deptId,
    method: "get",
  });
}

// 查询机构详细
export function getDept(deptId) {
  return request({
    url: "/system/org/getOrgDetail/" + deptId,
    method: "get",
  });
}

// 新增机构
export function addDept(data) {
  return request({
    url: "/system/org/saveOrgInfo",
    method: "post",
    data: data,
  });
}

// 修改机构
export function updateDept(data) {
  return request({
    url: "/system/org/updateOrgInfo",
    method: "post",
    data: data,
  });
}

// 删除机构
export function delDept(deptId) {
  return request({
    url: "/system/org/remove/" + deptId,
    method: "get",
  });
}

// 查询机构列表
export function listDeptOrg(query) {
  return request({
    url: "/system/org/getList",
    method: "get",
    params: query,
  })
}