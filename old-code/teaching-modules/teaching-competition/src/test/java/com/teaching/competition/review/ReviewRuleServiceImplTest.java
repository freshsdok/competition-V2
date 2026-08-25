package com.teaching.competition.review;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.competition.review.constant.ReviewConstants;
import com.teaching.competition.review.domain.ReviewCriteria;
import com.teaching.competition.review.domain.ReviewRound;
import com.teaching.competition.review.domain.ReviewRule;
import com.teaching.competition.review.enums.ReviewCriteriaType;
import com.teaching.competition.review.enums.ReviewRoundStatus;
import com.teaching.competition.review.enums.ReviewRuleScoreMode;
import com.teaching.competition.review.mapper.ReviewActivityMapper;
import com.teaching.competition.review.mapper.ReviewCriteriaMapper;
import com.teaching.competition.review.mapper.ReviewRoundMapper;
import com.teaching.competition.review.mapper.ReviewRuleMapper;
import com.teaching.competition.review.service.impl.ReviewRuleServiceImpl;
import com.teaching.competition.review.vo.ReviewRuleValidateVO;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 评分表配置增强测试。
 */
public class ReviewRuleServiceImplTest {

    @Test
    public void sumModeValidatesMaxScoreEqualsTotalScore() throws Exception {
        Fixture fixture = createFixture(rule(ReviewRuleScoreMode.SUM.getCode(), "100", ReviewConstants.NO), numberCriteria("60", null), numberCriteria("40", null));

        ReviewRuleValidateVO passed = fixture.service.validateRule(90L);

        Assert.assertTrue(passed.getValid());
        Assert.assertEquals(new BigDecimal("100.00"), passed.getMaxScoreSum());

        fixture.criteriaList.get(1).setMaxScore(new BigDecimal("30"));
        ReviewRuleValidateVO failed = fixture.service.validateRule(90L);

        Assert.assertFalse(failed.getValid());
        Assert.assertTrue(failed.getErrors().get(0).contains("最高分合计必须等于规则总分"));
    }

    @Test
    public void weightedModeRequiresWeightSumOneHundred() throws Exception {
        Fixture fixture = createFixture(rule(ReviewRuleScoreMode.WEIGHTED_SUM.getCode(), "100", ReviewConstants.NO),
                numberCriteria("100", "40"), numberCriteria("100", "60"));

        ReviewRuleValidateVO passed = fixture.service.validateRule(90L);

        Assert.assertTrue(passed.getValid());
        Assert.assertEquals(new BigDecimal("100.0000"), passed.getWeightSum());
        Assert.assertEquals(new BigDecimal("100.00"), passed.getWeightedMaxScore());

        fixture.criteriaList.get(1).setWeight(new BigDecimal("50"));
        ReviewRuleValidateVO failed = fixture.service.validateRule(90L);

        Assert.assertFalse(failed.getValid());
        Assert.assertTrue(failed.getErrors().get(0).contains("权重合计必须为 100"));
    }

    @Test
    public void singleChoiceAndTextCriteriaCanValidate() throws Exception {
        Fixture fixture = createFixture(rule(ReviewRuleScoreMode.SUM.getCode(), "30", ReviewConstants.NO),
                choiceCriteria(), textCriteria());

        ReviewRuleValidateVO result = fixture.service.validateRule(90L);

        Assert.assertTrue(result.getValid());
        Assert.assertEquals(new BigDecimal("30.00"), result.getMaxScoreSum());
    }

    @Test
    public void enableRunsValidation() throws Exception {
        Fixture fixture = createFixture(rule(ReviewRuleScoreMode.WEIGHTED_SUM.getCode(), "100", ReviewConstants.NO),
                numberCriteria("100", "40"), numberCriteria("100", "60"));

        fixture.service.enable(90L);

        ArgumentCaptor<ReviewRule> captor = ArgumentCaptor.forClass(ReviewRule.class);
        verify(fixture.ruleMapper).update(captor.capture());
        Assert.assertEquals(ReviewConstants.YES, captor.getValue().getEnabled());
    }

    @Test
    public void copyRuleCopiesCriteriaAndOptionsJson() throws Exception {
        Fixture fixture = createFixture(rule(ReviewRuleScoreMode.SUM.getCode(), "30", ReviewConstants.NO),
                choiceCriteria(), textCriteria());
        when(fixture.ruleMapper.insert(any(ReviewRule.class))).thenAnswer(invocation -> {
            ReviewRule copied = invocation.getArgument(0);
            copied.setId(91L);
            return 1;
        });

        ReviewRule copied = fixture.service.copy(90L);

        Assert.assertEquals(Long.valueOf(91L), copied.getId());
        Assert.assertEquals(ReviewConstants.NO, copied.getEnabled());
        ArgumentCaptor<ReviewCriteria> criteriaCaptor = ArgumentCaptor.forClass(ReviewCriteria.class);
        verify(fixture.criteriaMapper, times(2)).insert(criteriaCaptor.capture());
        Assert.assertEquals(91L, criteriaCaptor.getAllValues().get(0).getRuleId().longValue());
        Assert.assertEquals(choiceCriteria().getOptionsJson(), criteriaCaptor.getAllValues().get(0).getOptionsJson());
    }

    @Test(expected = ServiceException.class)
    public void usedRuleCannotBeDeleted() throws Exception {
        Fixture fixture = createFixture(rule(ReviewRuleScoreMode.SUM.getCode(), "100", ReviewConstants.NO), numberCriteria("100", null));
        when(fixture.ruleMapper.countSubmittedRecordsByRuleId(90L)).thenReturn(1);

        fixture.service.deleteByIds(new Long[]{90L});
    }

    @Test
    public void enabledRuleCanBindToNotStartedRound() throws Exception {
        Fixture fixture = createFixture(rule(ReviewRuleScoreMode.SUM.getCode(), "100", ReviewConstants.YES), numberCriteria("100", null));
        ReviewRound round = new ReviewRound();
        round.setId(5L);
        round.setActivityId(1L);
        round.setStatus(ReviewRoundStatus.NOT_STARTED.getCode());

        fixture.service.validateBindableRule(90L, round);
    }

    private static Fixture createFixture(ReviewRule rule, ReviewCriteria... criteria) throws Exception {
        Fixture fixture = new Fixture();
        fixture.service = new TestReviewRuleServiceImpl();
        fixture.ruleMapper = mock(ReviewRuleMapper.class);
        fixture.criteriaMapper = mock(ReviewCriteriaMapper.class);
        fixture.activityMapper = mock(ReviewActivityMapper.class);
        fixture.roundMapper = mock(ReviewRoundMapper.class);
        fixture.rule = rule;
        fixture.criteriaList = new ArrayList<>(Arrays.asList(criteria));

        setField(fixture.service, "mapper", fixture.ruleMapper);
        setField(fixture.service, "criteriaMapper", fixture.criteriaMapper);
        setField(fixture.service, "activityMapper", fixture.activityMapper);
        setField(fixture.service, "roundMapper", fixture.roundMapper);

        when(fixture.ruleMapper.selectById(90L)).thenReturn(rule);
        when(fixture.criteriaMapper.selectList(any())).thenAnswer(invocation -> fixture.criteriaList);
        return fixture;
    }

    private static ReviewRule rule(String scoreMode, String totalScore, String enabled) {
        ReviewRule rule = new ReviewRule();
        rule.setId(90L);
        rule.setActivityId(1L);
        rule.setRuleName("评分表");
        rule.setScoreMode(scoreMode);
        rule.setTotalScore(new BigDecimal(totalScore));
        rule.setAnonymousMode("NONE");
        rule.setEnabled(enabled);
        return rule;
    }

    private static ReviewCriteria numberCriteria(String maxScore, String weight) {
        ReviewCriteria criteria = new ReviewCriteria();
        criteria.setId(System.nanoTime());
        criteria.setRuleId(90L);
        criteria.setCriteriaName("数字评分" + maxScore);
        criteria.setScoreType(ReviewCriteriaType.NUMBER.getCode());
        criteria.setMinScore(BigDecimal.ZERO);
        criteria.setMaxScore(new BigDecimal(maxScore));
        criteria.setWeight(weight == null ? null : new BigDecimal(weight));
        criteria.setRequired(ReviewConstants.YES);
        criteria.setEnabled(ReviewConstants.YES);
        criteria.setSortOrder(1);
        return criteria;
    }

    private static ReviewCriteria choiceCriteria() {
        ReviewCriteria criteria = new ReviewCriteria();
        criteria.setId(101L);
        criteria.setRuleId(90L);
        criteria.setCriteriaName("单选赋分");
        criteria.setScoreType(ReviewCriteriaType.SINGLE_CHOICE.getCode());
        criteria.setOptionsJson("[{\"label\":\"优秀\",\"value\":\"A\",\"score\":30},{\"label\":\"一般\",\"value\":\"B\",\"score\":10}]");
        criteria.setRequired(ReviewConstants.YES);
        criteria.setEnabled(ReviewConstants.YES);
        criteria.setSortOrder(1);
        return criteria;
    }

    private static ReviewCriteria textCriteria() {
        ReviewCriteria criteria = new ReviewCriteria();
        criteria.setId(102L);
        criteria.setRuleId(90L);
        criteria.setCriteriaName("文本评价");
        criteria.setScoreType(ReviewCriteriaType.TEXT.getCode());
        criteria.setRequired(ReviewConstants.YES);
        criteria.setEnabled(ReviewConstants.YES);
        criteria.setSortOrder(2);
        return criteria;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static class TestReviewRuleServiceImpl extends ReviewRuleServiceImpl {
        @Override
        protected String currentUsername() {
            return "rule-admin";
        }
    }

    private static class Fixture {
        private TestReviewRuleServiceImpl service;
        private ReviewRuleMapper ruleMapper;
        private ReviewCriteriaMapper criteriaMapper;
        private ReviewActivityMapper activityMapper;
        private ReviewRoundMapper roundMapper;
        private ReviewRule rule;
        private List<ReviewCriteria> criteriaList;
    }
}
