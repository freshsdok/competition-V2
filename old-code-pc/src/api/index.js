import request from "@/utils/request";
//注册
export function authregister(data) {
  return request({
    url: "/system/auth/pc/register",
    method: "post",
    data,
  });
}
//重置密码
export function authresetPwd(data) {
  return request({
    url: "/system/auth/pc/resetPwd",
    method: "post",
    data,
  });
}
//获取短信验证码
export function getPhoneCode(data) {
  return request({
    url: "/system/auth/pc/sendCode",
    method: "post",
    data,
  });
}
//获取短信验证码
export function getPhoneCodeCaptcha(data) {
  return request({
    url: "/system/auth/pc/captcha",
    method: "post",
    data,
  });
}

//获取邮箱验证码
export function sendEmailCode(data) {
  return request({
    url: "/system/auth/pc/sendCode",
    method: "post",
    data,
  });
}
//登录
export function authlogin(data) {
  return request({
    url: "/system/auth/pc/login",
    method: "post",
    data,
  });
}
//登录
export function userInfoLogin(data) {
  return request({
    url: "/system/auth/pc/userInfoLogin",
    method: "post",
    data,
  });
}
//验证手机号邮箱是否存在
export function checkUserAccount(params) {
  return request({
    url: "/system/auth/pc/checkUserAccount",
    method: "get",
    params,
  });
}

// 已登录用户修改手机号或邮箱时检查新账号是否可用
export function checkPersonalAccountAvailable(params) {
  return request({
    url: "/system/personalCenter/checkUserAccountAvailable",
    method: "get",
    params,
  });
}



//获取用户信息
export function authinfo(params) {
  return request({
    url: "/system/auth/info",
    method: "get",
    params,
  });
}
//个人中心获取用户信息
export function getAuthInfo(params) {
  return request({
    url: "/system/personalCenter/getUserCenterInfo",
    method: "get",
    params,
  });
}



//获取查用户成绩接口
export function userGradeInfolist(params) {
  return request({
    url: "/competition/userGradeInfo/list",
    method: "get",
    params,
  });
}
//保存作品
export function saveCompetitionWorks(data) {
  return request({
    url: "/competition/competitionWorks/saveCompetitionWorks",
    method: "post",
    data,
  });
}

// 获取用户中心信息
export function getUserCenterInfo(params) {
  return request({
    url: "/system/personalCenter/getUserCenterInfo",
    method: "get",
    params,
  });
}

// 校验验证码信息
export function checkRegisterInfo(data) {
  return request({
    url: "/system/auth/pc/checkRegisterInfo",
    method: "post",
    data,
  });
}

// 获取菜单列表
export function getMenuList(params) {
  return request({
    url: "/system/menu/pc/list",
    method: "get",
    params,
  });
}

export function getMenuDetailById(menuId) {
  return request({
    url: `/system/menu/pc/${menuId}`,
    method: "get"
  });
}

export function getMenuDetailSecondById(menuId) {
  return request({
    url: `/system/menu/pc/detail/${menuId}`,
    method: "get"
  });
}
// 公告
export function getNotices() {
  return request({
    url: `/content/contentDetail/pc/getNotices`,
    method: "get"
  });
}
export function logout() {
  return request({
    url: `/auth/logout`,
    method: "delete"
  });
}

// 获取我的文件未阅读数
export function unReadCount() {
  return request({
    url: `/system/fileDistributeUserTask/unReadCount`,
    method: "get"
  });
}

// 获取系统时间
export function getSystemDate() {
  return request({
    url: `/competition/userCompetition/getSystemDate`,
    method: "get"
  });
}
