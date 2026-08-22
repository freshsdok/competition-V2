import request from "@/utils/request";

// 获取ID配置
export function getExamRoomConfig(params) {
  return request({
    url: "/wxApp/wxQcCodeRecordUser/list",
    method: "get",
    params
  });
}

// 获取赛场列表
export function getExamRoomList(params) {
  return request({
    url: "/wxApp/wxQcCodeRecordUser/wxQcCodeRecordList",
    method: "get",
    params
  });
}

// 新增赛场
export function addExamRoom(data) {
  return request({
    url: "/wxApp/wxQcCodeRecord/addWxQcCodeRecord",
    method: "post",
    data
  });
}

// 修改赛场
export function updateExamRoom(data) {
  return request({
    url: "/wxApp/wxQcCodeRecord",
    method: "put",
    data
  });
}

// 赛场详情
export function getExamRoomDetail(recordId) {
  return request({
    url: `/wxApp/wxQcCodeRecord/codeBase/${recordId}`,
    method: "get",
  });
}
