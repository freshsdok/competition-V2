package com.teaching.competition.review.service.impl;

import com.teaching.competition.review.domain.ReviewResultPublishLog;
import com.teaching.competition.review.mapper.ReviewResultPublishLogMapper;
import com.teaching.competition.review.service.IReviewResultPublishLogService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 结果发布日志表Service业务层处理。
 */
@Service
public class ReviewResultPublishLogServiceImpl extends AbstractReviewCrudService<ReviewResultPublishLog> implements IReviewResultPublishLogService {
    @Autowired
    private ReviewResultPublishLogMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewResultPublishLog> mapper() {
        return mapper;
    }
}
