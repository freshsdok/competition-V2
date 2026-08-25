package com.teaching.competition.domain;

import lombok.Data;

import java.util.Date;

/**
 * 后台证书图片缓存统计。
 */
@Data
public class CertificateImageCacheStats {
    private long totalCount;
    private long successCount;
    private long pendingCount;
    private long syncingCount;
    private long notFoundCount;
    private long failedCount;
    private double coverageRate;
    private int requestsPerSecond;
    private Long estimatedRemainingSeconds;
    private Date lastRunTime;
    private CertificateImageSyncRun currentRun;
}
