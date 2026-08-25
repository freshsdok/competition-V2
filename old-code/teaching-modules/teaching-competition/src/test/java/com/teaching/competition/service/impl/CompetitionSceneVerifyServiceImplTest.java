package com.teaching.competition.service.impl;

import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionSceneSchedule;
import com.teaching.competition.domain.CompetitionSceneVerifyReq;
import com.teaching.competition.domain.CompetitionSceneVerifyResult;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.mapper.CompetitionSceneCredentialMapper;
import com.teaching.competition.mapper.CompetitionSceneOperationLogMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleMapper;
import com.teaching.competition.service.ICompetitionSceneSubjectOperationStateService;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompetitionSceneVerifyServiceImplTest {

    private CompetitionSceneVerifyServiceImpl service;
    private CompetitionSceneCredentialMapper credentialMapper;
    private CompetitionSceneScheduleMapper scheduleMapper;

    @Before
    public void setUp() throws Exception {
        service = new CompetitionSceneVerifyServiceImpl();
        credentialMapper = mock(CompetitionSceneCredentialMapper.class);
        scheduleMapper = mock(CompetitionSceneScheduleMapper.class);
        inject("credentialMapper", credentialMapper);
        inject("scheduleMapper", scheduleMapper);
        inject("operationLogMapper", mock(CompetitionSceneOperationLogMapper.class));
        inject("competitionApplyInfoMapper", mock(CompetitionApplyInfoMapper.class));
        inject("operationStateService", mock(ICompetitionSceneSubjectOperationStateService.class));
    }

    @Test
    public void scanPersonalCoreCredentialDoesNotIncludeTeammateCredentialFromSameSchedule() {
        CompetitionSceneCredential core = coreCredential(987L, 11244L, 129385L, "11244");
        CompetitionSceneCredential own73 = scheduleCredential(988L, 73L, 11244L, 129385L, "11244");
        CompetitionSceneCredential teammate73 = scheduleCredential(990L, 73L, 11245L, 129386L, "11245");
        CompetitionSceneCredential own116 = scheduleCredential(2601L, 116L, 11244L, 129385L, "11244");
        CompetitionSceneCredential own104 = scheduleCredential(3097L, 104L, 11244L, 129385L, "11244");
        CompetitionSceneCredential staff = staffCredential();
        List<CompetitionSceneCredential> ownSchedules = Arrays.asList(own73, own116, own104);

        when(credentialMapper.selectCompetitionSceneCredentialByToken("core-token")).thenReturn(core);
        when(credentialMapper.selectCompetitionSceneCredentialList(any(CompetitionSceneCredential.class)))
                .thenAnswer(invocation -> {
                    CompetitionSceneCredential query = invocation.getArgument(0);
                    if ("staff-phone".equals(query.getPhone())) {
                        return Collections.singletonList(staff);
                    }
                    if ("TEAM-001".equals(query.getTeamCode())) {
                        return Arrays.asList(teammate73, own73, own116, own104);
                    }
                    if (Long.valueOf(11244L).equals(query.getUserId())
                            || Long.valueOf(129385L).equals(query.getMemberId())
                            || (CompetitionSceneConstants.SUBJECT_TYPE_USER.equals(query.getSubjectType())
                            && "11244".equals(query.getSubjectCode()))) {
                        return ownSchedules;
                    }
                    return Collections.emptyList();
                });

        CompetitionSceneVerifyReq req = new CompetitionSceneVerifyReq();
        req.setQrContent("csc_core-token");
        req.setOperatorPhone("staff-phone");

        CompetitionSceneVerifyResult result = service.scan(req);

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_PASS, result.getOperationResult());
        assertEquals(3, result.getScheduleActionGroups().size());
        assertEquals(3, result.getScheduleActionGroups().stream()
                .map(item -> item.getScheduleId())
                .distinct()
                .count());
        assertFalse(result.getScheduleActionGroups().stream()
                .anyMatch(item -> Long.valueOf(990L).equals(item.getTargetCredentialId())));
        verify(credentialMapper, never()).selectCompetitionSceneCredentialList(
                argThat(query -> "TEAM-001".equals(query.getTeamCode())));
    }

    @Test
    public void volunteerCanOperateMemberInResponsibleSchedule() {
        CompetitionSceneCredential target = scheduleCredential(988L, 73L, 11244L, 129385L, "11244");
        CompetitionSceneCredential volunteer = volunteerCredential(73L);

        when(credentialMapper.selectCompetitionSceneCredentialByToken("schedule-token")).thenReturn(target);
        when(scheduleMapper.selectCompetitionSceneScheduleById(73L)).thenReturn(normalSchedule(73L));
        when(credentialMapper.selectCompetitionSceneCredentialList(any(CompetitionSceneCredential.class)))
                .thenAnswer(invocation -> {
                    CompetitionSceneCredential query = invocation.getArgument(0);
                    if ("volunteer-phone".equals(query.getPhone())) {
                        return Collections.singletonList(volunteer);
                    }
                    return Collections.emptyList();
                });

        CompetitionSceneVerifyReq req = new CompetitionSceneVerifyReq();
        req.setQrContent("csc_schedule-token");
        req.setOperatorPhone("volunteer-phone");

        CompetitionSceneVerifyResult result = service.scan(req);

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_PASS, result.getOperationResult());
        assertEquals(CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER, result.getOperatorRole());
        assertEquals(1, result.getScheduleActionGroups().size());
        assertEquals(3, result.getAvailableActions().size());
    }

    @Test
    public void volunteerCannotOperateMemberInOtherSchedule() {
        CompetitionSceneCredential target = scheduleCredential(988L, 73L, 11244L, 129385L, "11244");
        CompetitionSceneCredential volunteer = volunteerCredential(116L);

        when(credentialMapper.selectCompetitionSceneCredentialByToken("schedule-token")).thenReturn(target);
        when(scheduleMapper.selectCompetitionSceneScheduleById(73L)).thenReturn(normalSchedule(73L));
        when(credentialMapper.selectCompetitionSceneCredentialList(any(CompetitionSceneCredential.class)))
                .thenAnswer(invocation -> {
                    CompetitionSceneCredential query = invocation.getArgument(0);
                    if ("volunteer-phone".equals(query.getPhone())) {
                        return Collections.singletonList(volunteer);
                    }
                    return Collections.emptyList();
                });

        CompetitionSceneVerifyReq req = new CompetitionSceneVerifyReq();
        req.setQrContent("csc_schedule-token");
        req.setOperatorPhone("volunteer-phone");

        CompetitionSceneVerifyResult result = service.scan(req);

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_PASS, result.getOperationResult());
        assertEquals(CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER, result.getOperatorRole());
        assertEquals(0, result.getScheduleActionGroups().size());
        assertEquals(0, result.getAvailableActions().size());
    }

    @Test
    public void volunteerScanningCoreCredentialOnlyGetsResponsibleScheduleActions() {
        CompetitionSceneCredential core = coreCredential(987L, 11244L, 129385L, "11244");
        CompetitionSceneCredential own73 = scheduleCredential(988L, 73L, 11244L, 129385L, "11244");
        CompetitionSceneCredential own116 = scheduleCredential(2601L, 116L, 11244L, 129385L, "11244");
        CompetitionSceneCredential volunteer = volunteerCredential(73L);

        when(credentialMapper.selectCompetitionSceneCredentialByToken("core-token")).thenReturn(core);
        when(credentialMapper.selectCompetitionSceneCredentialList(any(CompetitionSceneCredential.class)))
                .thenAnswer(invocation -> {
                    CompetitionSceneCredential query = invocation.getArgument(0);
                    if ("volunteer-phone".equals(query.getPhone())) {
                        return Collections.singletonList(volunteer);
                    }
                    if (Long.valueOf(11244L).equals(query.getUserId())
                            || Long.valueOf(129385L).equals(query.getMemberId())
                            || (CompetitionSceneConstants.SUBJECT_TYPE_USER.equals(query.getSubjectType())
                            && "11244".equals(query.getSubjectCode()))) {
                        return Arrays.asList(own73, own116);
                    }
                    return Collections.emptyList();
                });

        CompetitionSceneVerifyReq req = new CompetitionSceneVerifyReq();
        req.setQrContent("csc_core-token");
        req.setOperatorPhone("volunteer-phone");

        CompetitionSceneVerifyResult result = service.scan(req);

        assertEquals(CompetitionSceneConstants.OPERATION_RESULT_PASS, result.getOperationResult());
        assertEquals(1, result.getScheduleActionGroups().size());
        assertEquals(Long.valueOf(73L), result.getScheduleActionGroups().get(0).getScheduleId());
        assertEquals(3, result.getAvailableActions().size());
    }

    private CompetitionSceneCredential coreCredential(Long credentialId,
                                                      Long userId,
                                                      Long memberId,
                                                      String subjectCode) {
        CompetitionSceneCredential credential = baseCredential(credentialId, userId, memberId, subjectCode);
        credential.setCredentialToken("core-token");
        credential.setQrContent("csc_core-token");
        credential.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_COMPETITION);
        credential.setScopeRefId(81L);
        credential.setIssueChannel(CompetitionSceneConstants.ISSUE_CHANNEL_IMPORT);
        return credential;
    }

    private CompetitionSceneCredential scheduleCredential(Long credentialId,
                                                          Long scheduleId,
                                                          Long userId,
                                                          Long memberId,
                                                          String subjectCode) {
        CompetitionSceneCredential credential = baseCredential(credentialId, userId, memberId, subjectCode);
        credential.setScheduleId(scheduleId);
        credential.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE);
        credential.setScopeRefId(scheduleId);
        credential.setCredentialName("赛场" + scheduleId);
        credential.setIssueChannel(CompetitionSceneConstants.ISSUE_CHANNEL_IMPORT);
        return credential;
    }

    private CompetitionSceneCredential baseCredential(Long credentialId,
                                                      Long userId,
                                                      Long memberId,
                                                      String subjectCode) {
        CompetitionSceneCredential credential = new CompetitionSceneCredential();
        credential.setCredentialId(credentialId);
        credential.setCompetitionSeriesId(81L);
        credential.setCredentialType(CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        credential.setCredentialStatus(CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE);
        credential.setSubjectType(CompetitionSceneConstants.SUBJECT_TYPE_USER);
        credential.setSubjectCode(subjectCode);
        credential.setUserId(userId);
        credential.setUserName("测试学员" + userId);
        credential.setMemberId(memberId);
        credential.setTeamCode("TEAM-001");
        credential.setCompetitionRoleName(CompetitionSceneConstants.TARGET_ROLE_MEMBER);
        credential.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        return credential;
    }

    private CompetitionSceneCredential staffCredential() {
        CompetitionSceneCredential credential = new CompetitionSceneCredential();
        credential.setCredentialId(207L);
        credential.setCompetitionSeriesId(81L);
        credential.setCredentialType(CompetitionSceneConstants.CREDENTIAL_TYPE_STAFF);
        credential.setCredentialStatus(CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE);
        credential.setCompetitionRoleName(CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF);
        credential.setPhone("staff-phone");
        credential.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        return credential;
    }

    private CompetitionSceneCredential volunteerCredential(Long scheduleId) {
        CompetitionSceneCredential credential = new CompetitionSceneCredential();
        credential.setCredentialId(3000L + scheduleId);
        credential.setCompetitionSeriesId(81L);
        credential.setScheduleId(scheduleId);
        credential.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE);
        credential.setScopeRefId(scheduleId);
        credential.setCredentialType(CompetitionSceneConstants.CREDENTIAL_TYPE_STAFF);
        credential.setCredentialStatus(CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE);
        credential.setCompetitionRoleName(CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER);
        credential.setPhone("volunteer-phone");
        credential.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        return credential;
    }

    private CompetitionSceneSchedule normalSchedule(Long scheduleId) {
        CompetitionSceneSchedule schedule = new CompetitionSceneSchedule();
        schedule.setScheduleId(scheduleId);
        schedule.setStatus(CompetitionSceneConstants.STATUS_NORMAL);
        return schedule;
    }

    private void inject(String fieldName, Object value) throws Exception {
        Field field = CompetitionSceneVerifyServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }
}
