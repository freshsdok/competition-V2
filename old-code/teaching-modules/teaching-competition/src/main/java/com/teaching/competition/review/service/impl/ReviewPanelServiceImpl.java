package com.teaching.competition.review.service.impl;

import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.review.domain.ReviewPanel;
import com.teaching.competition.review.mapper.ReviewPanelMapper;
import com.teaching.competition.review.service.IReviewPanelService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 专家组表Service业务层处理。
 */
@Service
public class ReviewPanelServiceImpl extends AbstractReviewCrudService<ReviewPanel> implements IReviewPanelService {
    @Autowired
    private ReviewPanelMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewPanel> mapper() {
        return mapper;
    }

    @Override
    public int insert(ReviewPanel entity) {
        if (entity != null && StringUtils.isEmpty(entity.getStatus())) {
            entity.setStatus("ENABLED");
        }
        return super.insert(entity);
    }
}
