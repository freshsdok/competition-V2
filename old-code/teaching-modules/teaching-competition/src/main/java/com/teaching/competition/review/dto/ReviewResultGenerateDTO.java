package com.teaching.competition.review.dto;

import lombok.Data;

import java.util.List;

/**
 * 生成评审结果入参。
 */
@Data
public class ReviewResultGenerateDTO {
    private Long activityId;
    private Long roundId;
    private List<Long> objectIds;
    private Long generatedBy;
    private Boolean forceRegenerate;
}
