package com.teaching.competition.review.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端只读评分明细快照。
 */
@Data
public class ReviewScoreDetailReadonlyVO {
    private Long detailId;
    private Long recordId;
    private Long criteriaId;
    private String criteriaName;
    private String scoreType;
    private BigDecimal scoreValue;
    private String optionValue;
    private String textValue;
    private BigDecimal weight;
    private Integer sortOrder;
}
