package com.teaching.system.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.teaching.common.security.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.core.constant.CacheConstants;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.api.model.LoginUser;
import com.teaching.system.domain.SysUserOnline;
import com.teaching.system.service.ISysUserOnlineService;

/**
 * 在线用户监控
 *
 * @author teaching
 */
@RestController
@RequestMapping("/online")
public class SysUserOnlineController extends BaseController {
    @Autowired
    private ISysUserOnlineService userOnlineService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TokenService tokenService;

    @RequiresPermissions("monitor:online:list")
    @GetMapping("/list")
    public TableDataInfo list(String ipaddr, String userName) {
        // 获取所有登录token的key
        Collection<String> keys = redisService.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        List<SysUserOnline> userOnlineList = new ArrayList<SysUserOnline>();
        for (String key : keys) {
            LoginUser user = redisService.getCacheObject(key);
            // 清理已过期或失效的残留token
            if (user == null) {
                redisService.deleteObject(key);
                continue;
            }
            // 校验token有效性（防止Redis未过期但JWT已过期的情况）
            if (user.getExpireTime() != null && user.getExpireTime() < System.currentTimeMillis()) {
                redisService.deleteObject(key);
                continue;
            }
            // 根据查询条件筛选
            SysUserOnline online = null;
            if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
                online = userOnlineService.selectOnlineByInfoLike(ipaddr, userName, user);
            } else if (StringUtils.isNotEmpty(ipaddr)) {
                online = userOnlineService.selectOnlineByIpaddrLike(ipaddr, user);
            } else if (StringUtils.isNotEmpty(userName)) {
                online = userOnlineService.selectOnlineByUserNameLike(userName, user);
            } else {
                online = userOnlineService.loginUserToUserOnline(user);
            }
            // 只添加有效的在线用户
            if (online != null) {
                userOnlineList.add(online);
            }
        }
        // 按登录时间倒序排序（修复整数溢出问题）
        userOnlineList.sort((o1, o2) -> Long.compare(o2.getLoginTime(), o1.getLoginTime()));

        return getDataTable(userOnlineList);
    }

    /**
     * 强退用户
     */
    @RequiresPermissions("monitor:online:forceLogout")
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{tokenId}")
    public AjaxResult forceLogout(@PathVariable String tokenId) {
        redisService.deleteObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId);
        return success();
    }
}
