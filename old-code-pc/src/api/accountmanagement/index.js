import request from "@/utils/request";

//修改用户信息
export function updateUserInfo(data) {
  return request({
    url: "/system/personalCenter/updateUserInfo",
    method: "post",
    data,
  });
}

//修改用户手机号邮箱
export function updateUserInfoPhoneOrEmail(data) {
  return request({
    url: "/system/personalCenter/updateUserInfoPhoneOrEmail",
    method: "post",
    data,
  });
}


//修改用户密码

export function updatePwd(data) {
  return request({
    url: "/system/personalCenter/updatePwd",
    method: "post",
    data,
  });
}
// 获取身份认证信息
export function listpersonalCenter(params) {
  return request({
    url: "/system/personalCenter/list",
    method: "get",
    params:params,
  });
}
//新增身份认证信息 

export function saveIdentityInfo(data) {
  return request({
    url: "/system/identityInfo/saveIdentityInfo",
    method: "post",
    data,
  });
}
//修改身份认证信息 

export function updateIdentityInfo(data) {
  return request({
    url: "/system/identityInfo/updateIdentityInfo",
    method: "post",
    data,
  });
}

//实名认证  

export function saveAuthInfo(data) {
  return request({
    url: "/system/authInfo/saveAuthInfo",
    method: "post",
    data,
  });
}

//外籍实名认证  

export function taskpcrealName(data) {
  return request({
    url: "/system/task/pc/realName",
    method: "post",
    data,
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
// 专业查询
export function disciplinelist(params) {
  return request({
    url: "/system/discipline/pc/list",
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