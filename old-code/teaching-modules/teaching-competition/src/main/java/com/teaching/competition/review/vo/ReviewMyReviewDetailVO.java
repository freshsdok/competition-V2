package com.teaching.competition.review.vo;

import com.teaching.competition.review.domain.ReviewAssignment;
import com.teaching.competition.review.domain.ReviewCriteria;
import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectMaterial;
import com.teaching.competition.review.domain.ReviewObjectMember;
import com.teaching.competition.review.domain.ReviewRecord;
import com.teaching.competition.review.domain.ReviewRound;
import com.teaching.competition.review.domain.ReviewRule;
import com.teaching.competition.review.domain.ReviewScoreDetail;
import lombok.Data;

import java.util.List;

/**
 * 专家端评审任务详情。
 */
@Data
public class ReviewMyReviewDetailVO {
    private ReviewAssignment assignment;

    private ReviewObject reviewObject;

    private ReviewRound round;

    private List<ReviewObjectMember> members;

    private List<ReviewObjectMaterial> materials;

    private ReviewRule rule;

    private List<ReviewCriteria> criteriaList;

    private ReviewRecord existingRecord;

    private List<ReviewScoreDetail> existingScoreDetails;

    private Boolean canReview;

    private String cannotReviewReason;
}
