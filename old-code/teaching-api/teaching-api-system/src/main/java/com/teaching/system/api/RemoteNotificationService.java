package com.teaching.system.api;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.ServiceNameConstants;
import com.teaching.common.core.domain.R;
import com.teaching.system.api.domain.NotificationSendDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 站内信远程服务
 */
@FeignClient(contextId = "remoteNotificationService", value = ServiceNameConstants.SYSTEM_SERVICE)
public interface RemoteNotificationService {

    /**
     * 发送站内信（内部调用）
     */
    @PostMapping("/notification/sender/inner/send")
    R<Boolean> send(@RequestBody NotificationSendDTO dto,
                    @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}

