package com.teaching.common.core.constant;

/**
 * 权限相关通用常量
 *
 * @author teaching
 */
public class SecurityConstants
{
    /**
     * 用户ID字段
     */
    public static final String DETAILS_USER_ID = "user_id";

    /**
     * 用户名字段
     */
    public static final String DETAILS_USERNAME = "username";

    /**
     * 授权信息字段
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * 请求来源
     */
    public static final String FROM_SOURCE = "from-source";

    /**
     * 内部请求
     */
    public static final String INNER = "inner";

    /**
     * 用户标识
     */
    public static final String USER_KEY = "user_key";

    /**
     * 平台
     */
    public static final String TJ_PLATFORM_TYPE = "tjPlatformType";
    /**
     * 小程序标识
     */
    public static final String MINI_PROGRAM = "miniProgram";

    /**
     * 登录用户
     */
    public static final String LOGIN_USER = "login_user";

    /**
     * 角色权限
     */
    public static final String ROLE_PERMISSION = "role_permission";
    /**
     * 缓存微信二维码配置
     */
    public static final String WX_CODE_CONFIG = "WX_CODE_CONFIG:";
    /**
     * 缓存微信二维码记录及配置信息
     */
    public static final String WX_QC_CODE_RECORD = "WX_QC_CODE_RECORD:";
    /**
     * 微信登录
     */
    public static final String WX_LOGIN_INFO = "WX_LOGIN_INFO:";
    /**
     * 根据userId+competitionSeriesId查询的报名信息（整个团队）
     */
    public static final String COMPETITION_APPLY_TEAM_INFO = "COMPETITION_APPLY_TEAM_INFO:";
    /**
     * 根据赛事系列id查询报名信息（所有团队）
     */
    public static final String COMPETITION_APPLY_TEAM_INFO_BY_SERIESID = "COMPETITION_APPLY_TEAM_INFO_BY_SERIESID:";

    /**
     * 用户报名信息索引（userId -> teamCodes）
     */
    public static final String COMPETITION_APPLY_USER_TEAM_INDEX = "COMPETITION_APPLY_USER_TEAM_INDEX:";

    /**
     * 扫码重试索引（schoolName|userName|idCard后6位 -> teamCodes）
     */
    public static final String COMPETITION_APPLY_RETRY_INDEX = "COMPETITION_APPLY_RETRY_INDEX:";
}
