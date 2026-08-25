package com.teaching.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.ClientInfoUtils;
import com.teaching.common.core.utils.ip.IpUtils;
import com.teaching.system.api.RemoteLogService;
import com.teaching.system.api.domain.SysLogininfor;

/**
 * 记录日志方法
 * 
 * @author teaching
 */
@Component
public class SysRecordLogService
{
    @Autowired
    private RemoteLogService remoteLogService;

    /**
     * 记录登录信息
     * 
     * @param username 用户名
     * @param status 状态
     * @param message 消息内容
     * @param request HTTP请求对象
     * @return
     */
    public void recordLogininfor(String username, String status, String message, HttpServletRequest request)
    {
        SysLogininfor logininfor = new SysLogininfor();
        logininfor.setUserName(username);
        logininfor.setIpaddr(IpUtils.getIpAddr());
        logininfor.setMsg(message);
        // 日志状态
        if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER))
        {
            logininfor.setStatus(Constants.LOGIN_SUCCESS_STATUS);
            // 登录成功或退出登录时补充操作系统和浏览器信息
            if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT))
            {
                // 直接从request获取User-Agent，避免RequestContextHolder上下文丢失
                String userAgent = request != null ? ClientInfoUtils.getUserAgent(request) : "";
                String browser = ClientInfoUtils.parseBrowser(userAgent);
                String os = ClientInfoUtils.parseOS(userAgent);
                logininfor.setBrowser(browser);
                logininfor.setOs(os);
            }
        }
        else if (Constants.LOGIN_FAIL.equals(status))
        {
            logininfor.setStatus(Constants.LOGIN_FAIL_STATUS);
        }
        remoteLogService.saveLogininfor(logininfor, SecurityConstants.INNER);
    }
    
    /**
     * 记录登录信息（兼容旧方法，不传request）
     * 
     * @param username 用户名
     * @param status 状态
     * @param message 消息内容
     */
    public void recordLogininfor(String username, String status, String message)
    {
        recordLogininfor(username, status, message, null);
    }
}
