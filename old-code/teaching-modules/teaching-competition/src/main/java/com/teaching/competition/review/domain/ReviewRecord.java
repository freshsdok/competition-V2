package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.Date;

        /**
         * 专家评审记录表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewRecord extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long activityId;

    private Long roundId;

    private Long objectId;

    private Long assignmentId;

    private Long reviewerId;

    private Long reviewerUserId;

    private String recordStatus;

    private BigDecimal totalScore;

    private String grade;

    private String recommendation;

    private String commentText;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submittedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date returnedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lockedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date invalidTime;

    private String invalidReason;
        }
