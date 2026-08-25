package com.teaching.system.service.impl;

import com.teaching.system.mapper.SysMenuMapper;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SysMenuServiceImplSecurityTest {

    @Test
    public void publicMenuResponseUsesAFieldAllowlist() throws Exception {
        SysMenuMapper mapper = mock(SysMenuMapper.class);
        SysMenuServiceImpl service = new SysMenuServiceImpl();
        setField(service, "menuMapper", mapper);

        Map<String, Object> column = new HashMap<>();
        column.put("columnId", 7L);
        column.put("columnType", "1");
        column.put("menuName", "公开栏目");
        column.put("createBy", "internal-admin");

        Map<String, Object> detail = new HashMap<>();
        detail.put("detailId", 8L);
        detail.put("detailTitle", "公开标题");
        detail.put("detailContent", "列表页不应返回正文");
        detail.put("detailImage", "/image.png");
        detail.put("orderNum", 1);
        detail.put("createBy", "internal-admin");
        detail.put("delFlag", "0");

        when(mapper.selectColumnByMenuId(3L)).thenReturn(column);
        when(mapper.selectDetailByColumnId(7L)).thenReturn(List.of(detail));

        Map<String, Object> result = service.selectMenuInfoById(3L);
        List<?> detailList = (List<?>) result.get("detailList");
        Map<?, ?> publicDetail = (Map<?, ?>) detailList.get(0);

        assertEquals("公开栏目", result.get("menuName"));
        assertFalse(result.containsKey("columnId"));
        assertFalse(result.containsKey("createBy"));
        assertTrue(publicDetail.containsKey("detailId"));
        assertFalse(publicDetail.containsKey("detailContent"));
        assertFalse(publicDetail.containsKey("createBy"));
        assertFalse(publicDetail.containsKey("delFlag"));
    }

    @Test
    public void missingOrPrivateMenuReturnsEmptyResult() throws Exception {
        SysMenuMapper mapper = mock(SysMenuMapper.class);
        SysMenuServiceImpl service = new SysMenuServiceImpl();
        setField(service, "menuMapper", mapper);
        when(mapper.selectColumnByMenuId(99L)).thenReturn(null);

        assertTrue(service.selectMenuInfoById(99L).isEmpty());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
