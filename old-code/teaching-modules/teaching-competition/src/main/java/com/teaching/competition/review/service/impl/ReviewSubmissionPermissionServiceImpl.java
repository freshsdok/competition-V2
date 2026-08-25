package com.teaching.competition.review.service.impl;

import com.teaching.competition.review.domain.ReviewSubmissionPermission;
import com.teaching.competition.review.mapper.ReviewSubmissionPermissionMapper;
import com.teaching.competition.review.service.IReviewSubmissionPermissionService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 填报权限表Service业务层处理。
 */
@Service
public class ReviewSubmissionPermissionServiceImpl extends AbstractReviewCrudService<ReviewSubmissionPermission> implements IReviewSubmissionPermissionService {
    @Autowired
    private ReviewSubmissionPermissionMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewSubmissionPermission> mapper() {
        return mapper;
    }
}
