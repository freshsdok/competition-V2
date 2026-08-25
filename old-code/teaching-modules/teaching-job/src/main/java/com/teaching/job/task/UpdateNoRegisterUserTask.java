package com.teaching.job.task;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.system.api.RemoteUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("updateNoRegisterUserTask")
public class UpdateNoRegisterUserTask {
    private static final Logger logger = LoggerFactory.getLogger(UpdateNoRegisterUserTask.class);

    @Autowired
    private RemoteUserService remoteUserService;

    public void updateNoRegisterUser(String updateSize) {
        logger.info("开始刷新用户状态及密码");
        try {
            remoteUserService.updateNoRegisterUser(updateSize, SecurityConstants.INNER);
        } catch (Exception e) {
            logger.error("刷新用户状态及密码失败:"+e);
        }
    }
}
