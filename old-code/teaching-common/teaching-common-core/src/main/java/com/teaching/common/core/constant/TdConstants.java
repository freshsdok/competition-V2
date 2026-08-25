package com.teaching.common.core.constant;

/**
 * 天大常量信息
 *
 * @author teaching
 */
public class TdConstants {
    /**
     * 审核状态4已通过
     * 整个流程审核状态（字典check_status）  2待审核，3审核中，4已通过，5已拒绝
     */
    public static final String CHECK_STATUS_TG = "4";
    /**
     * 审核状态5已拒绝
     * 整个流程审核状态（字典check_status）  2待审核，3审核中，4已通过，5已拒绝
     */
    public static final String CHECK_STATUS_JJ = "5";

    /**
     * 审核状态3审核中
     * 整个流程审核状态（字典check_status）  2待审核，3审核中，4已通过，5已拒绝
     */
    public static final String CHECK_STATUS_SHZ = "3";
    /**
     * 审核状态2待审核
     * 整个流程审核状态（字典check_status）  2待审核，3审核中，4已通过，5已拒绝
     */
    public static final String CHECK_STATUS_DSH = "2";

    /**
     * 流程类型 页面
     * audit_flow_type page页面，race赛事，course课程,team团队,user用户
     */
    public static final String AUDIT_FLOW_TYPE_PAGE = "page";
    /**
     * 流程类型 赛事
     * audit_flow_type page页面，race赛事，course课程,team团队,user用户
     */
    public static final String AUDIT_FLOW_TYPE_RACE = "race";
    /**
     * 流程类型 课程
     * audit_flow_type page页面，race赛事，course课程,team团队,user用户
     */
    public static final String AUDIT_FLOW_TYPE_COURSE = "course";
    /**
     * 流程类型 团队
     * audit_flow_type page页面，race赛事，course课程,team团队,user用户
     */
    public static final String AUDIT_FLOW_TYPE_TEAM = "team";
    /**
     * 流程类型 用户 身份认证
     * audit_flow_type page页面，race赛事，course课程,team团队,user用户
     */
    public static final String AUDIT_FLOW_TYPE_USER = "user";
    /**
     * 流程类型 学生 学生认证
     */
    public static final String AUDIT_FLOW_TYPE_STUDENT = "student";
    /**
     * 流程类型 教师 教师认证
     */
    public static final String AUDIT_FLOW_TYPE_TEACHER = "teacher";
    /**
     * 流程类型 学校 学校认证
     */
    public static final String AUDIT_FLOW_TYPE_SCHOOL = "school";
    /**
     * 流程类型 企业 企业认证
     */
    public static final String AUDIT_FLOW_TYPE_ENTERPRISE = "enterprise";
    /**
     * 流程类型 资讯
     */
    public static final String AUDIT_FLOW_TYPE_INFO = "info";
    /**
     * 报名
     */
    public static final String AUDIT_FLOW_TYPE_APPLY = "apply";
    /**
     * 通知公告
     */
    public static final String AUDIT_FLOW_TYPE_NOTICE = "notice";
    /**
     * 章节视频
     */
    public static final String AUDIT_FLOW_TYPE_CHAPTERVIDEO = "chapterVideo";
    /**
     * 实名认证
     */
    public static final String AUDIT_FLOW_TYPE_REALNAME = "realName";
    /**
     * 赛道 审核
     */
    public static final String AUDIT_FLOW_TYPE_RACETRACK = "raceTrack";

    /**
     * 页面表名
     */
    public static final String TABLE_NAME_PAGE = "page_manager_info";
    /**
     * 赛事表名
     */
    public static final String TABLE_NAME_RACE = "competition_series_info";
    /**
     * 课程表名
     */
    public static final String TABLE_NAME_COURSE = "course_info";
    /**
     * 身份认证 学生认证、教师认证、学校认证、企业认证
     */
    public static final String TABLE_NAME_IDENTITY = "identity_info";
    /**
     * 团队
     */
    public static final String TABLE_NAME_TEAM = "team_manager_info";
    /**
     * 资讯表名
     */
    public static final String TABLE_NAME_INFO = "news_info";
    /**
     * 报名
     */
    public static final String TABLE_NAME_APPLY = "competition_apply_info";
    /**
     * 通知公告
     */
    public static final String TABLE_NAME_NOTICE = "notice_info";
    /**
     * 章节视频
     */
    public static final String TABLE_NAME_CHAPTERVIDEO = "course_chapter_video";
    /**
     * 实名认证
     */
    public static final String TABLE_NAME_REALNAME = "auth_info";
    /**
     * 赛道 审核
     */
    public static final String TABLE_NAME_RACETRACK = "competition_track_config";

    /**
     * 发布状态0  0草稿，1已发布，2已下架
     */
    public static final String PUBLISH_STATUS_CG = "0";
    /**
     * 发布状态1  0草稿，1已发布，2已下架
     */
    public static final String PUBLISH_STATUS_YFB = "1";
    /**
     * 发布状态2  0草稿，1已发布，2已下架
     */
    public static final String PUBLISH_STATUS_YXJ = "2";

    /**
     * 用户来源 admin后台创建
     */
    public static final String USER_SOURCES_ADMIN = "admin";
    /**
     * 用户来源 pc用户注册
     */
    public static final String USER_SOURCES_PC = "pc";
    /**
     * 用户来源 import系统导入
     */
    public static final String USER_SOURCES_IMPORT = "import";
}
