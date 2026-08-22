import request from "@/utils/request";

export function listBookableSceneResource(params) {
  return request({
    url: "/competition/userCompetition/sceneResource/bookableList",
    method: "get",
    params,
  });
}

export function getBookableSceneResource(scheduleResourceId) {
  return request({
    url: `/competition/userCompetition/sceneResource/${scheduleResourceId}`,
    method: "get",
  });
}

export function listBookableSceneResourceSlot(params) {
  return request({
    url: "/competition/userCompetition/sceneResourceSlot/list",
    method: "get",
    params,
  });
}

export function submitSceneResourceReservation(data) {
  return request({
    url: "/competition/userCompetition/sceneResourceReservation",
    method: "post",
    data,
  });
}

export function listMySceneResourceReservation() {
  return request({
    url: "/competition/userCompetition/sceneResourceReservation/myList",
    method: "get",
  });
}

export function cancelSceneResourceReservation(data) {
  return request({
    url: "/competition/userCompetition/sceneResourceReservation/cancel",
    method: "post",
    data,
  });
}
