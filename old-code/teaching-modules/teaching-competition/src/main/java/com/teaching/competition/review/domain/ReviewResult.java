package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.Date;

        /**
         * 评审结果表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewResult extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long activityId;

    private Long roundId;

    private Long objectId;

    private Integer reviewerCount;

    private Integer submittedCount;

    private BigDecimal calculatedScore;

    private String calculatedGrade;

    private Integer calculatedRank;

    private String evaluationConclusion;

    private Long conclusionGeneratedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date conclusionGeneratedTime;

    private String resultStatus;

    private Long generatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date generatedTime;

    private Long publishedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishedTime;

    private Long revokedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date revokedTime;
        }
