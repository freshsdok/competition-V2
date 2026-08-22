import request from "@/utils/request";

function buildCertificateImportFormData(file, config) {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("competitionSeriesId", config.competitionSeriesId);
  formData.append("certificateType", config.certificateType);
  formData.append("issuanceDate", config.issuanceDate);
  return formData;
}

// 校验证书Excel并返回历史表、用户源表及未关联数据预览
export function previewCertificateImport(file, config) {
  return request({
    url: "/competition/competition/certificateImport/preview",
    method: "post",
    data: buildCertificateImportFormData(file, config),
    headers: {
      "Content-Type": "multipart/form-data",
      repeatSubmit: false,
    },
  });
}

// 校验证书Excel并生成双表导入SQL，不直接写入数据库
export function generateCertificateImportSql(file, config) {
  return request({
    url: "/competition/competition/certificateImport/generateSql",
    method: "post",
    data: buildCertificateImportFormData(file, config),
    headers: {
      "Content-Type": "multipart/form-data",
      repeatSubmit: false,
    },
    responseType: "blob",
  });
}
