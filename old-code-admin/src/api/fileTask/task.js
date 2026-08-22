import request from "@/utils/request";

function encodeUtf8Base64(value) {
  const text = value || "";
  if (typeof TextEncoder === "undefined") {
    return window.btoa(unescape(encodeURIComponent(text)));
  }
  const bytes = new TextEncoder().encode(text);
  const chunkSize = 0x8000;
  let binary = "";
  for (let offset = 0; offset < bytes.length; offset += chunkSize) {
    binary += String.fromCharCode(...bytes.subarray(offset, offset + chunkSize));
  }
  return window.btoa(binary);
}

// 文件任务列表
export function getLists(query) {
  return request({
    url: '/system/fileDistributeTask/list',
    method: 'get',
    params: query
  })
}


// 保存文件任务
export function saveFileTask(data) {
  return request({
    url: '/system/fileDistributeTask/saveFileTask',
    method: 'post',
    data: data
  })
}

// 编辑文件任务
export function editFileTask(data) {
  return request({
    url: '/system/fileDistributeTask/editFileTask',
    method: 'post',
    data: data
  })
}
// 删除文件任务
export function removeFileTask(id) {
  return request({
    url: `/system/fileDistributeTask/remove/${id}`,
    method: 'get',
  })
}

// 更新文件任务状态
export function updateTaskStatus(data) {
  return request({
    url: '/system/fileDistributeTask/updateTaskStatus',
    method: 'post',
    data: data
  })
}

// 导出管理列表
export function exportManageList(params) {
  return request({
    url: '/system/exportManage/list',
    method: 'get',
    params: params
  })
}

//下载文件日志
export function downLoadRecordList(params) {
  return request({
    url: '/system/downLoadRecord/list',
    method: 'get',
    params: params
  })
}

// 文件任务应上传人员
export function getFileTaskRecipients(taskId, params) {
  return request({
    url: `/system/fileDistributeTask/${taskId}/recipients`,
    method: "get",
    params,
  });
}

// 发送文件任务通知
export function sendFileTaskNotification(taskId, data) {
  const payload = {
    ...data,
    contentBase64: encodeUtf8Base64(data.content),
  };
  delete payload.content;
  return request({
    url: `/system/fileDistributeTask/${taskId}/notifications`,
    method: "post",
    data: payload,
  });
}

// 文件任务通知发送历史
export function getFileTaskNotifications(taskId, params) {
  return request({
    url: `/system/fileDistributeTask/${taskId}/notifications`,
    method: "get",
    params,
  });
}

// 文件任务通知详情
export function getFileTaskNotification(taskId, notificationId) {
  return request({
    url: `/system/fileDistributeTask/${taskId}/notifications/${notificationId}`,
    method: "get",
  });
}

// 撤回文件任务通知
export function withdrawFileTaskNotification(taskId, notificationId) {
  return request({
    url: `/system/fileDistributeTask/${taskId}/notifications/${notificationId}/withdraw`,
    method: "put",
  });
}
