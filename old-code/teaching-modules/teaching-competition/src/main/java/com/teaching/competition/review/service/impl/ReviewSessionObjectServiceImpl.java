package com.teaching.competition.review.service.impl;

import com.teaching.competition.review.domain.ReviewSessionObject;
import com.teaching.competition.review.mapper.ReviewSessionObjectMapper;
import com.teaching.competition.review.service.IReviewSessionObjectService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 现场评审对象顺序表Service业务层处理。
 */
@Service
public class ReviewSessionObjectServiceImpl extends AbstractReviewCrudService<ReviewSessionObject> implements IReviewSessionObjectService {
    @Autowired
    private ReviewSessionObjectMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewSessionObject> mapper() {
        return mapper;
    }
}
