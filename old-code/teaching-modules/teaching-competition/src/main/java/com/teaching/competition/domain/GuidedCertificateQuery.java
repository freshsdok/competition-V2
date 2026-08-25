package com.teaching.competition.domain;

import lombok.Data;

/**
 * 负责人证书列表服务端筛选条件。
 */
@Data
public class GuidedCertificateQuery {
    private String keyword;
    private String contestName;
    private String session;
    private String contestArea;
    private Integer runingNumYear;
    private String cacheStatus;
}
