package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.util.Date;

        /**
         * 评审轮次表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewRound extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long activityId;

    private String roundName;

    private Integer roundNo;

    private String roundType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Long ruleId;

    private String status;

    private String description;
        }
