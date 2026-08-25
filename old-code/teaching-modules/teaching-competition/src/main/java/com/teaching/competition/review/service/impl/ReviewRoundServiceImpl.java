package com.teaching.competition.review.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.review.domain.ReviewRound;
import com.teaching.competition.review.enums.ReviewRoundStatus;
import com.teaching.competition.review.enums.ReviewRoundType;
import com.teaching.competition.review.mapper.ReviewRoundMapper;
import com.teaching.competition.review.service.IReviewRuleService;
import com.teaching.competition.review.service.IReviewRoundService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 评审轮次表Service业务层处理。
 */
@Service
public class ReviewRoundServiceImpl extends AbstractReviewCrudService<ReviewRound> implements IReviewRoundService {
    @Autowired
    private ReviewRoundMapper mapper;

    @Autowired
    private IReviewRuleService reviewRuleService;

    @Override
    protected ReviewCrudMapper<ReviewRound> mapper() {
        return mapper;
    }

    @Override
    public int insert(ReviewRound entity) {
        if (StringUtils.isEmpty(entity.getRoundType())) {
            entity.setRoundType(ReviewRoundType.MATERIAL_REVIEW.getCode());
        }
        if (StringUtils.isEmpty(entity.getStatus())) {
            entity.setStatus(ReviewRoundStatus.DRAFT.getCode());
        }
        return super.insert(entity);
    }

    @Override
    public int update(ReviewRound entity) {
        if (entity == null || entity.getId() == null) {
            throw new ServiceException("更新轮次ID不能为空");
        }
        ReviewRound existed = mapper.selectById(entity.getId());
        if (existed == null) {
            throw new ServiceException("评审轮次不存在");
        }
        if (!isRoundCrudEditable(existed.getStatus())) {
            throw new ServiceException("评审轮次已开始、结束或归档，不能通过基础接口修改");
        }
        if (StringUtils.isNotEmpty(entity.getStatus()) && !entity.getStatus().equals(existed.getStatus())) {
            throw new ServiceException("评审轮次状态必须通过专用流程变更，不能通过基础编辑接口直接修改");
        }
        if (entity.getRuleId() != null && !Objects.equals(entity.getRuleId(), existed.getRuleId())) {
            reviewRuleService.validateBindableRule(entity.getRuleId(), existed);
        }
        entity.setActivityId(null);
        entity.setStatus(null);
        return super.update(entity);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("删除轮次ID不能为空");
        }
        for (Long id : ids) {
            ReviewRound round = mapper.selectById(id);
            if (round == null) {
                throw new ServiceException("评审轮次不存在：" + id);
            }
            if (!isRoundCrudEditable(round.getStatus())) {
                throw new ServiceException("评审轮次已开始、结束或归档，不能通过基础接口删除");
            }
        }
        return super.deleteByIds(ids);
    }

    @Override
    public int bindRule(Long roundId, Long ruleId) {
        if (roundId == null) {
            throw new ServiceException("评审轮次ID不能为空");
        }
        if (ruleId == null) {
            throw new ServiceException("评分规则ID不能为空");
        }
        ReviewRound round = mapper.selectById(roundId);
        if (round == null) {
            throw new ServiceException("评审轮次不存在");
        }
        reviewRuleService.validateBindableRule(ruleId, round);
        ReviewRound update = new ReviewRound();
        update.setId(roundId);
        update.setRuleId(ruleId);
        fillUpdate(update);
        return mapper.update(update);
    }

    private boolean isRoundCrudEditable(String status) {
        return StringUtils.isEmpty(status)
                || ReviewRoundStatus.DRAFT.getCode().equals(status)
                || ReviewRoundStatus.NOT_STARTED.getCode().equals(status)
                || ReviewRoundStatus.DISABLED.getCode().equals(status);
    }
}
