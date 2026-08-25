package com.teaching.auth.service;

import java.util.Date;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.teaching.common.core.constant.CacheConstants;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.UserConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.enums.UserStatus;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.text.Convert;
import com.teaching.common.core.utils.AuditLogUtils;
import com.teaching.common.core.utils.ClientInfoUtils;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.ip.IpUtils;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteAuditLogService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.SysAuditLog;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.model.LoginUser;

/**
 * 登录校验方法
 * 
 * @author teaching
 */
@Component
public class SysLoginService
{
    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private SysRecordLogService recordLogService;

    @Autowired
    private RedisService redisService;

    @Autowired(required = false)
    private RemoteAuditLogService remoteAuditLogService;

    /**
     * 登录
     */
    public LoginUser login(String username, String password, HttpServletRequest request)
    {
        // 用户名或密码为空 错误
        if (StringUtils.isAnyBlank(username, password))
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户/密码必须填写", request);
            throw new ServiceException("用户/密码必须填写");
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户密码不在指定范围", request);
            throw new ServiceException("用户密码不在指定范围");
        }
        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户名不在指定范围", request);
            throw new ServiceException("用户名不在指定范围");
        }
        // IP黑名单校验
        String blackStr = Convert.toStr(redisService.getCacheObject(CacheConstants.SYS_LOGIN_BLACKIPLIST));
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "很遗憾，访问IP已被列入系统黑名单", request);
            throw new ServiceException("很遗憾，访问IP已被列入系统黑名单");
        }
        // 查询用户信息
        R<LoginUser> userResult = remoteUserService.getUserInfo(username, SecurityConstants.INNER);

        if (R.FAIL == userResult.getCode())
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户不存在", request);
            // 记录审计日志
            recordUserNotFoundAudit(username, request);
            throw new ServiceException(userResult.getMsg());
        }

        LoginUser userInfo = userResult.getData();
        SysUser user = userResult.getData().getSysUser();
        if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "对不起，您的账号已被删除", request);
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "用户已停用，请联系管理员", request);
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }
        passwordService.validate(user, password);
        recordLogService.recordLogininfor(username, Constants.LOGIN_SUCCESS, "登录成功", request);
        recordLoginInfo(user.getUserId());
        // 记录登录审计日志
        recordLoginAudit(user, request);
        return userInfo;
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        // 更新用户登录IP
        sysUser.setLoginIp(IpUtils.getIpAddr());
        // 更新用户登录时间
        sysUser.setLoginDate(DateUtils.getNowDate());
        remoteUserService.recordUserLogin(sysUser, SecurityConstants.INNER);
    }

    public void logout(String loginName, HttpServletRequest request)
    {
        recordLogService.recordLogininfor(loginName, Constants.LOGOUT, "退出成功", request);
        // 记录登出审计日志
        recordLogoutAudit(loginName, request);
    }

    /**
     * 注册
     */
    public void register(String username, String password)
    {
        // 用户名或密码为空 错误
        if (StringUtils.isAnyBlank(username, password))
        {
            throw new ServiceException("用户/密码必须填写");
        }
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            throw new ServiceException("账户长度必须在2到20个字符之间");
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            throw new ServiceException("密码长度必须在5到20个字符之间");
        }

        // 注册用户信息
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);
        sysUser.setNickName(username);
        sysUser.setPwdUpdateDate(DateUtils.getNowDate());
        sysUser.setPassword(SecurityUtils.encryptPassword(password));
        R<?> registerResult = remoteUserService.registerUserInfo(sysUser, SecurityConstants.INNER);

        if (R.FAIL == registerResult.getCode())
        {
            throw new ServiceException(registerResult.getMsg());
        }
        recordLogService.recordLogininfor(username, Constants.REGISTER, "注册成功");
    }

    /**
     * 记录登录审计日志
     */
    private void recordLoginAudit(SysUser user, HttpServletRequest request)
    {
        try
        {
            if (remoteAuditLogService == null)
            {
                return;
            }

            SysAuditLog auditLog = new SysAuditLog();
            auditLog.setAuditType("登录审计");
            auditLog.setAuditCategory("安全事件");
            auditLog.setEventName("用户登录");
            auditLog.setEventDesc("用户成功登录系统");

            // 用户信息
            auditLog.setUserId(user.getUserId());
            auditLog.setUserName(user.getUserName());
            if (user.getOrg() != null)
            {
                auditLog.setDeptName(user.getOrg().getOrgName());
            }

            // 操作信息
            auditLog.setOperationType("登录");
            auditLog.setOperationModule("认证服务");
            if (request != null)
            {
                auditLog.setRequestUrl(request.getRequestURI());
                auditLog.setRequestMethod(request.getMethod());
            }

            // 安全信息
            String currentIp = IpUtils.getIpAddr();
            auditLog.setIpAddress(currentIp);
            if (request != null)
            {
                String userAgent = ClientInfoUtils.getUserAgent(request);
                auditLog.setUserAgent(userAgent);
                auditLog.setBrowser(ClientInfoUtils.parseBrowser(userAgent));
                auditLog.setOs(ClientInfoUtils.parseOS(userAgent));
                auditLog.setDeviceType(AuditLogUtils.getDeviceType(userAgent));
            }

            // 异常检测
            boolean isAbnormal = false;
            StringBuilder abnormalReason = new StringBuilder();

            // 检测异地登录
            String lastLoginIp = user.getLoginIp();
            if (AuditLogUtils.isRemoteLogin(currentIp, lastLoginIp))
            {
                isAbnormal = true;
                abnormalReason.append("检测到异地登录；");
            }

            // 检测非工作时间登录
            if (AuditLogUtils.isNonWorkingHours())
            {
                isAbnormal = true;
                abnormalReason.append("非工作时间登录；");
            }

            auditLog.setIsAbnormal(isAbnormal ? "1" : "0");
            auditLog.setAbnormalReason(abnormalReason.toString());

            // 风险级别
            auditLog.setRiskLevel(isAbnormal ? "MEDIUM" : "LOW");

            // 审计状态
            auditLog.setAuditStatus("0"); // 待审计
            auditLog.setOperationTime(new Date());

            // 异步保存审计日志
            remoteAuditLogService.saveAuditLog(auditLog, SecurityConstants.INNER);
        }
        catch (Exception e)
        {
            // 记录审计日志失败不影响主业务
        }
    }

    /**
     * 记录登出审计日志
     */
    private void recordLogoutAudit(String loginName, HttpServletRequest request)
    {
        try
        {
            if (remoteAuditLogService == null)
            {
                return;
            }

            SysAuditLog auditLog = new SysAuditLog();
            auditLog.setAuditType("登录审计");
            auditLog.setAuditCategory("安全事件");
            auditLog.setRiskLevel("LOW");
            auditLog.setEventName("用户登出");
            auditLog.setEventDesc("用户退出系统");

            // 用户信息
            auditLog.setUserName(loginName);

            // 操作信息
            auditLog.setOperationType("登出");
            auditLog.setOperationModule("认证服务");
            if (request != null)
            {
                auditLog.setRequestUrl(request.getRequestURI());
                auditLog.setRequestMethod(request.getMethod());
            }

            // 安全信息
            auditLog.setIpAddress(IpUtils.getIpAddr());

            // 审计状态
            auditLog.setIsAbnormal("0");
            auditLog.setAuditStatus("0");
            auditLog.setOperationTime(new Date());

            // 异步保存审计日志
            remoteAuditLogService.saveAuditLog(auditLog, SecurityConstants.INNER);
        }
        catch (Exception e)
        {
            // 记录审计日志失败不影响主业务
        }
    }

    /**
     * 记录用户不存在审计日志
     */
    private void recordUserNotFoundAudit(String username, HttpServletRequest request)
    {
        try
        {
            if (remoteAuditLogService == null)
            {
                return;
            }

            SysAuditLog auditLog = new SysAuditLog();
            auditLog.setAuditType("登录审计");
            auditLog.setAuditCategory("异常行为");
            auditLog.setRiskLevel("MEDIUM");
            auditLog.setEventName("用户不存在");
            auditLog.setEventDesc("尝试使用不存在的用户名登录");

            // 用户信息
            auditLog.setUserName(username);

            // 操作信息
            auditLog.setOperationType("登录");
            auditLog.setOperationModule("认证服务");
            if (request != null)
            {
                auditLog.setRequestUrl(request.getRequestURI());
                auditLog.setRequestMethod(request.getMethod());
            }

            // 安全信息
            auditLog.setIpAddress(IpUtils.getIpAddr());
            if (request != null)
            {
                String userAgent = ClientInfoUtils.getUserAgent(request);
                auditLog.setUserAgent(userAgent);
                auditLog.setBrowser(ClientInfoUtils.parseBrowser(userAgent));
                auditLog.setOs(ClientInfoUtils.parseOS(userAgent));
            }

            // 异常信息
            auditLog.setIsAbnormal("1");
            auditLog.setAbnormalReason("尝试使用不存在的用户名登录，可能是账号探测行为");

            // 审计状态
            auditLog.setAuditStatus("0");
            auditLog.setOperationTime(new Date());

            // 异步保存审计日志
            remoteAuditLogService.saveAuditLog(auditLog, SecurityConstants.INNER);
        }
        catch (Exception e)
        {
            // 记录审计日志失败不影响主业务
        }
    }
}
