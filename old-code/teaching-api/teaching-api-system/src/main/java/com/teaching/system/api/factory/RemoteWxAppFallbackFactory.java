package com.teaching.system.api.factory;

import com.teaching.common.core.domain.R;
import com.teaching.system.api.RemoteWxAppService;
import com.teaching.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;

public class RemoteWxAppFallbackFactory implements FallbackFactory<RemoteWxAppService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteWxAppFallbackFactory.class);
    @Override
    public RemoteWxAppService create(Throwable throwable) {
        log.error("wx服务调用失败:{}", throwable.getMessage());
        return new RemoteWxAppService() {
            @Override
            public R<LoginUser> getUserInfo(String source) {
                return R.fail("删除redisKey失败:" + throwable.getMessage());
            }
        };
    }
}
