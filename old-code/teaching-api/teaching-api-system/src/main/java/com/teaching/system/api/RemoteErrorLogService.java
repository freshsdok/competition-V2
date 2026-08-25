package com.teaching.system.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.ServiceNameConstants;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.domain.SysErrorLog;

/**
 * 错误日志服务
 * 
 * @author teaching
 */
@FeignClient(contextId = "remoteErrorLogService", value = ServiceNameConstants.SYSTEM_SERVICE)
public interface RemoteErrorLogService
{
    /**
     * 保存错误日志
     *
     * @param errorLog 错误日志信息
     * @param source 请求来源
     * @return 结果
     */
    @PostMapping("/errorlog")
    R<Boolean> saveErrorLog(@RequestBody SysErrorLog errorLog, @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
