package com.teaching.wxApp.service.impl;

import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.service.TokenService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.model.LoginUser;
import com.teaching.wxApp.domain.WxLoginDTO;
import com.teaching.wxApp.service.WxAuthService;
import com.teaching.wxApp.utils.WxUtils;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WxAuthServiceImpl implements WxAuthService {

    @Autowired
    private WxUtils wxUtils;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RemoteUserService userService;
    @Autowired
    private RedisService redisService;

    @Override
    public String wxLogin(WxLoginDTO wxLoginDTO) {
        // 入参基础校验（避免空指针）
        if (wxLoginDTO == null || StringUtils.isEmpty(wxLoginDTO.getCode())) {
            String errorMsg = "登录参数缺失，code不能为空";
            log.error(errorMsg);
            throw new GlobalException("参数错误，请检查请求内容");
        }

        try {
            WxMaJscode2SessionResult sessionResult = null;
            // 3. 调用微信接口获取 OpenID/UnionID/SessionKey（核心步骤）
            sessionResult = wxUtils.getOpenIdAndSessionKey(wxLoginDTO.getCode());
//            //测试使用
//            WxMaJscode2SessionResult sessionResult = new WxMaJscode2SessionResult();
//            sessionResult.setOpenid("o1Wd514nLNE-OcMoi0qM05z9fR0o");
//            sessionResult.setSessionKey("asdasdasdasdasdasdasd");

            if (sessionResult == null) {
                throw new RuntimeException("微信接口返回空结果，无法获取登录信息");
            }

            // 4. OpenID 校验（微信登录核心标识，必须非空）
            String openId = sessionResult.getOpenid();
            if (StringUtils.isEmpty(openId)) {
                throw new RuntimeException("从微信接口获取 OpenID 失败，OpenID 为空");
            }

            // 5. UnionID 非强制校验（仅当开放平台绑定后才返回，避免非绑定场景登录失败）
            String unionid = sessionResult.getUnionid();
            if (StringUtils.isEmpty(unionid)) {
                log.warn("当前用户未绑定微信开放平台，UnionID 为空，OpenID：{}", openId);
            }

            // 6. 查询用户（避免后续重复查询）
//            SysUser user = null;
            SysUser user = redisService.getCacheObject(SecurityConstants.WX_LOGIN_INFO + openId);
            String phone = null;
            if(user == null){
                //改成传手机号
                phone = wxUtils.getWxPhone(sessionResult.getSessionKey(), wxLoginDTO.getIv(), wxLoginDTO.getEncryptedData());
                wxLoginDTO.setPhone(phone);
                log.warn("查询用户{}","手机号,openId{}",phone,openId);
                R<SysUser> integerR = userService.queryWxSysUser(phone, SecurityConstants.INNER);
                if(R.isSuccess(integerR) && integerR.getData() != null){
                    user = integerR.getData();
                    redisService.setCacheObject(SecurityConstants.WX_LOGIN_INFO + openId, user);
                }
            }
            if(user == null){
                log.warn("查询用户成功，但没找到"+openId);
                user = buildNewWxUserHasPhone(sessionResult, wxLoginDTO); // 构建新用户对象
                R<Long> createWxUserR = userService.registerWxSysUser(user, SecurityConstants.INNER);
                if(R.isSuccess(createWxUserR) && createWxUserR.getData() > 0){
                    user.setUserId(createWxUserR.getData());
                    log.info("微信用户注册成功");
                } else {
                    log.error("微信用户注册失败");
                    throw new GlobalException("微信用户注册失败");
                }
                System.out.println("注册微信用户成功"+user.getUserId());
            }
            LoginUser loginUser = new LoginUser();
            loginUser.setSysUser(user);
            loginUser.setUserid(user.getUserId());
            loginUser.setUsername(user.getUserName());
            loginUser.setLoginTime(System.currentTimeMillis());
            return tokenService.createToken1(loginUser);

        } catch (WxErrorException e) {
            // 微信接口异常（如 code 过期、签名错误等，需特殊捕获）
            String errorMsg = String.format("微信接口调用失败：%s（错误码：%s）", e.getError().getErrorMsg(), e.getError().getErrorCode());
            log.error(errorMsg, e);
            throw new GlobalException(errorMsg);
        } catch (RuntimeException e) {
            // 业务逻辑异常（如参数错误、保存失败等）
            log.error("微信登录业务处理失败：{}", e.getMessage(), e);
            throw e; // 向上抛出，由全局异常处理器统一返回
        } catch (Exception e) {
            // 未知异常（兜底处理，避免服务崩溃）
            String errorMsg = "微信登录过程发生未知异常";
            log.error(errorMsg, e);
            throw new GlobalException(errorMsg);
        }
    }

    /**
     * 生成 JWT 令牌
     *
     * @param user 已保存的用户对象
     * @return JWT令牌
     */
    private String generateJwtToken(SysUser user,String openId) {
        if (user == null || user.getUserId() == null) {
            throw new RuntimeException("生成 JWT 失败：用户ID为空");
        }

        // 构建 JWT 载荷（仅包含必要信息，避免敏感数据）
        Map<String, Object> claims = new HashMap<>(2);
        claims.put("userId", user.getUserId());
        claims.put("openId", openId);
        claims.put("username", user.getUserName());

        // 调用 JWT 服务生成令牌（指定设备类型）
        return tokenService.generateToken(claims);
    }

    /**
     * 构建新微信用户对象
     *
     * @param sessionResult 微信登录结果（OpenID/UnionID/SessionKey）
     * @param wxLoginDTO    登录参数（含手机号解密所需 iv/encryptedData）
     * @return 新用户对象
     */
    private SysUser buildNewWxUser(WxMaJscode2SessionResult sessionResult, WxLoginDTO wxLoginDTO) {
        SysUser user = new SysUser();
        user.setOpenId(sessionResult.getOpenid());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setUserType("03");

        // 解密手机号（需校验 iv/encryptedData 非空，避免解密失败）
        String sessionKey = sessionResult.getSessionKey();
        if (!StringUtils.isEmpty(sessionKey)
                && !StringUtils.isEmpty(wxLoginDTO.getIv())
                && !StringUtils.isEmpty(wxLoginDTO.getEncryptedData())) {
            try {
                String phone = wxUtils.getWxPhone(sessionKey, wxLoginDTO.getIv(), wxLoginDTO.getEncryptedData());
                user.setPhonenumber(phone);
                user.setUserName(phone);
                user.setNickName(phone);
                log.info("新用户手机号解密成功，OpenID：{}，手机号：{}", user.getOpenId(), phone);
            } catch (Exception e) {
                log.warn("新用户手机号解密失败，OpenID：{}", user.getOpenId(), e);
            }
        } else {
            log.warn("新用户手机号解密参数缺失，无法解密，OpenID：{}", user.getOpenId());
        }

        return user;
    }

    /**
     * 构建新微信用户对象
     * @param sessionResult
     * @param wxLoginDTO
     * @return
     */
    private SysUser buildNewWxUserHasPhone(WxMaJscode2SessionResult sessionResult, WxLoginDTO wxLoginDTO) {
        SysUser user = new SysUser();
        user.setOpenId(sessionResult.getOpenid());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setUserType("03");
        String phone = wxLoginDTO.getPhone();
        user.setPhonenumber(phone);
        user.setUserName(phone);
        user.setNickName(phone);
        return user;
    }
}
