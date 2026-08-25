package com.teaching.competition.review.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 评分规则配置校验结果。
 */
@Data
public class ReviewRuleValidateVO {
    private Boolean valid = Boolean.TRUE;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private String scoreMode;
    private BigDecimal totalScore;
    private BigDecimal maxScoreSum = BigDecimal.ZERO;
    private BigDecimal weightSum = BigDecimal.ZERO;
    private BigDecimal weightedMaxScore = BigDecimal.ZERO;
    private Integer countableCriteriaCount = 0;

    public void addError(String message) {
        errors.add(message);
        valid = Boolean.FALSE;
    }

    public void addWarning(String message) {
        warnings.add(message);
    }
}
