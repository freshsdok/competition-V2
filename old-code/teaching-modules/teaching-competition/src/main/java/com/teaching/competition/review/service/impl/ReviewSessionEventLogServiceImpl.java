package com.teaching.competition.review.service.impl;

import com.teaching.competition.review.domain.ReviewSessionEventLog;
import com.teaching.competition.review.mapper.ReviewSessionEventLogMapper;
import com.teaching.competition.review.service.IReviewSessionEventLogService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 现场事件日志表Service业务层处理。
 */
@Service
public class ReviewSessionEventLogServiceImpl extends AbstractReviewCrudService<ReviewSessionEventLog> implements IReviewSessionEventLogService {
    @Autowired
    private ReviewSessionEventLogMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewSessionEventLog> mapper() {
        return mapper;
    }
}
