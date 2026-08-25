package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.annotation.Excels;
import com.teaching.common.core.web.domain.BaseEntity;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

/**
 * 赛事晋级申请报名信息对象 competition_promoted_apply_info
 *
 * @author teaching
 * @date 2026-05-19
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompetitionPromotedApplyPcInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 晋级报名id
     */
    private Long applyId;

    /**
     * 赛事系列id
     */
    private Long competitionSeriesId;

    /**
     * 赛事名称
     */
    @Excel(name = "赛事名称")
    private String competitionName;
    /**
     * 赛道名称
     */
    @Excel(name = "赛道名称")
    private String competitionTrackName;
    /**
     * 二级分类名称
     */
    private String secondLevelName;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 团队code
     */
    @Excel(name = "团队编号")
    private String teamCode;

    /**
     * 团队名称
     */
    @Excel(name = "团队名称")
    private String teamName;

    /**
     * 参赛姓名
     */
    private String userName;

    /**
     * /**
     * 专业 /部门
     */
    private String profession;
    /**
     * 性别
     */
    private String sex;
    /**
     * 参赛角色
     */
    private String competitionRoleName;
    /**
     * 学号/工号
     */
    private String employeeCode;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;
    /**
     * 团队队员及指导教师顺序
     */
    private Integer teamSort;


    /**
     * 机构id
     */
    private Long orgId;


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
     * 支付状态
     */
    private String payStatus;



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
     * 省份名称
     */
    private String provinceName;

    /**
     * 国籍名称
     */
    private String nationalityName;


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
    @Excel(name = "团队报名状态", readConverterExp = "1=已报名,0=未报名")
    private String applyStatus;

    /**
     * 报名时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date registrationTime;

    /**
     * 版本
     */
    private Long version;

    /**
     * 报名金额
     */
    private String fee;

    /**
     * 晋级人员信息列表
     */
    private List<PromotedPlayerInfo> promotedPlayerInfoList;


    // 暂定6名队员
    @Excels({
            @Excel(name = "姓名1", targetAttr = "userName", type = Excel.Type.EXPORT),
            @Excel(name = "性别1", targetAttr = "sex", type = Excel.Type.EXPORT),
            @Excel(name = "手机号1", targetAttr = "phone", type = Excel.Type.EXPORT),
            @Excel(name = "邮箱1", targetAttr = "email", type = Excel.Type.EXPORT),
            @Excel(name = "身份证号1", targetAttr = "idCard", type = Excel.Type.EXPORT)
    })
    private PromotedPlayerInfo promotedPlayerInfoOne;

    @Excels({
            @Excel(name = "姓名2", targetAttr = "userName", type = Excel.Type.EXPORT),
            @Excel(name = "性别2", targetAttr = "sex", type = Excel.Type.EXPORT),
            @Excel(name = "手机号2", targetAttr = "phone", type = Excel.Type.EXPORT),
            @Excel(name = "邮箱2", targetAttr = "email", type = Excel.Type.EXPORT),
            @Excel(name = "身份证号2", targetAttr = "idCard", type = Excel.Type.EXPORT)
    })
    private PromotedPlayerInfo promotedPlayerInfoTwo;

    @Excels({
            @Excel(name = "姓名3", targetAttr = "userName", type = Excel.Type.EXPORT),
            @Excel(name = "性别3", targetAttr = "sex", type = Excel.Type.EXPORT),
            @Excel(name = "手机号3", targetAttr = "phone", type = Excel.Type.EXPORT),
            @Excel(name = "邮箱3", targetAttr = "email", type = Excel.Type.EXPORT),
            @Excel(name = "身份证号3", targetAttr = "idCard", type = Excel.Type.EXPORT)
    })
    private PromotedPlayerInfo promotedPlayerInfoThree;

    @Excels({
            @Excel(name = "姓名4", targetAttr = "userName", type = Excel.Type.EXPORT),
            @Excel(name = "性别4", targetAttr = "sex", type = Excel.Type.EXPORT),
            @Excel(name = "手机号4", targetAttr = "phone", type = Excel.Type.EXPORT),
            @Excel(name = "邮箱4", targetAttr = "email", type = Excel.Type.EXPORT),
            @Excel(name = "身份证号4", targetAttr = "idCard", type = Excel.Type.EXPORT)
    })
    private PromotedPlayerInfo promotedPlayerInfoFour;

    @Excels({
            @Excel(name = "姓名5", targetAttr = "userName", type = Excel.Type.EXPORT),
            @Excel(name = "性别5", targetAttr = "sex", type = Excel.Type.EXPORT),
            @Excel(name = "手机号5", targetAttr = "phone", type = Excel.Type.EXPORT),
            @Excel(name = "邮箱5", targetAttr = "email", type = Excel.Type.EXPORT),
            @Excel(name = "身份证号5", targetAttr = "idCard", type = Excel.Type.EXPORT)
    })
    private PromotedPlayerInfo promotedPlayerInfoFive;

    @Excels({
            @Excel(name = "姓名6", targetAttr = "userName", type = Excel.Type.EXPORT),
            @Excel(name = "性别6", targetAttr = "sex", type = Excel.Type.EXPORT),
            @Excel(name = "手机号6", targetAttr = "phone", type = Excel.Type.EXPORT),
            @Excel(name = "邮箱6", targetAttr = "email", type = Excel.Type.EXPORT),
            @Excel(name = "身份证号6", targetAttr = "idCard", type = Excel.Type.EXPORT)
    })
    private PromotedPlayerInfo promotedPlayerInfoSix;

    @Excels({
            @Excel(name = "教师姓名1", targetAttr = "userName", type = Excel.Type.EXPORT),
//            @Excel(name = "教师手机号1", targetAttr = "phone", type = Excel.Type.EXPORT),
//            @Excel(name = "教师邮箱1", targetAttr = "email", type = Excel.Type.EXPORT),
    })
    private PromotedPlayerInfo promotedTeacherOne;

    @Excels({
            @Excel(name = "教师姓名2", targetAttr = "userName", type = Excel.Type.EXPORT),
//            @Excel(name = "教师手机号2", targetAttr = "phone", type = Excel.Type.EXPORT),
//            @Excel(name = "教师邮箱2", targetAttr = "email", type = Excel.Type.EXPORT),
    })
    private PromotedPlayerInfo promotedTeacherTwo;

}
