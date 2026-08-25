package com.teaching.competition.review.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.review.domain.ReviewActivity;
import com.teaching.competition.review.enums.ReviewActivityStatus;
import com.teaching.competition.review.enums.ReviewObjectType;
import com.teaching.competition.review.enums.ReviewPublishMode;
import com.teaching.competition.review.enums.ReviewSubmissionMode;
import com.teaching.competition.review.mapper.ReviewActivityMapper;
import com.teaching.competition.review.service.IReviewActivityService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 评审活动表Service业务层处理。
 */
@Service
public class ReviewActivityServiceImpl extends AbstractReviewCrudService<ReviewActivity> implements IReviewActivityService {
    @Autowired
    private ReviewActivityMapper mapper;

    @Override
    protected ReviewCrudMapper<ReviewActivity> mapper() {
        return mapper;
    }

    @Override
    public int insert(ReviewActivity entity) {
        if (StringUtils.isEmpty(entity.getStatus())) {
            entity.setStatus(ReviewActivityStatus.DRAFT.getCode());
        }
        if (StringUtils.isEmpty(entity.getObjectType())) {
            entity.setObjectType(ReviewObjectType.PROJECT.getCode());
        }
        if (StringUtils.isEmpty(entity.getSubmissionMode())) {
            entity.setSubmissionMode(ReviewSubmissionMode.BUSINESS_IMPORTED.getCode());
        }
        if (StringUtils.isEmpty(entity.getAnonymousMode())) {
            entity.setAnonymousMode("NONE");
        }
        if (StringUtils.isEmpty(entity.getResultPublishMode())) {
            entity.setResultPublishMode(ReviewPublishMode.NONE.getCode());
        }
        return super.insert(entity);
    }

    @Override
    public int update(ReviewActivity entity) {
        if (entity == null || entity.getId() == null) {
            throw new ServiceException("更新活动ID不能为空");
        }
        ReviewActivity existed = mapper.selectById(entity.getId());
        if (existed == null) {
            throw new ServiceException("评审活动不存在");
        }
        if (!isActivityCrudEditable(existed.getStatus())) {
            throw new ServiceException("评审活动已进入评审、汇总、发布或归档阶段，不能修改");
        }
//        if (StringUtils.isNotEmpty(entity.getStatus()) && !entity.getStatus().equals(existed.getStatus())) {
//            throw new ServiceException("评审活动状态必须通过专用流程变更，不能通过基础编辑接口直接修改");
//        }
        entity.setStatus(null);
        return super.update(entity);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("删除活动ID不能为空");
        }
        for (Long id : ids) {
            ReviewActivity activity = mapper.selectById(id);
            if (activity == null) {
                throw new ServiceException("评审活动不存在：" + id);
            }
            if (!ReviewActivityStatus.DRAFT.getCode().equals(activity.getStatus())
                    && !ReviewActivityStatus.DISABLED.getCode().equals(activity.getStatus())) {
                throw new ServiceException("只能删除草稿或停用状态的评审活动");
            }
        }
        return super.deleteByIds(ids);
    }

    private boolean isActivityCrudEditable(String status) {
        return StringUtils.isEmpty(status)
                || ReviewActivityStatus.DRAFT.getCode().equals(status)
                || ReviewActivityStatus.SUBMITTING.getCode().equals(status)
                || ReviewActivityStatus.SUBMIT_CLOSED.getCode().equals(status)
                || ReviewActivityStatus.DISABLED.getCode().equals(status);
    }
}
