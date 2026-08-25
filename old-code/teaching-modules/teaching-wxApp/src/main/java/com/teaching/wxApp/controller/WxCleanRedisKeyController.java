package com.teaching.wxApp.controller;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.annotation.InnerAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/wxClean")
public class WxCleanRedisKeyController extends BaseController {

    @Autowired
    private RedisService redisService;

    @InnerAuth
    @GetMapping("/redisKey")
    public AjaxResult clean() {
        // 定制化清楚wxApp缓存
        String cleanKey = SecurityConstants.COMPETITION_APPLY_TEAM_INFO_BY_SERIESID + "*";
        String indexKey = SecurityConstants.COMPETITION_APPLY_USER_TEAM_INDEX + "*";
        String retryIndexKey = SecurityConstants.COMPETITION_APPLY_RETRY_INDEX + "*";
        logger.info("开始删除redisKey:" + cleanKey+ "," + indexKey+ "," + retryIndexKey);
        Collection<String> keys = redisService.keys(cleanKey);
        redisService.deleteObject(keys);
        Collection<String> indexKeys = redisService.keys(indexKey);
        redisService.deleteObject(indexKeys);
        Collection<String> retryIndexKeys = redisService.keys(retryIndexKey);
        redisService.deleteObject(retryIndexKeys);
        return success();
    }
}
