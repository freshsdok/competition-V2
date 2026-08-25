package com.teaching.common.core.utils;

import java.util.Map;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.TokenConstants;
import com.teaching.common.core.text.Convert;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.core.env.Environment;

/**
 * Jwt工具类
 *
 * @author teaching
 */
public class JwtUtils
{
    private static final String[] PROPERTY_KEYS = { "teaching.jwt.secret", "jwt.secret", "token.secret" };

    private static final String[] ENV_KEYS = { "TEACHING_JWT_SECRET", "JWT_SECRET" };

    public static volatile String secret;

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claims)
    {
        String token = Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, getSecret()).compact();
        return token;
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    public static Claims parseToken(String token)
    {
        return Jwts.parser().setSigningKey(getSecret()).parseClaimsJws(token).getBody();
    }

    public static String getSecret()
    {
        String configuredSecret = secret;
        if (StringUtils.isNotEmpty(configuredSecret))
        {
            return configuredSecret;
        }
        synchronized (JwtUtils.class)
        {
            if (StringUtils.isEmpty(secret))
            {
                secret = resolveSecret();
            }
            return secret;
        }
    }

    private static String resolveSecret()
    {
        String configuredSecret = resolveFromSystemProperties();
        if (StringUtils.isNotEmpty(configuredSecret))
        {
            return configuredSecret.trim();
        }
        configuredSecret = resolveFromEnvironmentVariables();
        if (StringUtils.isNotEmpty(configuredSecret))
        {
            return configuredSecret.trim();
        }
        configuredSecret = resolveFromSpringEnvironment();
        if (StringUtils.isNotEmpty(configuredSecret))
        {
            return configuredSecret.trim();
        }
        return TokenConstants.SECRET;
    }

    private static String resolveFromSystemProperties()
    {
        for (String propertyKey : PROPERTY_KEYS)
        {
            String configuredSecret = System.getProperty(propertyKey);
            if (StringUtils.isNotEmpty(configuredSecret))
            {
                return configuredSecret;
            }
        }
        return null;
    }

    private static String resolveFromEnvironmentVariables()
    {
        for (String envKey : ENV_KEYS)
        {
            String configuredSecret = System.getenv(envKey);
            if (StringUtils.isNotEmpty(configuredSecret))
            {
                return configuredSecret;
            }
        }
        return null;
    }

    private static String resolveFromSpringEnvironment()
    {
        try
        {
            Environment environment = SpringUtils.getBean(Environment.class);
            for (String propertyKey : PROPERTY_KEYS)
            {
                String configuredSecret = environment.getProperty(propertyKey);
                if (StringUtils.isNotEmpty(configuredSecret))
                {
                    return configuredSecret;
                }
            }
        }
        catch (Exception ignored)
        {
        }
        return null;
    }

    /**
     * 根据令牌获取用户标识
     * 
     * @param token 令牌
     * @return 用户ID
     */
    public static String getUserKey(String token)
    {
        Claims claims = parseToken(token);
        return getValue(claims, SecurityConstants.USER_KEY);
    }

    /**
     * 根据令牌获取用户标识
     * 
     * @param claims 身份信息
     * @return 用户ID
     */
    public static String getUserKey(Claims claims)
    {
        return getValue(claims, SecurityConstants.USER_KEY);
    }

    /**
     * 根据令牌获取用户ID
     * 
     * @param token 令牌
     * @return 用户ID
     */
    public static String getUserId(String token)
    {
        Claims claims = parseToken(token);
        return getValue(claims, SecurityConstants.DETAILS_USER_ID);
    }

    /**
     * 根据身份信息获取用户ID
     * 
     * @param claims 身份信息
     * @return 用户ID
     */
    public static String getUserId(Claims claims)
    {
        return getValue(claims, SecurityConstants.DETAILS_USER_ID);
    }

    /**
     * 根据令牌获取用户名
     * 
     * @param token 令牌
     * @return 用户名
     */
    public static String getUserName(String token)
    {
        Claims claims = parseToken(token);
        return getValue(claims, SecurityConstants.DETAILS_USERNAME);
    }

    /**
     * 根据身份信息获取用户名
     * 
     * @param claims 身份信息
     * @return 用户名
     */
    public static String getUserName(Claims claims)
    {
        return getValue(claims, SecurityConstants.DETAILS_USERNAME);
    }

    /**
     * 根据身份信息获取键值
     * 
     * @param claims 身份信息
     * @param key 键
     * @return 值
     */
    public static String getValue(Claims claims, String key)
    {
        return Convert.toStr(claims.get(key), "");
    }
}
