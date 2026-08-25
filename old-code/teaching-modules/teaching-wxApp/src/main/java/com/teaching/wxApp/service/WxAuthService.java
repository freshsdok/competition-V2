package com.teaching.wxApp.service;

import com.teaching.wxApp.domain.WxLoginDTO;

import java.util.Map;

public interface WxAuthService {

    /**
     * 微信用户登录
     * @param wxLoginDTO 微信小程序传递的登录code\手机号\邀请码(非必填)
     * @return 登录结果
     */
    String wxLogin(WxLoginDTO wxLoginDTO);
}
