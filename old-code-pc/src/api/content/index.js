import request from "@/utils/request";

//获取问题列表
export function getQuestions(params) {
  return request({
    url: "/content/questions/pc/list",
    method: "get",
    params,
  });
}
