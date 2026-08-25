package com.teaching.competition.review;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.mapper.CompetitionSceneCredentialMapper;
import com.teaching.competition.mapper.TeamManagerInfoMapper;
import com.teaching.competition.review.domain.ReviewActivity;
import com.teaching.competition.review.domain.ReviewAssignment;
import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectCertificateRef;
import com.teaching.competition.review.domain.ReviewObjectMember;
import com.teaching.competition.review.domain.ReviewResult;
import com.teaching.competition.review.domain.ReviewSession;
import com.teaching.competition.review.domain.ReviewSessionObject;
import com.teaching.competition.review.domain.ReviewSubmissionPermission;
import com.teaching.competition.review.dto.ReviewObjectImportDTO;
import com.teaching.competition.review.dto.ReviewResultConclusionDTO;
import com.teaching.competition.review.dto.ReviewResultGenerateDTO;
import com.teaching.competition.review.dto.ReviewSessionCurrentObjectDTO;
import com.teaching.competition.review.enums.ReviewCertificateType;
import com.teaching.competition.review.enums.ReviewCertificateValidStatus;
import com.teaching.competition.review.enums.ReviewObjectStatus;
import com.teaching.competition.review.enums.ReviewPermissionStatus;
import com.teaching.competition.review.enums.ReviewPermissionType;
import com.teaching.competition.review.enums.ReviewResultStatus;
import com.teaching.competition.review.mapper.ReviewActivityMapper;
import com.teaching.competition.review.mapper.ReviewAssignmentMapper;
import com.teaching.competition.review.mapper.ReviewAuditLogMapper;
import com.teaching.competition.review.mapper.ReviewObjectCertificateRefMapper;
import com.teaching.competition.review.mapper.ReviewObjectExternalRefMapper;
import com.teaching.competition.review.mapper.ReviewObjectMapper;
import com.teaching.competition.review.mapper.ReviewObjectMemberMapper;
import com.teaching.competition.review.mapper.ReviewResultMapper;
import com.teaching.competition.review.mapper.ReviewResultPublishLogMapper;
import com.teaching.competition.review.mapper.ReviewSessionEventLogMapper;
import com.teaching.competition.review.mapper.ReviewSessionMapper;
import com.teaching.competition.review.mapper.ReviewSessionObjectMapper;
import com.teaching.competition.review.mapper.ReviewSubmissionPermissionMapper;
import com.teaching.competition.review.service.impl.ReviewObjectServiceImpl;
import com.teaching.competition.review.service.impl.ReviewRecordServiceImpl;
import com.teaching.competition.review.service.impl.ReviewResultServiceImpl;
import com.teaching.competition.review.service.impl.ReviewSessionServiceImpl;
import com.teaching.competition.review.vo.ReviewCertificateResolveResultVO;
import com.teaching.competition.review.vo.ReviewCertificateResolveVO;
import com.teaching.competition.review.vo.ReviewObjectImportPreviewVO;
import com.teaching.competition.review.vo.ReviewObjectImportResultVO;
import com.teaching.competition.review.vo.ReviewResultGenerateItemVO;
import com.teaching.competition.review.vo.ReviewResultGenerateResponseVO;
import com.teaching.competition.review.vo.ReviewSessionCurrentObjectVO;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.TeamManagerInfo;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通用评审模块核心流程冒烟测试。
 */
public class ReviewModulePhase1SmokeTest {

    @Test
    public void importCompetitionTeamCreatesObjectMembersPermissionsAndCertificates() throws Exception {
        ReviewObjectFixture fixture = createReviewObjectFixture();
        stubCompetitionTeamSource(fixture);
        when(fixture.objectMapper.selectBySourceRef(eq(1L), eq("competition"), eq("TEAM"), eq("T001")))
                .thenReturn(null);

        ReviewObjectImportDTO dto = teamImportDTO(false);
        List<ReviewObjectImportPreviewVO> preview = fixture.service.importPreview(dto);
        Assert.assertEquals(1, preview.size());
        Assert.assertTrue(preview.get(0).getCanImport());
        Assert.assertEquals(3, preview.get(0).getMemberCount().intValue());
        Assert.assertEquals(2, preview.get(0).getCertificateCount().intValue());

        ReviewObjectImportResultVO result = fixture.service.importFromBusiness(dto);

        Assert.assertEquals(1, result.getSuccessCount());
        Assert.assertEquals(0, result.getSkipCount());
        Assert.assertEquals(0, result.getFailedCount());
        Assert.assertEquals(10L, result.getCreatedObjectIds().get(0).longValue());

        ArgumentCaptor<ReviewObject> objectCaptor = ArgumentCaptor.forClass(ReviewObject.class);
        verify(fixture.objectMapper).insert(objectCaptor.capture());
        Assert.assertEquals("TEAM-T001", objectCaptor.getValue().getObjectCode());
        Assert.assertEquals("Alpha Team", objectCaptor.getValue().getObjectName());
        Assert.assertEquals("T001", objectCaptor.getValue().getSourceTeamId());

        verify(fixture.externalRefMapper).insert(any());
        verify(fixture.objectMemberMapper, times(3)).insert(any(ReviewObjectMember.class));
        verify(fixture.submissionPermissionMapper, times(2)).insert(any(ReviewSubmissionPermission.class));
        verify(fixture.certificateRefMapper, times(2)).insert(any(ReviewObjectCertificateRef.class));

        ArgumentCaptor<ReviewSubmissionPermission> permissionCaptor =
                ArgumentCaptor.forClass(ReviewSubmissionPermission.class);
        verify(fixture.submissionPermissionMapper, times(2)).insert(permissionCaptor.capture());
        for (ReviewSubmissionPermission permission : permissionCaptor.getAllValues()) {
            Assert.assertEquals(ReviewPermissionType.EDIT_SUBMIT.getCode(), permission.getPermissionType());
            Assert.assertEquals(ReviewPermissionStatus.ACTIVE.getCode(), permission.getStatus());
        }

        ArgumentCaptor<ReviewObjectCertificateRef> certificateCaptor =
                ArgumentCaptor.forClass(ReviewObjectCertificateRef.class);
        verify(fixture.certificateRefMapper, times(2)).insert(certificateCaptor.capture());
        Assert.assertEquals(ReviewCertificateValidStatus.VALID.getCode(), certificateCaptor.getAllValues().get(0).getValidStatus());
        Assert.assertEquals(ReviewCertificateType.CONTESTANT.getCode(), certificateCaptor.getAllValues().get(0).getCertificateType());
    }

    @Test
    public void importCompetitionRegistrationCreatesReviewObject() throws Exception {
        ReviewObjectFixture fixture = createReviewObjectFixture();
        stubCompetitionRegistrationSource(fixture);
        when(fixture.objectMapper.selectBySourceRef(eq(1L), eq("competition"), eq("REGISTRATION"), eq("101")))
                .thenReturn(null);

        ReviewObjectImportDTO dto = registrationImportDTO(false);
        List<ReviewObjectImportPreviewVO> preview = fixture.service.importPreview(dto);
        Assert.assertEquals(1, preview.size());
        Assert.assertTrue(preview.get(0).getCanImport());
        Assert.assertEquals("T001", preview.get(0).getTeamCode());

        ReviewObjectImportResultVO result = fixture.service.importFromBusiness(dto);

        Assert.assertEquals(1, result.getSuccessCount());
        ArgumentCaptor<ReviewObject> objectCaptor = ArgumentCaptor.forClass(ReviewObject.class);
        verify(fixture.objectMapper).insert(objectCaptor.capture());
        Assert.assertEquals("REGISTRATION-101", objectCaptor.getValue().getObjectCode());
        Assert.assertEquals("101", objectCaptor.getValue().getSourceRegistrationId());
        verify(fixture.objectMemberMapper, times(3)).insert(any(ReviewObjectMember.class));
        verify(fixture.certificateRefMapper, times(2)).insert(any(ReviewObjectCertificateRef.class));
    }

    @Test
    public void duplicateImportSkipsAndOverwriteInvalidatesThenResyncs() throws Exception {
        ReviewObjectFixture skippedFixture = createReviewObjectFixture();
        stubCompetitionTeamSource(skippedFixture);
        ReviewObject existed = existingReviewObject();
        when(skippedFixture.objectMapper.selectBySourceRef(eq(1L), eq("competition"), eq("TEAM"), eq("T001")))
                .thenReturn(existed);

        ReviewObjectImportResultVO skipped = skippedFixture.service.importFromBusiness(teamImportDTO(false));

        Assert.assertEquals(1, skipped.getSkipCount());
        verify(skippedFixture.objectMapper, never()).insert(any(ReviewObject.class));
        verify(skippedFixture.certificateRefMapper, never()).insert(any(ReviewObjectCertificateRef.class));

        ReviewObjectFixture overwrittenFixture = createReviewObjectFixture();
        stubCompetitionTeamSource(overwrittenFixture);
        when(overwrittenFixture.objectMapper.selectBySourceRef(eq(1L), eq("competition"), eq("TEAM"), eq("T001")))
                .thenReturn(existingReviewObject());
        when(overwrittenFixture.certificateRefMapper.invalidateByObjectId(eq(1L), eq(10L), any()))
                .thenReturn(2);

        ReviewObjectImportResultVO overwritten = overwrittenFixture.service.importFromBusiness(teamImportDTO(true));

        Assert.assertEquals(1, overwritten.getSuccessCount());
        verify(overwrittenFixture.objectMapper).update(any(ReviewObject.class));
        verify(overwrittenFixture.objectMemberMapper).deleteByObjectId(eq(1L), eq(10L), any());
        verify(overwrittenFixture.submissionPermissionMapper).deleteByObjectId(eq(1L), eq(10L), any());
        verify(overwrittenFixture.certificateRefMapper).invalidateByObjectId(eq(1L), eq(10L), any());
        verify(overwrittenFixture.certificateRefMapper, times(2)).insert(any(ReviewObjectCertificateRef.class));
    }

    @Test
    public void resolveCertificateReturnsEnhancedCandidates() throws Exception {
        ReviewObjectFixture fixture = createReviewObjectFixture();
        ReviewSession session = new ReviewSession();
        session.setId(20L);
        session.setActivityId(1L);
        when(fixture.sessionMapper.selectById(20L)).thenReturn(session);

        ReviewSessionObject sessionObject = new ReviewSessionObject();
        sessionObject.setSessionId(20L);
        sessionObject.setObjectId(10L);
        when(fixture.sessionObjectMapper.selectList(any())).thenReturn(Collections.singletonList(sessionObject));

        ReviewCertificateResolveVO vo = new ReviewCertificateResolveVO();
        vo.setObjectId(10L);
        vo.setActivityId(1L);
        vo.setObjectCode("TEAM-T001");
        vo.setObjectName("Alpha Team");
        vo.setSubmitStatus(ReviewObjectStatus.LOCKED.getCode());
        vo.setCertificateCode("CERT-001");
        vo.setCertificateType(ReviewCertificateType.CONTESTANT.getCode());
        vo.setValidStatus(ReviewCertificateValidStatus.VALID.getCode());
        when(fixture.certificateRefMapper.selectResolveList(1L, "CERT-001"))
                .thenReturn(Collections.singletonList(vo));

        ReviewCertificateResolveResultVO resolved = fixture.service.resolveCertificate(1L, "CERT-001", 20L);

        Assert.assertEquals(1, resolved.getMatchedCount().intValue());
        Assert.assertTrue(resolved.getCandidates().get(0).getInSession());
        Assert.assertEquals(10L, resolved.getCandidates().get(0).getObjectId().longValue());
    }

    @Test
    public void setCurrentSessionObjectWritesStateAndEvent() throws Exception {
        ReviewSessionMapper sessionMapper = mock(ReviewSessionMapper.class);
        ReviewObjectMapper objectMapper = mock(ReviewObjectMapper.class);
        ReviewSessionObjectMapper sessionObjectMapper = mock(ReviewSessionObjectMapper.class);
        ReviewSessionEventLogMapper eventLogMapper = mock(ReviewSessionEventLogMapper.class);
        ReviewSessionServiceImpl service = new ReviewSessionServiceImpl();
        setField(service, "mapper", sessionMapper);
        setField(service, "objectMapper", objectMapper);
        setField(service, "sessionObjectMapper", sessionObjectMapper);
        setField(service, "eventLogMapper", eventLogMapper);

        ReviewSession session = new ReviewSession();
        session.setId(20L);
        session.setActivityId(1L);
        session.setRoundId(2L);
        ReviewObject object = new ReviewObject();
        object.setId(10L);
        object.setActivityId(1L);
        object.setSubmitStatus(ReviewObjectStatus.LOCKED.getCode());
        object.setObjectCode("P-001");
        object.setObjectName("项目001");
        ReviewSessionObject sessionObject = new ReviewSessionObject();
        sessionObject.setId(30L);
        sessionObject.setActivityId(1L);
        sessionObject.setRoundId(2L);
        sessionObject.setSessionId(20L);
        sessionObject.setObjectId(10L);
        when(sessionMapper.selectById(20L)).thenReturn(session);
        when(objectMapper.selectById(10L)).thenReturn(object);
        when(sessionObjectMapper.selectList(any())).thenReturn(Collections.singletonList(sessionObject));

        ReviewSessionCurrentObjectDTO dto = new ReviewSessionCurrentObjectDTO();
        dto.setObjectId(10L);
        dto.setOperatorUserId(99L);
        dto.setSourceType("SCAN");
        dto.setCertificateCode("CERT-001");
        ReviewSessionCurrentObjectVO vo = service.setCurrentObject(20L, dto);

        Assert.assertEquals(10L, vo.getObjectId().longValue());
        ArgumentCaptor<ReviewSession> sessionCaptor = ArgumentCaptor.forClass(ReviewSession.class);
        verify(sessionMapper).update(sessionCaptor.capture());
        Assert.assertEquals(10L, sessionCaptor.getValue().getCurrentObjectId().longValue());
        verify(sessionObjectMapper).update(any(ReviewSessionObject.class));
        verify(sessionObjectMapper, never()).insert(any(ReviewSessionObject.class));
        verify(eventLogMapper).insert(any());
    }

    @Test(expected = ServiceException.class)
    public void setCurrentSessionObjectRejectsDraftObject() throws Exception {
        ReviewSessionMapper sessionMapper = mock(ReviewSessionMapper.class);
        ReviewObjectMapper objectMapper = mock(ReviewObjectMapper.class);
        ReviewSessionObjectMapper sessionObjectMapper = mock(ReviewSessionObjectMapper.class);
        ReviewSessionEventLogMapper eventLogMapper = mock(ReviewSessionEventLogMapper.class);
        ReviewSessionServiceImpl service = new ReviewSessionServiceImpl();
        setField(service, "mapper", sessionMapper);
        setField(service, "objectMapper", objectMapper);
        setField(service, "sessionObjectMapper", sessionObjectMapper);
        setField(service, "eventLogMapper", eventLogMapper);

        ReviewSession session = new ReviewSession();
        session.setId(20L);
        session.setActivityId(1L);
        ReviewObject object = new ReviewObject();
        object.setId(10L);
        object.setActivityId(1L);
        object.setSubmitStatus(ReviewObjectStatus.DRAFT.getCode());
        when(sessionMapper.selectById(20L)).thenReturn(session);
        when(objectMapper.selectById(10L)).thenReturn(object);

        ReviewSessionCurrentObjectDTO dto = new ReviewSessionCurrentObjectDTO();
        dto.setObjectId(10L);
        service.setCurrentObject(20L, dto);
    }

    @Test(expected = ServiceException.class)
    public void setCurrentSessionObjectRejectsObjectOutsideSession() throws Exception {
        ReviewSessionMapper sessionMapper = mock(ReviewSessionMapper.class);
        ReviewObjectMapper objectMapper = mock(ReviewObjectMapper.class);
        ReviewSessionObjectMapper sessionObjectMapper = mock(ReviewSessionObjectMapper.class);
        ReviewSessionEventLogMapper eventLogMapper = mock(ReviewSessionEventLogMapper.class);
        ReviewSessionServiceImpl service = new ReviewSessionServiceImpl();
        setField(service, "mapper", sessionMapper);
        setField(service, "objectMapper", objectMapper);
        setField(service, "sessionObjectMapper", sessionObjectMapper);
        setField(service, "eventLogMapper", eventLogMapper);

        ReviewSession session = new ReviewSession();
        session.setId(20L);
        session.setActivityId(1L);
        ReviewObject object = new ReviewObject();
        object.setId(10L);
        object.setActivityId(1L);
        object.setSubmitStatus(ReviewObjectStatus.LOCKED.getCode());
        when(sessionMapper.selectById(20L)).thenReturn(session);
        when(objectMapper.selectById(10L)).thenReturn(object);
        when(sessionObjectMapper.selectList(any())).thenReturn(Collections.emptyList());

        ReviewSessionCurrentObjectDTO dto = new ReviewSessionCurrentObjectDTO();
        dto.setObjectId(10L);
        service.setCurrentObject(20L, dto);
    }

    @Test
    public void legacyReviewRecordSubmitIsDisabled() {
        ReviewRecordServiceImpl service = new ReviewRecordServiceImpl();
        try {
            service.submit(null);
            Assert.fail("旧评分记录提交接口应被禁用");
        } catch (ServiceException ex) {
            Assert.assertTrue(ex.getMessage().contains("旧评分记录写入接口已禁用"));
        }
    }

    @Test
    public void generateAverageResultAndConclusionDoesNotDirectlyChangeScore() throws Exception {
        ReviewResultMapper resultMapper = mock(ReviewResultMapper.class);
        ReviewResultPublishLogMapper publishLogMapper = mock(ReviewResultPublishLogMapper.class);
        ReviewResultServiceImpl service = new ReviewResultServiceImpl();
        setField(service, "mapper", resultMapper);
        setField(service, "publishLogMapper", publishLogMapper);

        ReviewResultGenerateItemVO item = new ReviewResultGenerateItemVO();
        item.setActivityId(1L);
        item.setRoundId(2L);
        item.setObjectId(10L);
        item.setObjectCode("OBJ-10");
        item.setObjectName("测试项目");
        item.setAssignedCount(2);
        item.setSubmittedCount(2);
        item.setCalculatedScore(new BigDecimal("90.00"));
        when(resultMapper.selectGenerateItems(any())).thenReturn(Collections.singletonList(item));
        when(resultMapper.selectList(any())).thenReturn(Collections.emptyList());
        final ReviewResult[] insertedResult = new ReviewResult[1];
        when(resultMapper.insert(any())).thenAnswer(invocation -> {
            ReviewResult result = invocation.getArgument(0);
            result.setId(70L);
            insertedResult[0] = result;
            return 1;
        });

        ReviewResultGenerateDTO generateDTO = new ReviewResultGenerateDTO();
        generateDTO.setActivityId(1L);
        generateDTO.setRoundId(2L);
        ReviewResultGenerateResponseVO generated = service.generate(generateDTO);

        Assert.assertEquals(1, generated.getGeneratedCount().intValue());
        Assert.assertEquals(new BigDecimal("90.00"), generated.getResults().get(0).getCalculatedScore());
        Assert.assertEquals(ReviewResultStatus.GENERATED.getCode(), generated.getResults().get(0).getResultStatus());

        ReviewResult existed = insertedResult[0];
        when(resultMapper.selectById(70L)).thenReturn(existed);
        ReviewResultConclusionDTO conclusionDTO = new ReviewResultConclusionDTO();
        conclusionDTO.setEvaluationConclusion("建议发布");
        ReviewResult updated = service.updateConclusion(70L, conclusionDTO);

        Assert.assertEquals("建议发布", updated.getEvaluationConclusion());
        Assert.assertEquals(new BigDecimal("90.00"), updated.getCalculatedScore());
    }

    private static ReviewObjectFixture createReviewObjectFixture() throws Exception {
        ReviewObjectFixture fixture = new ReviewObjectFixture();
        fixture.objectMapper = mock(ReviewObjectMapper.class);
        fixture.activityMapper = mock(ReviewActivityMapper.class);
        fixture.externalRefMapper = mock(ReviewObjectExternalRefMapper.class);
        fixture.certificateRefMapper = mock(ReviewObjectCertificateRefMapper.class);
        fixture.objectMemberMapper = mock(ReviewObjectMemberMapper.class);
        fixture.submissionPermissionMapper = mock(ReviewSubmissionPermissionMapper.class);
        fixture.auditLogMapper = mock(ReviewAuditLogMapper.class);
        fixture.sessionMapper = mock(ReviewSessionMapper.class);
        fixture.sessionObjectMapper = mock(ReviewSessionObjectMapper.class);
        fixture.competitionApplyInfoMapper = mock(CompetitionApplyInfoMapper.class);
        fixture.teamManagerInfoMapper = mock(TeamManagerInfoMapper.class);
        fixture.competitionSceneCredentialMapper = mock(CompetitionSceneCredentialMapper.class);
        fixture.service = new ReviewObjectServiceImpl();
        setField(fixture.service, "mapper", fixture.objectMapper);
        setField(fixture.service, "activityMapper", fixture.activityMapper);
        setField(fixture.service, "externalRefMapper", fixture.externalRefMapper);
        setField(fixture.service, "certificateRefMapper", fixture.certificateRefMapper);
        setField(fixture.service, "objectMemberMapper", fixture.objectMemberMapper);
        setField(fixture.service, "submissionPermissionMapper", fixture.submissionPermissionMapper);
        setField(fixture.service, "auditLogMapper", fixture.auditLogMapper);
        setField(fixture.service, "sessionMapper", fixture.sessionMapper);
        setField(fixture.service, "sessionObjectMapper", fixture.sessionObjectMapper);
        setField(fixture.service, "competitionApplyInfoMapper", fixture.competitionApplyInfoMapper);
        setField(fixture.service, "teamManagerInfoMapper", fixture.teamManagerInfoMapper);
        setField(fixture.service, "competitionSceneCredentialMapper", fixture.competitionSceneCredentialMapper);

        ReviewActivity activity = new ReviewActivity();
        activity.setId(1L);
        when(fixture.activityMapper.selectById(1L)).thenReturn(activity);
        when(fixture.objectMapper.insert(any(ReviewObject.class))).thenAnswer(invocation -> {
            ReviewObject object = invocation.getArgument(0);
            object.setId(10L);
            return 1;
        });
        final long[] memberId = {1000L};
        when(fixture.objectMemberMapper.insert(any(ReviewObjectMember.class))).thenAnswer(invocation -> {
            ReviewObjectMember member = invocation.getArgument(0);
            member.setId(memberId[0]++);
            return 1;
        });
        when(fixture.externalRefMapper.selectList(any())).thenReturn(Collections.emptyList());
        return fixture;
    }

    private static void stubCompetitionTeamSource(ReviewObjectFixture fixture) {
        TeamManagerInfo team = new TeamManagerInfo();
        team.setTeamId(100L);
        team.setTeamCode("T001");
        team.setTeamName("Alpha Team");
        team.setTeamLeaderId(201L);
        team.setSchoolName("Alpha University");
        team.setLeaderTeacher("王老师");
        team.setLeaderTeacherPhone("13900000000");
        team.setLeaderTeacherEmail("teacher@example.com");
        team.setCompetitionTrackId("TRACK-A");
        team.setCompetitionTrackName("工程实践赛");
        team.setSecondLevelCode("GROUP-1");
        team.setSecondLevelName("本科组");

        CompetitionApplyInfo leader = buildApply(101L, 201L, "队长甲", ApplyConstants.TEAM_LEADER_MEMBER, 1);
        leader.setLeaderTeacherId(301L);
        leader.setLeaderTeacher("王老师");
        leader.setLeaderTeacherPhone("13900000000");
        leader.setLeaderTeacherEmail("teacher@example.com");
        CompetitionApplyInfo member = buildApply(102L, 202L, "队员乙", ApplyConstants.TEAM_MEMBER, 2);

        CompetitionSceneCredential cert1 = buildCredential(501L, "CERT-001", 101L, 201L, "队长甲");
        CompetitionSceneCredential cert2 = buildCredential(502L, "CERT-002", 102L, 202L, "队员乙");

        when(fixture.teamManagerInfoMapper.selectTeamManagerInfoByTeamCode(null, "T001")).thenReturn(team);
        when(fixture.competitionApplyInfoMapper.selectCompetitionApplyTeamCode("T001"))
                .thenReturn(Arrays.asList(leader, member));
        when(fixture.competitionSceneCredentialMapper.selectCompetitionSceneCredentialList(any()))
                .thenReturn(Arrays.asList(cert1, cert2));
    }

    private static void stubCompetitionRegistrationSource(ReviewObjectFixture fixture) {
        TeamManagerInfo team = new TeamManagerInfo();
        team.setTeamId(100L);
        team.setTeamCode("T001");
        team.setTeamName("Alpha Team");
        team.setTeamLeaderId(201L);
        team.setSchoolName("Alpha University");
        team.setLeaderTeacher("王老师");
        team.setLeaderTeacherPhone("13900000000");
        team.setLeaderTeacherEmail("teacher@example.com");
        team.setCompetitionTrackId("TRACK-A");
        team.setCompetitionTrackName("工程实践赛");
        team.setSecondLevelCode("GROUP-1");
        team.setSecondLevelName("本科组");

        CompetitionApplyInfo leader = buildApply(101L, 201L, "队长甲", ApplyConstants.TEAM_LEADER_MEMBER, 1);
        leader.setLeaderTeacherId(301L);
        leader.setLeaderTeacher("王老师");
        leader.setLeaderTeacherPhone("13900000000");
        leader.setLeaderTeacherEmail("teacher@example.com");
        CompetitionApplyInfo member = buildApply(102L, 202L, "队员乙", ApplyConstants.TEAM_MEMBER, 2);

        CompetitionSceneCredential cert1 = buildCredential(501L, "CERT-001", 101L, 201L, "队长甲");
        CompetitionSceneCredential cert2 = buildCredential(502L, "CERT-002", 102L, 202L, "队员乙");

        when(fixture.competitionApplyInfoMapper.selectCompetitionApplyInfoByMemberId(101L)).thenReturn(leader);
        when(fixture.teamManagerInfoMapper.selectTeamManagerInfoByTeamCode(null, "T001")).thenReturn(team);
        when(fixture.competitionApplyInfoMapper.selectCompetitionApplyTeamCode("T001"))
                .thenReturn(Arrays.asList(leader, member));
        when(fixture.competitionSceneCredentialMapper.selectCompetitionSceneCredentialList(any()))
                .thenReturn(Arrays.asList(cert1, cert2));
    }

    private static CompetitionApplyInfo buildApply(Long memberId, Long userId, String userName,
                                                   String roleName, Integer sort) {
        CompetitionApplyInfo apply = new CompetitionApplyInfo();
        apply.setMemberId(memberId);
        apply.setUserId(userId);
        apply.setUserName(userName);
        apply.setCompetitionRoleName(roleName);
        apply.setTeamSort(sort);
        apply.setTeamCode("T001");
        apply.setTeamName("Alpha Team");
        apply.setPhone("13800000000");
        apply.setEmail(userName + "@example.com");
        apply.setSchoolName("Alpha University");
        apply.setOrgId(900L);
        apply.setCompetitionTrackId("TRACK-A");
        apply.setCompetitionTrackName("工程实践赛");
        apply.setSecondLevelCode("GROUP-1");
        apply.setSecondLevelName("本科组");
        return apply;
    }

    private static CompetitionSceneCredential buildCredential(Long credentialId, String credentialNo,
                                                              Long memberId, Long userId, String userName) {
        CompetitionSceneCredential credential = new CompetitionSceneCredential();
        credential.setCredentialId(credentialId);
        credential.setCredentialNo(credentialNo);
        credential.setCredentialType(CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        credential.setCredentialStatus(CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE);
        credential.setTeamCode("T001");
        credential.setMemberId(memberId);
        credential.setUserId(userId);
        credential.setUserName(userName);
        return credential;
    }

    private static ReviewObjectImportDTO teamImportDTO(boolean overwriteExisting) {
        ReviewObjectImportDTO dto = new ReviewObjectImportDTO();
        dto.setActivityId(1L);
        dto.setSourceModule("competition");
        dto.setSourceBizType("TEAM");
        dto.setSourceBizIds(Collections.singletonList("T001"));
        dto.setOverwriteExisting(overwriteExisting);
        dto.setSyncCertificate(true);
        return dto;
    }

    private static ReviewObjectImportDTO registrationImportDTO(boolean overwriteExisting) {
        ReviewObjectImportDTO dto = new ReviewObjectImportDTO();
        dto.setActivityId(1L);
        dto.setSourceModule("competition");
        dto.setSourceBizType("REGISTRATION");
        dto.setSourceBizIds(Collections.singletonList("101"));
        dto.setOverwriteExisting(overwriteExisting);
        dto.setSyncCertificate(true);
        return dto;
    }

    private static ReviewObject existingReviewObject() {
        ReviewObject object = new ReviewObject();
        object.setId(10L);
        object.setActivityId(1L);
        object.setObjectCode("TEAM-T001");
        object.setObjectName("Alpha Team");
        object.setSubmitStatus(ReviewObjectStatus.DRAFT.getCode());
        return object;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static class ReviewObjectFixture {
        private ReviewObjectServiceImpl service;
        private ReviewObjectMapper objectMapper;
        private ReviewActivityMapper activityMapper;
        private ReviewObjectExternalRefMapper externalRefMapper;
        private ReviewObjectCertificateRefMapper certificateRefMapper;
        private ReviewObjectMemberMapper objectMemberMapper;
        private ReviewSubmissionPermissionMapper submissionPermissionMapper;
        private ReviewAuditLogMapper auditLogMapper;
        private ReviewSessionMapper sessionMapper;
        private ReviewSessionObjectMapper sessionObjectMapper;
        private CompetitionApplyInfoMapper competitionApplyInfoMapper;
        private TeamManagerInfoMapper teamManagerInfoMapper;
        private CompetitionSceneCredentialMapper competitionSceneCredentialMapper;
    }
}
