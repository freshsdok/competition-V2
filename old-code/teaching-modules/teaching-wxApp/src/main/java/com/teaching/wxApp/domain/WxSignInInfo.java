package com.teaching.wxApp.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;

import java.util.Date;

/**
 * 签到信息对象 wx_sign_in_info
 *
 * @author teaching
 * @date 2026-04-08
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WxSignInInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 签到id
     */
    private Long signId;

    /**
     * 学生ID
     */
    private Long userId;


    /*
     * 用户手机号
     */
    @Excel(name = "签到手机号")
    private String phoneNumber;

    /**
     * 签到名称
     */
    @Excel(name = "签到姓名")
    private String realName;

    /**
     * 二维码配置名称
     */
    @Excel(name = "签到二维码名称")
    private String codeConfigName;

    /**
     * 签到时间
     */
    @Excel(name = "签到时间", dateFormat = "yyyy-MM-dd HH:mm:ss",width = 22)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date signTime;

    /**
     * 学校名称
     */
    @Excel(name = "签到学校")
    private String schoolName;

    /**
     * 二维码扫描结果类型
     * 1有报名信息，2没报名信息，3接口错误
     */
    @Excel(name = "签到状态", readConverterExp = "1=签到成功,2=无需签到,3=接口异常")
    private String resultType;

    /**
     * 签到方式 正常扫码签到，信息查询签到
     */
    @Excel(name = "签到类型")
    private String checkInType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "签到入库时间", dateFormat = "yyyy-MM-dd HH:mm:ss", width = 22)
    private Date createTime;

    /**
     * 签到者姓名
     */
    @Excel(name = "签到人")
    private String signName;

    /**
     * 扫码ip
     */
    @Excel(name = "签到IP地址")
    private String ip;

    /**
     * 二维码ID
     */
    private Long recordId;

    /**
     * 版本
     */
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 用户昵称 签到姓名
     */
    private String nickName;

    /**
     * 签到开始时间
     */
    private String signTimeStart;

    /**
     * 签到结束时间
     */
    private String signTimeEnd;

    /**
     * 创建起始时间
     */
    private String createTimeStart;

    /**
     * 创建结束时间
     */
    private String createTimeEnd;

    /**
     * 签到者昵称
     */
    private String signNick;

    /**
     * 导出类型
     */
    private String exportType;


    @Override
    public Date getCreateTime() {
        return this.createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
