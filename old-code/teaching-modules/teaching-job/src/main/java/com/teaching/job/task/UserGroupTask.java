package com.teaching.job.task;

import com.teaching.common.core.constant.CacheConstants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.redis.service.RedisService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.model.LoginUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 */
@Component("userGroupTask")
public class UserGroupTask {

    private static final Logger logger = LoggerFactory.getLogger(UserGroupTask.class);
    @Autowired
    private RemoteUserService remoteUserService;
    @Autowired
    private RedisService redisService;

    /**
     * 更新某用户组下的人员信息
     *
     * @param userGroupId
     */
    public void updateUserIdsByUserGroup(Long userGroupId) {
        try {
            remoteUserService.updateUserIdsByUserGroup(userGroupId, SecurityConstants.INNER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新所有用户组下的人员信息
     */
    public void updateUserIdsByUserGroups() {
        try {
            remoteUserService.updateUserIdsByUserGroup(null, SecurityConstants.INNER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新所有登录用户的token过期时间（过期时间大于7200秒的）
     */
    public void updateLoginTokens() {
        redisService.keys(CacheConstants.LOGIN_TOKEN_KEY + "*").forEach(key -> {
            if(key.endsWith("_"+SecurityConstants.MINI_PROGRAM)){
                return;
            }
            LoginUser loginUser = redisService.getCacheObject(key);
            if (loginUser != null) {
                Long expireTime = loginUser.getExpireTime();
                Long loginTime = loginUser.getLoginTime();
                if (expireTime != null && loginTime != null && (expireTime - loginTime > 7200000L)) {
                    loginUser.setExpireTime(loginTime + 7200000L);
                    redisService.setCacheObject(key, loginUser, 120L, TimeUnit.MINUTES);
                }
            }
            long expire = redisService.getExpire(key);
            //expire>7200的设置成7200
            if (expire > 7200) {
                redisService.expire(key, 7200);
            }
        });
    }

    /**
     * 处理pdf文件
     * @param fileTaskId 文件任务id  （file_upload_manager表的file_task_id（文件任务id(file_task表中id)））
     */
    public void handlePdf(String fileTaskId){
        logger.info("开始处理pdf文件:{}",fileTaskId);
        remoteUserService.handlePdf(fileTaskId, SecurityConstants.INNER);
        logger.info("处理pdf文件结束:{}",fileTaskId);
    }
}
