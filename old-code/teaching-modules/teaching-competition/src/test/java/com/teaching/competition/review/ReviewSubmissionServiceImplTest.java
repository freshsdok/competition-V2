package com.teaching.competition.review;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.competition.review.constant.ReviewConstants;
import com.teaching.competition.review.domain.ReviewActivity;
import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectMaterial;
import com.teaching.competition.review.domain.ReviewObjectMember;
import com.teaching.competition.review.domain.ReviewObjectSubmitLog;
import com.teaching.competition.review.domain.ReviewSubmissionPermission;
import com.teaching.competition.review.dto.ReviewSubmissionActionDTO;
import com.teaching.competition.review.dto.ReviewSubmissionDraftDTO;
import com.teaching.competition.review.dto.ReviewSubmissionMaterialDTO;
import com.teaching.competition.review.enums.ReviewActivityStatus;
import com.teaching.competition.review.enums.ReviewMemberRole;
import com.teaching.competition.review.enums.ReviewObjectStatus;
import com.teaching.competition.review.enums.ReviewPermissionStatus;
import com.teaching.competition.review.enums.ReviewPermissionType;
import com.teaching.competition.review.enums.ReviewSubmitActionType;
import com.teaching.competition.review.mapper.ReviewActivityMapper;
import com.teaching.competition.review.mapper.ReviewAuditLogMapper;
import com.teaching.competition.review.mapper.ReviewObjectMapper;
import com.teaching.competition.review.mapper.ReviewObjectMaterialMapper;
import com.teaching.competition.review.mapper.ReviewObjectMemberMapper;
import com.teaching.competition.review.mapper.ReviewObjectSubmitLogMapper;
import com.teaching.competition.review.mapper.ReviewSubmissionPermissionMapper;
import com.teaching.competition.review.service.impl.ReviewSubmissionServiceImpl;
import com.teaching.competition.review.vo.ReviewSubmissionCloseResultVO;
import com.teaching.competition.review.vo.ReviewSubmissionTaskVO;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通用评审模块第四包填报流程测试。
 */
public class ReviewSubmissionServiceImplTest {

    @Test
    public void myListOnlyReturnsCurrentUsersPermissions() throws Exception {
        Fixture fixture = createFixture();

        List<ReviewSubmissionTaskVO> ownTasks = fixture.service.myList();
        Assert.assertEquals(1, ownTasks.size());
        Assert.assertEquals(10L, ownTasks.get(0).getObjectId().longValue());
        Assert.assertTrue(ownTasks.get(0).getEditable());

        fixture.service.userId = 202L;
        List<ReviewSubmissionTaskVO> otherTasks = fixture.service.myList();
        Assert.assertTrue(otherTasks.isEmpty());
    }

    @Test
    public void draftObjectCanBeSavedAndWritesLog() throws Exception {
        Fixture fixture = createFixture();
        ReviewSubmissionDraftDTO dto = draftDTO();

        ReviewObject saved = fixture.service.saveDraft(10L, dto);

        Assert.assertEquals("新项目", saved.getObjectName());
        verify(fixture.objectMapper).update(any(ReviewObject.class));
        verifySubmitLog(fixture, ReviewSubmitActionType.SAVE_DRAFT.getCode());
    }

    @Test(expected = ServiceException.class)
    public void submittedObjectCannotBeEditedDirectly() throws Exception {
        Fixture fixture = createFixture();
        fixture.object.setSubmitStatus(ReviewObjectStatus.SUBMITTED.getCode());

        fixture.service.saveDraft(10L, draftDTO());
    }

    @Test
    public void submitDraftMarksSubmittedAndRecordsSubmitter() throws Exception {
        Fixture fixture = createFixture();

        ReviewObject submitted = fixture.service.submit(10L);

        Assert.assertEquals(ReviewObjectStatus.SUBMITTED.getCode(), submitted.getSubmitStatus());
        Assert.assertEquals(201L, submitted.getSubmittedBy().longValue());
        Assert.assertNotNull(submitted.getSubmitTime());
        verify(fixture.permissionMapper, times(1)).update(any(ReviewSubmissionPermission.class));
        verifySubmitLog(fixture, ReviewSubmitActionType.SUBMIT.getCode());
    }

    @Test
    public void submittedBeforeDeadlineCanRequestWithdraw() throws Exception {
        Fixture fixture = createFixture();
        fixture.object.setSubmitStatus(ReviewObjectStatus.SUBMITTED.getCode());
        ReviewSubmissionActionDTO dto = new ReviewSubmissionActionDTO();
        dto.setActionReason("需要补充材料");

        ReviewObject object = fixture.service.withdrawRequest(10L, dto);

        Assert.assertEquals(ReviewObjectStatus.WITHDRAW_REQUESTED.getCode(), object.getSubmitStatus());
        verifySubmitLog(fixture, ReviewSubmitActionType.WITHDRAW_REQUEST.getCode());
    }

    @Test
    public void withdrawApproveAllowsEditingAgain() throws Exception {
        Fixture fixture = createFixture();
        fixture.object.setSubmitStatus(ReviewObjectStatus.WITHDRAW_REQUESTED.getCode());

        ReviewObject approved = fixture.service.withdrawApprove(10L, actionDTO("同意"));
        Assert.assertEquals(ReviewObjectStatus.WITHDRAW_APPROVED.getCode(), approved.getSubmitStatus());

        ReviewObject saved = fixture.service.saveDraft(10L, draftDTO());
        Assert.assertEquals("新项目", saved.getObjectName());
        verifySubmitLog(fixture, ReviewSubmitActionType.WITHDRAW_APPROVE.getCode());
    }

    @Test
    public void withdrawRejectKeepsSubmittedStatus() throws Exception {
        Fixture fixture = createFixture();
        fixture.object.setSubmitStatus(ReviewObjectStatus.WITHDRAW_REQUESTED.getCode());

        ReviewObject rejected = fixture.service.withdrawReject(10L, actionDTO("材料已足够"));

        Assert.assertEquals(ReviewObjectStatus.SUBMITTED.getCode(), rejected.getSubmitStatus());
        verifySubmitLog(fixture, ReviewSubmitActionType.WITHDRAW_REJECT.getCode());
    }

    @Test
    public void closeSubmissionLocksSubmittedAndInvalidatesUnsubmitted() throws Exception {
        Fixture fixture = createFixture();
        ReviewObject submitted = reviewObject(11L, ReviewObjectStatus.SUBMITTED.getCode());
        ReviewObject draft = reviewObject(12L, ReviewObjectStatus.DRAFT.getCode());
        ReviewObject withdrawApproved = reviewObject(13L, ReviewObjectStatus.WITHDRAW_APPROVED.getCode());
        when(fixture.objectMapper.selectList(any())).thenReturn(Arrays.asList(submitted, draft, withdrawApproved));

        ReviewSubmissionCloseResultVO result = fixture.service.closeSubmission(1L);

        Assert.assertEquals(1, result.getLockedCount().intValue());
        Assert.assertEquals(2, result.getInvalidCount().intValue());
        Assert.assertEquals(ReviewObjectStatus.LOCKED.getCode(), submitted.getSubmitStatus());
        Assert.assertEquals(ReviewObjectStatus.INVALID.getCode(), draft.getSubmitStatus());
        Assert.assertEquals(ReviewActivityStatus.SUBMIT_CLOSED.getCode(), fixture.activity.getStatus());
        verify(fixture.activityMapper).update(fixture.activity);
    }

    @Test(expected = ServiceException.class)
    public void lockedObjectCannotBeEdited() throws Exception {
        Fixture fixture = createFixture();
        fixture.object.setSubmitStatus(ReviewObjectStatus.LOCKED.getCode());

        fixture.service.saveDraft(10L, draftDTO());
    }

    @Test
    public void materialAddAndDeleteUsesLogicalDeleteAndWritesLog() throws Exception {
        Fixture fixture = createFixture();
        ReviewSubmissionMaterialDTO dto = new ReviewSubmissionMaterialDTO();
        dto.setMaterialName("申报书");
        dto.setMaterialType("PDF");
        dto.setFileName("declare.pdf");
        dto.setFileUrl("https://example.com/declare.pdf");
        dto.setFileSize(100L);

        ReviewObjectMaterial material = fixture.service.addMaterial(10L, dto);

        Assert.assertEquals("申报书", material.getMaterialName());
        Assert.assertEquals(201L, material.getUploadBy().longValue());
        verify(fixture.materialMapper).insert(any(ReviewObjectMaterial.class));

        material.setId(50L);
        when(fixture.materialMapper.selectById(50L)).thenReturn(material);
        fixture.service.deleteMaterial(50L);

        Assert.assertEquals("DELETED", material.getStatus());
        verify(fixture.materialMapper).deleteByIds(any(Long[].class), any());
        verifySubmitLog(fixture, ReviewSubmitActionType.MATERIAL_ADD.getCode());
        verifySubmitLog(fixture, ReviewSubmitActionType.MATERIAL_DELETE.getCode());
    }

    private static Fixture createFixture() throws Exception {
        Fixture fixture = new Fixture();
        fixture.objectMapper = mock(ReviewObjectMapper.class);
        fixture.activityMapper = mock(ReviewActivityMapper.class);
        fixture.objectMemberMapper = mock(ReviewObjectMemberMapper.class);
        fixture.materialMapper = mock(ReviewObjectMaterialMapper.class);
        fixture.permissionMapper = mock(ReviewSubmissionPermissionMapper.class);
        fixture.submitLogMapper = mock(ReviewObjectSubmitLogMapper.class);
        fixture.auditLogMapper = mock(ReviewAuditLogMapper.class);
        fixture.service = new TestReviewSubmissionServiceImpl();
        setField(fixture.service, "objectMapper", fixture.objectMapper);
        setField(fixture.service, "activityMapper", fixture.activityMapper);
        setField(fixture.service, "objectMemberMapper", fixture.objectMemberMapper);
        setField(fixture.service, "materialMapper", fixture.materialMapper);
        setField(fixture.service, "permissionMapper", fixture.permissionMapper);
        setField(fixture.service, "submitLogMapper", fixture.submitLogMapper);
        setField(fixture.service, "auditLogMapper", fixture.auditLogMapper);

        fixture.activity = new ReviewActivity();
        fixture.activity.setId(1L);
        fixture.activity.setActivityName("评审活动");
        fixture.activity.setSubmitDeadline(new Date(System.currentTimeMillis() + 86400000L));
        fixture.object = reviewObject(10L, ReviewObjectStatus.DRAFT.getCode());
        fixture.permission = permission(201L, 10L);

        ReviewObjectMember leader = new ReviewObjectMember();
        leader.setObjectId(10L);
        leader.setMemberName("负责人");
        leader.setMemberRole(ReviewMemberRole.LEADER.getCode());
        leader.setIsPrimary(ReviewConstants.YES);

        when(fixture.activityMapper.selectById(1L)).thenReturn(fixture.activity);
        when(fixture.objectMapper.selectById(10L)).thenReturn(fixture.object);
        when(fixture.objectMapper.selectList(any())).thenReturn(Collections.singletonList(fixture.object));
        when(fixture.objectMemberMapper.selectList(any())).thenReturn(Collections.singletonList(leader));
        when(fixture.materialMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(fixture.permissionMapper.selectList(any())).thenAnswer(invocation -> {
            ReviewSubmissionPermission query = invocation.getArgument(0);
            if (Objects.equals(query.getUserId(), fixture.permission.getUserId())
                    && (query.getObjectId() == null || Objects.equals(query.getObjectId(), 10L))
                    && ReviewPermissionStatus.ACTIVE.getCode().equals(query.getStatus())) {
                return Collections.singletonList(fixture.permission);
            }
            return Collections.emptyList();
        });
        return fixture;
    }

    private static ReviewObject reviewObject(Long id, String status) {
        ReviewObject object = new ReviewObject();
        object.setId(id);
        object.setActivityId(1L);
        object.setObjectCode("OBJ-" + id);
        object.setObjectName("项目" + id);
        object.setSummary("项目摘要");
        object.setContactPhone("13800000000");
        object.setSubmitStatus(status);
        return object;
    }

    private static ReviewSubmissionPermission permission(Long userId, Long objectId) {
        ReviewSubmissionPermission permission = new ReviewSubmissionPermission();
        permission.setId(30L);
        permission.setActivityId(1L);
        permission.setObjectId(objectId);
        permission.setUserId(userId);
        permission.setPermissionType(ReviewPermissionType.EDIT_SUBMIT.getCode());
        permission.setStatus(ReviewPermissionStatus.ACTIVE.getCode());
        return permission;
    }

    private static ReviewSubmissionDraftDTO draftDTO() {
        ReviewSubmissionDraftDTO dto = new ReviewSubmissionDraftDTO();
        dto.setObjectName("新项目");
        dto.setSummary("新的摘要");
        dto.setOrgName("测试单位");
        dto.setContactName("联系人");
        dto.setContactPhone("13900000000");
        dto.setContactEmail("contact@example.com");
        dto.setSubjectCode1("A01");
        dto.setCategoryCodes("科技");
        dto.setKeywords("创新");
        return dto;
    }

    private static ReviewSubmissionActionDTO actionDTO(String reason) {
        ReviewSubmissionActionDTO dto = new ReviewSubmissionActionDTO();
        dto.setActionReason(reason);
        return dto;
    }

    private static void verifySubmitLog(Fixture fixture, String actionType) {
        ArgumentCaptor<ReviewObjectSubmitLog> captor = ArgumentCaptor.forClass(ReviewObjectSubmitLog.class);
        verify(fixture.submitLogMapper, org.mockito.Mockito.atLeastOnce()).insert(captor.capture());
        boolean found = false;
        for (ReviewObjectSubmitLog log : captor.getAllValues()) {
            if (actionType.equals(log.getActionType())) {
                found = true;
                break;
            }
        }
        Assert.assertTrue("缺少日志：" + actionType, found);
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

    private static class TestReviewSubmissionServiceImpl extends ReviewSubmissionServiceImpl {
        private Long userId = 201L;

        @Override
        protected Long currentUserId() {
            return userId;
        }

        @Override
        protected String currentUsername() {
            return "tester";
        }
    }

    private static class Fixture {
        private TestReviewSubmissionServiceImpl service;
        private ReviewObjectMapper objectMapper;
        private ReviewActivityMapper activityMapper;
        private ReviewObjectMemberMapper objectMemberMapper;
        private ReviewObjectMaterialMapper materialMapper;
        private ReviewSubmissionPermissionMapper permissionMapper;
        private ReviewObjectSubmitLogMapper submitLogMapper;
        private ReviewAuditLogMapper auditLogMapper;
        private ReviewActivity activity;
        private ReviewObject object;
        private ReviewSubmissionPermission permission;
    }
}
