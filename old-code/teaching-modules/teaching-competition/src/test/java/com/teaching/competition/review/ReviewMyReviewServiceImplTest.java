package com.teaching.competition.review;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.competition.review.constant.ReviewConstants;
import com.teaching.competition.review.domain.ReviewAssignment;
import com.teaching.competition.review.domain.ReviewAuditLog;
import com.teaching.competition.review.domain.ReviewCriteria;
import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectMaterial;
import com.teaching.competition.review.domain.ReviewObjectMember;
import com.teaching.competition.review.domain.ReviewRecord;
import com.teaching.competition.review.domain.ReviewRound;
import com.teaching.competition.review.domain.ReviewRule;
import com.teaching.competition.review.domain.ReviewScoreDetail;
import com.teaching.competition.review.dto.ReviewMyReviewScoreDTO;
import com.teaching.competition.review.dto.ReviewScoreDetailDTO;
import com.teaching.competition.review.enums.ReviewAssignmentStatus;
import com.teaching.competition.review.enums.ReviewCriteriaType;
import com.teaching.competition.review.enums.ReviewObjectStatus;
import com.teaching.competition.review.enums.ReviewRecordStatus;
import com.teaching.competition.review.enums.ReviewRoundType;
import com.teaching.competition.review.enums.ReviewRuleScoreMode;
import com.teaching.competition.review.mapper.ReviewActivityMapper;
import com.teaching.competition.review.mapper.ReviewAssignmentMapper;
import com.teaching.competition.review.mapper.ReviewAuditLogMapper;
import com.teaching.competition.review.mapper.ReviewCriteriaMapper;
import com.teaching.competition.review.mapper.ReviewObjectMapper;
import com.teaching.competition.review.mapper.ReviewObjectMaterialMapper;
import com.teaching.competition.review.mapper.ReviewObjectMemberMapper;
import com.teaching.competition.review.mapper.ReviewRecordMapper;
import com.teaching.competition.review.mapper.ReviewRoundMapper;
import com.teaching.competition.review.mapper.ReviewRuleMapper;
import com.teaching.competition.review.mapper.ReviewScoreDetailMapper;
import com.teaching.competition.review.mapper.ReviewSessionMapper;
import com.teaching.competition.review.service.impl.ReviewMyReviewServiceImpl;
import com.teaching.competition.review.vo.ReviewMyReviewTaskVO;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通用评审模块第五包专家评分流程测试。
 */
public class ReviewMyReviewServiceImplTest {

    @Test
    public void expertOnlySeesOwnAssignments() throws Exception {
        Fixture fixture = createFixture();

        List<ReviewMyReviewTaskVO> ownTasks = fixture.service.myList(null, null, null, null, null, null, null);

        Assert.assertEquals(1, ownTasks.size());
        Assert.assertEquals(10L, ownTasks.get(0).getObjectId().longValue());
        Assert.assertTrue(ownTasks.get(0).getCanReview());

        fixture.service.userId = 202L;
        List<ReviewMyReviewTaskVO> otherTasks = fixture.service.myList(null, null, null, null, null, null, null);
        Assert.assertTrue(otherTasks.isEmpty());
    }

    @Test(expected = ServiceException.class)
    public void expertCannotOpenOthersAssignment() throws Exception {
        Fixture fixture = createFixture();
        fixture.assignment.setReviewerUserId(202L);

        fixture.service.detail(20L);
    }

    @Test(expected = ServiceException.class)
    public void unlockedObjectCannotBeReviewed() throws Exception {
        Fixture fixture = createFixture();
        fixture.object.setSubmitStatus(ReviewObjectStatus.SUBMITTED.getCode());

        fixture.service.saveDraft(20L, scoreDTO());
    }

    @Test(expected = ServiceException.class)
    public void invalidObjectCannotBeReviewed() throws Exception {
        Fixture fixture = createFixture();
        fixture.object.setSubmitStatus(ReviewObjectStatus.INVALID.getCode());

        fixture.service.saveDraft(20L, scoreDTO());
    }

    @Test
    public void onsiteDraftObjectCanBeReviewed() throws Exception {
        Fixture fixture = createFixture();
        fixture.object.setSubmitStatus(ReviewObjectStatus.DRAFT.getCode());
        fixture.round.setRoundType(ReviewRoundType.ONSITE_DEFENSE.getCode());
        fixture.assignment.setAssignmentType("ONSITE");

        ReviewRecord record = fixture.service.saveDraft(20L, scoreDTO());

        Assert.assertEquals(ReviewRecordStatus.DRAFT.getCode(), record.getRecordStatus());
        Assert.assertEquals(ReviewAssignmentStatus.IN_PROGRESS.getCode(), fixture.assignment.getStatus());
    }

    @Test
    public void draftSaveUpdatesAssignmentAndWritesSnapshotDetails() throws Exception {
        Fixture fixture = createFixture();

        ReviewRecord record = fixture.service.saveDraft(20L, scoreDTO());

        Assert.assertEquals(ReviewRecordStatus.DRAFT.getCode(), record.getRecordStatus());
        Assert.assertEquals(ReviewAssignmentStatus.IN_PROGRESS.getCode(), fixture.assignment.getStatus());
        Assert.assertEquals(new BigDecimal("98.00"), record.getTotalScore());
        verify(fixture.assignmentMapper).update(fixture.assignment);
        verify(fixture.scoreDetailMapper, times(3)).insert(any(ReviewScoreDetail.class));

        ArgumentCaptor<ReviewScoreDetail> detailCaptor = ArgumentCaptor.forClass(ReviewScoreDetail.class);
        verify(fixture.scoreDetailMapper, times(3)).insert(detailCaptor.capture());
        Assert.assertEquals("创新性", detailCaptor.getAllValues().get(0).getCriteriaName());
        Assert.assertEquals(new BigDecimal("1.0000"), detailCaptor.getAllValues().get(0).getWeight());
        verify(fixture.auditLogMapper).insert(any(ReviewAuditLog.class));
    }

    @Test
    public void weightedSumUsesPercentageWeight() throws Exception {
        Fixture fixture = createFixture();
        fixture.rule.setScoreMode(ReviewRuleScoreMode.WEIGHTED_SUM.getCode());
        fixture.criteriaList.get(0).setWeight(new BigDecimal("40"));
        fixture.criteriaList.get(1).setWeight(new BigDecimal("60"));

        ReviewRecord record = fixture.service.saveDraft(20L, scoreDTO());

        Assert.assertEquals(new BigDecimal("41.20"), record.getTotalScore());
    }

    @Test(expected = ServiceException.class)
    public void submitRequiresRequiredCriteria() throws Exception {
        Fixture fixture = createFixture();
        ReviewMyReviewScoreDTO dto = scoreDTO();
        dto.getScoreDetails().remove(2);

        fixture.service.submit(20L, dto);
    }

    @Test(expected = ServiceException.class)
    public void numberScoreMustStayInRange() throws Exception {
        Fixture fixture = createFixture();
        ReviewMyReviewScoreDTO dto = scoreDTO();
        dto.getScoreDetails().get(0).setScoreValue(new BigDecimal("120"));

        fixture.service.submit(20L, dto);
    }

    @Test
    public void submitMarksRecordAndAssignmentSubmitted() throws Exception {
        Fixture fixture = createFixture();

        ReviewRecord record = fixture.service.submit(20L, scoreDTO());

        Assert.assertEquals(ReviewRecordStatus.SUBMITTED.getCode(), record.getRecordStatus());
        Assert.assertEquals(ReviewAssignmentStatus.SUBMITTED.getCode(), fixture.assignment.getStatus());
        Assert.assertNotNull(record.getSubmittedTime());
        Assert.assertNotNull(fixture.assignment.getSubmittedTime());
        verify(fixture.assignmentMapper).update(fixture.assignment);
    }

    @Test(expected = ServiceException.class)
    public void submittedRecordCannotBeEditedAgain() throws Exception {
        Fixture fixture = createFixture();
        ReviewRecord submitted = record(30L, ReviewRecordStatus.SUBMITTED.getCode());
        fixture.records.add(submitted);

        fixture.service.saveDraft(20L, scoreDTO());
    }

    private static Fixture createFixture() throws Exception {
        Fixture fixture = new Fixture();
        fixture.service = new TestReviewMyReviewServiceImpl();
        fixture.assignmentMapper = mock(ReviewAssignmentMapper.class);
        fixture.recordMapper = mock(ReviewRecordMapper.class);
        fixture.scoreDetailMapper = mock(ReviewScoreDetailMapper.class);
        fixture.objectMapper = mock(ReviewObjectMapper.class);
        fixture.activityMapper = mock(ReviewActivityMapper.class);
        fixture.roundMapper = mock(ReviewRoundMapper.class);
        fixture.ruleMapper = mock(ReviewRuleMapper.class);
        fixture.criteriaMapper = mock(ReviewCriteriaMapper.class);
        fixture.objectMemberMapper = mock(ReviewObjectMemberMapper.class);
        fixture.materialMapper = mock(ReviewObjectMaterialMapper.class);
        fixture.sessionMapper = mock(ReviewSessionMapper.class);
        fixture.auditLogMapper = mock(ReviewAuditLogMapper.class);

        setField(fixture.service, "assignmentMapper", fixture.assignmentMapper);
        setField(fixture.service, "recordMapper", fixture.recordMapper);
        setField(fixture.service, "scoreDetailMapper", fixture.scoreDetailMapper);
        setField(fixture.service, "objectMapper", fixture.objectMapper);
        setField(fixture.service, "activityMapper", fixture.activityMapper);
        setField(fixture.service, "roundMapper", fixture.roundMapper);
        setField(fixture.service, "ruleMapper", fixture.ruleMapper);
        setField(fixture.service, "criteriaMapper", fixture.criteriaMapper);
        setField(fixture.service, "objectMemberMapper", fixture.objectMemberMapper);
        setField(fixture.service, "materialMapper", fixture.materialMapper);
        setField(fixture.service, "sessionMapper", fixture.sessionMapper);
        setField(fixture.service, "auditLogMapper", fixture.auditLogMapper);

        fixture.assignment = assignment(201L);
        fixture.object = object(ReviewObjectStatus.LOCKED.getCode());
        fixture.round = round();
        fixture.rule = rule();
        fixture.criteriaList = criteriaList();
        fixture.records = new ArrayList<>();

        when(fixture.assignmentMapper.selectById(20L)).thenReturn(fixture.assignment);
        when(fixture.assignmentMapper.selectList(any())).thenAnswer(invocation -> {
            ReviewAssignment query = invocation.getArgument(0);
            if (Long.valueOf(201L).equals(query.getReviewerUserId())) {
                return Collections.singletonList(fixture.assignment);
            }
            return Collections.emptyList();
        });
        when(fixture.objectMapper.selectById(10L)).thenReturn(fixture.object);
        when(fixture.roundMapper.selectById(5L)).thenReturn(fixture.round);
        when(fixture.ruleMapper.selectById(90L)).thenReturn(fixture.rule);
        when(fixture.criteriaMapper.selectList(any())).thenReturn(fixture.criteriaList);
        when(fixture.objectMemberMapper.selectList(any())).thenReturn(Collections.singletonList(member()));
        when(fixture.materialMapper.selectList(any())).thenReturn(Collections.singletonList(material()));
        when(fixture.recordMapper.selectList(any())).thenAnswer(invocation -> fixture.records);
        when(fixture.recordMapper.insert(any(ReviewRecord.class))).thenAnswer(invocation -> {
            ReviewRecord record = invocation.getArgument(0);
            record.setId(30L);
            fixture.records.clear();
            fixture.records.add(record);
            return 1;
        });
        return fixture;
    }

    private static ReviewAssignment assignment(Long reviewerUserId) {
        ReviewAssignment assignment = new ReviewAssignment();
        assignment.setId(20L);
        assignment.setActivityId(1L);
        assignment.setRoundId(5L);
        assignment.setObjectId(10L);
        assignment.setReviewerId(8L);
        assignment.setReviewerUserId(reviewerUserId);
        assignment.setStatus(ReviewAssignmentStatus.ASSIGNED.getCode());
        return assignment;
    }

    private static ReviewObject object(String status) {
        ReviewObject object = new ReviewObject();
        object.setId(10L);
        object.setActivityId(1L);
        object.setObjectCode("OBJ-10");
        object.setObjectName("评审测试项目");
        object.setSubmitStatus(status);
        return object;
    }

    private static ReviewRound round() {
        ReviewRound round = new ReviewRound();
        round.setId(5L);
        round.setActivityId(1L);
        round.setRoundName("材料评审");
        round.setRuleId(90L);
        return round;
    }

    private static ReviewRule rule() {
        ReviewRule rule = new ReviewRule();
        rule.setId(90L);
        rule.setActivityId(1L);
        rule.setRoundId(5L);
        rule.setRuleName("专家评分表");
        rule.setScoreMode(ReviewRuleScoreMode.SUM.getCode());
        rule.setEnabled(ReviewConstants.YES);
        return rule;
    }

    private static List<ReviewCriteria> criteriaList() {
        ReviewCriteria number = new ReviewCriteria();
        number.setId(100L);
        number.setRuleId(90L);
        number.setCriteriaName("创新性");
        number.setScoreType(ReviewCriteriaType.NUMBER.getCode());
        number.setMinScore(BigDecimal.ZERO);
        number.setMaxScore(new BigDecimal("100"));
        number.setWeight(new BigDecimal("1.0000"));
        number.setRequired(ReviewConstants.YES);
        number.setEnabled(ReviewConstants.YES);
        number.setSortOrder(1);

        ReviewCriteria choice = new ReviewCriteria();
        choice.setId(101L);
        choice.setRuleId(90L);
        choice.setCriteriaName("推荐意见");
        choice.setScoreType(ReviewCriteriaType.SINGLE_CHOICE.getCode());
        choice.setOptionsJson("[{\"label\":\"推荐\",\"value\":\"YES\",\"score\":10},{\"label\":\"不推荐\",\"value\":\"NO\",\"score\":0}]");
        choice.setRequired(ReviewConstants.YES);
        choice.setEnabled(ReviewConstants.YES);
        choice.setSortOrder(2);

        ReviewCriteria text = new ReviewCriteria();
        text.setId(102L);
        text.setRuleId(90L);
        text.setCriteriaName("文字评价");
        text.setScoreType(ReviewCriteriaType.TEXT.getCode());
        text.setRequired(ReviewConstants.YES);
        text.setEnabled(ReviewConstants.YES);
        text.setSortOrder(3);
        return Arrays.asList(number, choice, text);
    }

    private static ReviewRecord record(Long id, String status) {
        ReviewRecord record = new ReviewRecord();
        record.setId(id);
        record.setActivityId(1L);
        record.setRoundId(5L);
        record.setObjectId(10L);
        record.setAssignmentId(20L);
        record.setReviewerUserId(201L);
        record.setRecordStatus(status);
        return record;
    }

    private static ReviewObjectMember member() {
        ReviewObjectMember member = new ReviewObjectMember();
        member.setObjectId(10L);
        member.setMemberName("负责人");
        return member;
    }

    private static ReviewObjectMaterial material() {
        ReviewObjectMaterial material = new ReviewObjectMaterial();
        material.setObjectId(10L);
        material.setVisibleToReviewer(ReviewConstants.YES);
        material.setStatus("NORMAL");
        return material;
    }

    private static ReviewMyReviewScoreDTO scoreDTO() {
        ReviewScoreDetailDTO number = new ReviewScoreDetailDTO();
        number.setCriteriaId(100L);
        number.setScoreValue(new BigDecimal("88"));

        ReviewScoreDetailDTO choice = new ReviewScoreDetailDTO();
        choice.setCriteriaId(101L);
        choice.setOptionValue("YES");

        ReviewScoreDetailDTO text = new ReviewScoreDetailDTO();
        text.setCriteriaId(102L);
        text.setTextValue("材料完整，建议进入下一轮。");

        ReviewMyReviewScoreDTO dto = new ReviewMyReviewScoreDTO();
        dto.setScoreDetails(new ArrayList<>(Arrays.asList(number, choice, text)));
        dto.setCommentText("总体表现良好");
        dto.setRecommendation("推荐");
        return dto;
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

    private static class TestReviewMyReviewServiceImpl extends ReviewMyReviewServiceImpl {
        private Long userId = 201L;

        @Override
        protected Long currentUserId() {
            return userId;
        }

        @Override
        protected String currentUsername() {
            return "expert";
        }
    }

    private static class Fixture {
        private TestReviewMyReviewServiceImpl service;
        private ReviewAssignmentMapper assignmentMapper;
        private ReviewRecordMapper recordMapper;
        private ReviewScoreDetailMapper scoreDetailMapper;
        private ReviewObjectMapper objectMapper;
        private ReviewActivityMapper activityMapper;
        private ReviewRoundMapper roundMapper;
        private ReviewRuleMapper ruleMapper;
        private ReviewCriteriaMapper criteriaMapper;
        private ReviewObjectMemberMapper objectMemberMapper;
        private ReviewObjectMaterialMapper materialMapper;
        private ReviewSessionMapper sessionMapper;
        private ReviewAuditLogMapper auditLogMapper;
        private ReviewAssignment assignment;
        private ReviewObject object;
        private ReviewRound round;
        private ReviewRule rule;
        private List<ReviewCriteria> criteriaList;
        private List<ReviewRecord> records;
    }
}
