package com.teaching.competition.review.service.impl;

import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.review.domain.ReviewerProfile;
import com.teaching.competition.review.mapper.ReviewerProfileMapper;
import com.teaching.competition.review.service.IReviewerProfileService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 评审人画像表Service业务层处理。
 */
@Service
public class ReviewerProfileServiceImpl extends AbstractReviewCrudService<ReviewerProfile> implements IReviewerProfileService {
    @Autowired
    private ReviewerProfileMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewerProfile> mapper() {
        return mapper;
    }

    @Override
    public int insert(ReviewerProfile entity) {
        if (StringUtils.isEmpty(entity.getStatus())) {
            entity.setStatus("ENABLED");
        }
        return super.insert(entity);
    }
}
