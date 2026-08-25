package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.util.Date;

        /**
         * 评审活动表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewActivity extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private String activityName;

    private String activityCode;

    private String activityType;

    private String sourceModule;

    private String sourceBizType;

    private String objectType;

    private String submissionMode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitDeadline;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewEndTime;

    private String anonymousMode;

    private String resultPublishMode;

    private String status;

    private String description;
        }
