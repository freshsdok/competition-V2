package com.teaching.competition.review.service.impl;

import com.teaching.competition.review.domain.ReviewAuditLog;
import com.teaching.competition.review.mapper.ReviewAuditLogMapper;
import com.teaching.competition.review.service.IReviewAuditLogService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 审计日志表Service业务层处理。
 */
@Service
public class ReviewAuditLogServiceImpl extends AbstractReviewCrudService<ReviewAuditLog> implements IReviewAuditLogService {
    @Autowired
    private ReviewAuditLogMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewAuditLog> mapper() {
        return mapper;
    }
}
