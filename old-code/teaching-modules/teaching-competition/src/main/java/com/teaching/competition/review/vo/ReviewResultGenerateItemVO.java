package com.teaching.competition.review.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 简单平均分汇总项。
 */
@Data
public class ReviewResultGenerateItemVO {
    private Long activityId;
    private Long roundId;
    private Long objectId;
    private String objectCode;
    private String objectName;
    private Integer assignedCount;
    private Integer submittedCount;
    private BigDecimal calculatedScore;
}
