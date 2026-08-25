package com.teaching.competition.review.domain;

    import com.fasterxml.jackson.annotation.JsonFormat;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    /**
     * 专家组表。
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public class ReviewPanel extends ReviewBaseEntity {
        private static final long serialVersionUID = 1L;

private Long activityId;

private Long roundId;

private String panelName;

private String panelCode;

private Long leaderUserId;

private Long secretaryUserId;

private String status;
    }
