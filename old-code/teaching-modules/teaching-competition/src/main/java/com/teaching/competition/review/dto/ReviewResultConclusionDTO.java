package com.teaching.competition.review.dto;

import lombok.Data;

/**
 * 填写评审结果发布性结论入参。
 */
@Data
public class ReviewResultConclusionDTO {
    private String evaluationConclusion;
    private Long operatorUserId;
}
