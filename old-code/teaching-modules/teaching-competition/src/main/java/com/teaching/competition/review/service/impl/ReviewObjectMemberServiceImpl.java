package com.teaching.competition.review.service.impl;

import com.teaching.competition.review.domain.ReviewObjectMember;
import com.teaching.competition.review.mapper.ReviewObjectMemberMapper;
import com.teaching.competition.review.service.IReviewObjectMemberService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 评审对象成员表Service业务层处理。
 */
@Service
public class ReviewObjectMemberServiceImpl extends AbstractReviewCrudService<ReviewObjectMember> implements IReviewObjectMemberService {
    @Autowired
    private ReviewObjectMemberMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewObjectMember> mapper() {
        return mapper;
    }
}
