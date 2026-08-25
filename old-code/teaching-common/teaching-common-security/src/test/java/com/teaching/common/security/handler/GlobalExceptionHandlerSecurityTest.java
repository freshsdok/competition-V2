package com.teaching.common.security.handler;

import com.teaching.common.core.web.domain.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerSecurityTest {

    @Test
    public void runtimeExceptionResponseDoesNotExposeImplementationDetails() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/system/user/resetPwd");

        AjaxResult result = handler.handleRuntimeException(
                new RuntimeException("com.teaching.system.api.domain.SysUser.getNickName()"), request);

        assertEquals(500, result.get("code"));
        assertEquals("系统繁忙，请稍后重试", result.get("msg"));
        assertFalse(result.toString().contains("com.teaching"));
        assertNotNull(result.get("errorId"));
        assertTrue(result.get("errorId").toString().startsWith("ERR-"));
    }
}
