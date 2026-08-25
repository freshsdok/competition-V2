package com.teaching.competition.service.impl;

import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneMatchResult;
import com.teaching.competition.domain.CompetitionSceneSchedule;
import com.teaching.competition.domain.CompetitionSceneScheduleTarget;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.mapper.CompetitionSceneCredentialMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleTargetMapper;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompetitionSceneScheduleServiceImplTest {

    private CompetitionSceneScheduleServiceImpl service;
    private CompetitionSceneScheduleMapper scheduleMapper;
    private CompetitionSceneScheduleTargetMapper targetMapper;
    private CompetitionSceneCredentialMapper credentialMapper;
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Before
    public void setUp() throws Exception {
        service = new CompetitionSceneScheduleServiceImpl();
        scheduleMapper = mock(CompetitionSceneScheduleMapper.class);
        targetMapper = mock(CompetitionSceneScheduleTargetMapper.class);
        credentialMapper = mock(CompetitionSceneCredentialMapper.class);
        competitionApplyInfoMapper = mock(CompetitionApplyInfoMapper.class);

        inject("scheduleMapper", scheduleMapper);
        inject("targetMapper", targetMapper);
        inject("credentialMapper", credentialMapper);
        inject("competitionApplyInfoMapper", competitionApplyInfoMapper);
    }

    @Test
    public void matchScheduleTargetsRestoresSoftDeletedTarget() {
        CompetitionSceneSchedule schedule = schedule();
        CompetitionApplyInfo applyInfo = applyInfo(4570L, "Alice");
        CompetitionSceneScheduleTarget deletedTarget = deletedTarget(99L, "PERSON:4570");

        when(scheduleMapper.selectCompetitionSceneScheduleById(3L)).thenReturn(schedule);
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoList(any(CompetitionApplyInfo.class)))
                .thenReturn(Collections.singletonList(applyInfo));
        when(targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(3L))
                .thenReturn(Collections.emptyList());
        when(targetMapper.selectCompetitionSceneScheduleTargetByScheduleIdAndTargetKey(3L, "PERSON:4570"))
                .thenReturn(deletedTarget);
        when(targetMapper.restoreCompetitionSceneScheduleTarget(any(CompetitionSceneScheduleTarget.class)))
                .thenReturn(1);

        CompetitionSceneMatchResult result = service.matchScheduleTargets(3L);

        assertEquals(Integer.valueOf(1), result.getMatchedCount());
        assertEquals(Integer.valueOf(0), result.getSkippedCount());
        verify(targetMapper, never()).insertCompetitionSceneScheduleTarget(any(CompetitionSceneScheduleTarget.class));

        ArgumentCaptor<CompetitionSceneScheduleTarget> captor =
                ArgumentCaptor.forClass(CompetitionSceneScheduleTarget.class);
        verify(targetMapper).restoreCompetitionSceneScheduleTarget(captor.capture());
        CompetitionSceneScheduleTarget restored = captor.getValue();
        assertEquals(Long.valueOf(99L), restored.getTargetId());
        assertEquals(Long.valueOf(3L), restored.getScheduleId());
        assertEquals("PERSON:4570", restored.getTargetKey());
        assertEquals(Long.valueOf(4570L), restored.getMemberId());
        assertEquals(CompetitionSceneConstants.TARGET_SOURCE_APPLY, restored.getTargetSource());
        assertEquals(CompetitionSceneConstants.DEL_FLAG_NORMAL, restored.getDelFlag());
    }

    @Test
    public void matchScheduleTargetsSkipsDuplicateKeyFromConcurrentInsert() {
        CompetitionSceneSchedule schedule = schedule();
        CompetitionApplyInfo applyInfo = applyInfo(4570L, "Alice");
        CompetitionSceneScheduleTarget activeTarget = activeTarget(100L, "PERSON:4570");

        when(scheduleMapper.selectCompetitionSceneScheduleById(3L)).thenReturn(schedule);
        when(competitionApplyInfoMapper.selectCompetitionApplyInfoList(any(CompetitionApplyInfo.class)))
                .thenReturn(Collections.singletonList(applyInfo));
        when(targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(3L))
                .thenReturn(Collections.emptyList());
        when(targetMapper.selectCompetitionSceneScheduleTargetByScheduleIdAndTargetKey(3L, "PERSON:4570"))
                .thenReturn(null, activeTarget);
        when(targetMapper.insertCompetitionSceneScheduleTarget(any(CompetitionSceneScheduleTarget.class)))
                .thenThrow(new DuplicateKeyException("Duplicate entry '3-PERSON:4570'"));

        CompetitionSceneMatchResult result = service.matchScheduleTargets(3L);

        assertEquals(Integer.valueOf(0), result.getMatchedCount());
        assertEquals(Integer.valueOf(1), result.getSkippedCount());
        verify(targetMapper, never()).restoreCompetitionSceneScheduleTarget(any(CompetitionSceneScheduleTarget.class));
    }

    private CompetitionSceneSchedule schedule() {
        CompetitionSceneSchedule schedule = new CompetitionSceneSchedule();
        schedule.setScheduleId(3L);
        schedule.setCompetitionSeriesId(88L);
        schedule.setCompetitionTrackId("TRACK-A");
        schedule.setCompetitionTrackName("Track A");
        schedule.setSecondLevelCode("GROUP-A");
        schedule.setSecondLevelName("Group A");
        schedule.setCredentialType(CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        schedule.setConfigDimension(CompetitionSceneConstants.DIMENSION_PERSON);
        schedule.setWaitingGroupCode("WAIT-A");
        schedule.setWaitingGroupName("Waiting A");
        return schedule;
    }

    private CompetitionApplyInfo applyInfo(Long memberId, String userName) {
        CompetitionApplyInfo applyInfo = new CompetitionApplyInfo();
        applyInfo.setMemberId(memberId);
        applyInfo.setUserId(9000L);
        applyInfo.setUserName(userName);
        applyInfo.setPhone("13800000000");
        applyInfo.setEmail("test@example.com");
        applyInfo.setIdCardType("ID_CARD");
        applyInfo.setIdCard("110101199001011234");
        applyInfo.setSchool("SCH-A");
        applyInfo.setSchoolName("School A");
        applyInfo.setOrgId(10L);
        applyInfo.setOrgName("Org A");
        applyInfo.setCompetitionRoleName(CompetitionSceneConstants.TARGET_ROLE_MEMBER);
        applyInfo.setCompetitionTrackId("TRACK-A");
        applyInfo.setCompetitionTrackName("Track A");
        applyInfo.setSecondLevelCode("GROUP-A");
        applyInfo.setSecondLevelName("Group A");
        applyInfo.setCheckStatus("3");
        applyInfo.setPayStatus("paid");
        return applyInfo;
    }

    private CompetitionSceneScheduleTarget deletedTarget(Long targetId, String targetKey) {
        CompetitionSceneScheduleTarget target = activeTarget(targetId, targetKey);
        target.setDelFlag(CompetitionSceneConstants.DEL_FLAG_DELETED);
        return target;
    }

    private CompetitionSceneScheduleTarget activeTarget(Long targetId, String targetKey) {
        CompetitionSceneScheduleTarget target = new CompetitionSceneScheduleTarget();
        target.setTargetId(targetId);
        target.setScheduleId(3L);
        target.setTargetKey(targetKey);
        target.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        return target;
    }

    private void inject(String fieldName, Object value) throws Exception {
        Field field = CompetitionSceneScheduleServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }
}
