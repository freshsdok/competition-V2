package com.teaching.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.auth.form.LoginBody;
import com.teaching.auth.form.RegisterBody;
import com.teaching.auth.service.SysLoginService;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.utils.JwtUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.auth.AuthUtil;
import com.teaching.common.security.service.TokenService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.model.LoginUser;

/**
 * token 控制
 *
 * @author teaching
 */
@RestController
public class TokenController
{
    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysLoginService sysLoginService;

    @PostMapping("login")
    public R<?> login(@RequestBody LoginBody form, HttpServletRequest request)
    {
        // 用户登录
        LoginUser userInfo = sysLoginService.login(form.getUsername(), form.getPassword(), request);
        if (userInfo.getSysUser() ==null){
            return R.fail("不存在用户，请先注册");
        }
         //获取登录token
        return R.ok(tokenService.createToken(userInfo));
    }

    @DeleteMapping("logout")
    public R<?> logout(HttpServletRequest request)
    {
        String token = SecurityUtils.getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            try
            {
                String username = JwtUtils.getUserName(token);
                // 删除用户缓存记录
                AuthUtil.logoutByToken(token);
                // 记录用户退出日志
                sysLoginService.logout(username, request);
            }
            catch (Exception ignored)
            {
                // Token may have been signed with an older secret. Logout should still let the client clear it.
            }
        }
        return R.ok();
    }

    @PostMapping("refresh")
    public R<?> refresh(HttpServletRequest request)
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser)) {
            // 刷新令牌有效期
            tokenService.refreshToken(loginUser);
            return R.ok();
        }
        return R.ok();
    }

    @PostMapping("register")
    public R<?> register(@RequestBody RegisterBody registerBody)
    {
        // 用户注册
        sysLoginService.register(registerBody.getUsername(), registerBody.getPassword());
        return R.ok();
    }
}
