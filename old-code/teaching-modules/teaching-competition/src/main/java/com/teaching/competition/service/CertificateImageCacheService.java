package com.teaching.competition.service;

import com.teaching.competition.domain.CertificateFilterOptions;
import com.teaching.competition.domain.CertificateImageCache;
import com.teaching.competition.domain.CertificateImageCacheQuery;
import com.teaching.competition.domain.CertificateImageCacheStats;
import com.teaching.competition.domain.CertificateImageSyncRun;
import com.teaching.competition.domain.GuidedCertificateQuery;

import java.util.List;
import java.util.Map;

public interface CertificateImageCacheService {
    void seedGuidedCertificates(Long userId);

    List<CertificateImageCache> selectGuidedPage(Long userId, GuidedCertificateQuery query);

    CertificateFilterOptions selectGuidedFilterOptions(Long userId);

    Map<String, Object> enqueueFallback(Long userId, List<String> certCodes);

    String getPreviewUrl(Long userId, String certCode);

    CertificateImageCacheStats getStats();

    List<CertificateImageCache> selectAdminPage(CertificateImageCacheQuery query);

    CertificateImageSyncRun startBatch(String source, Long operatorId, String operatorName);

    CertificateImageSyncRun pauseBatch();

    CertificateImageSyncRun resumeBatch();

    int retryFailedRecords();

    int resetCertificate(String certCode);

    CertificateImageSyncRun getCurrentRun();

    List<CertificateImageSyncRun> getRunHistory();

    boolean syncOne(String certCode, boolean highPriority);
}
