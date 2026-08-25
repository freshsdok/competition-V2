package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 证书图片私有对象存储缓存元数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CertificateImageCache extends BaseEntity {
    private static final long serialVersionUID = 1L;

    public static final String PENDING = "PENDING";
    public static final String SYNCING = "SYNCING";
    public static final String SUCCESS = "SUCCESS";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String FAILED = "FAILED";

    private Long cacheId;
    private String certCode;
    private String contestName;
    private String name;
    private String session;
    private String contestArea;
    private Integer runingNumYear;
    @JsonIgnore
    private String objectKey;
    private String fileName;
    private String mimeType;
    private Long fileSize;
    private String sha256;
    private String cacheStatus;
    private Integer retryCount;
    private String lastError;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextRetryTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastSyncTime;
}
