package com.teaching.competition.review.vo;

import lombok.Data;

/**
 * 填报截止锁定结果。
 */
@Data
public class ReviewSubmissionCloseResultVO {
    private Long activityId;

    private Integer lockedCount = 0;

    private Integer invalidCount = 0;

    private Integer ignoredCount = 0;

    private String message;
}
