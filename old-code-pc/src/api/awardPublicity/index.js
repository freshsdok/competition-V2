import request from "@/utils/request";

// 获取获奖公示Tab列表
export function getAwardPublicityTabs() {
  return request({
    url: "/competition/awardDetailsUser/awardPublicityList",
    method: "get",
  });
}

// 根据Tab id查询获奖公示明细列表
export function getAwardDetailsList(params) {
  return request({
    url: "/competition/awardDetailsUser/awardDetailsList",
    method: "get",
    params,
  });
}

// 编辑保存获奖公示明细列表
export function updateAwardDetailsList(data) {
  return request({
    url: "/competition/awardDetailsUser/updateAwardDetailsList",
    method: "post",
    data,
  });
}
