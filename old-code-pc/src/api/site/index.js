import request from "@/utils/request";

//我的未读消息列表
export function getUnreadNotificationList(params) {
  return request({
    url: "/system/notification/sender/unread/list",
    method: "get",
    params,
  });
}

//我的全部消息
export function getInboxNotificationList(params) {
  return request({
    url: "/system/notification/sender/inbox/list",
    method: "get",
    params,
  });
}

//查看站内信详情并标记已读
export function getInboxNotificationDetail(ids,params) {
  return request({
    url: `/system/notification/sender/view/${ids}`,
    method: "get",
    params,
  });
}

//单个多个删除
export function deleteInboxNotification(ids,params) {
  return request({
    url: `/system/notification/sender/${ids}`,
    method: "delete",
    params,
  });
}

//全部已读
export function readAllInboxNotification(post) {
  return request({
    url: `/system/notification/sender/inbox/read-all`,
    method: "post",
    post,
  });
}

//全部删除
export function deleteAllInboxNotification(post) {
  return request({
    url: `/system/notification/sender/inbox/delete-all`,
    method: "post",
    post,
  });
}
