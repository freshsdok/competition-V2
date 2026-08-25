package com.teaching.competition.service;

import com.teaching.competition.domain.CertificateImportPreviewResult;
import com.teaching.competition.domain.CertificateImportRequest;
import com.teaching.competition.domain.CertificateImportSqlResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 获奖证书导入 SQL 生成服务。
 */
public interface ICertificateImportService {
    CertificateImportSqlResult generateImportSql(MultipartFile file);

    CertificateImportPreviewResult previewImport(MultipartFile file, CertificateImportRequest request);

    CertificateImportSqlResult generateImportSql(MultipartFile file, CertificateImportRequest request);
}
