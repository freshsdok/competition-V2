package com.teaching.competition.review.domain;

    import com.fasterxml.jackson.annotation.JsonFormat;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    /**
     * 评审对象参赛证映射表。
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public class ReviewObjectCertificateRef extends ReviewBaseEntity {
        private static final long serialVersionUID = 1L;

private Long activityId;

private Long roundId;

private Long objectId;

private String certificateId;

private String certificateCode;

private String certificateType;

private String personId;

private Long userId;

private Long memberId;

private String memberName;

private String memberRole;

private String sourceModule;

private String sourceBizId;

private String sourceTeamId;

private String sourceRegistrationId;

private String validStatus;
    }
