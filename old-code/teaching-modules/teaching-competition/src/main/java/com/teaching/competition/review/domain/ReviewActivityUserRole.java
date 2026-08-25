package com.teaching.competition.review.domain;

    import com.fasterxml.jackson.annotation.JsonFormat;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    /**
     * 活动内用户角色表。
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public class ReviewActivityUserRole extends ReviewBaseEntity {
        private static final long serialVersionUID = 1L;

private Long activityId;

private Long userId;

private String roleType;

private Long reviewerId;

private Long panelId;

private String enabled;
    }
