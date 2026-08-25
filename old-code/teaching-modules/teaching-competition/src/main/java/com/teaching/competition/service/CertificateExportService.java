package com.teaching.competition.service;

import com.teaching.competition.domain.CertificateExportRequest;
import com.teaching.competition.domain.CertificateExportTask;

import java.util.Map;

public interface CertificateExportService {
    CertificateExportTask createTask(Long userId, CertificateExportRequest request);

    CertificateExportTask getTask(Long userId, String taskId);

    Map<String, String> getDownload(Long userId, String taskId);
}
