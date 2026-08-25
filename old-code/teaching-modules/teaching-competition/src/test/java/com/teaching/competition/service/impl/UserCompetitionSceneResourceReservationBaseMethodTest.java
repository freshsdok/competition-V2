package com.teaching.competition.service.impl;

import com.teaching.competition.contant.CompetitionSceneResourceConstants;
import com.teaching.competition.domain.CompetitionSceneReservationSubject;
import com.teaching.competition.exception.CompetitionSceneReservationException;
import com.teaching.competition.mapper.CompetitionSceneResourceScheduleScopeMapper;
import com.teaching.competition.mapper.CompetitionSceneResourceSlotGroupScopeMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLIntegrityConstraintViolationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserCompetitionSceneResourceReservationBaseMethodTest {

    private UserCompetitionSceneResourceServiceImpl service;
    private CompetitionSceneResourceScheduleScopeMapper scheduleScopeMapper;
    private CompetitionSceneResourceSlotGroupScopeMapper slotGroupScopeMapper;

    @Before
    public void setUp() throws Exception {
        service = new UserCompetitionSceneResourceServiceImpl();
        scheduleScopeMapper = mock(CompetitionSceneResourceScheduleScopeMapper.class);
        slotGroupScopeMapper = mock(CompetitionSceneResourceSlotGroupScopeMapper.class);
        inject("scheduleScopeMapper", scheduleScopeMapper);
        inject("slotGroupScopeMapper", slotGroupScopeMapper);
    }

    @Test
    public void buildActiveReservationKeyUsesConfirmedFormat() throws Exception {
        Object result = invoke("buildActiveReservationKey",
                new Class[]{Long.class, String.class, String.class},
                100L, CompetitionSceneResourceConstants.SUBJECT_TYPE_TEAM, "TEAM001");

        assertEquals("RESV:100:TEAM:TEAM001", result);
    }

    @Test
    public void checkIdempotencyKeyRequiredRejectsBlankValue() throws Exception {
        try {
            invoke("checkIdempotencyKeyRequired", new Class[]{String.class}, " ");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof CompetitionSceneReservationException);
            CompetitionSceneReservationException cause = (CompetitionSceneReservationException) e.getCause();
            assertEquals(CompetitionSceneResourceConstants.ERROR_IDEMPOTENCY_KEY_REQUIRED, cause.getErrorCode());
            return;
        }
        throw new AssertionError("Expected idempotency validation to reject blank value");
    }

    @Test
    public void calculateCapacitySnapshotUsesSharedOccupancyRules() throws Exception {
        Object snapshot = invoke("calculateCapacitySnapshot",
                new Class[]{Integer.class, Integer.class, Boolean.class},
                3, 4, Boolean.TRUE);

        assertEquals(Integer.valueOf(3), readSnapshot(snapshot, "getOccupyPeopleCount"));
        assertEquals(Integer.valueOf(1), readSnapshot(snapshot, "getReservedDeviceCount"));
        assertEquals(Integer.valueOf(3), readSnapshot(snapshot, "getReservedWorkstationCount"));
        assertEquals(Boolean.TRUE, readSnapshot(snapshot, "getSharedOccupancySnapshot"));
        assertEquals(Integer.valueOf(4), readSnapshot(snapshot, "getWorkstationCountSnapshot"));
    }

    @Test
    public void calculateCapacitySnapshotUsesExclusiveOccupancyRules() throws Exception {
        Object snapshot = invoke("calculateCapacitySnapshot",
                new Class[]{Integer.class, Integer.class, Boolean.class},
                5, 4, Boolean.FALSE);

        assertEquals(Integer.valueOf(5), readSnapshot(snapshot, "getOccupyPeopleCount"));
        assertEquals(Integer.valueOf(2), readSnapshot(snapshot, "getReservedDeviceCount"));
        assertEquals(Integer.valueOf(8), readSnapshot(snapshot, "getReservedWorkstationCount"));
        assertEquals(Boolean.FALSE, readSnapshot(snapshot, "getSharedOccupancySnapshot"));
        assertEquals(Integer.valueOf(4), readSnapshot(snapshot, "getWorkstationCountSnapshot"));
    }

    @Test
    public void calculateOccupyPeopleCountUsesTeamParticipantCount() throws Exception {
        CompetitionSceneReservationSubject subject = new CompetitionSceneReservationSubject();
        subject.setSubjectType(CompetitionSceneResourceConstants.SUBJECT_TYPE_TEAM);
        subject.setParticipantCount(6);

        Object result = invoke("calculateOccupyPeopleCount",
                new Class[]{CompetitionSceneReservationSubject.class},
                subject);

        assertEquals(6, result);
    }

    @Test
    public void checkScopeAndGroupMethodsDelegateToBaseMappers() throws Exception {
        when(scheduleScopeMapper.countEnabledScope(10L, 20L)).thenReturn(1);
        when(slotGroupScopeMapper.countEnabledBySlotId(30L)).thenReturn(1);
        when(slotGroupScopeMapper.countAllowedGroup(30L, "G1")).thenReturn(1);
        when(slotGroupScopeMapper.countAllowedGroup(30L, "G2")).thenReturn(0);

        assertTrue((Boolean) invoke("checkScheduleScopeAllowed",
                new Class[]{Long.class, Long.class}, 10L, 20L));
        assertTrue((Boolean) invoke("checkSlotGroupAllowed",
                new Class[]{Long.class, String.class}, 30L, "G1"));
        assertFalse((Boolean) invoke("checkSlotGroupAllowed",
                new Class[]{Long.class, String.class}, 30L, "G2"));
    }

    @Test
    public void classifyReservationUniqueConflictUsesUniqueKeyNameBeforeSqlColumnNames() throws Exception {
        String sqlWithBothColumns = "insert into competition_scene_resource_reservation "
                + "(active_reservation_key, idempotency_key) values (?, ?) ";
        Throwable active = new DataIntegrityViolationException(sqlWithBothColumns
                + "Duplicate entry 'RESV:100:TEAM:T1' for key "
                + "'competition_scene_resource_reservation.uk_scene_resource_active_reservation_key'");
        Throwable idem = new DataIntegrityViolationException(sqlWithBothColumns
                + "Duplicate entry 'idem-1' for key "
                + "'competition_scene_resource_reservation.uk_scene_resource_idempotency_key'");

        assertEquals("ACTIVE_RESERVATION_KEY", String.valueOf(invoke("classifyReservationUniqueConflict",
                new Class[]{Throwable.class, String.class, String.class},
                active, "idem-1", "RESV:100:TEAM:T1")));
        assertEquals("IDEMPOTENCY_KEY", String.valueOf(invoke("classifyReservationUniqueConflict",
                new Class[]{Throwable.class, String.class, String.class},
                idem, "idem-1", "RESV:100:TEAM:T1")));
    }

    @Test
    public void classifyReservationUniqueConflictDoesNotSwallowOtherSqlErrors() throws Exception {
        Throwable other = new DataIntegrityViolationException("foreign key failed",
                new SQLIntegrityConstraintViolationException("Cannot add or update child row"));

        assertEquals("NONE", String.valueOf(invoke("classifyReservationUniqueConflict",
                new Class[]{Throwable.class, String.class, String.class},
                other, "idem-1", "RESV:100:TEAM:T1")));
    }

    private Object invoke(String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = UserCompetitionSceneResourceServiceImpl.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(service, args);
    }

    private Object readSnapshot(Object snapshot, String getterName) throws Exception {
        Method method = snapshot.getClass().getDeclaredMethod(getterName);
        method.setAccessible(true);
        return method.invoke(snapshot);
    }

    private void inject(String fieldName, Object value) throws Exception {
        Field field = UserCompetitionSceneResourceServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }
}
