package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 一次证书图片批量同步运行记录。
 */
@Data
public class CertificateImageSyncRun {
    public static final String RUNNING = "RUNNING";
    public static final String PAUSED = "PAUSED";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";

    private Long runId;
    private String source;
    private String runStatus;
    private Long totalCount;
    private Long processedCount;
    private Long successCount;
    private Long failureCount;
    private String currentCertCode;
    private String lastError;
    private Integer requestsPerSecond;
    private Long operatorId;
    private String operatorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
