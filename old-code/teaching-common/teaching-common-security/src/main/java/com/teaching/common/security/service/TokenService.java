package com.teaching.common.security.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.teaching.system.api.RemoteUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.teaching.common.core.constant.CacheConstants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.utils.JwtUtils;
import com.teaching.common.core.utils.ServletUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.ip.IpUtils;
import com.teaching.common.core.utils.uuid.IdUtils;
import com.teaching.common.redis.service.RedisService;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.model.LoginUser;

import javax.crypto.SecretKey;

import static com.teaching.common.core.utils.JwtUtils.parseToken;

/**
 * token验证处理
 *
 * @author teaching
 */
@Component
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private RemoteUserService userService;

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private final static long TOKEN_EXPIRE_TIME = CacheConstants.EXPIRATION;

    private final static String ACCESS_TOKEN = CacheConstants.LOGIN_TOKEN_KEY;

    private final static Long TOKEN_REFRESH_THRESHOLD_MINUTES = CacheConstants.REFRESH_TIME * MILLIS_MINUTE;

    // Redis键前缀
    private static final String TOKEN_PREFIX = "token:";
    private static final String USER_TOKEN_PREFIX = "user:token:";

    // 支持的设备类型（与前端传递的X-Device-Type对应）
    public enum DeviceType {
        PC, MINI_PROGRAM, UNKNOWN // 可扩展：ANDROID_APP、IOS_APP等
    }

    /**
     * 创建令牌
     */
    public Map<String, Object> createToken(LoginUser loginUser) {
        String token = IdUtils.fastUUID();
        Long userId = loginUser.getSysUser().getUserId();
        String userName = loginUser.getSysUser().getUserName();
        loginUser.setToken(token);
        loginUser.setUserid(userId);
        loginUser.setUsername(userName);
        loginUser.setIpaddr(IpUtils.getIpAddr());
        refreshToken(loginUser);

        // Jwt存储信息
        Map<String, Object> claimsMap = new HashMap<String, Object>();
        claimsMap.put(SecurityConstants.USER_KEY, token);
        claimsMap.put(SecurityConstants.DETAILS_USER_ID, userId);
        claimsMap.put(SecurityConstants.DETAILS_USERNAME, userName);

        // 接口返回信息
        Map<String, Object> rspMap = new HashMap<String, Object>();
        rspMap.put("access_token", JwtUtils.createToken(claimsMap));
        rspMap.put("expires_in", 120L);
        return rspMap;
    }

    public String createToken1(LoginUser loginUser) {
        String token = IdUtils.fastUUID();
        Long userId = loginUser.getSysUser().getUserId();
        String userName = loginUser.getSysUser().getUserName();
        loginUser.setToken(token);
        loginUser.setUserid(userId);
        loginUser.setUsername(userName);
        loginUser.setIpaddr(IpUtils.getIpAddr());
        refreshToken1(loginUser);

        // Jwt存储信息
        Map<String, Object> claimsMap = new HashMap<String, Object>();
        claimsMap.put(SecurityConstants.USER_KEY, token + "_" + SecurityConstants.MINI_PROGRAM);
        claimsMap.put(SecurityConstants.DETAILS_USER_ID, userId);
        claimsMap.put(SecurityConstants.DETAILS_USERNAME, userName);

        /*// 接口返回信息
        Map<String, Object> rspMap = new HashMap<String, Object>();
        rspMap.put("access_token", JwtUtils.createToken(claimsMap));
        rspMap.put("expires_in", 2592000000L);*/
        return JwtUtils.createToken(claimsMap);
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser() {
        return getLoginUser(ServletUtils.getRequest());
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser(HttpServletRequest request) {
        // 获取请求携带的令牌
        String token = SecurityUtils.getToken(request);
        return getLoginUser(token);
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser(String token) {
        LoginUser user = null;
        try {
            if (StringUtils.isNotEmpty(token)) {
                String userkey = JwtUtils.getUserKey(token);
                user = redisService.getCacheObject(getTokenKey(userkey));
                return user;
            }
        } catch (Exception e) {
            log.error("获取用户信息异常'{}'", e.getMessage());
        }
        return user;
    }

    /**
     * 设置用户身份信息
     */
    public void setLoginUser(LoginUser loginUser) {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken())) {
            refreshToken(loginUser);
        }
    }

    /**
     * 删除用户缓存信息
     */
    public void delLoginUser(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String userkey = JwtUtils.getUserKey(token);
            redisService.deleteObject(getTokenKey(userkey));
        }
    }

    /**
     * 验证令牌有效期，相差不足120分钟，自动刷新缓存
     *
     * @param loginUser
     */
    public void verifyToken(LoginUser loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= 3600000L) {
            if (SecurityConstants.MINI_PROGRAM.equals(loginUser.getPlatformType())) {
                refreshToken1(loginUser);
            } else {
                refreshToken(loginUser);
            }
        }
    }

    /**
     * 刷新令牌有效期
     *
     * @param loginUser 登录信息
     */
    public void refreshToken(LoginUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + 7200000L);
        // 根据uuid将loginUser缓存
        String userKey = getTokenKey(loginUser.getToken());
        redisService.setCacheObject(userKey, loginUser, 120L, TimeUnit.MINUTES);
    }

    public void refreshToken1(LoginUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + 7200000L);
        // 根据uuid将loginUser缓存
        String userKey = getTokenKey(loginUser.getToken());
        redisService.setCacheObject(userKey + "_" + SecurityConstants.MINI_PROGRAM, loginUser, 30L, TimeUnit.DAYS);
    }

    private String getTokenKey(String token) {
        return ACCESS_TOKEN + token;
    }

    /**
     * 生成JWT令牌（带设备维度，避免PC/小程序Token覆盖）
     *
     * @param claims 自定义声明（需包含userId、username等）
     * @return JWT令牌
     */
    public String generateToken(Map<String, Object> claims) {
        // 提取userId
        String userId = claims.get("userId") != null ? claims.get("userId").toString() : null;
        // 生成token
        String tokenId = IdUtils.fastUUID();
        // Jwt存储信息
        Map<String, Object> claimsMap = new HashMap<String, Object>();
        claimsMap.put(SecurityConstants.USER_KEY, tokenId);
        claimsMap.put(SecurityConstants.DETAILS_USER_ID, userId);
        String token = JwtUtils.createToken(claimsMap);
        if (userId != null) {
            // 存储token到Redis（token -> userId，不变）
            redisService.setCacheObject(ACCESS_TOKEN + token, userId, 30L, TimeUnit.DAYS);
            log.debug("令牌生成成功, userId: {}", userId);
        }
        return token;
    }
}
