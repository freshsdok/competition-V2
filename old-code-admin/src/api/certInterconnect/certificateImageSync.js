import request from "@/utils/request";

const baseUrl = "/competition/competition/certificateImageSync";

export function getCertificateImageOverview() {
  return request({ url: `${baseUrl}/overview`, method: "get" });
}

export function getCertificateImageList(params) {
  return request({ url: `${baseUrl}/list`, method: "get", params });
}

export function getCertificateImageHistory() {
  return request({ url: `${baseUrl}/history`, method: "get" });
}

export function startCertificateImageSync() {
  return request({ url: `${baseUrl}/start`, method: "post" });
}

export function pauseCertificateImageSync() {
  return request({ url: `${baseUrl}/pause`, method: "post" });
}

export function resumeCertificateImageSync() {
  return request({ url: `${baseUrl}/resume`, method: "post" });
}

export function retryCertificateImageFailures() {
  return request({ url: `${baseUrl}/retry`, method: "post" });
}

export function resetCertificateImage(certCode) {
  return request({
    url: `${baseUrl}/reset/${encodeURIComponent(certCode)}`,
    method: "post",
  });
}
