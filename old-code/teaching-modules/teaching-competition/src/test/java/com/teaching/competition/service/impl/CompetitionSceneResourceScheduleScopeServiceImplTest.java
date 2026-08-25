package com.teaching.competition.service.impl;

import com.teaching.competition.domain.CompetitionSceneResourceScheduleScope;
import com.teaching.competition.mapper.CompetitionSceneResourceScheduleScopeMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompetitionSceneResourceScheduleScopeServiceImplTest {

    private CompetitionSceneResourceScheduleScopeServiceImpl service;
    private CompetitionSceneResourceScheduleScopeMapper mapper;

    @Before
    public void setUp() throws Exception {
        service = new CompetitionSceneResourceScheduleScopeServiceImpl();
        mapper = mock(CompetitionSceneResourceScheduleScopeMapper.class);
        inject("scheduleScopeMapper", mapper);
    }

    @Test
    public void addManualBindScheduleInsertsNewScope() {
        when(mapper.selectEnabledScope(10L, 20L)).thenReturn(null);
        when(mapper.insertScope(any(CompetitionSceneResourceScheduleScope.class))).thenAnswer(invocation -> {
            CompetitionSceneResourceScheduleScope scope = invocation.getArgument(0);
            scope.setScopeId(99L);
            return 1;
        });

        CompetitionSceneResourceScheduleScope result = service.addManualBindSchedule(10L, 30L, 20L);

        assertEquals(Long.valueOf(99L), result.getScopeId());
        assertEquals("MANUAL_BIND", result.getSourceType());
        assertEquals(Integer.valueOf(1), result.getEnabled());
        ArgumentCaptor<CompetitionSceneResourceScheduleScope> captor =
                ArgumentCaptor.forClass(CompetitionSceneResourceScheduleScope.class);
        verify(mapper).insertScope(captor.capture());
        assertEquals(Long.valueOf(10L), captor.getValue().getScheduleResourceId());
        assertEquals(Long.valueOf(20L), captor.getValue().getAllowedScheduleId());
    }

    @Test
    public void listAndRemoveUseMapperWithDeletedAwareMethods() {
        List<CompetitionSceneResourceScheduleScope> scopes = Arrays.asList(scope(1L), scope(2L));
        when(mapper.selectByScheduleResourceId(10L)).thenReturn(scopes);
        when(mapper.selectAllowedScheduleIds(10L)).thenReturn(Arrays.asList(20L, 21L));
        when(mapper.logicalDeleteManualBind(eq(10L), eq(20L), anyString())).thenReturn(1);

        assertEquals(2, service.listByScheduleResourceId(10L).size());
        assertEquals(Arrays.asList(20L, 21L), service.listAllowedScheduleIds(10L));
        assertEquals(1, service.removeManualBindSchedule(10L, 20L));
    }

    @Test
    public void existsAllowedScheduleChecksEnabledScope() {
        when(mapper.countEnabledScope(10L, 20L)).thenReturn(1);

        assertTrue(service.existsAllowedSchedule(10L, 20L));
        assertFalse(service.existsAllowedSchedule(10L, null));
    }

    private CompetitionSceneResourceScheduleScope scope(Long scopeId) {
        CompetitionSceneResourceScheduleScope scope = new CompetitionSceneResourceScheduleScope();
        scope.setScopeId(scopeId);
        scope.setScheduleResourceId(10L);
        scope.setAllowedScheduleId(20L + scopeId);
        scope.setEnabled(1);
        scope.setDeleted(0);
        return scope;
    }

    private void inject(String fieldName, Object value) throws Exception {
        Field field = CompetitionSceneResourceScheduleScopeServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }
}
