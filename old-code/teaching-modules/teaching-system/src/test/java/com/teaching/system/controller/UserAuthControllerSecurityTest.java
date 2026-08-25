package com.teaching.system.controller;

import com.teaching.common.core.domain.R;
import com.teaching.common.core.utils.sign.RsaUtils;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.service.TokenService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.SysLogininfor;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.security.PcCaptchaRateLimiter;
import com.teaching.system.security.PcLoginClientIpResolver;
import com.teaching.system.security.PcLoginRateLimiter;
import com.teaching.system.service.IAuthInfoService;
import com.teaching.system.service.ISysLogininforService;
import com.teaching.system.service.ISysSenderMessageLogService;
import com.teaching.system.service.ISysUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"rawtypes", "unchecked"})
public class UserAuthControllerSecurityTest {

    private UserAuthController controller;
    private ISysUserService userService;
    private PcLoginRateLimiter rateLimiter;
    private PcCaptchaRateLimiter captchaRateLimiter;
    private PcLoginClientIpResolver clientIpResolver;
    private TaskExecutor captchaTaskExecutor;
    private ISysLogininforService logininforService;
    private RedisTemplate redisTemplate;
    private ValueOperations valueOperations;
    private RedisService redisService;

    @Before
    public void setUp() throws Exception {
        controller = new UserAuthController();
        userService = mock(ISysUserService.class);
        rateLimiter = mock(PcLoginRateLimiter.class);
        captchaRateLimiter = mock(PcCaptchaRateLimiter.class);
        clientIpResolver = mock(PcLoginClientIpResolver.class);
        captchaTaskExecutor = mock(TaskExecutor.class);
        logininforService = mock(ISysLogininforService.class);
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        redisService = mock(RedisService.class);

        setField(controller, "userService", userService);
        setField(controller, "pcLoginRateLimiter", rateLimiter);
        setField(controller, "pcCaptchaRateLimiter", captchaRateLimiter);
        setField(controller, "pcLoginClientIpResolver", clientIpResolver);
        setField(controller, "pcCaptchaTaskExecutor", captchaTaskExecutor);
        setField(controller, "logininforService", logininforService);
        setField(controller, "redisTemplate", redisTemplate);
        setField(controller, "redisService", redisService);
        setField(controller, "senderMessageLogService", mock(ISysSenderMessageLogService.class));
        setField(controller, "authInfoService", mock(IAuthInfoService.class));
        setField(controller, "tokenService", mock(TokenService.class));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(clientIpResolver.resolve(any(HttpServletRequest.class))).thenReturn("203.0.113.10");
        when(rateLimiter.reserveAttempt(anyString(), anyString()))
                .thenReturn(PcLoginRateLimiter.LimitStatus.allowed());
        when(rateLimiter.recordFailure(anyString(), anyString()))
                .thenReturn(PcLoginRateLimiter.LimitStatus.allowed());
        when(captchaRateLimiter.consume(anyString(), anyString()))
                .thenReturn(PcCaptchaRateLimiter.SendStatus.allowed());
    }

    @Test
    public void accountStatesExposeTheSameLoginFailure() throws Exception {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        String encryptedPassword = RsaUtils.encryptByPublicKey("submitted-password");

        when(userService.selectUserByUserInfo(any(SysUser.class)))
                .thenReturn(null)
                .thenReturn(user("2", "0", "different-password"))
                .thenReturn(user("0", "0", "submitted-password"))
                .thenReturn(user("2", "1", "submitted-password"));

        R<?> missing = controller.login(loginRequest("missing@example.com", encryptedPassword),
                httpRequest, mock(HttpServletResponse.class));
        R<?> wrongPassword = controller.login(loginRequest("known@example.com", encryptedPassword),
                httpRequest, mock(HttpServletResponse.class));
        R<?> wrongType = controller.login(loginRequest("admin@example.com", encryptedPassword),
                httpRequest, mock(HttpServletResponse.class));
        R<?> incomplete = controller.login(loginRequest("pending@example.com", encryptedPassword),
                httpRequest, mock(HttpServletResponse.class));

        assertEquivalentFailure(missing, wrongPassword);
        assertEquivalentFailure(missing, wrongType);
        assertEquivalentFailure(missing, incomplete);
        assertEquals("用户名或密码错误", missing.getMsg());
        assertNull(missing.getData());

        ArgumentCaptor<SysLogininfor> auditCaptor = ArgumentCaptor.forClass(SysLogininfor.class);
        verify(logininforService, org.mockito.Mockito.times(4))
                .insertLogininfor(auditCaptor.capture());
        Set<String> internalReasons = new HashSet<>();
        for (SysLogininfor audit : auditCaptor.getAllValues()) {
            internalReasons.add(audit.getMsg());
        }
        assertTrue(internalReasons.containsAll(List.of(
                "ACCOUNT_NOT_FOUND",
                "PASSWORD_INVALID",
                "ACCOUNT_TYPE_REJECTED",
                "ACCOUNT_STATUS_REJECTED")));
    }

    @Test
    public void blockedLoginReturnsHttp429AndRetryAfter() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(rateLimiter.reserveAttempt(anyString(), anyString()))
                .thenReturn(PcLoginRateLimiter.LimitStatus.blocked(120L));

        R<?> result = controller.login(loginRequest("user@example.com", "unused"), request, response);

        assertEquals(429, result.getCode());
        verify(response).setStatus(429);
        verify(response).setHeader("Retry-After", "120");
        verify(userService, never()).selectUserByUserInfo(any(SysUser.class));
    }

    @Test
    public void resetPasswordForUnknownAccountIsGenericAndDoesNotThrow() throws Exception {
        SysUser request = loginRequest(
                "missing@example.com", RsaUtils.encryptByPublicKey("new-password"));
        request.setMsgCode("123456");
        when(valueOperations.get(eq("EMAIL_CODE:missing@example.com"))).thenReturn(null);

        R<?> result = controller.updatePwd(request);

        assertEquals("账号或验证码无效", result.getMsg());
        assertNull(result.getData());
        verify(userService, never()).updateUserPwd(any(SysUser.class));
    }

    @Test
    public void publicAccountPrecheckDoesNotRevealExistence() {
        SysUser request = new SysUser();
        request.setUserName("target@example.com");

        AjaxResult result = controller.checkUserAccount(request);

        assertEquals(true, result.get(AjaxResult.DATA_TAG));
        verify(userService, never()).selectUserByUserInfo(any(SysUser.class));
    }

    @Test
    public void otpLoginAccountStatesExposeTheSameFailure() {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(userService.selectUserByUserInfo(any(SysUser.class)))
                .thenReturn(null)
                .thenReturn(user("0", "0", "unused"))
                .thenReturn(user("2", "1", "unused"))
                .thenReturn(user("2", "0", "unused"));

        R<?> missing = controller.emailLogin(
                otpRequest("missing@example.com"), httpRequest, mock(HttpServletResponse.class));
        R<?> wrongType = controller.emailLogin(
                otpRequest("admin@example.com"), httpRequest, mock(HttpServletResponse.class));
        R<?> incomplete = controller.emailLogin(
                otpRequest("pending@example.com"), httpRequest, mock(HttpServletResponse.class));
        R<?> wrongCode = controller.emailLogin(
                otpRequest("known@example.com"), httpRequest, mock(HttpServletResponse.class));

        assertEquivalentFailure(missing, wrongType);
        assertEquivalentFailure(missing, incomplete);
        assertEquivalentFailure(missing, wrongCode);
        assertEquals("账号或验证码无效", missing.getMsg());
    }

    @Test
    public void captchaAccountStatesExposeTheSameResponseAndOnlyEligibleUserIsDispatched() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(userService.selectUserByUserInfo(any(SysUser.class)))
                .thenReturn(null)
                .thenReturn(user("2", "0", "unused"));

        R<?> missing = controller.captcha(
                otpRequest("missing@example.com"), request, response);
        R<?> eligible = controller.captcha(
                otpRequest("known@example.com"), request, response);

        assertEquals(missing.getCode(), eligible.getCode());
        assertEquals(missing.getMsg(), eligible.getMsg());
        assertEquals(missing.getData(), eligible.getData());
        assertEquals("如果账号信息有效，验证码将发送至对应邮箱或手机",
                missing.getData());
        verify(userService, times(2)).selectUserByUserInfo(any(SysUser.class));
        verify(captchaTaskExecutor, times(1)).execute(any(Runnable.class));
    }

    @Test
    public void blockedCaptchaReturnsHttp429WithoutLookingUpAccount() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(captchaRateLimiter.consume(anyString(), anyString()))
                .thenReturn(PcCaptchaRateLimiter.SendStatus.blocked(90L));

        R<?> result = controller.captcha(
                otpRequest("known@example.com"), request, response);

        assertEquals(429, result.getCode());
        verify(response).setStatus(429);
        verify(response).setHeader("Retry-After", "90");
        verify(userService, never()).selectUserByUserInfo(any(SysUser.class));
        verify(captchaTaskExecutor, never()).execute(any(Runnable.class));
    }

    private static void assertEquivalentFailure(R<?> expected, R<?> actual) {
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getMsg(), actual.getMsg());
        assertEquals(expected.getData(), actual.getData());
    }

    private static SysUser loginRequest(String userName, String encryptedPassword) {
        SysUser user = new SysUser();
        user.setUserName(userName);
        user.setPassword(encryptedPassword);
        return user;
    }

    private static SysUser otpRequest(String userName) {
        SysUser user = new SysUser();
        user.setUserName(userName);
        user.setMsgCode("123456");
        return user;
    }

    private static SysUser user(String userType, String status, String password) {
        SysUser user = new SysUser();
        user.setUserId(10L);
        user.setUserName("stored-user");
        user.setUserType(userType);
        user.setStatus(status);
        user.setPassword(SecurityUtils.encryptPassword(password));
        return user;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
