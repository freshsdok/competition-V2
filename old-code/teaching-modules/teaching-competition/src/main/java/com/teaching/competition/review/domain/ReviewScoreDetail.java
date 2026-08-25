package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

        /**
         * 评分明细表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewScoreDetail extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long recordId;

    private Long activityId;

    private Long roundId;

    private Long objectId;

    private Long criteriaId;

    private String criteriaName;

    private String scoreType;

    private BigDecimal scoreValue;

    private String optionValue;

    private String textValue;

    private BigDecimal weight;

    private Integer sortOrder;
        }
