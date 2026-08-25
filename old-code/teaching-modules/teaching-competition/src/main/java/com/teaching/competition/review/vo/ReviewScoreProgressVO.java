package com.teaching.competition.review.vo;

import lombok.Data;

/**
 * 现场对象专家评分进度。
 */
@Data
public class ReviewScoreProgressVO {
    private Integer submittedCount;
    private Integer totalAssignedCount;
    private Integer unsubmittedCount;
    private String displayText;
}
