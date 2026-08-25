package com.teaching.system.api.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.RemoteErrorLogService;
import com.teaching.system.api.domain.SysErrorLog;

/**
 * 错误日志服务降级处理
 * 
 * @author teaching
 */
@Component
public class RemoteErrorLogFallbackFactory implements FallbackFactory<RemoteErrorLogService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteErrorLogFallbackFactory.class);

    @Override
    public RemoteErrorLogService create(Throwable throwable)
    {
        log.error("错误日志服务调用失败:{}", throwable.getMessage());
        return new RemoteErrorLogService()
        {
            @Override
            public R<Boolean> saveErrorLog(SysErrorLog errorLog, String source)
            {
                return R.fail("保存错误日志失败:" + throwable.getMessage());
            }
        };
    }
}
