package com.teaching.system.controller;

import com.teaching.common.core.context.SecurityContextHolder;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.service.ISysUserService;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonalCenterControllerSecurityTest
{
    private PersonalCenterController controller;
    private ISysUserService userService;

    @Before
    public void setUp() throws Exception
    {
        controller = new PersonalCenterController();
        userService = mock(ISysUserService.class);
        Field field = PersonalCenterController.class.getDeclaredField("userService");
        field.setAccessible(true);
        field.set(controller, userService);
        SecurityContextHolder.setUserId("42");
    }

    @After
    public void tearDown()
    {
        SecurityContextHolder.remove();
    }

    @Test
    public void authenticatedPhoneCheckShouldPreserveAvailabilityFeedback()
    {
        when(userService.checkPhoneUnique(argThat(user ->
                Long.valueOf(42L).equals(user.getUserId())
                        && "13800138000".equals(user.getPhonenumber()))))
                .thenReturn(false);

        SysUser request = new SysUser();
        request.setUserName("13800138000");
        AjaxResult result = controller.checkUserAccountAvailable(request);

        assertEquals(false, result.get(AjaxResult.DATA_TAG));
    }

    @Test
    public void malformedAccountShouldNotBeReportedAsAvailable()
    {
        SysUser request = new SysUser();
        request.setUserName("not-an-account");

        AjaxResult result = controller.checkUserAccountAvailable(request);

        assertEquals(false, result.get(AjaxResult.DATA_TAG));
    }
}
