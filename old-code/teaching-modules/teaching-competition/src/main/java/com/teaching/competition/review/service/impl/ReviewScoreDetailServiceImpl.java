package com.teaching.competition.review.service.impl;

import com.teaching.competition.review.domain.ReviewScoreDetail;
import com.teaching.competition.review.mapper.ReviewScoreDetailMapper;
import com.teaching.competition.review.service.IReviewScoreDetailService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 评分明细表Service业务层处理。
 */
@Service
public class ReviewScoreDetailServiceImpl extends AbstractReviewCrudService<ReviewScoreDetail> implements IReviewScoreDetailService {
    @Autowired
    private ReviewScoreDetailMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewScoreDetail> mapper() {
        return mapper;
    }
}
