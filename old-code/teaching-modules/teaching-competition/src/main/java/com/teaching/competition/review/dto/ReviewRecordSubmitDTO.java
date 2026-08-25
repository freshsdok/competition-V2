package com.teaching.competition.review.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提交评审记录入参。
 */
@Data
public class ReviewRecordSubmitDTO {
    private Long id;
    private Long activityId;
    private Long roundId;
    private Long objectId;
    private Long assignmentId;
    private Long reviewerId;
    private Long reviewerUserId;
    private BigDecimal totalScore;
    private String grade;
    private String recommendation;
    private String commentText;
    private List<ReviewScoreDetailDTO> details;
}
