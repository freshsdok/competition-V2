package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * 晋级人员信息
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PromotedPlayerInfo {
    /**
     * 晋级记录id
     */
    private Long applyId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 晋级角色名称
     */
    private String competitionRoleName;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 队伍排序
     */
    private Integer teamSort;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 学号/工号
     */
    private String employeeCode;

    /**
     * 机构id
     */
    private Long orgId;

    /**
     * 专业 /部门
     */
    private String profession;

    /**
     * 年级
     */
    private String classInfo;

    /**
     * 指导老师
     */
    private String guideTeacher;

    /**
     * 指导老师手机号
     */
    private String guideTeacherPhone;

    /**
     * 指导老师邮箱
     */
    private String guideTeacherEmail;

    /**
     * 报名时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date registrationTime;

    /**
     * 支付状态
     */
    private String payStatus;

    /**
     * 赛道名称
     */
    private String competitionTrackName;

    /**
     * 赛道编码
     */
    private String competitionTrackId;

    /**
     * 赛道二级分类
     */
    private String competitionTrackType;

    /**
     * 赛道二级分类编码(组别、赛道、子课题)
     */
    private String secondLevelCode;

    /**
     * 二级分类名称
     */
    private String secondLevelName;

    /**
     * 带队老师id
     */
    private Long leaderTeacherId;

    /**
     * 学校编码
     */
    private String school;

    /**
     * 省份code
     */
    private String province;

    /**
     * 国籍
     */
    private String nationality;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 省份名称
     */
    private String provinceName;

    /**
     * 国籍名称
     */
    private String nationalityName;

    /**
     * 性别
     */
    private String sex;


    /**
     * 发票状态
     */
    private String invoiceStatus;

    /**
     * 证件类型
     */
    private String idCardType;


    /**
     * 团队报名状态 已报名/未报名
     */
    private String applyStatus;

    /**
     * 版本
     */
    private Long version;
    private Long competitionSeriesId;
    private String teamCode;


    private Date createTime;

    private Date updateTime;

    private String createBy;

    private String updateBy;

    private String delFlag;

}
