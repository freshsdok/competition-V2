package com.teaching.competition.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneCredentialScopeGrant;
import com.teaching.competition.mapper.CompetitionSceneCredentialScopeGrantMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompetitionSceneCredentialScopeGrantServiceImplTest {

    private CompetitionSceneCredentialScopeGrantServiceImpl service;
    private CompetitionSceneCredentialScopeGrantMapper mapper;

    @Before
    public void setUp() throws Exception {
        service = new CompetitionSceneCredentialScopeGrantServiceImpl();
        mapper = mock(CompetitionSceneCredentialScopeGrantMapper.class);
        Field field = CompetitionSceneCredentialScopeGrantServiceImpl.class.getDeclaredField("grantMapper");
        field.setAccessible(true);
        field.set(service, mapper);
    }

    @Test
    public void hasAbilityIsConservative() {
        CompetitionSceneCredentialScopeGrant grant = grant("{\"identityVerify\":true,\"waiting\":true,\"scheduleEntry\":true,\"material\":false}");
        assertTrue(service.hasAbility(grant, "identityVerify"));
        assertTrue(service.hasAbility(grant, "IDENTITY_VERIFY"));
        assertTrue(service.hasAbility(grant, "waiting"));
        assertTrue(service.hasAbility(grant, CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN));
        assertTrue(service.hasAbility(grant, "scheduleEntry"));
        assertTrue(service.hasAbility(grant, "SCHEDULE_ENTRY"));
        assertFalse(service.hasAbility(grant, "material"));
        assertFalse(service.hasAbility(grant, "report"));
        assertFalse(service.hasAbility(grant, "unknownAbility"));
        assertFalse(service.hasAbility(grant, null));

        assertFalse(service.hasAbility(grant(null), "waiting"));
        assertFalse(service.hasAbility(grant(""), "waiting"));
        assertFalse(service.hasAbility(grant("{bad json"), "waiting"));
        assertFalse(service.hasAbility(grant("{\"waiting\":\"true\"}"), "waiting"));
        assertFalse(service.hasAbility(grant("{\"waiting\":1}"), "waiting"));
    }

    @Test
    public void checkScheduleAbilityChecksStatusDeletedWindowAndAbility() {
        CompetitionSceneCredentialScopeGrant active = grant("{\"waiting\":true}");
        active.setGrantStatus("ACTIVE");
        active.setDeleted(0);
        when(mapper.selectActiveScheduleGrants(anyLong(), anyLong()))
                .thenReturn(Collections.singletonList(active));
        assertTrue(service.checkScheduleAbility(1L, 10L, "waiting"));

        CompetitionSceneCredentialScopeGrant notStarted = grant("{\"waiting\":true}");
        notStarted.setGrantStatus("ACTIVE");
        notStarted.setDeleted(0);
        notStarted.setValidFrom(new Date(System.currentTimeMillis() + 60000L));
        when(mapper.selectActiveScheduleGrants(anyLong(), anyLong()))
                .thenReturn(Collections.singletonList(notStarted));
        assertFalse(service.checkScheduleAbility(1L, 10L, "waiting"));

        CompetitionSceneCredentialScopeGrant expired = grant("{\"waiting\":true}");
        expired.setGrantStatus("ACTIVE");
        expired.setDeleted(0);
        expired.setValidTo(new Date(System.currentTimeMillis() - 60000L));
        when(mapper.selectActiveScheduleGrants(anyLong(), anyLong()))
                .thenReturn(Collections.singletonList(expired));
        assertFalse(service.checkScheduleAbility(1L, 10L, "waiting"));

        CompetitionSceneCredentialScopeGrant revoked = grant("{\"waiting\":true}");
        revoked.setGrantStatus("REVOKED");
        revoked.setDeleted(0);
        when(mapper.selectActiveScheduleGrants(anyLong(), anyLong()))
                .thenReturn(Collections.singletonList(revoked));
        assertFalse(service.checkScheduleAbility(1L, 10L, "waiting"));

        CompetitionSceneCredentialScopeGrant deleted = grant("{\"waiting\":true}");
        deleted.setGrantStatus("ACTIVE");
        deleted.setDeleted(1);
        when(mapper.selectActiveScheduleGrants(anyLong(), anyLong()))
                .thenReturn(Collections.singletonList(deleted));
        assertFalse(service.checkScheduleAbility(1L, 10L, "waiting"));

        when(mapper.selectActiveScheduleGrants(anyLong(), anyLong())).thenReturn(List.of());
        assertFalse(service.checkScheduleAbility(1L, 10L, "waiting"));
        assertFalse(service.checkScheduleAbility(null, 10L, "waiting"));
        assertFalse(service.checkScheduleAbility(1L, null, "waiting"));
        assertFalse(service.checkScheduleAbility(1L, 10L, "unknownAbility"));
    }

    @Test
    public void insertGrantBuildsActiveKeyAndWhitelistedSnapshot() {
        CompetitionSceneCredentialScopeGrant grant = new CompetitionSceneCredentialScopeGrant();
        grant.setCredentialId(101L);
        grant.setCompetitionSeriesId(1L);
        grant.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE);
        grant.setScopeRefId(13L);
        grant.setSourceScheduleId(13L);
        grant.setSourceTargetId(44L);
        grant.setCredentialType(CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        grant.setSubjectType(CompetitionSceneConstants.SUBJECT_TYPE_USER);
        grant.setSubjectCode("1353");
        grant.setGrantSnapshotJson("{\"schedule\":{\"scheduleId\":13,\"scheduleName\":\"赛场A\"},"
                + "\"target\":{\"targetId\":44,\"userName\":\"张三\",\"phone\":\"13800000000\","
                + "\"email\":\"a@example.com\",\"idCardHash\":\"secret\",\"credentialToken\":\"tok\","
                + "\"qrContent\":\"csc_tok\",\"competitionRoleName\":\"MEMBER\",\"teamCode\":\"T001\","
                + "\"waitingGroupName\":\"第一组\"}}");

        service.insertGrant(grant);

        ArgumentCaptor<CompetitionSceneCredentialScopeGrant> captor =
                ArgumentCaptor.forClass(CompetitionSceneCredentialScopeGrant.class);
        verify(mapper).insertGrant(captor.capture());
        CompetitionSceneCredentialScopeGrant saved = captor.getValue();

        assertEquals("101:SCHEDULE:13:44", saved.getActiveGrantKey());
        assertEquals("SCHEDULE_TARGET", saved.getSourceType());
        assertNotNull(saved.getCreateTime());
        assertNotNull(saved.getUpdateTime());

        JSONObject snapshot = JSON.parseObject(saved.getGrantSnapshotJson());
        assertEquals(Integer.valueOf(13), snapshot.getInteger("scheduleId"));
        assertEquals("赛场A", snapshot.getString("scheduleName"));
        assertEquals(Integer.valueOf(44), snapshot.getInteger("targetId"));
        assertEquals("张三", snapshot.getString("targetName"));
        assertEquals("MEMBER", snapshot.getString("roleCode"));
        assertEquals("T001", snapshot.getString("teamCode"));
        assertEquals("第一组", snapshot.getString("groupName"));
        assertFalse(snapshot.containsKey("phone"));
        assertFalse(snapshot.containsKey("email"));
        assertFalse(snapshot.containsKey("idCardHash"));
        assertFalse(snapshot.containsKey("credentialToken"));
        assertFalse(snapshot.containsKey("qrContent"));
    }

    @Test
    public void revokedGrantHasNoActiveKey() {
        CompetitionSceneCredentialScopeGrant grant = new CompetitionSceneCredentialScopeGrant();
        grant.setCredentialId(101L);
        grant.setCompetitionSeriesId(1L);
        grant.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE);
        grant.setScopeRefId(13L);
        grant.setSourceScheduleId(13L);
        grant.setSourceTargetId(44L);
        grant.setCredentialType(CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        grant.setSubjectType(CompetitionSceneConstants.SUBJECT_TYPE_USER);
        grant.setSubjectCode("1353");
        grant.setGrantStatus("REVOKED");

        service.insertGrant(grant);

        ArgumentCaptor<CompetitionSceneCredentialScopeGrant> captor =
                ArgumentCaptor.forClass(CompetitionSceneCredentialScopeGrant.class);
        verify(mapper).insertGrant(captor.capture());
        assertNull(captor.getValue().getActiveGrantKey());
    }

    private CompetitionSceneCredentialScopeGrant grant(String abilityJson) {
        CompetitionSceneCredentialScopeGrant grant = new CompetitionSceneCredentialScopeGrant();
        grant.setAbilityJson(abilityJson);
        return grant;
    }
}
