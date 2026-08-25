package com.teaching.competition.review.service.impl;

import com.teaching.competition.review.domain.ReviewObjectExternalRef;
import com.teaching.competition.review.mapper.ReviewObjectExternalRefMapper;
import com.teaching.competition.review.service.IReviewObjectExternalRefService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 外部业务关联表Service业务层处理。
 */
@Service
public class ReviewObjectExternalRefServiceImpl extends AbstractReviewCrudService<ReviewObjectExternalRef> implements IReviewObjectExternalRefService {
    @Autowired
    private ReviewObjectExternalRefMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewObjectExternalRef> mapper() {
        return mapper;
    }
}
