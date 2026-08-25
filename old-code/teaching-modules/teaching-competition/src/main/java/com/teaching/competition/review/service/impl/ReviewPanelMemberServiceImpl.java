package com.teaching.competition.review.service.impl;

import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.review.domain.ReviewPanelMember;
import com.teaching.competition.review.mapper.ReviewPanelMemberMapper;
import com.teaching.competition.review.service.IReviewPanelMemberService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 专家组成员表Service业务层处理。
 */
@Service
public class ReviewPanelMemberServiceImpl extends AbstractReviewCrudService<ReviewPanelMember> implements IReviewPanelMemberService {
    @Autowired
    private ReviewPanelMemberMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewPanelMember> mapper() {
        return mapper;
    }

    @Override
    public int insert(ReviewPanelMember entity) {
        if (entity != null && StringUtils.isEmpty(entity.getMemberRole())) {
            entity.setMemberRole("MEMBER");
        }
        if (entity != null && StringUtils.isEmpty(entity.getStatus())) {
            entity.setStatus("ENABLED");
        }
        return super.insert(entity);
    }
}
