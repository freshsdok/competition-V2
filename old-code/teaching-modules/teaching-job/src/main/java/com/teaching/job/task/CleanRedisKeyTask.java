package com.teaching.job.task;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.system.api.RemoteWxAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("cleanRedisKeyTask")
public class CleanRedisKeyTask {

    @Autowired
    private RemoteWxAppService remoteWxAppService;

    public void cleanRedisKeyTask() {
        remoteWxAppService.getUserInfo(SecurityConstants.INNER);
    }
}
