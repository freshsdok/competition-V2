package com.teaching.competition.review.service.impl;

import com.teaching.competition.review.domain.ReviewObjectSubmitLog;
import com.teaching.competition.review.mapper.ReviewObjectSubmitLogMapper;
import com.teaching.competition.review.service.IReviewObjectSubmitLogService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 评审对象提交状态日志Service业务层处理。
 */
@Service
public class ReviewObjectSubmitLogServiceImpl extends AbstractReviewCrudService<ReviewObjectSubmitLog> implements IReviewObjectSubmitLogService {
    @Autowired
    private ReviewObjectSubmitLogMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewObjectSubmitLog> mapper() {
        return mapper;
    }
}
