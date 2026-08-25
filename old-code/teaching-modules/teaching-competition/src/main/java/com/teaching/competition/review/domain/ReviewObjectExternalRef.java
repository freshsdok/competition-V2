package com.teaching.competition.review.domain;

    import com.fasterxml.jackson.annotation.JsonFormat;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    /**
     * 外部业务关联表。
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public class ReviewObjectExternalRef extends ReviewBaseEntity {
        private static final long serialVersionUID = 1L;

private Long activityId;

private Long objectId;

private String sourceModule;

private String sourceBizType;

private String sourceBizId;

private String sourceBizCode;

private String sourceTeamId;

private String sourceRegistrationId;

private String relationType;

private String extraData;
    }
