package com.teaching.competition.review.dto;

import lombok.Data;

import java.util.List;

/**
 * 专家端保存/提交评分入参。
 */
@Data
public class ReviewMyReviewScoreDTO {
    private List<ReviewScoreDetailDTO> scoreDetails;

    private String commentText;

    private String recommendation;
}
