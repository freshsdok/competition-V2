package com.teaching.competition.review.service.impl;

import com.teaching.competition.review.domain.ReviewObjectMaterial;
import com.teaching.competition.review.mapper.ReviewObjectMaterialMapper;
import com.teaching.competition.review.service.IReviewObjectMaterialService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 评审材料表Service业务层处理。
 */
@Service
public class ReviewObjectMaterialServiceImpl extends AbstractReviewCrudService<ReviewObjectMaterial> implements IReviewObjectMaterialService {
    @Autowired
    private ReviewObjectMaterialMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewObjectMaterial> mapper() {
        return mapper;
    }
}
