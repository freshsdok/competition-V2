package com.teaching.competition.review.vo;

import lombok.Data;

/**
 * 参赛证解析评审对象结果。
 */
@Data
public class ReviewCertificateResolveVO {
    private Long objectId;
    private String objectCode;
    private String objectName;
    private Long activityId;
    private String submitStatus;
    private String certificateCode;
    private String certificateType;
    private String memberName;
    private String memberRole;
    private String sourceTeamId;
    private String sourceRegistrationId;
    private String validStatus;
    private Boolean inSession;
    private String warningMessage;
}
