package com.teaching.competition.review.service;

import com.teaching.competition.review.domain.ReviewRule;
import com.teaching.competition.review.domain.ReviewRound;
import com.teaching.competition.review.vo.ReviewRuleValidateVO;

/**
 * 评审规则表Service接口。
 */
public interface IReviewRuleService extends IReviewCrudService<ReviewRule> {
    ReviewRuleValidateVO validateRule(Long ruleId);

    int enable(Long ruleId);

    int disable(Long ruleId);

    ReviewRule copy(Long ruleId);

    void validateBindableRule(Long ruleId, ReviewRound round);
}
