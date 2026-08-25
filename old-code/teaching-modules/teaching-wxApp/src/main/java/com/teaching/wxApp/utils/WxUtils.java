package com.teaching.wxApp.utils;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


/**
 * 微信工具类，处理微信用户信息解密等操作
 */
@Component
@Slf4j
@AllArgsConstructor
public class WxUtils {

    @Autowired
    private WxMaService wxMaService;


    /**
     * 通过code获取openId和sessionKey
     *
     * @param code 微信登录code
     * @return 包含openId和sessionKey
     */
    public WxMaJscode2SessionResult getOpenIdAndSessionKey(String code) throws WxErrorException {
        if (!StringUtils.hasText(code)) {
            throw new RuntimeException("code 不能为空！");
        }
        WxMaJscode2SessionResult session = wxMaService.getUserService().getSessionInfo(code);
        log.info(session.getSessionKey());
        log.info(session.getOpenid());
        return session;
    }

    /**
     * 解密微信用户信息
     *
     * @param encryptedData 加密的用户数据
     * @param sessionKey    会话密钥
     * @param iv            加密向量
     * @return 解密后的用户信息
     */
    public WxMaUserInfo getUserInfo(String encryptedData, String sessionKey, String iv) throws WxErrorException {
        if (!StringUtils.hasText(encryptedData) || !StringUtils.hasText(sessionKey) || !StringUtils.hasText(iv)) {
            throw new RuntimeException("encryptedData、sessionKey和iv不能为空");
        }
        return wxMaService.getUserService().getUserInfo(sessionKey, encryptedData, iv);
    }

    /**
     * 获取用户手机号
     *
     * @param sessionKey 解密手机小程序接口必传参数
     * @return 解密后的用户信息
     */
    public String getPhone(String sessionKey, String iv, String encryptedData) {
        if (!StringUtils.hasText(sessionKey)) {
            throw new RuntimeException("sessionKey不能为空");
        }
        String phone = "";
        try {
            phone = decryptPhone(encryptedData, sessionKey, iv);

        } catch (Exception e) {
            throw new RuntimeException("获取手机号失败" + e.getMessage());
        }

        return phone;

    }

    public String getWxPhone(String sessionKey, String iv, String encryptedData) {

        // 1. 解析JSON字符串为JSONObject
        JSONObject phoneObj = JSONObject.parseObject(getPhone(sessionKey,iv,encryptedData));

        // 2. 提取手机号（国内业务选phoneNumber）
        return phoneObj.getString("phoneNumber");
    }


    /**
     * 过滤昵称中的特殊字符
     *
     * @param nickname wx昵称
     * @return 处理后的wx昵称
     */
    private String filterSpecialChars(String nickname) {
        if (nickname == null) {
            return "";
        }
        // 移除危险字符或表情符号（根据业务需求调整）
        return nickname.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9_]", "");
    }


    //解密手机号（AES-128-CBC解密）
    private String decryptPhone(String encryptedData, String sessionKey, String iv) throws Exception {
        // Base64解码
        byte[] encryptedDataBytes = Base64.getDecoder().decode(encryptedData);
        byte[] sessionKeyBytes = Base64.getDecoder().decode(sessionKey);
        byte[] ivBytes = Base64.getDecoder().decode(iv);

        // 初始化加密算法
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(sessionKeyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        // 解密并转成字符串
        byte[] decrypted = cipher.doFinal(encryptedDataBytes);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}