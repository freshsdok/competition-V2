import request from "@/utils/request";

// 用户赛证成绩列表
export function getCompetitionGradeList(params) {
  const { pageNum, pageSize, ...data } = params;
  return request({
    url: "/competition/competition/competitionGradeInfo/list",
    method: "post",
    params: { pageNum, pageSize },
    data: { ...data },
  });
}

// 获取用户赛证成绩
export function getCompetitionGradeInfo(gradeId) {
  return request({
    url: `/competition/competition/competitionGradeInfo/${gradeId}`,
    method: "get",
  });
}

// 修改用户赛证成绩
export function updateCompetitionGradeInfo(data) {
  return request({
    url: `/competition/competition/competitionGradeInfo/updateCompetitionGradeInfo`,
    method: "post",
    data,
  });
}

// 删除用户赛证成绩
export function delCompetitionGrade(gradeId) {
  return request({
    url: `/competition/competition/competitionGradeInfo/removeCompetitionGradeInfo/${gradeId}`,
    method: "get",
  });
}

// 导入用户赛证成绩
export function importCompetitionGrade(formData, params) {
  return request({
    url: `/competition/competition/competitionGradeInfo/importGradeInfo`,
    method: "post",
    data: formData,
    params: {
      reqJson: JSON.stringify(params),
    },
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

// 覆盖更新成绩信息
export function updateGradeInfo(data) {
  return request({
    url: `/competition/competition/competitionGradeInfo/updateGradeInfo`,
    method: "post",
    data,
  });
}
