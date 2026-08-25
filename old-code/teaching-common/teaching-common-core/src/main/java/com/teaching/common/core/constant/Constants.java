package com.teaching.common.core.constant;

/**
 * 通用常量信息
 * 
 * @author teaching
 */
public class Constants
{
    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * www主域
     */
    public static final String WWW = "www.";

    /**
     * RMI 远程方法调用
     */
    public static final String LOOKUP_RMI = "rmi:";

    /**
     * LDAP 远程方法调用
     */
    public static final String LOOKUP_LDAP = "ldap:";

    /**
     * LDAPS 远程方法调用
     */
    public static final String LOOKUP_LDAPS = "ldaps:";

    /**
     * http请求
     */
    public static final String HTTP = "http://";

    /**
     * https请求
     */
    public static final String HTTPS = "https://";

    /**
     * 成功标记
     */
    public static final Integer SUCCESS = 200;

    /**
     * 失败标记
     */
    public static final Integer FAIL = 500;

    /**
     * 登录成功状态
     */
    public static final String LOGIN_SUCCESS_STATUS = "0";

    /**
     * 登录失败状态
     */
    public static final String LOGIN_FAIL_STATUS = "1";

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    public static final String LOGOUT = "Logout";

    /**
     * 注册
     */
    public static final String REGISTER = "Register";

    /**
     * 登录失败
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 当前记录起始索引
     */
    public static final String PAGE_NUM = "pageNum";

    /**
     * 每页显示记录数
     */
    public static final String PAGE_SIZE = "pageSize";

    /**
     * 排序列
     */
    public static final String ORDER_BY_COLUMN = "orderByColumn";

    /**
     * 排序的方向 "desc" 或者 "asc".
     */
    public static final String IS_ASC = "isAsc";

    /**
     * 验证码有效期（分钟）
     */
    public static final long CAPTCHA_EXPIRATION = 2;

    /**
     * 资源映射路径 前缀
     */
    public static final String RESOURCE_PREFIX = "/profile";

    /**
     * 自动识别json对象白名单配置（仅允许解析的包名，范围越小越安全）
     */
    public static final String[] JSON_WHITELIST_STR = { "com.teaching" };

    /**
     * 定时任务白名单配置（仅允许访问的包名，如其他需要可以自行添加）
     */
    public static final String[] JOB_WHITELIST_STR = { "com.teaching.job.task" };

    /**
     * 定时任务违规的字符
     */
    public static final String[] JOB_ERROR_STR = { "java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
            "org.springframework", "org.apache", "com.teaching.common.core.utils.file" };

    /**
     * 审核状态
     */
    // 草稿
    public static final String DRAFT = "1";
    // 待审核
    public static final String NO_CHECK = "2";
    // 审核中
    public static final String CHECKING = "3";
    // 审核通过
    public static final String CHECK_PASS = "4";
    // 审核驳回
    public static final String CHECK_REJECT = "5";
    // 已发布
    public static final String COMPETITION_PUBLISH = "6";
    // 进行中
    public static final String COMPETITION_RUNNING = "7";
    // 已结束
    public static final String COMPETITION_END = "8";
    // 已撤销发布
    public static final String COMPETITION_REPEAL_PUBLISH = "9";

    /**
     * 实名认证状态
     */
    // 未认证
    public static final String AUTH_STATUS_WAIT = "1";
    // 待提交
    public static final String AUTH_STATUS_SUBMIT = "2";
    // 待审核
    public static final String AUTH_STATUS_CHECKING = "3";
    // 人脸识别中
    public static final String AUTH_STATUS_FACE_CHECKING = "4";
    // 认证通过
    public static final String AUTH_STATUS_PASS = "5";
    // 认证失败
    public static final String AUTH_STATUS_FAIL = "6";
    /**
     * 身份认证状态
     */
    // 未身份认证
    public static final String IDENTITY_NO_CHECK = "2";
    // 审核中
    public static final String IDENTITY_CHECKING = "3";
    // 审核通过
    public static final String IDENTITY_CHECK_PASS = "4";
    // 审核驳回
    public static final String IDENTITY_CHECK_REJECT = "5";
    // 认证通过
    public static final String IDENTITY_AUTH_PASS = "6";
    // 认证失败
    public static final String IDENTITY_AUTH_FAIL = "7";

    /**
     * 参赛方式
     */
    // 单人参赛
    public static final String JOIN_TYPE_PERSON = "1";
    // 团队参赛
    public static final String JOIN_TYPE_TEAM = "2";
    // 参赛人数不限
    public static final String JOIN_TYPE_NO_LIMIT = "3";

    /**
     * 作品评审状态
     */
    // 待评审
    public static final String WORKS_NO_JUDGE = "1";

    // 评审中
    public static final String WORKS_JUDGE_RUNNING = "2";

    // 评审完成
    public static final String WORKS_JUDGE_FINISH = "3";

    /**
     * 是否
     */

    public static final String IS_YES = "1";

    public static final String IS_NO = "2";

    /**
     * 身份认证类型
     */
    // 学生
    public static final String IDENTITY_TYPE_STUDENT = "student";

    // 教师
    public static final String IDENTITY_TYPE_TEACHER = "teacher";

    // 学校
    public static final String IDENTITY_TYPE_SCHOOL = "school";

    // 企业
    public static final String IDENTITY_TYPE_ENTERPRISE = "enterprise";

    // 加入团队状态
    // 等待
    public static final String JOIN_TEAM_WAIT = "1";
    // 同意
    public static final String JOIN_TEAM_AGREE = "2";
    // 拒绝
    public static final String JOIN_TEAM_REJECT = "3";
    // 晋级方式
    // 按照分数
    public static final String ADVANCE_TYPE_SCORE = "1";
    // 按照人数
    public static final String ADVANCE_TYPE_PERSON = "2";

    // 组别
    public static final String GROUP_CLASSIFY = "1";

    // 赛项
    public static final String COMPETITION_ITEM = "2";

    // 子课题
    public static final String SUBSIDIARY_SUBJECT = "3";

    // 文件发布任务状态
    public static final String TASK_STATUS_DRAFT = "1";
    public static final String TASK_STATUS_PUBLISH = "2";

    // 已分配
    public static final String GROUP_ALLOT_STATUS_YES = "1";
}
