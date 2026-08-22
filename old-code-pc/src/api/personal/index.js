import request from "@/utils/request";

export * from "./sceneResource";

//我的赛事列表
export function userCompetition(data) {
  return request({
    url: "/competition/userCompetition/list",
    method: "post",
    data,
  });
}

// 我的现场证件/参赛证列表
export function mySceneCredentialList() {
  return request({
    url: "/competition/userCompetition/sceneCredential/myList",
    method: "get",
  });
}

// 当前登录人可见的现场公告和个人通知
export function mySceneNoticeList() {
  return request({
    url: "/competition/sceneNotice/myList",
    method: "get",
  });
}

// 教师查看自己指导学生/团队参赛证
export function teacherStudentCredentials(params) {
  return request({
    url: "/competition/userCompetition/teacher/studentCredentials",
    method: "get",
    params,
  });
}

// 教师查看自己指导学生/团队参赛证详情
export function teacherStudentCredentialDetail(credentialId) {
  return request({
    url: `/competition/userCompetition/teacher/studentCredential/${credentialId}`,
    method: "get",
  });
}
// 订单列表 
export function orderlist(params) {
  return request({
    url: "/system/order/personalList",
    method: "get",
    params,
  });
}
// 取消订单
export function cancelOrder(id) {
  return request({
    url: `/system/order/cancelOrder/${id}`,
    method: "get"
  });
}
// 退费重缴取消订单
export function cancelRepaymentOrder(id) {
  return request({
    url: `/system/order/cancelRepaymentOrder/${id}`,
    method: "get"
  });
}
// 更新订单附件信息
export function updatePaymentProof(params) {
  return request({
    url: `/system/order/updatePaymentProof`,
    method: "get",
    params
  });
}
// 订单详细信息
export function orderchak(id) {
  return request({
    url: `/system/order/personal/${id}`,
    method: "get"
  });
}

// 发票列表
export function invoicelist(data,params) {
  return request({
    url: `/system/invoice/personalList?pageNum=${params.pageNum}&pageSize=${params.pageSize}`,
    method: "post",
    data
  });
}


// 查询历史作品信息
export function competitionWorks(params) {
  return request({
    url: `/competition/competitionWorks/getUserList`,
    method: "get",
    params
  });
}
// 查询订单我的订单
export function getOrderByUserIdAndCommodityId(params) {
  return request({
    url: `/system/order/getOrderByUserIdAndCommodityId`,
    method: "get",
    params
  });
}
// 查询订单我的订单所有状态项
export function perStatusCount(params) {
  return request({
    url: `/system/order/perStatusCount`,
    method: "get",
    params
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

// 发票结果查询
export function queryInvoiceResult(data) {
  return request({
    url: `/system/invoice/queryInvoiceResult`,
    method: "post",
    data
  });
}


// 查询订单下各团队用户开票状态
export function queryTeamAndUserByOrderId(data) {
  return request({
    url: `/system/invoice/queryTeamAndUserByOrderId`,
    method: "post",
    data
  });
}

// 根据订单id、人员id查询人员信息并进行不同商户的金额汇总

export function queryInvoiceAmount(data) {
  return request({
    url: `/system/invoice/queryInvoiceAmount`,
    method: "post",
    data
  });
}
export function invoiceapplyNew(data) {
  return request({
    url: `/system/invoice/applyNew`,
    method: "post",
    data
  });
}


// 快捷备注
export function selectCompetitionApplyInfoListByTeamCode(data) {
  return request({
    url: `/competition/userCompetition/selectCompetitionApplyInfoListByTeamCode`,
    method: "post",
    data
  });
}



// 个人库列表
export function selectInvoicePerInfo(params) {
  return request({
    url: `/system/invoicePerInfo/selectInvoicePerInfo`,
    method: "get",
    params
  });
}
