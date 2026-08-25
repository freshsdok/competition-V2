package com.teaching.competition.review.service.impl;

import com.teaching.competition.review.domain.ReviewActivityUserRole;
import com.teaching.competition.review.mapper.ReviewActivityUserRoleMapper;
import com.teaching.competition.review.service.IReviewActivityUserRoleService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 活动内用户角色表Service业务层处理。
 */
@Service
public class ReviewActivityUserRoleServiceImpl extends AbstractReviewCrudService<ReviewActivityUserRole> implements IReviewActivityUserRoleService {
    @Autowired
    private ReviewActivityUserRoleMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewActivityUserRole> mapper() {
        return mapper;
    }
}
