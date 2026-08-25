package com.teaching.competition.domain;

import lombok.Data;

/**
 * 后台证书图片缓存分页筛选条件。
 */
@Data
public class CertificateImageCacheQuery {
    private String certCode;
    private String name;
    private String contestName;
    private String contestArea;
    private String cacheStatus;
}
