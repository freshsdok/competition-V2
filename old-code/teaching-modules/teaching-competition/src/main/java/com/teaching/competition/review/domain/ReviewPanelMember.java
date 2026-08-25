package com.teaching.competition.review.domain;

    import com.fasterxml.jackson.annotation.JsonFormat;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    /**
     * 专家组成员表。
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public class ReviewPanelMember extends ReviewBaseEntity {
        private static final long serialVersionUID = 1L;

private Long activityId;

private Long roundId;

private Long panelId;

private Long userId;

private Long reviewerId;

private String memberRole;

private String status;
    }
