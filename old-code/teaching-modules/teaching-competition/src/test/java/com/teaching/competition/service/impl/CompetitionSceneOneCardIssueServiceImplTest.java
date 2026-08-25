package com.teaching.competition.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionSceneCredentialScopeGrant;
import com.teaching.competition.domain.CompetitionSceneOneCardIssueResult;
import com.teaching.competition.domain.CompetitionSceneSchedule;
import com.teaching.competition.domain.CompetitionSceneScheduleTarget;
import com.teaching.competition.mapper.CompetitionMainInfoMapper;
import com.teaching.competition.mapper.CompetitionSceneCredentialMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleTargetMapper;
import com.teaching.competition.service.ICompetitionSceneCredentialScopeGrantService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompetitionSceneOneCardIssueServiceImplTest {

    private CompetitionSceneOneCardIssueServiceImpl service;
    private CompetitionSceneScheduleTargetMapper targetMapper;
    private CompetitionSceneScheduleMapper scheduleMapper;
    private CompetitionSceneCredentialMapper credentialMapper;
    private ICompetitionSceneCredentialScopeGrantService grantService;
    private CompetitionMainInfoMapper competitionMainInfoMapper;
    private AtomicLong credentialIdSequence;
    private AtomicLong grantIdSequence;

    @Before
    public void setUp() throws Exception {
        service = new CompetitionSceneOneCardIssueServiceImpl();
        targetMapper = mock(CompetitionSceneScheduleTargetMapper.class);
        scheduleMapper = mock(CompetitionSceneScheduleMapper.class);
        credentialMapper = mock(CompetitionSceneCredentialMapper.class);
        grantService = mock(ICompetitionSceneCredentialScopeGrantService.class);
        competitionMainInfoMapper = mock(CompetitionMainInfoMapper.class);
        credentialIdSequence = new AtomicLong(1000L);
        grantIdSequence = new AtomicLong(9000L);

        inject("targetMapper", targetMapper);
        inject("scheduleMapper", scheduleMapper);
        inject("credentialMapper", credentialMapper);
        inject("grantService", grantService);
        inject("competitionMainInfoMapper", competitionMainInfoMapper);

        when(credentialMapper.selectCompetitionSceneCredentialByNo(anyString())).thenReturn(null);
        when(credentialMapper.selectCompetitionSceneCredentialByToken(anyString())).thenReturn(null);
        when(grantService.buildDefaultOperationWindowJson(any(CompetitionSceneSchedule.class))).thenReturn("{}");
        doAnswer(invocation -> {
            CompetitionSceneCredential credential = invocation.getArgument(0);
            credential.setCredentialId(credentialIdSequence.getAndIncrement());
            return 1;
        }).when(credentialMapper).insertCompetitionSceneCredential(any(CompetitionSceneCredential.class));
        doAnswer(invocation -> {
            CompetitionSceneCredentialScopeGrant grant = invocation.getArgument(0);
            if (grant.getGrantId() == null) {
                grant.setGrantId(grantIdSequence.getAndIncrement());
            }
            return grant;
        }).when(grantService).ensureScheduleGrant(any(CompetitionSceneCredentialScopeGrant.class));
    }

    @Test
    public void firstIssueCreatesCoreCredentialAndScheduleGrant() {
        CompetitionSceneSchedule schedule = schedule(13L);
        CompetitionSceneScheduleTarget target = target(44L, 13L, 1353L,
                CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT,
                CompetitionSceneConstants.TARGET_ROLE_MEMBER);
        when(targetMapper.selectCompetitionSceneScheduleTargetById(44L)).thenReturn(target);
        when(scheduleMapper.selectCompetitionSceneScheduleById(13L)).thenReturn(schedule);

        CompetitionSceneOneCardIssueResult result = service.issueOneCardByScheduleTarget(13L, 44L);

        assertEquals(Long.valueOf(1000L), result.getCredentialId());
        assertEquals(Long.valueOf(9000L), result.getGrantId());
        assertFalse(result.getReusedCredential());
        assertFalse(result.getReusedGrant());
        assertEquals("USER", result.getSubjectType());
        assertEquals("1353", result.getSubjectCode());
        assertEquals(CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT, result.getCredentialType());

        ArgumentCaptor<CompetitionSceneCredential> credentialCaptor =
                ArgumentCaptor.forClass(CompetitionSceneCredential.class);
        verify(credentialMapper).insertCompetitionSceneCredential(credentialCaptor.capture());
        CompetitionSceneCredential credential = credentialCaptor.getValue();
        assertNull(credential.getScheduleId());
        assertNull(credential.getTargetId());
        assertEquals(CompetitionSceneConstants.SCOPE_TYPE_COMPETITION, credential.getScopeType());
        assertEquals(CompetitionSceneConstants.ISSUE_CHANNEL_SCHEDULE_MATCH, credential.getIssueChannel());
        assertEquals(Long.valueOf(1L), credential.getScopeRefId());
        assertEquals("USER", credential.getSubjectType());
        assertEquals("1353", credential.getSubjectCode());
        assertEquals("1:USER:1353:PARTICIPANT", credential.getActiveCoreCredentialKey());

        JSONObject coreAbility = JSON.parseObject(credential.getAbilityJson());
        assertTrue(coreAbility.getBooleanValue("identityVerify"));
        assertTrue(coreAbility.getBooleanValue("report"));
        assertTrue(coreAbility.getBooleanValue("material"));
        assertFalse(coreAbility.getBooleanValue("waiting"));
        assertFalse(coreAbility.getBooleanValue("scheduleEntry"));
        assertFalse(coreAbility.getBooleanValue("resourceReservation"));

        ArgumentCaptor<CompetitionSceneCredentialScopeGrant> grantCaptor =
                ArgumentCaptor.forClass(CompetitionSceneCredentialScopeGrant.class);
        verify(grantService).ensureScheduleGrant(grantCaptor.capture());
        CompetitionSceneCredentialScopeGrant grant = grantCaptor.getValue();
        assertEquals(Long.valueOf(1000L), grant.getCredentialId());
        assertEquals(CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE, grant.getScopeType());
        assertEquals(Long.valueOf(13L), grant.getScopeRefId());
        assertEquals(Long.valueOf(13L), grant.getSourceScheduleId());
        assertEquals(Long.valueOf(44L), grant.getSourceTargetId());

        JSONObject grantAbility = JSON.parseObject(grant.getAbilityJson());
        assertFalse(grantAbility.getBooleanValue("report"));
        assertFalse(grantAbility.getBooleanValue("material"));
        assertTrue(grantAbility.getBooleanValue("waiting"));
        assertTrue(grantAbility.getBooleanValue("scheduleEntry"));
        assertTrue(grantAbility.getBooleanValue("resourceReservation"));

        verify(credentialMapper).selectEffectiveCompetitionScopeCredentialStrict(
                1L, "USER", "1353", CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        verify(credentialMapper, never()).selectEffectiveCompetitionScopeCredentialByUserId(anyLong(), anyLong());
    }

    @Test
    public void secondIssueReusesCredentialAndGrant() {
        CompetitionSceneSchedule schedule = schedule(13L);
        CompetitionSceneScheduleTarget target = target(44L, 13L, 1353L,
                CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT,
                CompetitionSceneConstants.TARGET_ROLE_MEMBER);
        CompetitionSceneCredential credential = credential(88L, "USER", "1353",
                CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        CompetitionSceneCredentialScopeGrant grant = grant(99L, 88L, 13L, 44L);
        when(targetMapper.selectCompetitionSceneScheduleTargetById(44L)).thenReturn(target);
        when(scheduleMapper.selectCompetitionSceneScheduleById(13L)).thenReturn(schedule);
        when(credentialMapper.selectEffectiveCompetitionScopeCredentialStrict(
                1L, "USER", "1353", CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT))
                .thenReturn(credential);
        when(grantService.findActiveScheduleGrant(88L, 13L, 44L)).thenReturn(grant);
        when(grantService.ensureScheduleGrant(any(CompetitionSceneCredentialScopeGrant.class))).thenReturn(grant);

        CompetitionSceneOneCardIssueResult result = service.issueOneCardByScheduleTarget(13L, 44L);

        assertEquals(Long.valueOf(88L), result.getCredentialId());
        assertEquals(Long.valueOf(99L), result.getGrantId());
        assertTrue(result.getReusedCredential());
        assertTrue(result.getReusedGrant());
        assertTrue(result.getAlreadyGranted());
        verify(credentialMapper, never()).insertCompetitionSceneCredential(any(CompetitionSceneCredential.class));
    }

    @Test
    public void secondScheduleTargetReusesCredentialAndCreatesNewGrant() {
        CompetitionSceneSchedule schedule = schedule(14L);
        CompetitionSceneScheduleTarget target = target(45L, 14L, 1353L,
                CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT,
                CompetitionSceneConstants.TARGET_ROLE_MEMBER);
        CompetitionSceneCredential credential = credential(88L, "USER", "1353",
                CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        when(targetMapper.selectCompetitionSceneScheduleTargetById(45L)).thenReturn(target);
        when(scheduleMapper.selectCompetitionSceneScheduleById(14L)).thenReturn(schedule);
        when(credentialMapper.selectEffectiveCompetitionScopeCredentialStrict(
                1L, "USER", "1353", CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT))
                .thenReturn(credential);

        CompetitionSceneOneCardIssueResult result = service.issueOneCardByScheduleTarget(14L, 45L);

        assertEquals(Long.valueOf(88L), result.getCredentialId());
        assertEquals(Long.valueOf(9000L), result.getGrantId());
        assertTrue(result.getReusedCredential());
        assertFalse(result.getReusedGrant());
        verify(credentialMapper, never()).insertCompetitionSceneCredential(any(CompetitionSceneCredential.class));

        ArgumentCaptor<CompetitionSceneCredentialScopeGrant> grantCaptor =
                ArgumentCaptor.forClass(CompetitionSceneCredentialScopeGrant.class);
        verify(grantService).ensureScheduleGrant(grantCaptor.capture());
        assertEquals(Long.valueOf(14L), grantCaptor.getValue().getScopeRefId());
        assertEquals(Long.valueOf(45L), grantCaptor.getValue().getSourceTargetId());
    }

    @Test
    public void sameUserDifferentCredentialTypeCreatesDifferentCoreCredentials() {
        CompetitionSceneSchedule participantSchedule = schedule(13L);
        CompetitionSceneSchedule teacherSchedule = schedule(15L);
        CompetitionSceneScheduleTarget participant = target(44L, 13L, 1353L,
                CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT,
                CompetitionSceneConstants.TARGET_ROLE_MEMBER);
        CompetitionSceneScheduleTarget teacher = target(46L, 15L, 1353L,
                CompetitionSceneConstants.CREDENTIAL_TYPE_TEACHER,
                CompetitionSceneConstants.TARGET_ROLE_TEACHER);
        when(targetMapper.selectCompetitionSceneScheduleTargetById(44L)).thenReturn(participant);
        when(targetMapper.selectCompetitionSceneScheduleTargetById(46L)).thenReturn(teacher);
        when(scheduleMapper.selectCompetitionSceneScheduleById(13L)).thenReturn(participantSchedule);
        when(scheduleMapper.selectCompetitionSceneScheduleById(15L)).thenReturn(teacherSchedule);

        service.issueOneCardByScheduleTarget(13L, 44L);
        service.issueOneCardByScheduleTarget(15L, 46L);

        ArgumentCaptor<CompetitionSceneCredential> credentialCaptor =
                ArgumentCaptor.forClass(CompetitionSceneCredential.class);
        verify(credentialMapper, times(2)).insertCompetitionSceneCredential(credentialCaptor.capture());
        CompetitionSceneCredential first = credentialCaptor.getAllValues().get(0);
        CompetitionSceneCredential second = credentialCaptor.getAllValues().get(1);
        assertEquals("1353", first.getSubjectCode());
        assertEquals("1353", second.getSubjectCode());
        assertNotEquals(first.getCredentialType(), second.getCredentialType());
        assertEquals("1:USER:1353:PARTICIPANT", first.getActiveCoreCredentialKey());
        assertEquals("1:USER:1353:TEACHER", second.getActiveCoreCredentialKey());

        verify(credentialMapper).selectEffectiveCompetitionScopeCredentialStrict(
                1L, "USER", "1353", CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        verify(credentialMapper).selectEffectiveCompetitionScopeCredentialStrict(
                1L, "USER", "1353", CompetitionSceneConstants.CREDENTIAL_TYPE_TEACHER);
    }

    @Test
    public void differentSubjectCodeDoesNotReuseCoreCredential() {
        CompetitionSceneSchedule schedule = schedule(13L);
        CompetitionSceneScheduleTarget firstTarget = target(44L, 13L, 1353L,
                CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT,
                CompetitionSceneConstants.TARGET_ROLE_MEMBER);
        CompetitionSceneScheduleTarget secondTarget = target(47L, 13L, 1354L,
                CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT,
                CompetitionSceneConstants.TARGET_ROLE_MEMBER);
        when(targetMapper.selectCompetitionSceneScheduleTargetById(44L)).thenReturn(firstTarget);
        when(targetMapper.selectCompetitionSceneScheduleTargetById(47L)).thenReturn(secondTarget);
        when(scheduleMapper.selectCompetitionSceneScheduleById(13L)).thenReturn(schedule);

        service.issueOneCardByScheduleTarget(13L, 44L);
        service.issueOneCardByScheduleTarget(13L, 47L);

        ArgumentCaptor<CompetitionSceneCredential> credentialCaptor =
                ArgumentCaptor.forClass(CompetitionSceneCredential.class);
        verify(credentialMapper, times(2)).insertCompetitionSceneCredential(credentialCaptor.capture());
        assertEquals("1353", credentialCaptor.getAllValues().get(0).getSubjectCode());
        assertEquals("1354", credentialCaptor.getAllValues().get(1).getSubjectCode());
        verify(credentialMapper).selectEffectiveCompetitionScopeCredentialStrict(
                1L, "USER", "1353", CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        verify(credentialMapper).selectEffectiveCompetitionScopeCredentialStrict(
                1L, "USER", "1354", CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
    }

    @Test
    public void duplicateKeyFallbackReusesExistingCoreCredential() {
        CompetitionSceneSchedule schedule = schedule(13L);
        CompetitionSceneScheduleTarget target = target(44L, 13L, 1353L,
                CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT,
                CompetitionSceneConstants.TARGET_ROLE_MEMBER);
        CompetitionSceneCredential existed = credential(88L, "USER", "1353",
                CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        CompetitionSceneCredentialScopeGrant grant = grant(99L, 88L, 13L, 44L);
        when(targetMapper.selectCompetitionSceneScheduleTargetById(44L)).thenReturn(target);
        when(scheduleMapper.selectCompetitionSceneScheduleById(13L)).thenReturn(schedule);
        when(credentialMapper.selectEffectiveCompetitionScopeCredentialStrict(
                1L, "USER", "1353", CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT))
                .thenReturn(null, existed);
        doAnswer(invocation -> {
            throw new DuplicateKeyException("uk_scene_credential_active_core_key");
        }).when(credentialMapper).insertCompetitionSceneCredential(any(CompetitionSceneCredential.class));
        when(grantService.ensureScheduleGrant(any(CompetitionSceneCredentialScopeGrant.class))).thenReturn(grant);

        CompetitionSceneOneCardIssueResult result = service.issueOneCardByScheduleTarget(13L, 44L);

        assertEquals(Long.valueOf(88L), result.getCredentialId());
        assertEquals(Long.valueOf(99L), result.getGrantId());
        assertTrue(result.getReusedCredential());
        assertFalse(result.getReusedGrant());
        verify(credentialMapper, times(2)).selectEffectiveCompetitionScopeCredentialStrict(
                1L, "USER", "1353", CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        verify(credentialMapper, never()).selectEffectiveCompetitionScopeCredentialByUserId(anyLong(), anyLong());

        ArgumentCaptor<CompetitionSceneCredentialScopeGrant> grantCaptor =
                ArgumentCaptor.forClass(CompetitionSceneCredentialScopeGrant.class);
        verify(grantService).ensureScheduleGrant(grantCaptor.capture());
        assertEquals(Long.valueOf(88L), grantCaptor.getValue().getCredentialId());
    }

    private void inject(String fieldName, Object value) throws Exception {
        Field field = CompetitionSceneOneCardIssueServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }

    private CompetitionSceneSchedule schedule(Long scheduleId) {
        CompetitionSceneSchedule schedule = new CompetitionSceneSchedule();
        schedule.setScheduleId(scheduleId);
        schedule.setScheduleName("赛场" + scheduleId);
        schedule.setCompetitionSeriesId(1L);
        schedule.setCompetitionName("测试赛事");
        schedule.setCredentialType(CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        schedule.setConfigDimension(CompetitionSceneConstants.DIMENSION_PERSON);
        schedule.setReportStartTime(new Date(System.currentTimeMillis() - 3600000L));
        schedule.setReportEndTime(new Date(System.currentTimeMillis() + 3600000L));
        schedule.setWaitingStartTime(new Date(System.currentTimeMillis() - 1800000L));
        schedule.setWaitingEndTime(new Date(System.currentTimeMillis() + 1800000L));
        schedule.setContestStartTime(new Date(System.currentTimeMillis() - 1200000L));
        schedule.setContestEndTime(new Date(System.currentTimeMillis() + 7200000L));
        schedule.setStatus(CompetitionSceneConstants.STATUS_NORMAL);
        schedule.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        return schedule;
    }

    private CompetitionSceneScheduleTarget target(Long targetId,
                                                  Long scheduleId,
                                                  Long userId,
                                                  String credentialType,
                                                  String roleCode) {
        CompetitionSceneScheduleTarget target = new CompetitionSceneScheduleTarget();
        target.setTargetId(targetId);
        target.setScheduleId(scheduleId);
        target.setCompetitionSeriesId(1L);
        target.setCredentialType(credentialType);
        target.setConfigDimension(CompetitionSceneConstants.DIMENSION_PERSON);
        target.setTargetKey("USER:" + userId);
        target.setTargetSource(CompetitionSceneConstants.TARGET_SOURCE_MANUAL);
        target.setUserId(userId);
        target.setUserName("用户" + userId);
        target.setCompetitionRoleName(roleCode);
        target.setWaitingGroupCode("G01");
        target.setWaitingGroupName("第一组");
        target.setMatchStatus(CompetitionSceneConstants.MATCH_STATUS_MATCHED);
        target.setStatus(CompetitionSceneConstants.STATUS_NORMAL);
        target.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        return target;
    }

    private CompetitionSceneCredential credential(Long credentialId,
                                                  String subjectType,
                                                  String subjectCode,
                                                  String credentialType) {
        CompetitionSceneCredential credential = new CompetitionSceneCredential();
        credential.setCredentialId(credentialId);
        credential.setCompetitionSeriesId(1L);
        credential.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_COMPETITION);
        credential.setSubjectType(subjectType);
        credential.setSubjectCode(subjectCode);
        credential.setCredentialType(credentialType);
        credential.setCredentialStatus(CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE);
        credential.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        return credential;
    }

    private CompetitionSceneCredentialScopeGrant grant(Long grantId,
                                                       Long credentialId,
                                                       Long scheduleId,
                                                       Long targetId) {
        CompetitionSceneCredentialScopeGrant grant = new CompetitionSceneCredentialScopeGrant();
        grant.setGrantId(grantId);
        grant.setCredentialId(credentialId);
        grant.setCompetitionSeriesId(1L);
        grant.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE);
        grant.setScopeRefId(scheduleId);
        grant.setSourceScheduleId(scheduleId);
        grant.setSourceTargetId(targetId);
        grant.setGrantStatus("ACTIVE");
        grant.setDeleted(0);
        return grant;
    }
}
