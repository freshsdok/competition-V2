package com.teaching.system.service;

import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.context.SecurityContextHolder;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.system.api.model.LoginUser;

/** 使用Token解析后的登录对象，不把请求头中的userId当作办理身份。 */
public final class TeamCollectionActor {
    private TeamCollectionActor() {}

    public static long currentUserId() {
        LoginUser user = SecurityContextHolder.get(SecurityConstants.LOGIN_USER, LoginUser.class);
        if (user == null || user.getUserid() == null || user.getUserid() <= 0) {
            throw new ServiceException("请先登录原平台", 401);
        }
        return user.getUserid();
    }
}
