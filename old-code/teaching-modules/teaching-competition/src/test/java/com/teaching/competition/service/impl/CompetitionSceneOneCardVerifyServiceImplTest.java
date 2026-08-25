package com.teaching.competition.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionSceneCredentialScopeGrant;
import com.teaching.competition.domain.CompetitionSceneOneCardVerifyReq;
import com.teaching.competition.domain.CompetitionSceneOneCardVerifyResult;
import com.teaching.competition.domain.CompetitionSceneOperationLog;
import com.teaching.competition.domain.CompetitionSceneSubjectOperationState;
import com.teaching.competition.domain.CompetitionSceneSubjectOperationStateQuery;
import com.teaching.competition.mapper.CompetitionSceneCredentialMapper;
import com.teaching.competition.mapper.CompetitionSceneOperationLogMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleMapper;
import com.teaching.competition.service.ICompetitionSceneCredentialScopeGrantService;
import com.teaching.competition.service.ICompetitionSceneSubjectOperationStateService;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompetitionSceneOneCardVerifyServiceImplTest {

    private CompetitionSceneOneCardVerifyServiceImpl service;
    private CompetitionSceneCredentialMapper credentialMapper;
    private ICompetitionSceneCredentialScopeGrantService grantService;
    private ICompetitionSceneSubjectOperationStateService operationStateService;
    private CompetitionSceneOperationLogMapper operationLogMapper;
    private CompetitionSceneScheduleMapper scheduleMapper;
    private Map<String, CompetitionSceneSubjectOperationState> stateStore;
    private List<CompetitionSceneOperationLog> logs;
    private AtomicLong stateIdSequence;
    private AtomicLong logIdSequence;

    @Before
    public void setUp() throws Exception {
        service = new CompetitionSceneOneCardVerifyServiceImpl();
        credentialMapper = mock(CompetitionSceneCredentialMapper.class);
        grantService = mock(ICompetitionSceneCredentialScopeGrantService.class);
        operationStateService = mock(ICompetitionSceneSubjectOperationStateService.class);
        operationLogMapper = mock(CompetitionSceneOperationLogMapper.class);
        scheduleMapper = mock(CompetitionSceneScheduleMapper.class);
        stateStore = new HashMap<>();
        logs = new ArrayList<>();
        stateIdSequence = new AtomicLong(5000L);
        logIdSequence = new AtomicLong(8000L);

        inject("credentialMapper", credentialMapper);
        inject("grantService", grantService);
        inject("operationStateService", operationStateService);
        inject("operationLogMapper", operationLogMapper);
        inject("scheduleMapper", scheduleMapper);

        doAnswer(invocation -> {
            CompetitionSceneSubjectOperationStateQuery query = invocation.getArgument(0);
            return stateStore.get(key(query));
        }).when(operationStateService).selectDoneOperationState(any(CompetitionSceneSubjectOperationStateQuery.class));
        doAnswer(invocation -> {
            CompetitionSceneSubjectOperationState state = invocation.getArgument(0);
            String key = key(state);
            CompetitionSceneSubjectOperationState existed = stateStore.get(key);
            if (existed != null) {
                return existed;
            }
            state.setStateId(stateIdSequence.getAndIncrement());
            stateStore.put(key, state);
            return state;
        }).when(operationStateService).insertDoneOperationStateIfAbsent(any(CompetitionSceneSubjectOperationState.class));
        doAnswer(invocation -> {
            CompetitionSceneOperationLog log = invocation.getArgument(0);
            log.setLogId(logIdSequence.getAndIncrement());
            logs.add(log);
            return 1;
        }).when(operationLogMapper).insertCompetitionSceneOperationLog(any(CompetitionSceneOperationLog.class));
        doAnswer(invocation -> hasGrantAbility(invocation.getArgument(0), invocation.getArgument(1)))
                .when(grantService).hasAbility(any(CompetitionSceneCredentialScopeGrant.class), any(String.class));
    }

    @Test
    public void scanCoreCredentialBuildsReportPreviewAndSafeLog() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        when(credentialMapper.selectCompetitionSceneCredentialByToken("token101")).thenReturn(credential);
        when(grantService.findActiveGrantsByCredential(101L))
                .thenReturn(Collections.singletonList(grant(301L, 101L, 13L, "T001")));

        CompetitionSceneOneCardVerifyReq req = scanReq("csc_token101", CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF);
        CompetitionSceneOneCardVerifyResult result = service.scan(req);

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_PASS, result.getOperationResult());
        assertEquals(Long.valueOf(101L), result.getCredential().getCredentialId());
        assertTrue(result.getAllowedActions().stream().anyMatch(item -> "REPORT".equals(item.getActionType())));
        assertFalse(result.getAllowedActions().stream().anyMatch(item -> "WAITING".equals(item.getActionType())));
        assertEquals(1, result.getScheduleActionGroups().size());
        assertLogIsSanitized(logs.get(0));
    }

    @Test
    public void scanRejectsInvalidCredential() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        credential.setCredentialStatus(CompetitionSceneConstants.CREDENTIAL_STATUS_REVOKED);
        when(credentialMapper.selectCompetitionSceneCredentialByToken("token101")).thenReturn(credential);

        CompetitionSceneOneCardVerifyResult result = service.scan(scanReq("csc_token101",
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_FAIL, result.getOperationResult());
        assertEquals("证件不是有效状态，请联系现场工作人员", result.getResultMessage());
    }

    @Test
    public void scanRejectsDeletedCredential() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        credential.setDelFlag("2");
        when(credentialMapper.selectCompetitionSceneCredentialByToken("token101")).thenReturn(credential);

        CompetitionSceneOneCardVerifyResult result = service.scan(scanReq("csc_token101",
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_FAIL, result.getOperationResult());
        assertEquals("证件已删除或不可用，请联系现场工作人员", result.getResultMessage());
    }

    @Test
    public void scanRejectsCredentialWithoutCompetitionSeriesId() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        credential.setCompetitionSeriesId(null);
        when(credentialMapper.selectCompetitionSceneCredentialByToken("token101")).thenReturn(credential);

        CompetitionSceneOneCardVerifyResult result = service.scan(scanReq("csc_token101",
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_FAIL, result.getOperationResult());
        assertEquals("证件所属大赛不能为空", result.getResultMessage());
    }

    @Test
    public void scanRejectsCredentialWithoutSubjectTypeOrSubjectCode() {
        CompetitionSceneCredential missingSubjectType = participantCredential(101L, 1001L, "T001");
        missingSubjectType.setSubjectType(null);
        when(credentialMapper.selectCompetitionSceneCredentialByToken("token101")).thenReturn(missingSubjectType);

        CompetitionSceneOneCardVerifyResult typeResult = service.scan(scanReq("csc_token101",
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));
        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_FAIL, typeResult.getOperationResult());
        assertEquals("证件人员类型不能为空", typeResult.getResultMessage());

        CompetitionSceneCredential missingSubjectCode = participantCredential(102L, 1002L, "T001");
        missingSubjectCode.setSubjectCode("");
        when(credentialMapper.selectCompetitionSceneCredentialByToken("token102")).thenReturn(missingSubjectCode);

        CompetitionSceneOneCardVerifyResult codeResult = service.scan(scanReq("csc_token102",
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));
        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_FAIL, codeResult.getOperationResult());
        assertEquals("证件主体编码不能为空", codeResult.getResultMessage());
    }

    @Test
    public void scanMaterialStaffBuildsMaterialSelfAndDelegatePreview() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        when(credentialMapper.selectCompetitionSceneCredentialByToken("token101")).thenReturn(credential);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Collections.emptyList());

        CompetitionSceneOneCardVerifyResult result = service.scan(scanReq("csc_token101",
                CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF));

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_PASS, result.getOperationResult());
        assertTrue(result.getAllowedActions().stream().anyMatch(item -> "MATERIAL_SELF".equals(item.getActionType())));
        assertTrue(result.getAllowedActions().stream().anyMatch(item -> "MATERIAL_DELEGATE".equals(item.getActionType())));
        assertFalse(result.getAllowedActions().stream().anyMatch(item -> "REPORT".equals(item.getActionType())));
        assertLogIsSanitized(logs.get(0));
    }

    @Test
    public void reportConfirmWritesCompetitionStateAndDuplicateReturnsAlreadyDone() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        when(credentialMapper.selectCompetitionSceneCredentialById(101L)).thenReturn(credential);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Collections.emptyList());

        CompetitionSceneOneCardVerifyReq req = confirmReq(101L, "REPORT", null,
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF);
        CompetitionSceneOneCardVerifyResult first = service.confirm(req);
        CompetitionSceneOneCardVerifyResult second = service.confirm(req);

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_PASS, first.getOperationResult());
        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_DUPLICATE, second.getOperationResult());
        assertTrue(second.getAlreadyDone());
        CompetitionSceneSubjectOperationState state = stateStore.values().iterator().next();
        assertEquals(CompetitionSceneConstants.SCOPE_TYPE_COMPETITION, state.getScopeType());
        assertEquals(Long.valueOf(1L), state.getScopeRefId());
        assertEquals(CompetitionSceneConstants.STATE_OPERATION_REPORT, state.getOperationType());
        verify(operationStateService, times(1)).insertDoneOperationStateIfAbsent(any(CompetitionSceneSubjectOperationState.class));
    }

    @Test
    public void materialSelfConfirmWritesUserCompetitionStateAndDuplicateReturnsAlreadyDone() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        when(credentialMapper.selectCompetitionSceneCredentialById(101L)).thenReturn(credential);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Collections.emptyList());

        CompetitionSceneOneCardVerifyReq req = confirmReq(101L, "MATERIAL_SELF", null,
                CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF);
        CompetitionSceneOneCardVerifyResult first = service.confirm(req);
        CompetitionSceneOneCardVerifyResult second = service.confirm(req);

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_PASS, first.getOperationResult());
        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_DUPLICATE, second.getOperationResult());
        CompetitionSceneSubjectOperationState state = stateStore.values().iterator().next();
        assertEquals(CompetitionSceneConstants.SCOPE_TYPE_COMPETITION, state.getScopeType());
        assertEquals(CompetitionSceneConstants.SUBJECT_TYPE_USER, state.getSubjectType());
        assertEquals("1001", state.getSubjectCode());
        assertEquals(CompetitionSceneConstants.STATE_OPERATION_MATERIAL, state.getOperationType());
        assertEquals(CompetitionSceneConstants.DELEGATE_RELATION_SELF, state.getDelegateRelation());
    }

    @Test
    public void materialDelegateAllowsSameTeamAndRejectsCrossTeam() {
        CompetitionSceneCredential receiver = participantCredential(101L, 1001L, "T001");
        CompetitionSceneCredential sameTeamDelegate = participantCredential(102L, 1002L, "T001");
        CompetitionSceneCredential crossTeamDelegate = participantCredential(103L, 1003L, "T002");
        when(credentialMapper.selectCompetitionSceneCredentialById(101L)).thenReturn(receiver);
        when(credentialMapper.selectCompetitionSceneCredentialById(102L)).thenReturn(sameTeamDelegate);
        when(credentialMapper.selectCompetitionSceneCredentialById(103L)).thenReturn(crossTeamDelegate);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Collections.emptyList());

        CompetitionSceneOneCardVerifyReq ok = confirmReq(101L, "MATERIAL_DELEGATE", null,
                CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF);
        ok.setDelegateCredentialId(102L);
        CompetitionSceneOneCardVerifyResult okResult = service.confirm(ok);

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_PASS, okResult.getOperationResult());
        CompetitionSceneSubjectOperationState state = stateStore.values().iterator().next();
        assertEquals(Long.valueOf(1002L), state.getDelegateUserId());
        assertEquals(Long.valueOf(102L), state.getDelegateCredentialId());
        assertEquals(CompetitionSceneConstants.DELEGATE_RELATION_TEAM_MEMBER, state.getDelegateRelation());

        stateStore.clear();
        CompetitionSceneOneCardVerifyReq fail = confirmReq(101L, "MATERIAL_DELEGATE", null,
                CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF);
        fail.setDelegateCredentialId(103L);
        CompetitionSceneOneCardVerifyResult failResult = service.confirm(fail);
        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_FAIL, failResult.getOperationResult());
        assertEquals("代领人与被领取人不属于同一团队", failResult.getResultMessage());
    }

    @Test
    public void materialDelegateRejectsReceiverCredentialItself() {
        CompetitionSceneCredential receiver = participantCredential(101L, 1001L, "T001");
        when(credentialMapper.selectCompetitionSceneCredentialById(101L)).thenReturn(receiver);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Collections.emptyList());

        CompetitionSceneOneCardVerifyReq req = confirmReq(101L, "MATERIAL_DELEGATE", null,
                CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF);
        req.setDelegateCredentialId(101L);
        CompetitionSceneOneCardVerifyResult result = service.confirm(req);

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_FAIL, result.getOperationResult());
        assertEquals("本人领取必须使用本人资料领取", result.getResultMessage());
    }

    @Test
    public void materialDelegateRejectsDelegateThatIsNotUserSubject() {
        CompetitionSceneCredential receiver = participantCredential(101L, 1001L, "T001");
        CompetitionSceneCredential delegate = participantCredential(102L, 1002L, "T001");
        delegate.setSubjectType(CompetitionSceneConstants.SUBJECT_TYPE_TEAM);
        delegate.setSubjectCode("TEAM:T001");
        when(credentialMapper.selectCompetitionSceneCredentialById(101L)).thenReturn(receiver);
        when(credentialMapper.selectCompetitionSceneCredentialById(102L)).thenReturn(delegate);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Collections.emptyList());

        CompetitionSceneOneCardVerifyReq req = confirmReq(101L, "MATERIAL_DELEGATE", null,
                CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF);
        req.setDelegateCredentialId(102L);
        CompetitionSceneOneCardVerifyResult result = service.confirm(req);

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_FAIL, result.getOperationResult());
        assertEquals("代领人必须是用户主体", result.getResultMessage());
    }

    @Test
    public void materialDelegateRejectsDelegateWithoutUserId() {
        CompetitionSceneCredential receiver = participantCredential(101L, 1001L, "T001");
        CompetitionSceneCredential delegate = participantCredential(102L, 1002L, "T001");
        delegate.setUserId(null);
        when(credentialMapper.selectCompetitionSceneCredentialById(101L)).thenReturn(receiver);
        when(credentialMapper.selectCompetitionSceneCredentialById(102L)).thenReturn(delegate);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Collections.emptyList());

        CompetitionSceneOneCardVerifyReq req = confirmReq(101L, "MATERIAL_DELEGATE", null,
                CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF);
        req.setDelegateCredentialId(102L);
        CompetitionSceneOneCardVerifyResult result = service.confirm(req);

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_FAIL, result.getOperationResult());
        assertEquals("代领人用户ID不能为空", result.getResultMessage());
    }

    @Test
    public void waitingPreviewAndConfirmRequireCurrentScheduleGrant() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        CompetitionSceneCredentialScopeGrant grant = grant(301L, 101L, 13L, "T001");
        when(credentialMapper.selectCompetitionSceneCredentialByToken("token101")).thenReturn(credential);
        when(credentialMapper.selectCompetitionSceneCredentialById(101L)).thenReturn(credential);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Collections.singletonList(grant));

        CompetitionSceneOneCardVerifyResult noSchedule = service.scan(scanReq("csc_token101",
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));
        assertFalse(noSchedule.getAllowedActions().stream().anyMatch(item -> "WAITING".equals(item.getActionType())));
        assertEquals(1, noSchedule.getScheduleActionGroups().size());
        assertTrue(noSchedule.getScheduleActionGroups().get(0).getActions().isEmpty());

        CompetitionSceneOneCardVerifyReq withSchedule = scanReq("csc_token101",
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF);
        withSchedule.setCurrentScheduleId(13L);
        CompetitionSceneOneCardVerifyResult preview = service.scan(withSchedule);
        assertTrue(preview.getAllowedActions().stream().anyMatch(item -> "WAITING".equals(item.getActionType())));

        CompetitionSceneOneCardVerifyResult missingScheduleConfirm = service.confirm(confirmReq(101L, "WAITING", null,
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));
        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_FAIL, missingScheduleConfirm.getOperationResult());

        CompetitionSceneOneCardVerifyResult success = service.confirm(confirmReq(101L, "WAITING", 13L,
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));
        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_PASS, success.getOperationResult());
        CompetitionSceneSubjectOperationState state = stateStore.values().iterator().next();
        assertEquals(CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE, state.getScopeType());
        assertEquals(Long.valueOf(13L), state.getScopeRefId());
        assertEquals(CompetitionSceneConstants.STATE_OPERATION_WAITING, state.getOperationType());

        CompetitionSceneOneCardVerifyResult duplicate = service.confirm(confirmReq(101L, "WAITING", 13L,
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));
        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_DUPLICATE, duplicate.getOperationResult());
    }

    @Test
    public void waitingWithoutGrantIsRejectedAndMultiScheduleGroupsAreReturned() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        CompetitionSceneCredentialScopeGrant grant13 = grant(301L, 101L, 13L, "T001");
        CompetitionSceneCredentialScopeGrant grant14 = grant(302L, 101L, 14L, "T001");
        when(credentialMapper.selectCompetitionSceneCredentialByToken("token101")).thenReturn(credential);
        when(credentialMapper.selectCompetitionSceneCredentialById(101L)).thenReturn(credential);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Arrays.asList(grant13, grant14));

        CompetitionSceneOneCardVerifyResult groups = service.scan(scanReq("csc_token101",
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));
        assertEquals(2, groups.getScheduleActionGroups().size());

        CompetitionSceneOneCardVerifyResult noGrant = service.confirm(confirmReq(101L, "WAITING", 99L,
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));
        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_FAIL, noGrant.getOperationResult());
        assertEquals("证件无当前赛场候场权限", noGrant.getResultMessage());
    }

    @Test
    public void waitingRejectsTeacherRoleEvenWhenGrantHasWaitingAbility() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        credential.setCompetitionRoleName(CompetitionSceneConstants.TARGET_ROLE_TEACHER);
        CompetitionSceneCredentialScopeGrant grant = grant(301L, 101L, 13L, "T001");
        when(credentialMapper.selectCompetitionSceneCredentialByToken("token101")).thenReturn(credential);
        when(credentialMapper.selectCompetitionSceneCredentialById(101L)).thenReturn(credential);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Collections.singletonList(grant));

        CompetitionSceneOneCardVerifyReq scanReq = scanReq("csc_token101",
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF);
        scanReq.setCurrentScheduleId(13L);
        CompetitionSceneOneCardVerifyResult preview = service.scan(scanReq);

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_PASS, preview.getOperationResult());
        assertFalse(preview.getAllowedActions().stream().anyMatch(item -> "WAITING".equals(item.getActionType())));
        assertEquals("被扫对象角色不允许候场", preview.getMatrixMessage());

        CompetitionSceneOneCardVerifyResult confirm = service.confirm(confirmReq(101L, "WAITING", 13L,
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));
        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_FAIL, confirm.getOperationResult());
        assertEquals("被扫对象角色不允许候场", confirm.getResultMessage());
    }

    @Test
    public void waitingRejectsMultipleActiveGrantsForSameSchedule() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        CompetitionSceneCredentialScopeGrant grantA = grant(301L, 101L, 13L, "T001");
        CompetitionSceneCredentialScopeGrant grantB = grant(302L, 101L, 13L, "T001");
        when(credentialMapper.selectCompetitionSceneCredentialByToken("token101")).thenReturn(credential);
        when(credentialMapper.selectCompetitionSceneCredentialById(101L)).thenReturn(credential);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Arrays.asList(grantA, grantB));

        CompetitionSceneOneCardVerifyReq scanReq = scanReq("csc_token101",
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF);
        scanReq.setCurrentScheduleId(13L);
        CompetitionSceneOneCardVerifyResult preview = service.scan(scanReq);
        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_PASS, preview.getOperationResult());
        assertFalse(preview.getAllowedActions().stream().anyMatch(item -> "WAITING".equals(item.getActionType())));
        assertEquals("当前赛场授权存在多条，请联系管理员处理", preview.getMatrixMessage());

        CompetitionSceneOneCardVerifyResult confirm = service.confirm(confirmReq(101L, "WAITING", 13L,
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));
        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_FAIL, confirm.getOperationResult());
        assertEquals("当前赛场授权存在多条，请联系管理员处理", confirm.getResultMessage());
    }

    @Test
    public void operationLogDoesNotContainSensitiveFields() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        when(credentialMapper.selectCompetitionSceneCredentialById(101L)).thenReturn(credential);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Collections.emptyList());

        CompetitionSceneOneCardVerifyReq req = confirmReq(101L, "REPORT", null,
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF);
        req.setCredentialToken("token101");
        req.setQrContent("csc_token101");
        service.confirm(req);

        assertFalse(logs.isEmpty());
        CompetitionSceneOperationLog log = logs.get(0);
        assertNull(log.getCredentialToken());
        assertNull(log.getIdCardSuffix());
        assertNull(log.getReceiverPhone());
        assertNull(log.getReceiverIdSuffix());
        assertNull(log.getOperatorPhone());
        assertNull(log.getOperatorOpenId());
        assertLogIsSanitized(log);
    }

    @Test
    public void confirmRemainsPassWhenPilotLogWriteFailsAfterStateSaved() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        when(credentialMapper.selectCompetitionSceneCredentialById(101L)).thenReturn(credential);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Collections.emptyList());
        reset(operationLogMapper);
        doThrow(new RuntimeException("log down"))
                .when(operationLogMapper).insertCompetitionSceneOperationLog(any(CompetitionSceneOperationLog.class));

        CompetitionSceneOneCardVerifyResult result = service.confirm(confirmReq(101L, "REPORT", null,
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_PASS, result.getOperationResult());
        assertTrue(result.getResultMessage().contains("日志写入失败但业务已完成"));
        assertEquals(1, stateStore.size());
    }

    @Test
    public void confirmRemainsPassWhenUpdateLastLogIdFailsAfterStateSaved() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        when(credentialMapper.selectCompetitionSceneCredentialById(101L)).thenReturn(credential);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Collections.emptyList());
        doThrow(new RuntimeException("update down"))
                .when(operationStateService).updateLastLogId(anyLong(), anyLong());

        CompetitionSceneOneCardVerifyResult result = service.confirm(confirmReq(101L, "REPORT", null,
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_PASS, result.getOperationResult());
        assertTrue(result.getResultMessage().contains("状态日志关联更新失败但业务已完成"));
        assertEquals(1, stateStore.size());
    }

    @Test
    public void confirmReturnsDuplicateWhenInsertReturnsExistingDoneState() {
        CompetitionSceneCredential credential = participantCredential(101L, 1001L, "T001");
        CompetitionSceneSubjectOperationState existing = doneState(credential,
                CompetitionSceneConstants.SCOPE_TYPE_COMPETITION,
                1L,
                CompetitionSceneConstants.SUBJECT_TYPE_USER,
                "1001",
                CompetitionSceneConstants.STATE_OPERATION_REPORT);
        existing.setRemark("EXISTING_DONE");
        when(credentialMapper.selectCompetitionSceneCredentialById(101L)).thenReturn(credential);
        when(grantService.findActiveGrantsByCredential(101L)).thenReturn(Collections.emptyList());
        doReturn(existing).when(operationStateService)
                .insertDoneOperationStateIfAbsent(any(CompetitionSceneSubjectOperationState.class));

        CompetitionSceneOneCardVerifyResult result = service.confirm(confirmReq(101L, "REPORT", null,
                CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF));

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_DUPLICATE, result.getOperationResult());
        assertTrue(result.getAlreadyDone());
        assertTrue(result.getDuplicate());
    }

    private void inject(String fieldName, Object value) throws Exception {
        Field field = CompetitionSceneOneCardVerifyServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }

    private CompetitionSceneCredential participantCredential(Long credentialId, Long userId, String teamCode) {
        CompetitionSceneCredential credential = new CompetitionSceneCredential();
        credential.setCredentialId(credentialId);
        credential.setCredentialNo("C" + credentialId);
        credential.setCredentialToken("token" + credentialId);
        credential.setQrContent("csc_token" + credentialId);
        credential.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_COMPETITION);
        credential.setScopeRefId(1L);
        credential.setIssueChannel(CompetitionSceneConstants.ISSUE_CHANNEL_SCHEDULE_MATCH);
        credential.setCompetitionSeriesId(1L);
        credential.setCompetitionName("测试赛事");
        credential.setCredentialType(CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        credential.setCredentialName("参赛证");
        credential.setConfigDimension(CompetitionSceneConstants.DIMENSION_PERSON);
        credential.setSubjectType(CompetitionSceneConstants.SUBJECT_TYPE_USER);
        credential.setSubjectCode(String.valueOf(userId));
        credential.setUserId(userId);
        credential.setUserName("用户" + userId);
        credential.setPhone("13800000000");
        credential.setIdCardSuffix("123456");
        credential.setTeamCode(teamCode);
        credential.setTeamName("团队" + teamCode);
        credential.setCompetitionRoleName(CompetitionSceneConstants.TARGET_ROLE_MEMBER);
        credential.setAbilityJson("{\"identityVerify\":true,\"report\":true,\"material\":true,\"waiting\":false,"
                + "\"scheduleEntry\":false,\"resourceReservation\":false,\"review\":false,\"vipAccess\":false}");
        credential.setValidFrom(new Date(System.currentTimeMillis() - 3600000L));
        credential.setValidTo(new Date(System.currentTimeMillis() + 3600000L));
        credential.setCredentialStatus(CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE);
        credential.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        return credential;
    }

    private CompetitionSceneCredentialScopeGrant grant(Long grantId, Long credentialId, Long scheduleId, String teamCode) {
        CompetitionSceneCredentialScopeGrant grant = new CompetitionSceneCredentialScopeGrant();
        grant.setGrantId(grantId);
        grant.setCredentialId(credentialId);
        grant.setCompetitionSeriesId(1L);
        grant.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE);
        grant.setScopeRefId(scheduleId);
        grant.setSourceType("SCHEDULE_TARGET");
        grant.setSourceScheduleId(scheduleId);
        grant.setSourceTargetId(scheduleId + 1000L);
        grant.setCredentialType(CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        grant.setRoleCode(CompetitionSceneConstants.TARGET_ROLE_MEMBER);
        grant.setSubjectType(CompetitionSceneConstants.SUBJECT_TYPE_USER);
        grant.setSubjectCode("1001");
        grant.setAbilityJson("{\"waiting\":true,\"scheduleEntry\":true,\"resourceReservation\":true}");
        grant.setValidFrom(new Date(System.currentTimeMillis() - 3600000L));
        grant.setValidTo(new Date(System.currentTimeMillis() + 3600000L));
        grant.setOperationWindowJson("{\"waiting\":{\"startTime\":\"2026-07-05 09:00:00\","
                + "\"endTime\":\"2026-07-05 10:00:00\",\"location\":\"候场区\"}}");
        grant.setGrantStatus("ACTIVE");
        grant.setGrantSnapshotJson("{\"scheduleId\":" + scheduleId + ",\"scheduleName\":\"赛场" + scheduleId
                + "\",\"targetId\":" + (scheduleId + 1000L) + ",\"targetName\":\"用户1001\","
                + "\"roleCode\":\"MEMBER\",\"credentialType\":\"PARTICIPANT\",\"teamCode\":\"" + teamCode
                + "\",\"subjectType\":\"USER\",\"subjectCode\":\"1001\",\"groupName\":\"第一组\"}");
        grant.setDeleted(0);
        return grant;
    }

    private CompetitionSceneSubjectOperationState doneState(CompetitionSceneCredential credential,
                                                            String scopeType,
                                                            Long scopeRefId,
                                                            String subjectType,
                                                            String subjectCode,
                                                            String operationType) {
        CompetitionSceneSubjectOperationState state = new CompetitionSceneSubjectOperationState();
        state.setStateId(stateIdSequence.getAndIncrement());
        state.setCompetitionSeriesId(credential.getCompetitionSeriesId());
        state.setScopeType(scopeType);
        state.setScopeRefId(scopeRefId);
        state.setSubjectType(subjectType);
        state.setSubjectCode(subjectCode);
        state.setOperationType(operationType);
        state.setOperationStatus(CompetitionSceneConstants.STATE_STATUS_DONE);
        state.setOperationTime(new Date());
        state.setCredentialId(credential.getCredentialId());
        state.setDeleted(CompetitionSceneConstants.STATE_DELETED_NO);
        return state;
    }

    private CompetitionSceneOneCardVerifyReq scanReq(String qrContent, String operatorRole) {
        CompetitionSceneOneCardVerifyReq req = new CompetitionSceneOneCardVerifyReq();
        req.setQrContent(qrContent);
        req.setOperatorRole(operatorRole);
        req.setDeviceInfo("unit-test-device");
        return req;
    }

    private CompetitionSceneOneCardVerifyReq confirmReq(Long credentialId, String actionType, Long scheduleId, String operatorRole) {
        CompetitionSceneOneCardVerifyReq req = new CompetitionSceneOneCardVerifyReq();
        req.setCredentialId(credentialId);
        req.setActionType(actionType);
        req.setCurrentScheduleId(scheduleId);
        req.setOperatorRole(operatorRole);
        req.setDeviceInfo("unit-test-device");
        req.setIdempotencyKey("idem-" + actionType + "-" + scheduleId);
        return req;
    }

    private boolean hasGrantAbility(CompetitionSceneCredentialScopeGrant grant, String abilityCode) {
        try {
            JSONObject ability = JSON.parseObject(grant.getAbilityJson());
            Object value = ability.get(abilityCode);
            return value instanceof Boolean && Boolean.TRUE.equals(value);
        } catch (Exception e) {
            return false;
        }
    }

    private String key(CompetitionSceneSubjectOperationStateQuery query) {
        return query.getCompetitionSeriesId() + ":" + query.getScopeType() + ":" + query.getScopeRefId()
                + ":" + query.getSubjectType() + ":" + query.getSubjectCode() + ":" + query.getOperationType();
    }

    private String key(CompetitionSceneSubjectOperationState state) {
        return state.getCompetitionSeriesId() + ":" + state.getScopeType() + ":" + state.getScopeRefId()
                + ":" + state.getSubjectType() + ":" + state.getSubjectCode() + ":" + state.getOperationType();
    }

    private void assertLogIsSanitized(CompetitionSceneOperationLog log) {
        String combined = String.valueOf(log.getRequestPayload()) + String.valueOf(log.getResponsePayload())
                + String.valueOf(log.getRemark());
        assertFalse(combined.contains("token101"));
        assertFalse(combined.contains("csc_"));
        assertFalse(combined.contains("13800000000"));
        assertFalse(combined.contains("123456"));
        assertFalse(combined.contains("credentialToken"));
        assertFalse(combined.contains("qrContent"));
        assertFalse(combined.contains("idCard"));
        assertFalse(combined.contains("phone"));
    }
}
