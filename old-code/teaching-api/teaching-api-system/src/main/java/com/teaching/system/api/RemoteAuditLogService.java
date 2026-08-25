package com.teaching.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.ServiceNameConstants;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.domain.SysAuditLog;

/**
 * 审计日志服务
 * 
 * @author teaching
 */
@FeignClient(contextId = "remoteAuditLogService", value = ServiceNameConstants.SYSTEM_SERVICE)
public interface RemoteAuditLogService
{
    /**
     * 保存审计日志
     *
     * @param auditLog 审计日志信息
     * @param source 请求来源
     * @return 结果
     */
    @PostMapping("/auditlog")
    R<Boolean> saveAuditLog(@RequestBody SysAuditLog auditLog, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
