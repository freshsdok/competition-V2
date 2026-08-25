package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

/**
 * 负责人证书异步 ZIP 导出任务。
 */
@Data
public class CertificateExportTask {
    public static final String QUEUED = "QUEUED";
    public static final String RESOLVING = "RESOLVING";
    public static final String PACKAGING = "PACKAGING";
    public static final String UPLOADING = "UPLOADING";
    public static final String COMPLETED = "COMPLETED";
    public static final String PARTIAL = "PARTIAL";
    public static final String FAILED = "FAILED";
    public static final String EXPIRED = "EXPIRED";

    private String taskId;
    private Long userId;
    private String exportScope;
    @JsonIgnore
    private String selectedCertCodes;
    private String taskStatus;
    private String phase;
    private Integer progress;
    private Long totalCount;
    private Long processedCount;
    private Long successCount;
    private Long failureCount;
    @JsonIgnore
    private String zipObjectKey;
    private String zipFileName;
    private String missingCertCodes;
    private String lastError;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expiresAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;
}
