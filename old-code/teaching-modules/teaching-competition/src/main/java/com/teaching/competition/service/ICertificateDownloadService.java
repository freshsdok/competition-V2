package com.teaching.competition.service;

import com.teaching.competition.domain.CertificateDownloadSummary;
import com.teaching.competition.domain.CertificatePictureListResult;

/**
 * 用户证书下载服务。
 */
public interface ICertificateDownloadService {
    CertificateDownloadSummary getGuidedCertificateSummary(Long userId);

    CertificatePictureListResult getGuidedCertificatePictures(Long userId);
}
