package com.teaching.competition.review.service;

import com.teaching.competition.review.domain.ReviewRound;

/**
 * 评审轮次表Service接口。
 */
public interface IReviewRoundService extends IReviewCrudService<ReviewRound> {
    int bindRule(Long roundId, Long ruleId);
}
