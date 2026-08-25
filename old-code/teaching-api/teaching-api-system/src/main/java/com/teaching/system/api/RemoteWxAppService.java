package com.teaching.system.api;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.ServiceNameConstants;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.factory.RemoteUserFallbackFactory;
import com.teaching.system.api.model.LoginUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(contextId = "remoteWxAppService", value = ServiceNameConstants.WX_SERVICE, fallbackFactory = RemoteUserFallbackFactory.class)
public interface RemoteWxAppService {

    @GetMapping("/wxClean/redisKey")
    public R<LoginUser> getUserInfo(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
