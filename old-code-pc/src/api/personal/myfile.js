import request from "@/utils/request";
//我的赛事列表
export function fileDistributeUserTasklist(params) {
  return request({
    url: "/system/fileDistributeUserTask/list",
    method: "get",
    params,
  });
}

export function saveFileUploadRecordUser(data) {
  return request({
    url: "/system/fileDistributeUserTask/saveFileUploadRecordUser",
    method: "post",
    data,
  });
}
// 记录下载
export function fileDownloadRecord(params) {
  return request({
    url: "/system/fileDistributeUserTask/fileDownloadRecord",
    method: "get",
    params,
  });
}
//下载日志
export function downLoadRecord(data) {
  return request({
    url: "/system/downLoadRecord",
    method: "post",
    data,
  });
}
// 已读未读
export function fileTaskReadRecord(params) {
  return request({
    url: "/system/fileDistributeUserTask/fileTaskReadRecord",
    method: "get",
    params,
  });
}

// 获取文件任务通知标题列表
export function getFileTaskNotifications(taskId) {
  return request({
    url: `/system/fileDistributeUserTask/${taskId}/notifications`,
    method: "get",
  });
}

// 获取文件任务通知详情
export function getFileTaskNotificationDetail(taskId, notificationId) {
  return request({
    url: `/system/fileDistributeUserTask/${taskId}/notifications/${notificationId}`,
    method: "get",
  });
}

// 获取当前时间
export function getSystemDate() {
  return request({
    url: "/system/fileDistributeUserTask/getSystemDate",
    method: "get",
  });
}
//任务提交
export function saveFileUploadManagerUser(data) {
  return request({
    url: "/system/fileDistributeUserTask/saveFileUploadManagerUser",
    method: "post",
    data,
  });
}
//重新认证
export function updateSubmitStatus(data) {
  return request({
    url: "/system/fileDistributeUserTask/updateSubmitStatus",
    method: "post",
    data,
  });
}

export function exportPresignedUrl(params) {
  return request({
    url: '/file/oss/presignedUrl',
    method: 'get',
    params: params
  })
}

export function getOssClientKey(params) {
  return request({
    url: '/file/oss/temporaryVoucher',
    method: 'get',
    params: params
  })
}
