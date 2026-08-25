package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

        /**
         * 评审规则表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewRule extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long activityId;

    private Long roundId;

    private String ruleName;

    private String scoreMode;

    private BigDecimal totalScore;

    private String anonymousMode;

    private String description;

    private String enabled;
        }
