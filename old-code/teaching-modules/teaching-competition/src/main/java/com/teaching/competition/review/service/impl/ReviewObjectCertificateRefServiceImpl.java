package com.teaching.competition.review.service.impl;

import com.teaching.competition.review.domain.ReviewObjectCertificateRef;
import com.teaching.competition.review.mapper.ReviewObjectCertificateRefMapper;
import com.teaching.competition.review.service.IReviewObjectCertificateRefService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 评审对象参赛证映射表Service业务层处理。
 */
@Service
public class ReviewObjectCertificateRefServiceImpl extends AbstractReviewCrudService<ReviewObjectCertificateRef> implements IReviewObjectCertificateRefService {
    @Autowired
    private ReviewObjectCertificateRefMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewObjectCertificateRef> mapper() {
        return mapper;
    }
}
