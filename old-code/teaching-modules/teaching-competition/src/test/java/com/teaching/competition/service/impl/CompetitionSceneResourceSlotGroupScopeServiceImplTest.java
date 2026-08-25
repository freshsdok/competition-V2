package com.teaching.competition.service.impl;

import com.teaching.competition.domain.CompetitionSceneResourceSlotGroupScope;
import com.teaching.competition.mapper.CompetitionSceneResourceSlotGroupScopeMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompetitionSceneResourceSlotGroupScopeServiceImplTest {

    private CompetitionSceneResourceSlotGroupScopeServiceImpl service;
    private CompetitionSceneResourceSlotGroupScopeMapper mapper;

    @Before
    public void setUp() throws Exception {
        service = new CompetitionSceneResourceSlotGroupScopeServiceImpl();
        mapper = mock(CompetitionSceneResourceSlotGroupScopeMapper.class);
        inject("slotGroupScopeMapper", mapper);
    }

    @Test
    public void replaceSlotGroupsDeletesOldAndInsertsNewScopes() {
        when(mapper.logicalDeleteBySlotId(eq(11L), anyString())).thenReturn(2);
        when(mapper.batchInsertScopes(anyList())).thenReturn(2);

        int affected = service.replaceSlotGroups(11L, 22L,
                Arrays.asList(group("G1", "组别一"), group("G2", "组别二"), group("G1", "重复")));

        assertEquals(4, affected);
        verify(mapper).logicalDeleteBySlotId(eq(11L), anyString());
        ArgumentCaptor<List<CompetitionSceneResourceSlotGroupScope>> captor = ArgumentCaptor.forClass(List.class);
        verify(mapper).batchInsertScopes(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertEquals(Long.valueOf(11L), captor.getValue().get(0).getSlotId());
        assertEquals(Long.valueOf(22L), captor.getValue().get(0).getScheduleResourceId());
    }

    @Test
    public void isSlotGroupAllowedAllowsWhenNoGroupConfigured() {
        when(mapper.countEnabledBySlotId(11L)).thenReturn(0);

        assertTrue(service.isSlotGroupAllowed(11L, null));
    }

    @Test
    public void isSlotGroupAllowedRequiresMatchingGroupWhenConfigured() {
        when(mapper.countEnabledBySlotId(11L)).thenReturn(2);
        when(mapper.countAllowedGroup(11L, "G1")).thenReturn(1);
        when(mapper.countAllowedGroup(11L, "G9")).thenReturn(0);

        assertTrue(service.isSlotGroupAllowed(11L, "G1"));
        assertFalse(service.isSlotGroupAllowed(11L, "G9"));
        assertFalse(service.isSlotGroupAllowed(11L, ""));
    }

    @Test
    public void listMethodsUseDeletedAwareMapperQueries() {
        when(mapper.selectBySlotId(11L)).thenReturn(Arrays.asList(group("G1", "组别一")));
        when(mapper.selectByScheduleResourceId(22L)).thenReturn(Arrays.asList(group("G2", "组别二")));

        assertEquals(1, service.listBySlotId(11L).size());
        assertEquals(1, service.listByScheduleResourceId(22L).size());
    }

    private CompetitionSceneResourceSlotGroupScope group(String code, String name) {
        CompetitionSceneResourceSlotGroupScope group = new CompetitionSceneResourceSlotGroupScope();
        group.setAllowedGroupCode(code);
        group.setAllowedGroupName(name);
        return group;
    }

    private void inject(String fieldName, Object value) throws Exception {
        Field field = CompetitionSceneResourceSlotGroupScopeServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }
}
