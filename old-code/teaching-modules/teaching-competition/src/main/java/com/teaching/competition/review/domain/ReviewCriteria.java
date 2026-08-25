package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

        /**
         * 评分指标表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewCriteria extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long ruleId;

    private Long parentId;

    private String criteriaName;

    private String criteriaDesc;

    private String scoreType;

    private BigDecimal minScore;

    private BigDecimal maxScore;

    private BigDecimal weight;

    private String required;

    private String optionsJson;

    private Integer sortOrder;

    private String enabled;
        }
