package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.util.Date;

        /**
         * 评审任务分配表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewAssignment extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long activityId;

    private Long roundId;

    private Long objectId;

    private Long reviewerId;

    private Long reviewerUserId;

    private Long panelId;

    private String assignmentType;

    private String status;

    private Long assignedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date assignedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submittedTime;
        }
