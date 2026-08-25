package com.teaching.competition.review.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.review.constant.ReviewConstants;
import com.teaching.competition.review.domain.ReviewActivity;
import com.teaching.competition.review.domain.ReviewCriteria;
import com.teaching.competition.review.domain.ReviewRound;
import com.teaching.competition.review.domain.ReviewRule;
import com.teaching.competition.review.enums.ReviewActivityStatus;
import com.teaching.competition.review.enums.ReviewCriteriaType;
import com.teaching.competition.review.enums.ReviewRoundStatus;
import com.teaching.competition.review.mapper.ReviewActivityMapper;
import com.teaching.competition.review.mapper.ReviewCriteriaMapper;
import com.teaching.competition.review.mapper.ReviewRoundMapper;
import com.teaching.competition.review.mapper.ReviewRuleMapper;
import com.teaching.competition.review.service.IReviewCriteriaService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * 评分指标表Service业务层处理。
 */
@Service
public class ReviewCriteriaServiceImpl extends AbstractReviewCrudService<ReviewCriteria> implements IReviewCriteriaService {
    @Autowired
    private ReviewCriteriaMapper mapper;

    @Autowired
    private ReviewRuleMapper ruleMapper;

    @Autowired
    private ReviewActivityMapper activityMapper;

    @Autowired
    private ReviewRoundMapper roundMapper;

    @Override
    protected ReviewCrudMapper<ReviewCriteria> mapper() {
        return mapper;
    }

    @Override
    public int insert(ReviewCriteria entity) {
        if (entity == null) {
            throw new ServiceException("保存评分指标不能为空");
        }
        validateCriteriaConfigEditable(entity.getRuleId());
        validateRuleNotUsed(entity.getRuleId());
        if (StringUtils.isEmpty(entity.getScoreType())) {
            entity.setScoreType(ReviewCriteriaType.NUMBER.getCode());
        }
        if (StringUtils.isEmpty(entity.getRequired())) {
            entity.setRequired(ReviewConstants.YES);
        }
        if (StringUtils.isEmpty(entity.getEnabled())) {
            entity.setEnabled(ReviewConstants.YES);
        }
        validateCriteria(entity);
        return super.insert(entity);
    }

    @Override
    public int update(ReviewCriteria entity) {
        if (entity == null || entity.getId() == null) {
            throw new ServiceException("更新评分指标ID不能为空");
        }
        ReviewCriteria existed = mapper.selectById(entity.getId());
        if (existed == null) {
            throw new ServiceException("评分指标不存在");
        }
        validateCriteriaConfigEditable(existed.getRuleId());
        validateRuleNotUsed(existed.getRuleId());
        entity.setRuleId(existed.getRuleId());
        mergeForValidate(existed, entity);
        validateCriteria(entity);
        entity.setRuleId(null);
        return super.update(entity);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("删除评分指标ID不能为空");
        }
        for (Long id : ids) {
            ReviewCriteria criteria = mapper.selectById(id);
            if (criteria == null) {
                throw new ServiceException("评分指标不存在：" + id);
            }
            validateCriteriaConfigEditable(criteria.getRuleId());
            validateRuleNotUsed(criteria.getRuleId());
        }
        return super.deleteByIds(ids);
    }

    private void validateCriteria(ReviewCriteria criteria) {
        if (StringUtils.isEmpty(criteria.getCriteriaName())) {
            throw new ServiceException("评分指标名称不能为空");
        }
        if (!isScoreType(criteria.getScoreType())) {
            throw new ServiceException("评分指标类型无效");
        }
        if (ReviewCriteriaType.NUMBER.getCode().equals(criteria.getScoreType())) {
            if (criteria.getMinScore() == null) {
                criteria.setMinScore(BigDecimal.ZERO);
            }
            if (criteria.getMaxScore() == null) {
                throw new ServiceException("数字评分指标最高分不能为空");
            }
            if (criteria.getMaxScore().compareTo(criteria.getMinScore()) < 0) {
                throw new ServiceException("数字评分指标最高分不能小于最低分");
            }
        }
        if (ReviewCriteriaType.SINGLE_CHOICE.getCode().equals(criteria.getScoreType())) {
            validateOptionsJson(criteria.getOptionsJson());
        }
    }

    private void validateOptionsJson(String optionsJson) {
        if (StringUtils.isEmpty(optionsJson)) {
            throw new ServiceException("单选评分指标至少需要配置一个选项");
        }
        try {
            Object parsed = JSON.parse(optionsJson);
            if (!(parsed instanceof JSONArray)) {
                throw new ServiceException("单选评分指标选项必须为数组 JSON");
            }
            JSONArray array = (JSONArray) parsed;
            if (array.isEmpty()) {
                throw new ServiceException("单选评分指标至少需要配置一个选项");
            }
            Set<String> values = new HashSet<>();
            for (Object item : array) {
                if (!(item instanceof JSONObject)) {
                    throw new ServiceException("单选评分指标选项格式无效");
                }
                JSONObject object = (JSONObject) item;
                String value = object.getString("value");
                if (StringUtils.isEmpty(value)) {
                    throw new ServiceException("单选评分指标选项值不能为空");
                }
                if (!values.add(value)) {
                    throw new ServiceException("单选评分指标选项值不能重复：" + value);
                }
                if (readScore(object) == null) {
                    throw new ServiceException("单选评分指标选项分值必须为数字：" + value);
                }
            }
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException("单选评分指标选项 JSON 解析失败");
        }
    }

    private BigDecimal readScore(JSONObject object) {
        Object score = object.get("score");
        if (score == null) {
            score = object.get("scoreValue");
        }
        if (score instanceof Number) {
            return new BigDecimal(String.valueOf(score));
        }
        try {
            return score == null ? null : new BigDecimal(String.valueOf(score));
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean isScoreType(String scoreType) {
        return ReviewCriteriaType.NUMBER.getCode().equals(scoreType)
                || ReviewCriteriaType.SINGLE_CHOICE.getCode().equals(scoreType)
                || ReviewCriteriaType.TEXT.getCode().equals(scoreType);
    }

    private void mergeForValidate(ReviewCriteria existed, ReviewCriteria entity) {
        if (StringUtils.isEmpty(entity.getCriteriaName())) {
            entity.setCriteriaName(existed.getCriteriaName());
        }
        if (StringUtils.isEmpty(entity.getScoreType())) {
            entity.setScoreType(existed.getScoreType());
        }
        if (entity.getMinScore() == null) {
            entity.setMinScore(existed.getMinScore());
        }
        if (entity.getMaxScore() == null) {
            entity.setMaxScore(existed.getMaxScore());
        }
        if (entity.getOptionsJson() == null) {
            entity.setOptionsJson(existed.getOptionsJson());
        }
    }

    private void validateRuleNotUsed(Long ruleId) {
        if (ruleId != null && ruleMapper.countSubmittedRecordsByRuleId(ruleId) > 0) {
            throw new ServiceException("评分规则已有提交评分记录，禁止修改评分指标，请复制规则后调整");
        }
    }

    private void validateCriteriaConfigEditable(Long ruleId) {
        if (ruleId == null) {
            return;
        }
        ReviewRule rule = ruleMapper.selectById(ruleId);
        if (rule == null) {
            throw new ServiceException("评分规则不存在");
        }
        if (rule.getActivityId() != null) {
            ReviewActivity activity = activityMapper.selectById(rule.getActivityId());
            if (activity != null && !isActivityConfigEditable(activity.getStatus())) {
                throw new ServiceException("评审活动已进入评审、汇总、发布或归档阶段，不能修改评分指标");
            }
        }
        if (rule.getRoundId() != null) {
            ReviewRound round = roundMapper.selectById(rule.getRoundId());
            if (round != null && !isRoundConfigEditable(round.getStatus())) {
                throw new ServiceException("评审轮次已开始、结束或归档，不能修改评分指标");
            }
        }
    }

    private boolean isActivityConfigEditable(String status) {
        return StringUtils.isEmpty(status)
                || ReviewActivityStatus.DRAFT.getCode().equals(status)
                || ReviewActivityStatus.SUBMITTING.getCode().equals(status)
                || ReviewActivityStatus.SUBMIT_CLOSED.getCode().equals(status);
    }

    private boolean isRoundConfigEditable(String status) {
        return StringUtils.isEmpty(status)
                || ReviewRoundStatus.DRAFT.getCode().equals(status)
                || ReviewRoundStatus.NOT_STARTED.getCode().equals(status);
    }
}
