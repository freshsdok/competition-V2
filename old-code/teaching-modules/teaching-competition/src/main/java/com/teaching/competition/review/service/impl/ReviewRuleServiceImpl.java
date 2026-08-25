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
import com.teaching.competition.review.enums.ReviewRuleScoreMode;
import com.teaching.competition.review.mapper.ReviewActivityMapper;
import com.teaching.competition.review.mapper.ReviewCriteriaMapper;
import com.teaching.competition.review.mapper.ReviewRoundMapper;
import com.teaching.competition.review.mapper.ReviewRuleMapper;
import com.teaching.competition.review.service.IReviewRuleService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import com.teaching.competition.review.vo.ReviewRuleValidateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 评审规则表Service业务层处理。
 */
@Service
public class ReviewRuleServiceImpl extends AbstractReviewCrudService<ReviewRule> implements IReviewRuleService {
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Autowired
    private ReviewRuleMapper mapper;

    @Autowired
    private ReviewActivityMapper activityMapper;

    @Autowired
    private ReviewRoundMapper roundMapper;

    @Autowired
    private ReviewCriteriaMapper criteriaMapper;

    @Override
    protected ReviewCrudMapper<ReviewRule> mapper() {
        return mapper;
    }

    @Override
    public int insert(ReviewRule entity) {
        if (entity == null) {
            throw new ServiceException("保存评分规则不能为空");
        }
        validateRuleConfigEditable(entity.getActivityId(), entity.getRoundId());
        applyDefaultValue(entity);
        if (ReviewConstants.YES.equals(entity.getEnabled())) {
            throw new ServiceException("新增评分规则请先保存为停用状态，配置指标并校验通过后再启用");
        }
        return super.insert(entity);
    }

    @Override
    public int update(ReviewRule entity) {
        if (entity == null || entity.getId() == null) {
            throw new ServiceException("更新评分规则ID不能为空");
        }
        ReviewRule existed = mapper.selectById(entity.getId());
        if (existed == null) {
            throw new ServiceException("评分规则不存在");
        }
        validateRuleConfigEditable(existed.getActivityId(), existed.getRoundId());
        if (hasSubmittedRecord(entity.getId()) && hasCoreFieldChange(existed, entity)) {
            throw new ServiceException("该评分规则已有提交评分记录，禁止直接修改核心字段，请复制规则后调整");
        }
        if (StringUtils.isNotEmpty(entity.getEnabled()) && !Objects.equals(entity.getEnabled(), existed.getEnabled())) {
            throw new ServiceException("评分规则启用状态请通过启用/停用专用接口变更");
        }
        applyDefaultValue(entity);
        entity.setActivityId(null);
        entity.setRoundId(null);
        entity.setEnabled(null);
        return super.update(entity);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("删除评分规则ID不能为空");
        }
        for (Long id : ids) {
            ReviewRule rule = mapper.selectById(id);
            if (rule == null) {
                throw new ServiceException("评分规则不存在：" + id);
            }
            validateRuleConfigEditable(rule.getActivityId(), rule.getRoundId());
            if (hasSubmittedRecord(id)) {
                throw new ServiceException("评分规则已有提交评分记录，禁止删除，请复制规则后调整");
            }
        }
        return super.deleteByIds(ids);
    }

    @Override
    public ReviewRuleValidateVO validateRule(Long ruleId) {
        ReviewRule rule = requireRule(ruleId);
        ReviewRuleValidateVO result = new ReviewRuleValidateVO();
        result.setScoreMode(StringUtils.isEmpty(rule.getScoreMode()) ? ReviewRuleScoreMode.SUM.getCode() : rule.getScoreMode());
        result.setTotalScore(rule.getTotalScore());

        if (!isScoreMode(result.getScoreMode())) {
            result.addError("评分计算方式无效：" + result.getScoreMode());
            return result;
        }

        List<ReviewCriteria> criteriaList = selectCriteria(ruleId);
        if (criteriaList.isEmpty()) {
            result.addError("评分规则至少需要配置一个评分指标");
        }

        for (ReviewCriteria criteria : criteriaList) {
            if (ReviewConstants.NO.equals(criteria.getEnabled())) {
                result.addWarning("已停用指标不参与校验：" + criteria.getCriteriaName());
                continue;
            }
            validateCriteriaStructure(criteria, result);
            if (!isCountableCriteria(criteria)) {
                continue;
            }
            result.setCountableCriteriaCount(result.getCountableCriteriaCount() + 1);
            BigDecimal maxScore = resolveCriteriaMaxScore(criteria, result);
            result.setMaxScoreSum(result.getMaxScoreSum().add(maxScore));
            if (criteria.getWeight() != null) {
                result.setWeightSum(result.getWeightSum().add(criteria.getWeight()));
                result.setWeightedMaxScore(result.getWeightedMaxScore()
                        .add(maxScore.multiply(criteria.getWeight()).divide(HUNDRED, 4, RoundingMode.HALF_UP)));
            } else if (ReviewRuleScoreMode.WEIGHTED_SUM.getCode().equals(result.getScoreMode())) {
                result.addError("WEIGHTED_SUM 模式下参与计分指标权重不能为空：" + criteria.getCriteriaName());
            }
        }

        validateScoreMode(rule, result);
        result.setMaxScoreSum(result.getMaxScoreSum().setScale(2, RoundingMode.HALF_UP));
        result.setWeightSum(result.getWeightSum().setScale(4, RoundingMode.HALF_UP));
        result.setWeightedMaxScore(result.getWeightedMaxScore().setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    @Override
    public int enable(Long ruleId) {
        ReviewRule rule = requireRule(ruleId);
        validateRuleConfigEditable(rule.getActivityId(), rule.getRoundId());
        requireValidRule(ruleId);
        ReviewRule update = new ReviewRule();
        update.setId(ruleId);
        update.setEnabled(ReviewConstants.YES);
        fillUpdate(update);
        return mapper.update(update);
    }

    @Override
    public int disable(Long ruleId) {
        ReviewRule rule = requireRule(ruleId);
        validateRuleConfigEditable(rule.getActivityId(), rule.getRoundId());
        ReviewRule update = new ReviewRule();
        update.setId(ruleId);
        update.setEnabled(ReviewConstants.NO);
        fillUpdate(update);
        return mapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewRule copy(Long ruleId) {
        ReviewRule source = requireRule(ruleId);
        ReviewRule copied = new ReviewRule();
        copied.setActivityId(source.getActivityId());
        copied.setRoundId(null);
        copied.setRuleName(source.getRuleName() + "副本");
        copied.setScoreMode(source.getScoreMode());
        copied.setTotalScore(source.getTotalScore());
        copied.setAnonymousMode(source.getAnonymousMode());
        copied.setDescription(source.getDescription());
        copied.setEnabled(ReviewConstants.NO);
        copied.setRemark(source.getRemark());
        super.insert(copied);

        for (ReviewCriteria criteria : selectCriteria(ruleId)) {
            ReviewCriteria item = new ReviewCriteria();
            item.setRuleId(copied.getId());
            item.setParentId(criteria.getParentId());
            item.setCriteriaName(criteria.getCriteriaName());
            item.setCriteriaDesc(criteria.getCriteriaDesc());
            item.setScoreType(criteria.getScoreType());
            item.setMinScore(criteria.getMinScore());
            item.setMaxScore(criteria.getMaxScore());
            item.setWeight(criteria.getWeight());
            item.setRequired(criteria.getRequired());
            item.setOptionsJson(criteria.getOptionsJson());
            item.setSortOrder(criteria.getSortOrder());
            item.setEnabled(criteria.getEnabled());
            item.setRemark(criteria.getRemark());
            fillCreateBase(item);
            criteriaMapper.insert(item);
        }
        return copied;
    }

    @Override
    public void validateBindableRule(Long ruleId, ReviewRound round) {
        ReviewRule rule = requireRule(ruleId);
        if (!ReviewConstants.YES.equals(rule.getEnabled())) {
            throw new ServiceException("只有已启用且校验通过的评分规则才能绑定轮次");
        }
        if (round == null || round.getId() == null) {
            throw new ServiceException("评审轮次不存在");
        }
        if (!Objects.equals(rule.getActivityId(), round.getActivityId())) {
            throw new ServiceException("评分规则和评审轮次必须属于同一评审活动");
        }
        if (rule.getRoundId() != null && !Objects.equals(rule.getRoundId(), round.getId())) {
            throw new ServiceException("该评分规则已限定到其他轮次，不能绑定当前轮次");
        }
        if (!isRoundConfigEditable(round.getStatus())) {
            throw new ServiceException("评审轮次已开始、结束或归档，禁止更换评分规则");
        }
        requireValidRule(ruleId);
    }

    private void applyDefaultValue(ReviewRule entity) {
        if (StringUtils.isEmpty(entity.getScoreMode())) {
            entity.setScoreMode(ReviewRuleScoreMode.SUM.getCode());
        }
        if (StringUtils.isEmpty(entity.getAnonymousMode())) {
            entity.setAnonymousMode("NONE");
        }
        if (StringUtils.isEmpty(entity.getEnabled())) {
            entity.setEnabled(ReviewConstants.NO);
        }
    }

    private ReviewRule requireRule(Long ruleId) {
        if (ruleId == null) {
            throw new ServiceException("评分规则ID不能为空");
        }
        ReviewRule rule = mapper.selectById(ruleId);
        if (rule == null) {
            throw new ServiceException("评分规则不存在");
        }
        return rule;
    }

    private List<ReviewCriteria> selectCriteria(Long ruleId) {
        ReviewCriteria query = new ReviewCriteria();
        query.setRuleId(ruleId);
        List<ReviewCriteria> list = criteriaMapper.selectList(query);
        if (list != null) {
            list.sort(Comparator.comparing(ReviewCriteria::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                    .thenComparing(ReviewCriteria::getId, Comparator.nullsLast(Long::compareTo)));
        }
        return list == null ? List.of() : list;
    }

    private void requireValidRule(Long ruleId) {
        ReviewRuleValidateVO validate = validateRule(ruleId);
        if (!Boolean.TRUE.equals(validate.getValid())) {
            throw new ServiceException("评分规则校验未通过：" + String.join("；", validate.getErrors()));
        }
    }

    private boolean hasSubmittedRecord(Long ruleId) {
        return ruleId != null && mapper.countSubmittedRecordsByRuleId(ruleId) > 0;
    }

    private boolean hasCoreFieldChange(ReviewRule existed, ReviewRule entity) {
        return fieldChanged(existed.getRuleName(), entity.getRuleName())
                || fieldChanged(existed.getScoreMode(), entity.getScoreMode())
                || fieldChanged(existed.getTotalScore(), entity.getTotalScore())
                || fieldChanged(existed.getAnonymousMode(), entity.getAnonymousMode())
                || fieldChanged(existed.getRoundId(), entity.getRoundId())
                || fieldChanged(existed.getActivityId(), entity.getActivityId());
    }

    private boolean fieldChanged(Object existed, Object incoming) {
        if (existed instanceof BigDecimal && incoming instanceof BigDecimal) {
            return ((BigDecimal) existed).compareTo((BigDecimal) incoming) != 0;
        }
        return incoming != null && !Objects.equals(existed, incoming);
    }

    private void validateCriteriaStructure(ReviewCriteria criteria, ReviewRuleValidateVO result) {
        if (StringUtils.isEmpty(criteria.getCriteriaName())) {
            result.addError("评分指标名称不能为空");
        }
        if (!isScoreType(criteria.getScoreType())) {
            result.addError("评分指标类型无效：" + criteria.getCriteriaName());
            return;
        }
        if (ReviewCriteriaType.NUMBER.getCode().equals(criteria.getScoreType())) {
            if (criteria.getMinScore() == null) {
                criteria.setMinScore(BigDecimal.ZERO);
            }
            if (criteria.getMaxScore() == null) {
                result.addError("数字评分指标最高分不能为空：" + criteria.getCriteriaName());
            } else if (criteria.getMaxScore().compareTo(criteria.getMinScore()) < 0) {
                result.addError("数字评分指标最高分不能小于最低分：" + criteria.getCriteriaName());
            }
        }
    }

    private boolean isCountableCriteria(ReviewCriteria criteria) {
        return ReviewCriteriaType.NUMBER.getCode().equals(criteria.getScoreType())
                || ReviewCriteriaType.SINGLE_CHOICE.getCode().equals(criteria.getScoreType());
    }

    private BigDecimal resolveCriteriaMaxScore(ReviewCriteria criteria, ReviewRuleValidateVO result) {
        if (ReviewCriteriaType.NUMBER.getCode().equals(criteria.getScoreType())) {
            return criteria.getMaxScore() == null ? BigDecimal.ZERO : criteria.getMaxScore();
        }
        if (ReviewCriteriaType.SINGLE_CHOICE.getCode().equals(criteria.getScoreType())) {
            return parseOptions(criteria, result).maxScore;
        }
        return BigDecimal.ZERO;
    }

    private void validateScoreMode(ReviewRule rule, ReviewRuleValidateVO result) {
        String scoreMode = result.getScoreMode();
        if (ReviewRuleScoreMode.SUM.getCode().equals(scoreMode)) {
            if (rule.getTotalScore() == null) {
                result.addError("SUM 模式下规则总分不能为空");
                return;
            }
            if (result.getMaxScoreSum().compareTo(rule.getTotalScore()) != 0) {
                result.addError("SUM 模式下可计分指标最高分合计必须等于规则总分，当前合计 "
                        + result.getMaxScoreSum() + "，规则总分 " + rule.getTotalScore());
            }
            return;
        }
        if (ReviewRuleScoreMode.WEIGHTED_SUM.getCode().equals(scoreMode)) {
            if (rule.getTotalScore() == null) {
                result.addError("WEIGHTED_SUM 模式下规则总分不能为空");
            }
            if (result.getWeightSum().compareTo(HUNDRED) != 0) {
                result.addError("WEIGHTED_SUM 模式下参与计分指标权重合计必须为 100，当前合计 " + result.getWeightSum());
            }
            if (rule.getTotalScore() != null) {
                int compare = result.getWeightedMaxScore().compareTo(rule.getTotalScore());
                if (compare > 0) {
                    result.addError("WEIGHTED_SUM 模式下理论最高分不能超过规则总分，当前理论最高分 "
                            + result.getWeightedMaxScore() + "，规则总分 " + rule.getTotalScore());
                } else if (compare < 0) {
                    result.addWarning("WEIGHTED_SUM 模式下理论最高分低于规则总分，请确认是否符合预期");
                }
            }
            return;
        }
        if (ReviewRuleScoreMode.AVERAGE.getCode().equals(scoreMode)) {
            result.addWarning("AVERAGE 模式下 total_score 仅作为参考，最终分为可计分指标平均值");
        }
    }

    private OptionParseResult parseOptions(ReviewCriteria criteria, ReviewRuleValidateVO result) {
        OptionParseResult parse = new OptionParseResult();
        if (StringUtils.isEmpty(criteria.getOptionsJson())) {
            result.addError("单选评分指标至少需要配置一个选项：" + criteria.getCriteriaName());
            return parse;
        }
        try {
            Object parsed = JSON.parse(criteria.getOptionsJson());
            if (!(parsed instanceof JSONArray)) {
                result.addError("单选评分指标选项必须为数组 JSON：" + criteria.getCriteriaName());
                return parse;
            }
            JSONArray array = (JSONArray) parsed;
            if (array.isEmpty()) {
                result.addError("单选评分指标至少需要配置一个选项：" + criteria.getCriteriaName());
                return parse;
            }
            Set<String> values = new HashSet<>();
            for (Object item : array) {
                if (!(item instanceof JSONObject)) {
                    result.addError("单选评分指标选项格式无效：" + criteria.getCriteriaName());
                    continue;
                }
                JSONObject object = (JSONObject) item;
                String value = object.getString("value");
                if (StringUtils.isEmpty(value)) {
                    result.addError("单选评分指标选项值不能为空：" + criteria.getCriteriaName());
                } else if (!values.add(value)) {
                    result.addError("单选评分指标选项值不能重复：" + criteria.getCriteriaName() + " - " + value);
                }
                BigDecimal score = readScore(object);
                if (score == null) {
                    result.addError("单选评分指标选项分值必须为数字：" + criteria.getCriteriaName());
                } else if (score.compareTo(parse.maxScore) > 0) {
                    parse.maxScore = score;
                }
            }
        } catch (Exception ex) {
            result.addError("单选评分指标选项 JSON 解析失败：" + criteria.getCriteriaName());
        }
        return parse;
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

    private boolean isScoreMode(String scoreMode) {
        return ReviewRuleScoreMode.SUM.getCode().equals(scoreMode)
                || ReviewRuleScoreMode.WEIGHTED_SUM.getCode().equals(scoreMode)
                || ReviewRuleScoreMode.AVERAGE.getCode().equals(scoreMode);
    }

    private boolean isScoreType(String scoreType) {
        return ReviewCriteriaType.NUMBER.getCode().equals(scoreType)
                || ReviewCriteriaType.SINGLE_CHOICE.getCode().equals(scoreType)
                || ReviewCriteriaType.TEXT.getCode().equals(scoreType);
    }

    private void validateRuleConfigEditable(Long activityId, Long roundId) {
        if (activityId != null) {
            ReviewActivity activity = activityMapper.selectById(activityId);
            if (activity != null && !isActivityConfigEditable(activity.getStatus())) {
                throw new ServiceException("评审活动已进入评审、汇总、发布或归档阶段，不能修改评分规则");
            }
        }
        if (roundId != null) {
            ReviewRound round = roundMapper.selectById(roundId);
            if (round != null && !isRoundConfigEditable(round.getStatus())) {
                throw new ServiceException("评审轮次已开始、结束或归档，不能修改评分规则");
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

    private static class OptionParseResult {
        private BigDecimal maxScore = BigDecimal.ZERO;
    }
}
