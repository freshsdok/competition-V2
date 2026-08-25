package com.teaching.competition.review.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 评分明细入参。
 */
@Data
public class ReviewScoreDetailDTO {
    private Long criteriaId;
    private String criteriaName;
    private String scoreType;
    private BigDecimal scoreValue;
    private String optionValue;
    private String textValue;
    private BigDecimal weight;
    private Integer sortOrder;
}
