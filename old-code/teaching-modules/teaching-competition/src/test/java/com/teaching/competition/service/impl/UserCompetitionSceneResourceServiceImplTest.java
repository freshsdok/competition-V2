package com.teaching.competition.service.impl;

import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.DictConstant;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.contant.CompetitionSceneResourceConstants;
import com.teaching.competition.domain.CompetitionSceneResourceBookableVO;
import com.teaching.competition.domain.CompetitionSceneResourceReservation;
import com.teaching.competition.domain.CompetitionSceneResourceReservationCancelReq;
import com.teaching.competition.domain.CompetitionSceneResourceReservationReq;
import com.teaching.competition.domain.CompetitionSceneResourceReservationVO;
import com.teaching.competition.domain.CompetitionSceneResourceSlot;
import com.teaching.competition.domain.CompetitionSceneResourceSlotGroupScope;
import com.teaching.competition.domain.CompetitionSceneResourceSlotQuery;
import com.teaching.competition.domain.CompetitionSceneResourceSlotVO;
import com.teaching.competition.domain.CompetitionSceneScheduleResource;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceQuery;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceVO;
import com.teaching.competition.domain.CompetitionSceneScheduleTarget;
import com.teaching.competition.exception.CompetitionSceneReservationException;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.mapper.CompetitionSceneCredentialMapper;
import com.teaching.competition.mapper.CompetitionSceneResourceMapper;
import com.teaching.competition.mapper.CompetitionSceneResourceReservationMapper;
import com.teaching.competition.mapper.CompetitionSceneResourceScheduleScopeMapper;
import com.teaching.competition.mapper.CompetitionSceneResourceSlotGroupScopeMapper;
import com.teaching.competition.mapper.CompetitionSceneResourceSlotMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleResourceMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleTargetMapper;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserCompetitionSceneResourceServiceImplTest {

    private static final Long USER_ID = 1357L;
    private static final Long TEAM_MEMBER_B_ID = 2468L;
    private static final Long SOURCE_SCHEDULE_ID = 3L;
    private static final Long DEPLOY_SCHEDULE_ID = 30L;
    private static final Long SCHEDULE_RESOURCE_ID = 10L;
    private static final Long SLOT_ID = 20L;
    private static final Long COMPETITION_SERIES_ID = 100L;

    private UserCompetitionSceneResourceServiceImpl service;
    private CompetitionSceneScheduleResourceMapper scheduleResourceMapper;
    private CompetitionSceneResourceMapper resourceMapper;
    private CompetitionSceneResourceSlotMapper slotMapper;
    private CompetitionSceneResourceReservationMapper reservationMapper;
    private CompetitionSceneResourceScheduleScopeMapper scheduleScopeMapper;
    private CompetitionSceneResourceSlotGroupScopeMapper slotGroupScopeMapper;
    private CompetitionSceneScheduleTargetMapper targetMapper;
    private CompetitionSceneCredentialMapper credentialMapper;
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Before
    public void setUp() throws Exception {
        service = new UserCompetitionSceneResourceServiceImpl();
        scheduleResourceMapper = mock(CompetitionSceneScheduleResourceMapper.class);
        resourceMapper = mock(CompetitionSceneResourceMapper.class);
        slotMapper = mock(CompetitionSceneResourceSlotMapper.class);
        reservationMapper = mock(CompetitionSceneResourceReservationMapper.class);
        scheduleScopeMapper = mock(CompetitionSceneResourceScheduleScopeMapper.class);
        slotGroupScopeMapper = mock(CompetitionSceneResourceSlotGroupScopeMapper.class);
        targetMapper = mock(CompetitionSceneScheduleTargetMapper.class);
        credentialMapper = mock(CompetitionSceneCredentialMapper.class);
        competitionApplyInfoMapper = mock(CompetitionApplyInfoMapper.class);

        inject("scheduleResourceMapper", scheduleResourceMapper);
        inject("resourceMapper", resourceMapper);
        inject("slotMapper", slotMapper);
        inject("reservationMapper", reservationMapper);
        inject("scheduleScopeMapper", scheduleScopeMapper);
        inject("slotGroupScopeMapper", slotGroupScopeMapper);
        inject("targetMapper", targetMapper);
        inject("credentialMapper", credentialMapper);
        inject("competitionApplyInfoMapper", competitionApplyInfoMapper);

        when(targetMapper.selectCompetitionSceneScheduleTargetList(any(CompetitionSceneScheduleTarget.class)))
                .thenReturn(Collections.singletonList(personTarget(USER_ID, SOURCE_SCHEDULE_ID, "G1", "组别一")));
        when(targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(SOURCE_SCHEDULE_ID))
                .thenReturn(Collections.singletonList(personTarget(USER_ID, SOURCE_SCHEDULE_ID, "G1", "组别一")));
        when(scheduleResourceMapper.selectCompetitionSceneScheduleResourceList(any(CompetitionSceneScheduleResourceQuery.class)))
                .thenReturn(Collections.singletonList(scheduleResourceVO(SCHEDULE_RESOURCE_ID, true)));
        when(scheduleResourceMapper.selectCompetitionSceneScheduleResourceById(SCHEDULE_RESOURCE_ID))
                .thenReturn(scheduleResourceVO(SCHEDULE_RESOURCE_ID, true));
        when(scheduleResourceMapper.selectCompetitionSceneScheduleResourceEntityById(SCHEDULE_RESOURCE_ID))
                .thenReturn(scheduleResourceEntity(SCHEDULE_RESOURCE_ID, true, 4));
        when(slotMapper.selectCompetitionSceneResourceSlotEntityById(SLOT_ID))
                .thenReturn(slotEntity(SLOT_ID, SCHEDULE_RESOURCE_ID, CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 4));
        when(slotMapper.selectCompetitionSceneResourceSlotList(any(CompetitionSceneResourceSlotQuery.class)))
                .thenReturn(Collections.singletonList(slotVO(SLOT_ID, SCHEDULE_RESOURCE_ID)));
        when(slotGroupScopeMapper.selectBySlotId(anyLong())).thenReturn(Collections.emptyList());
    }

    @Test
    public void bookableListFiltersByScheduleScope() {
        when(scheduleScopeMapper.countEnabledScope(SCHEDULE_RESOURCE_ID, SOURCE_SCHEDULE_ID)).thenReturn(0);

        assertTrue(service.selectBookableResourceList(USER_ID, null).isEmpty());

        when(scheduleScopeMapper.countEnabledScope(SCHEDULE_RESOURCE_ID, SOURCE_SCHEDULE_ID)).thenReturn(1);
        List<CompetitionSceneResourceBookableVO> list = service.selectBookableResourceList(USER_ID, null);

        assertEquals(1, list.size());
        assertEquals(SOURCE_SCHEDULE_ID, list.get(0).getUserSourceScheduleId());
        assertEquals("G1", list.get(0).getGroupCode());
        assertFalse(Boolean.TRUE.equals(list.get(0).getHasExistingReservation()));
    }

    @Test
    public void slotListAllowsNoGroupAndFiltersConfiguredGroup() {
        allowScheduleScope();
        when(slotGroupScopeMapper.countEnabledBySlotId(SLOT_ID)).thenReturn(0);

        assertEquals(1, service.selectBookableSlotList(USER_ID, SCHEDULE_RESOURCE_ID).size());

        when(slotGroupScopeMapper.countEnabledBySlotId(SLOT_ID)).thenReturn(1);
        when(slotGroupScopeMapper.countAllowedGroup(SLOT_ID, "G1")).thenReturn(1);
        when(slotGroupScopeMapper.selectBySlotId(SLOT_ID))
                .thenReturn(Collections.singletonList(groupScope("G1", "组别一")));
        List<CompetitionSceneResourceSlotVO> hit = service.selectBookableSlotList(USER_ID, SCHEDULE_RESOURCE_ID);
        assertEquals(1, hit.size());
        assertEquals(Collections.singletonList("组别一"), hit.get(0).getAllowedGroupNames());

        when(slotGroupScopeMapper.countAllowedGroup(SLOT_ID, "G1")).thenReturn(0);
        assertTrue(service.selectBookableSlotList(USER_ID, SCHEDULE_RESOURCE_ID).isEmpty());
    }

    @Test
    public void submitRejectsStartedClosedAndGroupMismatchSlots() {
        allowScheduleScope();
        CompetitionSceneResourceReservationReq req = request("k-started");
        when(slotMapper.selectCompetitionSceneResourceSlotEntityById(SLOT_ID))
                .thenReturn(startedSlot());
        assertBusinessError(CompetitionSceneResourceConstants.ERROR_SLOT_NOT_OPEN,
                () -> service.submitReservation(USER_ID, req));

        when(slotMapper.selectCompetitionSceneResourceSlotEntityById(SLOT_ID))
                .thenReturn(slotEntity(SLOT_ID, SCHEDULE_RESOURCE_ID, CompetitionSceneResourceConstants.SLOT_STATUS_CLOSED, 4));
        assertBusinessError(CompetitionSceneResourceConstants.ERROR_SLOT_NOT_OPEN,
                () -> service.submitReservation(USER_ID, request("k-closed")));

        when(slotMapper.selectCompetitionSceneResourceSlotEntityById(SLOT_ID))
                .thenReturn(slotEntity(SLOT_ID, SCHEDULE_RESOURCE_ID, CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 4));
        when(slotGroupScopeMapper.countEnabledBySlotId(SLOT_ID)).thenReturn(1);
        when(slotGroupScopeMapper.countAllowedGroup(SLOT_ID, "G1")).thenReturn(0);
        assertBusinessError(CompetitionSceneResourceConstants.ERROR_SLOT_GROUP_DENIED,
                () -> service.submitReservation(USER_ID, request("k-group")));
    }

    @Test
    public void submitSharedReservationUsesPeopleCountAndDoesNotDeductDevices() {
        useTeamSubject(USER_ID, TEAM_MEMBER_B_ID, 3);
        allowScheduleScope();
        when(slotMapper.reserveSharedCompetitionSceneResourceSlotCapacity(eq(SLOT_ID), eq(3), any(Date.class), anyString()))
                .thenReturn(1);
        mockInsertReservation(88L);
        when(reservationMapper.selectCompetitionSceneResourceReservationById(88L))
                .thenReturn(reservationVO(88L, "TEAM", "T1"));

        CompetitionSceneResourceReservationVO result = service.submitReservation(USER_ID, request("team-shared"));

        assertEquals(Long.valueOf(88L), result.getReservationId());
        ArgumentCaptor<CompetitionSceneResourceReservation> captor =
                ArgumentCaptor.forClass(CompetitionSceneResourceReservation.class);
        verify(reservationMapper).insertCompetitionSceneResourceReservation(captor.capture());
        assertEquals("RESV:100:TEAM:T1", captor.getValue().getActiveReservationKey());
        assertEquals(Integer.valueOf(3), captor.getValue().getOccupyPeopleCount());
        assertEquals(Integer.valueOf(1), captor.getValue().getReservedDeviceCount());
        assertEquals(Integer.valueOf(3), captor.getValue().getReservedWorkstationCount());
        assertEquals(Boolean.TRUE, captor.getValue().getSharedOccupancySnapshot());
        verify(slotMapper, never()).reserveExclusiveCompetitionSceneResourceSlotCapacity(
                anyLong(), anyInt(), anyInt(), any(Date.class), anyString());
    }

    @Test
    public void submitExclusiveReservationDeductsWholeDevicesAndWorkstations() {
        useTeamSubject(USER_ID, TEAM_MEMBER_B_ID, 5);
        allowScheduleScope();
        when(scheduleResourceMapper.selectCompetitionSceneScheduleResourceEntityById(SCHEDULE_RESOURCE_ID))
                .thenReturn(scheduleResourceEntity(SCHEDULE_RESOURCE_ID, false, 4));
        when(slotMapper.reserveExclusiveCompetitionSceneResourceSlotCapacity(
                eq(SLOT_ID), eq(2), eq(8), any(Date.class), anyString())).thenReturn(1);
        mockInsertReservation(89L);
        when(reservationMapper.selectCompetitionSceneResourceReservationById(89L))
                .thenReturn(reservationVO(89L, "TEAM", "T1"));

        service.submitReservation(USER_ID, request("team-exclusive"));

        verify(slotMapper).reserveExclusiveCompetitionSceneResourceSlotCapacity(
                eq(SLOT_ID), eq(2), eq(8), any(Date.class), anyString());
        verify(slotMapper, never()).reserveSharedCompetitionSceneResourceSlotCapacity(
                anyLong(), anyInt(), any(Date.class), anyString());
    }

    @Test
    public void submitPersonalReservationUsesOnePersonCapacity() {
        allowScheduleScope();
        when(slotMapper.reserveSharedCompetitionSceneResourceSlotCapacity(eq(SLOT_ID), eq(1), any(Date.class), anyString()))
                .thenReturn(1);
        mockInsertReservation(90L);
        when(reservationMapper.selectCompetitionSceneResourceReservationById(90L))
                .thenReturn(reservationVO(90L, "USER", String.valueOf(USER_ID)));

        service.submitReservation(USER_ID, request("personal"));

        ArgumentCaptor<CompetitionSceneResourceReservation> captor =
                ArgumentCaptor.forClass(CompetitionSceneResourceReservation.class);
        verify(reservationMapper).insertCompetitionSceneResourceReservation(captor.capture());
        assertEquals(Integer.valueOf(1), captor.getValue().getOccupyPeopleCount());
        assertEquals(Integer.valueOf(1), captor.getValue().getReservedWorkstationCount());
    }

    @Test
    public void submitReturnsIdempotentResultWithoutCapacityDeduction() {
        CompetitionSceneResourceReservationVO existing = reservationVO(77L, "USER", String.valueOf(USER_ID));
        when(reservationMapper.selectReservationByIdempotencyKey("repeat")).thenReturn(existing);

        CompetitionSceneResourceReservationVO result = service.submitReservation(USER_ID, request("repeat"));

        assertEquals(Long.valueOf(77L), result.getReservationId());
        verify(reservationMapper, never()).insertCompetitionSceneResourceReservation(any(CompetitionSceneResourceReservation.class));
        verify(slotMapper, never()).reserveSharedCompetitionSceneResourceSlotCapacity(
                anyLong(), anyInt(), any(Date.class), anyString());
    }

    @Test
    public void submitActiveKeyConflictReturnsAlreadyReserved() {
        allowScheduleScope();
        CompetitionSceneResourceReservationVO existing = reservationVO(66L, "USER", String.valueOf(USER_ID));
        when(reservationMapper.selectEffectiveReservationByActiveKey("RESV:100:USER:" + USER_ID)).thenReturn(existing);

        assertBusinessError(CompetitionSceneResourceConstants.ERROR_ALREADY_RESERVED,
                () -> service.submitReservation(USER_ID, request("active-conflict")));
        verify(slotMapper, never()).reserveSharedCompetitionSceneResourceSlotCapacity(
                anyLong(), anyInt(), any(Date.class), anyString());
    }

    @Test
    public void duplicateKeyConflictsResolveToIdempotencyOrActiveReservation() {
        allowScheduleScope();
        doThrow(new DuplicateKeyException("uk_scene_resource_idempotency_key"))
                .when(reservationMapper).insertCompetitionSceneResourceReservation(any(CompetitionSceneResourceReservation.class));
        when(reservationMapper.selectReservationByIdempotencyKey("dup-idem"))
                .thenReturn(null, null, reservationVO(55L, "USER", String.valueOf(USER_ID)));

        CompetitionSceneResourceReservationVO idem = service.submitReservation(USER_ID, request("dup-idem"));
        assertEquals(Long.valueOf(55L), idem.getReservationId());
        verify(slotMapper, never()).reserveSharedCompetitionSceneResourceSlotCapacity(
                anyLong(), anyInt(), any(Date.class), anyString());

        when(reservationMapper.selectReservationByIdempotencyKey("dup-active")).thenReturn(null);
        when(reservationMapper.selectEffectiveReservationByActiveKey("RESV:100:USER:" + USER_ID))
                .thenReturn(reservationVO(56L, "USER", String.valueOf(USER_ID)));
        assertBusinessError(CompetitionSceneResourceConstants.ERROR_ALREADY_RESERVED,
                () -> service.submitReservation(USER_ID, request("dup-active")));
    }

    @Test
    public void wrappedActiveKeyConflictReturnsAlreadyReservedWithoutSql500() {
        allowScheduleScope();
        doThrow(new DataIntegrityViolationException(
                "Duplicate entry 'RESV:100:USER:1357' for key 'uk_scene_resource_active_reservation_key'"))
                .when(reservationMapper).insertCompetitionSceneResourceReservation(any(CompetitionSceneResourceReservation.class));
        when(reservationMapper.selectEffectiveReservationByActiveKey("RESV:100:USER:" + USER_ID))
                .thenReturn(reservationVO(57L, "USER", String.valueOf(USER_ID)));

        assertBusinessError(CompetitionSceneResourceConstants.ERROR_ALREADY_RESERVED,
                () -> service.submitReservation(USER_ID, request("dup-active-wrapped")));

        verify(slotMapper, never()).reserveSharedCompetitionSceneResourceSlotCapacity(
                anyLong(), anyInt(), any(Date.class), anyString());
        verify(slotMapper, never()).reserveExclusiveCompetitionSceneResourceSlotCapacity(
                anyLong(), anyInt(), anyInt(), any(Date.class), anyString());
    }

    @Test
    public void wrappedIdempotencyConflictReturnsExistingReservationWithoutDeductingCapacity() {
        allowScheduleScope();
        doThrow(new DataIntegrityViolationException(
                "Duplicate entry 'idem-wrapped' for key 'uk_scene_resource_idempotency_key'"))
                .when(reservationMapper).insertCompetitionSceneResourceReservation(any(CompetitionSceneResourceReservation.class));
        when(reservationMapper.selectReservationByIdempotencyKey("idem-wrapped"))
                .thenReturn(null, null, reservationVO(58L, "USER", String.valueOf(USER_ID)));

        CompetitionSceneResourceReservationVO result = service.submitReservation(USER_ID, request("idem-wrapped"));

        assertEquals(Long.valueOf(58L), result.getReservationId());
        verify(slotMapper, never()).reserveSharedCompetitionSceneResourceSlotCapacity(
                anyLong(), anyInt(), any(Date.class), anyString());
        verify(slotMapper, never()).reserveExclusiveCompetitionSceneResourceSlotCapacity(
                anyLong(), anyInt(), anyInt(), any(Date.class), anyString());
    }

    @Test
    public void duplicateActiveKeyConflictRetriesAndReturnsAlreadyReserved() {
        allowScheduleScope();
        doThrow(new DuplicateKeyException(
                "Duplicate entry 'RESV:100:USER:1357' for key 'uk_scene_resource_active_reservation_key'"))
                .when(reservationMapper).insertCompetitionSceneResourceReservation(any(CompetitionSceneResourceReservation.class));
        when(reservationMapper.selectEffectiveReservationByActiveKey("RESV:100:USER:" + USER_ID))
                .thenReturn(null, null, reservationVO(59L, "USER", String.valueOf(USER_ID)));

        try {
            service.submitReservation(USER_ID, request("dup-active-retry"));
        } catch (CompetitionSceneReservationException e) {
            assertEquals(CompetitionSceneResourceConstants.ERROR_ALREADY_RESERVED, e.getErrorCode());
            assertNotNull(e.getExistingReservation());
            assertEquals(Long.valueOf(59L), e.getExistingReservation().getReservationId());
            verify(reservationMapper, org.mockito.Mockito.times(3))
                    .selectEffectiveReservationByActiveKey("RESV:100:USER:" + USER_ID);
            verify(slotMapper, never()).reserveSharedCompetitionSceneResourceSlotCapacity(
                    anyLong(), anyInt(), any(Date.class), anyString());
            return;
        }
        throw new AssertionError("Expected duplicate active key to return ALREADY_RESERVED");
    }

    @Test
    public void duplicateActiveKeyConflictReturnsRetryLaterWhenExistingReservationCannotBeRead() {
        allowScheduleScope();
        doThrow(new DuplicateKeyException(
                "Duplicate entry 'RESV:100:USER:1357' for key 'uk_scene_resource_active_reservation_key'"))
                .when(reservationMapper).insertCompetitionSceneResourceReservation(any(CompetitionSceneResourceReservation.class));
        when(reservationMapper.selectEffectiveReservationByActiveKey("RESV:100:USER:" + USER_ID))
                .thenReturn(null);

        assertBusinessError(CompetitionSceneResourceConstants.ERROR_RESERVATION_CONFLICT_RETRY_LATER,
                () -> service.submitReservation(USER_ID, request("dup-active-missing")));
        verify(reservationMapper, org.mockito.Mockito.times(4))
                .selectEffectiveReservationByActiveKey("RESV:100:USER:" + USER_ID);
        verify(slotMapper, never()).reserveSharedCompetitionSceneResourceSlotCapacity(
                anyLong(), anyInt(), any(Date.class), anyString());
    }

    @Test
    public void duplicateIdempotencyConflictReturnsRetryLaterWhenOriginalReservationCannotBeRead() {
        allowScheduleScope();
        doThrow(new DuplicateKeyException(
                "Duplicate entry 'idem-missing' for key 'uk_scene_resource_idempotency_key'"))
                .when(reservationMapper).insertCompetitionSceneResourceReservation(any(CompetitionSceneResourceReservation.class));
        when(reservationMapper.selectReservationByIdempotencyKey("idem-missing"))
                .thenReturn(null);

        assertBusinessError(CompetitionSceneResourceConstants.ERROR_IDEMPOTENCY_CONFLICT_RETRY_LATER,
                () -> service.submitReservation(USER_ID, request("idem-missing")));
        verify(reservationMapper, org.mockito.Mockito.times(4))
                .selectReservationByIdempotencyKey("idem-missing");
        verify(slotMapper, never()).reserveSharedCompetitionSceneResourceSlotCapacity(
                anyLong(), anyInt(), any(Date.class), anyString());
    }

    @Test
    public void otherSqlIntegrityExceptionsAreNotConvertedToReservationConflict() {
        allowScheduleScope();
        DataIntegrityViolationException exception = new DataIntegrityViolationException("foreign key failed",
                new SQLIntegrityConstraintViolationException("Cannot add or update a child row"));
        doThrow(exception)
                .when(reservationMapper).insertCompetitionSceneResourceReservation(any(CompetitionSceneResourceReservation.class));

        try {
            service.submitReservation(USER_ID, request("other-sql"));
        } catch (DataIntegrityViolationException e) {
            assertEquals(exception, e);
            return;
        }
        throw new AssertionError("Expected non reservation unique conflict to be rethrown");
    }

    @Test
    public void capacityUpdateFailureRejectsReservation() {
        allowScheduleScope();
        mockInsertReservation(91L);
        when(slotMapper.reserveSharedCompetitionSceneResourceSlotCapacity(eq(SLOT_ID), eq(1), any(Date.class), anyString()))
                .thenReturn(0);

        assertBusinessError(CompetitionSceneResourceConstants.ERROR_CAPACITY_NOT_ENOUGH,
                () -> service.submitReservation(USER_ID, request("capacity-fail")));
    }

    @Test
    public void sharedReservationMarksFullOnlyWhenWorkstationsAreExhausted() {
        allowScheduleScope();
        mockInsertReservation(92L);
        when(reservationMapper.selectCompetitionSceneResourceReservationById(92L))
                .thenReturn(reservationVO(92L, "USER", String.valueOf(USER_ID)));
        when(slotMapper.selectCompetitionSceneResourceSlotEntityById(SLOT_ID))
                .thenReturn(slotEntity(SLOT_ID, SCHEDULE_RESOURCE_ID, CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 4),
                        slotWithRemaining(CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 2, 0));
        when(slotMapper.reserveSharedCompetitionSceneResourceSlotCapacity(eq(SLOT_ID), eq(1), any(Date.class), anyString()))
                .thenReturn(1);

        service.submitReservation(USER_ID, request("shared-full"));

        verify(slotMapper).updateCompetitionSceneResourceSlotStatusIfCurrent(eq(SLOT_ID),
                eq(CompetitionSceneResourceConstants.SLOT_STATUS_OPEN),
                eq(CompetitionSceneResourceConstants.SLOT_STATUS_FULL), anyString());
    }

    @Test
    public void sharedReservationKeepsOpenWhenWorkstationsRemainEvenIfDevicesAreZero() {
        allowScheduleScope();
        mockInsertReservation(93L);
        when(reservationMapper.selectCompetitionSceneResourceReservationById(93L))
                .thenReturn(reservationVO(93L, "USER", String.valueOf(USER_ID)));
        when(slotMapper.selectCompetitionSceneResourceSlotEntityById(SLOT_ID))
                .thenReturn(slotEntity(SLOT_ID, SCHEDULE_RESOURCE_ID, CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 4),
                        slotWithRemaining(CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 0, 1));
        when(slotMapper.reserveSharedCompetitionSceneResourceSlotCapacity(eq(SLOT_ID), eq(1), any(Date.class), anyString()))
                .thenReturn(1);

        service.submitReservation(USER_ID, request("shared-open"));

        verify(slotMapper, never()).updateCompetitionSceneResourceSlotStatusIfCurrent(eq(SLOT_ID),
                eq(CompetitionSceneResourceConstants.SLOT_STATUS_OPEN),
                eq(CompetitionSceneResourceConstants.SLOT_STATUS_FULL), anyString());
    }

    @Test
    public void exclusiveReservationKeepsOpenWhenDeviceAndWorkstationRemain() {
        allowScheduleScope();
        when(scheduleResourceMapper.selectCompetitionSceneScheduleResourceEntityById(SCHEDULE_RESOURCE_ID))
                .thenReturn(scheduleResourceEntity(SCHEDULE_RESOURCE_ID, false, 4));
        mockInsertReservation(94L);
        when(reservationMapper.selectCompetitionSceneResourceReservationById(94L))
                .thenReturn(reservationVO(94L, "USER", String.valueOf(USER_ID)));
        when(slotMapper.selectCompetitionSceneResourceSlotEntityById(SLOT_ID))
                .thenReturn(slotEntity(SLOT_ID, SCHEDULE_RESOURCE_ID, CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 4),
                        slotWithRemaining(CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 1, 4));
        when(slotMapper.reserveExclusiveCompetitionSceneResourceSlotCapacity(eq(SLOT_ID), eq(1), eq(4),
                any(Date.class), anyString())).thenReturn(1);

        service.submitReservation(USER_ID, request("exclusive-open"));

        verify(slotMapper, never()).updateCompetitionSceneResourceSlotStatusIfCurrent(eq(SLOT_ID),
                eq(CompetitionSceneResourceConstants.SLOT_STATUS_OPEN),
                eq(CompetitionSceneResourceConstants.SLOT_STATUS_FULL), anyString());
    }

    @Test
    public void exclusiveReservationMarksFullWhenDeviceIsExhausted() {
        allowScheduleScope();
        when(scheduleResourceMapper.selectCompetitionSceneScheduleResourceEntityById(SCHEDULE_RESOURCE_ID))
                .thenReturn(scheduleResourceEntity(SCHEDULE_RESOURCE_ID, false, 4));
        mockInsertReservation(95L);
        when(reservationMapper.selectCompetitionSceneResourceReservationById(95L))
                .thenReturn(reservationVO(95L, "USER", String.valueOf(USER_ID)));
        when(slotMapper.selectCompetitionSceneResourceSlotEntityById(SLOT_ID))
                .thenReturn(slotEntity(SLOT_ID, SCHEDULE_RESOURCE_ID, CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 4),
                        slotWithRemaining(CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 0, 4));
        when(slotMapper.reserveExclusiveCompetitionSceneResourceSlotCapacity(eq(SLOT_ID), eq(1), eq(4),
                any(Date.class), anyString())).thenReturn(1);

        service.submitReservation(USER_ID, request("exclusive-device-full"));

        verify(slotMapper).updateCompetitionSceneResourceSlotStatusIfCurrent(eq(SLOT_ID),
                eq(CompetitionSceneResourceConstants.SLOT_STATUS_OPEN),
                eq(CompetitionSceneResourceConstants.SLOT_STATUS_FULL), anyString());
    }

    @Test
    public void exclusiveReservationMarksFullWhenWorkstationIsExhausted() {
        allowScheduleScope();
        when(scheduleResourceMapper.selectCompetitionSceneScheduleResourceEntityById(SCHEDULE_RESOURCE_ID))
                .thenReturn(scheduleResourceEntity(SCHEDULE_RESOURCE_ID, false, 4));
        mockInsertReservation(96L);
        when(reservationMapper.selectCompetitionSceneResourceReservationById(96L))
                .thenReturn(reservationVO(96L, "USER", String.valueOf(USER_ID)));
        when(slotMapper.selectCompetitionSceneResourceSlotEntityById(SLOT_ID))
                .thenReturn(slotEntity(SLOT_ID, SCHEDULE_RESOURCE_ID, CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 4),
                        slotWithRemaining(CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 1, 0));
        when(slotMapper.reserveExclusiveCompetitionSceneResourceSlotCapacity(eq(SLOT_ID), eq(1), eq(4),
                any(Date.class), anyString())).thenReturn(1);

        service.submitReservation(USER_ID, request("exclusive-workstation-full"));

        verify(slotMapper).updateCompetitionSceneResourceSlotStatusIfCurrent(eq(SLOT_ID),
                eq(CompetitionSceneResourceConstants.SLOT_STATUS_OPEN),
                eq(CompetitionSceneResourceConstants.SLOT_STATUS_FULL), anyString());
    }

    @Test
    public void myReservationListUsesCompetitionSeriesSubjectVisibility() {
        useTeamSubject(USER_ID, TEAM_MEMBER_B_ID, 3);
        CompetitionSceneResourceReservationVO teamReservation = reservationVO(99L, "TEAM", "T1");
        when(reservationMapper.selectVisibleCompetitionSceneResourceReservationList(any()))
                .thenReturn(Collections.singletonList(teamReservation));

        List<CompetitionSceneResourceReservationVO> list = service.selectMyReservationList(TEAM_MEMBER_B_ID);

        assertEquals(1, list.size());
        assertEquals(Long.valueOf(99L), list.get(0).getReservationId());
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(reservationMapper).selectVisibleCompetitionSceneResourceReservationList(captor.capture());
        assertEquals(1, captor.getValue().size());
    }

    @Test
    public void cancelUsesSnapshotAndDoesNotReleaseTwice() {
        when(targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(SOURCE_SCHEDULE_ID))
                .thenReturn(Collections.singletonList(personTarget(USER_ID, SOURCE_SCHEDULE_ID, "G1", "组别一")));
        CompetitionSceneResourceReservation reservation = reservationEntity(101L, true);
        when(reservationMapper.selectCompetitionSceneResourceReservationEntityById(101L)).thenReturn(reservation);
        when(slotMapper.selectCompetitionSceneResourceSlotEntityById(SLOT_ID))
                .thenReturn(slotEntity(SLOT_ID, SCHEDULE_RESOURCE_ID, CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 4));
        when(reservationMapper.cancelCompetitionSceneResourceReservation(eq(101L), any(), anyString())).thenReturn(1);
        when(reservationMapper.selectCompetitionSceneResourceReservationById(101L))
                .thenReturn(reservationVO(101L, "USER", String.valueOf(USER_ID)));

        service.cancelReservation(USER_ID, cancelReq(101L));

        verify(slotMapper).releaseSharedCompetitionSceneResourceSlotCapacity(eq(SLOT_ID), eq(3), anyString());
        verify(slotMapper, never()).releaseExclusiveCompetitionSceneResourceSlotCapacity(
                anyLong(), anyInt(), anyInt(), anyString());

        CompetitionSceneResourceReservation cancelled = reservationEntity(102L, false);
        cancelled.setReservationStatus(CompetitionSceneResourceConstants.RESERVATION_STATUS_CANCELLED);
        when(reservationMapper.selectCompetitionSceneResourceReservationEntityById(102L)).thenReturn(cancelled);
        when(reservationMapper.selectCompetitionSceneResourceReservationById(102L))
                .thenReturn(reservationVO(102L, "USER", String.valueOf(USER_ID)));
        service.cancelReservation(USER_ID, cancelReq(102L));
        verify(slotMapper, never()).releaseExclusiveCompetitionSceneResourceSlotCapacity(
                eq(SLOT_ID), eq(2), eq(8), anyString());
    }

    @Test
    public void cancelExclusiveReservationReleasesDevicesAndWorkstations() {
        when(targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(SOURCE_SCHEDULE_ID))
                .thenReturn(Collections.singletonList(personTarget(USER_ID, SOURCE_SCHEDULE_ID, "G1", "组别一")));
        CompetitionSceneResourceReservation reservation = reservationEntity(103L, false);
        when(reservationMapper.selectCompetitionSceneResourceReservationEntityById(103L)).thenReturn(reservation);
        when(slotMapper.selectCompetitionSceneResourceSlotEntityById(SLOT_ID))
                .thenReturn(slotEntity(SLOT_ID, SCHEDULE_RESOURCE_ID, CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 4));
        when(reservationMapper.cancelCompetitionSceneResourceReservation(eq(103L), any(), anyString())).thenReturn(1);
        when(reservationMapper.selectCompetitionSceneResourceReservationById(103L))
                .thenReturn(reservationVO(103L, "USER", String.valueOf(USER_ID)));

        service.cancelReservation(USER_ID, cancelReq(103L));

        verify(slotMapper).releaseExclusiveCompetitionSceneResourceSlotCapacity(eq(SLOT_ID), eq(2), eq(8), anyString());
    }

    @Test
    public void cancelRestoresFullSlotToOpenWhenCapacityRecoveredBeforeStart() {
        when(targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(SOURCE_SCHEDULE_ID))
                .thenReturn(Collections.singletonList(personTarget(USER_ID, SOURCE_SCHEDULE_ID, "G1", "组别一")));
        CompetitionSceneResourceReservation reservation = reservationEntity(104L, true);
        when(reservationMapper.selectCompetitionSceneResourceReservationEntityById(104L)).thenReturn(reservation);
        when(slotMapper.selectCompetitionSceneResourceSlotEntityById(SLOT_ID))
                .thenReturn(slotEntity(SLOT_ID, SCHEDULE_RESOURCE_ID, CompetitionSceneResourceConstants.SLOT_STATUS_FULL, 4),
                        slotWithRemaining(CompetitionSceneResourceConstants.SLOT_STATUS_FULL, 2, 3));
        when(reservationMapper.cancelCompetitionSceneResourceReservation(eq(104L), any(), anyString())).thenReturn(1);
        when(slotMapper.releaseSharedCompetitionSceneResourceSlotCapacity(eq(SLOT_ID), eq(3), anyString())).thenReturn(1);
        when(reservationMapper.selectCompetitionSceneResourceReservationById(104L))
                .thenReturn(reservationVO(104L, "USER", String.valueOf(USER_ID)));

        service.cancelReservation(USER_ID, cancelReq(104L));

        verify(slotMapper).updateCompetitionSceneResourceSlotStatusIfCurrent(eq(SLOT_ID),
                eq(CompetitionSceneResourceConstants.SLOT_STATUS_FULL),
                eq(CompetitionSceneResourceConstants.SLOT_STATUS_OPEN), anyString());
    }

    @Test
    public void cancelDoesNotRestoreClosedSlotToOpen() {
        when(targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(SOURCE_SCHEDULE_ID))
                .thenReturn(Collections.singletonList(personTarget(USER_ID, SOURCE_SCHEDULE_ID, "G1", "组别一")));
        CompetitionSceneResourceReservation reservation = reservationEntity(105L, false);
        when(reservationMapper.selectCompetitionSceneResourceReservationEntityById(105L)).thenReturn(reservation);
        when(slotMapper.selectCompetitionSceneResourceSlotEntityById(SLOT_ID))
                .thenReturn(slotEntity(SLOT_ID, SCHEDULE_RESOURCE_ID, CompetitionSceneResourceConstants.SLOT_STATUS_CLOSED, 4),
                        slotWithRemaining(CompetitionSceneResourceConstants.SLOT_STATUS_CLOSED, 2, 8));
        when(reservationMapper.cancelCompetitionSceneResourceReservation(eq(105L), any(), anyString())).thenReturn(1);
        when(slotMapper.releaseExclusiveCompetitionSceneResourceSlotCapacity(eq(SLOT_ID), eq(2), eq(8), anyString()))
                .thenReturn(1);
        when(reservationMapper.selectCompetitionSceneResourceReservationById(105L))
                .thenReturn(reservationVO(105L, "USER", String.valueOf(USER_ID)));

        service.cancelReservation(USER_ID, cancelReq(105L));

        verify(slotMapper, never()).updateCompetitionSceneResourceSlotStatusIfCurrent(eq(SLOT_ID),
                eq(CompetitionSceneResourceConstants.SLOT_STATUS_FULL),
                eq(CompetitionSceneResourceConstants.SLOT_STATUS_OPEN), anyString());
    }

    @Test
    public void cancelStatusConstantUsesDoubleLSpelling() {
        assertEquals("CANCELLED", CompetitionSceneResourceConstants.RESERVATION_STATUS_CANCELLED);
    }

    private void allowScheduleScope() {
        when(scheduleScopeMapper.countEnabledScope(SCHEDULE_RESOURCE_ID, SOURCE_SCHEDULE_ID)).thenReturn(1);
    }

    private void mockInsertReservation(Long reservationId) {
        doAnswer(invocation -> {
            CompetitionSceneResourceReservation reservation = invocation.getArgument(0);
            reservation.setReservationId(reservationId);
            return 1;
        }).when(reservationMapper).insertCompetitionSceneResourceReservation(any(CompetitionSceneResourceReservation.class));
    }

    private void useTeamSubject(Long... userIds) {
        useTeamSubject(userIds[0], userIds.length > 1 ? userIds[1] : null, userIds.length);
    }

    private void useTeamSubject(Long firstUserId, Long secondUserId, int memberCount) {
        CompetitionSceneScheduleTarget teamTarget = teamTarget();
        when(targetMapper.selectCompetitionSceneScheduleTargetList(any(CompetitionSceneScheduleTarget.class)))
                .thenReturn(Collections.singletonList(teamTarget));
        when(targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(SOURCE_SCHEDULE_ID))
                .thenReturn(Collections.singletonList(teamTarget));
        CompetitionApplyInfo[] members = new CompetitionApplyInfo[memberCount];
        for (int i = 0; i < memberCount; i++) {
            Long userId = i == 0 ? firstUserId : (i == 1 && secondUserId != null ? secondUserId : 9000L + i);
            members[i] = applyInfo(userId);
        }
        when(competitionApplyInfoMapper.selectCertCompetitionApplyInfoListByUserTeamCodeANoTeacher(any()))
                .thenReturn(Arrays.asList(members));
    }

    private CompetitionSceneResourceReservationReq request(String idempotencyKey) {
        CompetitionSceneResourceReservationReq req = new CompetitionSceneResourceReservationReq();
        req.setSlotId(SLOT_ID);
        req.setIdempotencyKey(idempotencyKey);
        return req;
    }

    private CompetitionSceneResourceReservationCancelReq cancelReq(Long reservationId) {
        CompetitionSceneResourceReservationCancelReq req = new CompetitionSceneResourceReservationCancelReq();
        req.setReservationId(reservationId);
        req.setCancelReason("cancel");
        return req;
    }

    private CompetitionSceneScheduleResourceVO scheduleResourceVO(Long scheduleResourceId, boolean shared) {
        CompetitionSceneScheduleResourceVO vo = new CompetitionSceneScheduleResourceVO();
        vo.setScheduleResourceId(scheduleResourceId);
        vo.setScheduleId(DEPLOY_SCHEDULE_ID);
        vo.setResourceId(1L);
        vo.setEventId(1L);
        vo.setScheduleName("Deploy Schedule");
        vo.setCompetitionName("Competition");
        vo.setResourceName("Resource");
        vo.setResourceType("DEVICE");
        vo.setDeploymentLocation("Room");
        vo.setDeployedDeviceCount(2);
        vo.setWorkstationsPerDevice(4);
        vo.setTotalWorkstations(8);
        vo.setSlotDurationMinutes(30);
        vo.setSharedOccupancy(shared);
        vo.setBookingStatus(CompetitionSceneResourceConstants.BOOKING_STATUS_OPEN);
        vo.setBookingCloseTime(new Date(System.currentTimeMillis() + 3600_000L));
        return vo;
    }

    private CompetitionSceneScheduleResource scheduleResourceEntity(Long scheduleResourceId, boolean shared, int workstations) {
        CompetitionSceneScheduleResource entity = new CompetitionSceneScheduleResource();
        entity.setScheduleResourceId(scheduleResourceId);
        entity.setScheduleId(DEPLOY_SCHEDULE_ID);
        entity.setResourceId(1L);
        entity.setEventId(1L);
        entity.setDeployedDeviceCount(2);
        entity.setWorkstationsPerDevice(workstations);
        entity.setTotalWorkstations(8);
        entity.setSharedOccupancy(shared);
        entity.setBookingStatus(CompetitionSceneResourceConstants.BOOKING_STATUS_OPEN);
        entity.setBookingCloseTime(new Date(System.currentTimeMillis() + 3600_000L));
        return entity;
    }

    private CompetitionSceneResourceSlotVO slotVO(Long slotId, Long scheduleResourceId) {
        CompetitionSceneResourceSlotVO vo = new CompetitionSceneResourceSlotVO();
        vo.setSlotId(slotId);
        vo.setScheduleResourceId(scheduleResourceId);
        vo.setScheduleId(DEPLOY_SCHEDULE_ID);
        vo.setResourceId(1L);
        vo.setEventId(1L);
        vo.setStartTime(new Date(System.currentTimeMillis() + 600_000L));
        vo.setEndTime(new Date(System.currentTimeMillis() + 1800_000L));
        vo.setWorkstationCount(4);
        vo.setRemainingDeviceCount(2);
        vo.setRemainingWorkstationCount(8);
        vo.setSlotStatus(CompetitionSceneResourceConstants.SLOT_STATUS_OPEN);
        return vo;
    }

    private CompetitionSceneResourceSlot slotEntity(Long slotId,
                                                    Long scheduleResourceId,
                                                    String slotStatus,
                                                    int workstationCount) {
        CompetitionSceneResourceSlot slot = new CompetitionSceneResourceSlot();
        slot.setSlotId(slotId);
        slot.setScheduleResourceId(scheduleResourceId);
        slot.setScheduleId(DEPLOY_SCHEDULE_ID);
        slot.setResourceId(1L);
        slot.setEventId(1L);
        slot.setStartTime(new Date(System.currentTimeMillis() + 600_000L));
        slot.setEndTime(new Date(System.currentTimeMillis() + 1800_000L));
        slot.setWorkstationCount(workstationCount);
        slot.setRemainingDeviceCount(2);
        slot.setRemainingWorkstationCount(8);
        slot.setSlotStatus(slotStatus);
        return slot;
    }

    private CompetitionSceneResourceSlot slotWithRemaining(String slotStatus,
                                                           int remainingDeviceCount,
                                                           int remainingWorkstationCount) {
        CompetitionSceneResourceSlot slot = slotEntity(SLOT_ID, SCHEDULE_RESOURCE_ID, slotStatus, 4);
        slot.setRemainingDeviceCount(remainingDeviceCount);
        slot.setRemainingWorkstationCount(remainingWorkstationCount);
        return slot;
    }

    private CompetitionSceneResourceSlot startedSlot() {
        CompetitionSceneResourceSlot slot = slotEntity(SLOT_ID, SCHEDULE_RESOURCE_ID,
                CompetitionSceneResourceConstants.SLOT_STATUS_OPEN, 4);
        slot.setStartTime(new Date(System.currentTimeMillis() - 60_000L));
        return slot;
    }

    private CompetitionSceneResourceReservationVO reservationVO(Long reservationId, String subjectType, String subjectCode) {
        CompetitionSceneResourceReservationVO vo = new CompetitionSceneResourceReservationVO();
        vo.setReservationId(reservationId);
        vo.setSlotId(SLOT_ID);
        vo.setScheduleResourceId(SCHEDULE_RESOURCE_ID);
        vo.setScheduleId(DEPLOY_SCHEDULE_ID);
        vo.setReservationSourceScheduleId(SOURCE_SCHEDULE_ID);
        vo.setCompetitionSeriesId(COMPETITION_SERIES_ID);
        vo.setResourceId(1L);
        vo.setSubjectType(subjectType);
        vo.setSubjectCode(subjectCode);
        vo.setUserId(USER_ID);
        vo.setOperatorUserId(USER_ID);
        vo.setOperatorName("operator");
        vo.setOccupyPeopleCount(1);
        vo.setReservedDeviceCount(1);
        vo.setReservedWorkstationCount(1);
        vo.setReservationStatus(CompetitionSceneResourceConstants.RESERVATION_STATUS_RESERVED);
        vo.setSlotStartTime(new Date(System.currentTimeMillis() + 600_000L));
        vo.setSlotEndTime(new Date(System.currentTimeMillis() + 1800_000L));
        return vo;
    }

    private CompetitionSceneResourceReservation reservationEntity(Long reservationId, boolean shared) {
        CompetitionSceneResourceReservation reservation = new CompetitionSceneResourceReservation();
        reservation.setReservationId(reservationId);
        reservation.setSlotId(SLOT_ID);
        reservation.setScheduleResourceId(SCHEDULE_RESOURCE_ID);
        reservation.setScheduleId(DEPLOY_SCHEDULE_ID);
        reservation.setReservationSourceScheduleId(SOURCE_SCHEDULE_ID);
        reservation.setCompetitionSeriesId(COMPETITION_SERIES_ID);
        reservation.setSubjectType(CompetitionSceneResourceConstants.SUBJECT_TYPE_USER);
        reservation.setSubjectCode(String.valueOf(USER_ID));
        reservation.setUserId(USER_ID);
        reservation.setReservedDeviceCount(shared ? 1 : 2);
        reservation.setReservedWorkstationCount(shared ? 3 : 8);
        reservation.setCoveredWorkstationCount(shared ? 3 : 8);
        reservation.setSharedOccupancySnapshot(shared);
        reservation.setReservationStatus(CompetitionSceneResourceConstants.RESERVATION_STATUS_RESERVED);
        return reservation;
    }

    private CompetitionSceneResourceSlotGroupScope groupScope(String groupCode, String groupName) {
        CompetitionSceneResourceSlotGroupScope scope = new CompetitionSceneResourceSlotGroupScope();
        scope.setSlotId(SLOT_ID);
        scope.setScheduleResourceId(SCHEDULE_RESOURCE_ID);
        scope.setAllowedGroupCode(groupCode);
        scope.setAllowedGroupName(groupName);
        scope.setEnabled(1);
        scope.setDeleted(0);
        return scope;
    }

    private CompetitionSceneScheduleTarget personTarget(Long userId,
                                                        Long scheduleId,
                                                        String groupCode,
                                                        String groupName) {
        CompetitionSceneScheduleTarget target = new CompetitionSceneScheduleTarget();
        target.setTargetId(100L);
        target.setScheduleId(scheduleId);
        target.setCompetitionSeriesId(COMPETITION_SERIES_ID);
        target.setUserId(userId);
        target.setUserName("Alice");
        target.setConfigDimension(CompetitionSceneConstants.DIMENSION_PERSON);
        target.setSecondLevelCode(groupCode);
        target.setSecondLevelName(groupName);
        target.setStatus(CompetitionSceneConstants.STATUS_NORMAL);
        target.setMatchStatus(CompetitionSceneConstants.MATCH_STATUS_MATCHED);
        target.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        return target;
    }

    private CompetitionSceneScheduleTarget teamTarget() {
        CompetitionSceneScheduleTarget target = personTarget(USER_ID, SOURCE_SCHEDULE_ID, "G1", "组别一");
        target.setConfigDimension(CompetitionSceneConstants.DIMENSION_TEAM);
        target.setTeamCode("T1");
        target.setTeamName("Team 1");
        return target;
    }

    private CompetitionApplyInfo applyInfo(Long userId) {
        CompetitionApplyInfo info = new CompetitionApplyInfo();
        info.setCompetitionSeriesId(COMPETITION_SERIES_ID);
        info.setUserId(userId);
        info.setTeamCode("T1");
        info.setTeamName("Team 1");
        info.setUserName("Member " + userId);
        info.setPayStatus(DictConstant.PAID);
        info.setCheckStatus(Constants.CHECK_PASS);
        info.setCompetitionRoleName("参赛选手");
        return info;
    }

    private void assertBusinessError(String errorCode, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (CompetitionSceneReservationException e) {
            assertEquals(errorCode, e.getErrorCode());
            return;
        }
        throw new AssertionError("Expected business error " + errorCode);
    }

    private void inject(String fieldName, Object value) throws Exception {
        Field field = UserCompetitionSceneResourceServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run();
    }
}
