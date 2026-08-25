package com.teaching.auth.service;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.teaching.common.core.constant.CacheConstants;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.ip.IpUtils;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteAuditLogService;
import com.teaching.system.api.domain.SysAuditLog;
import com.teaching.system.api.domain.SysUser;

/**
 * 登录密码方法
 * 
 * @author teaching
 */
@Component
public class SysPasswordService
{
    @Autowired
    private RedisService redisService;

    private int maxRetryCount = CacheConstants.PASSWORD_MAX_RETRY_COUNT;

    private Long lockTime = CacheConstants.PASSWORD_LOCK_TIME;

    @Autowired
    private SysRecordLogService recordLogService;

    @Autowired(required = false)
    private RemoteAuditLogService remoteAuditLogService;

    /**
     * 登录账户密码错误次数缓存键名
     * 
     * @param username 用户名
     * @return 缓存键key
     */
    private String getCacheKey(String username)
    {
        return CacheConstants.PWD_ERR_CNT_KEY + username;
    }

    public void validate(SysUser user, String password)
    {
        String username = user.getUserName();

        Integer retryCount = redisService.getCacheObject(getCacheKey(username));

        if (retryCount == null)
        {
            retryCount = 0;
        }

        if (retryCount >= Integer.valueOf(maxRetryCount).intValue())
        {
            String errMsg = String.format("密码输入错误%s次，帐户锁定%s分钟", maxRetryCount, lockTime);
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL,errMsg);
            // 记录审计日志
            recordPasswordErrorAudit(username, retryCount, "账户已被锁定");
            throw new ServiceException(errMsg);
        }

        if (!matches(user, password))
        {
            retryCount = retryCount + 1;
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, String.format("密码输入错误%s次", retryCount));
            redisService.setCacheObject(getCacheKey(username), retryCount, lockTime, TimeUnit.MINUTES);
            // 记录审计日志
            recordPasswordErrorAudit(username, retryCount, "密码错误");
            throw new ServiceException("用户不存在/密码错误");
        }
        else
        {
            clearLoginRecordCache(username);
        }
    }

    public boolean matches(SysUser user, String rawPassword)
    {
        return SecurityUtils.matchesPassword(rawPassword, user.getPassword());
    }

    public void clearLoginRecordCache(String loginName)
    {
        if (redisService.hasKey(getCacheKey(loginName)))
        {
            redisService.deleteObject(getCacheKey(loginName));
        }
    }

    /**
     * 记录密码错误审计日志
     */
    private void recordPasswordErrorAudit(String username, int retryCount, String reason)
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
            
            // 根据错误次数设置风险级别
            if (retryCount >= maxRetryCount)
            {
                auditLog.setRiskLevel("CRITICAL");
            }
            else if (retryCount >= 3)
            {
                auditLog.setRiskLevel("HIGH");
            }
            else
            {
                auditLog.setRiskLevel("MEDIUM");
            }

            auditLog.setEventName("密码错误");
            auditLog.setEventDesc(String.format("用户连续%d次输入错误密码", retryCount));

            // 用户信息
            auditLog.setUserName(username);

            // 操作信息
            auditLog.setOperationType("登录");
            auditLog.setOperationModule("认证服务");

            // 安全信息
            auditLog.setIpAddress(IpUtils.getIpAddr());

            // 异常信息
            auditLog.setIsAbnormal("1");
            auditLog.setAbnormalReason(reason + "，可能存在暴力破解风险");

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
}
