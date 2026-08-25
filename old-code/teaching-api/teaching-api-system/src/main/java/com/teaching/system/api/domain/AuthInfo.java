package com.teaching.system.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.TdConstants;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 实名认证对象 auth_info
 *
 * @author teaching
 * @date 2025-10-13
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 实名认证iD
     */
    private Long authId;

    /**
     * 用户id
     */
    @Excel(name = "用户id")
    private Long userId;

    /**
     * 认证时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "认证时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date authTime;

    /**
     * 真实姓名
     */
    @Excel(name = "真实姓名")
    private String realName;

    /**
     * 证件证号
     */
    @Excel(name = "证件证号")
    private String idCard;

    /**
     * 证件类型
     */
    @Excel(name = "证件类型")
    private String idCardType;
    /**
     * 证件类型名称
     */
    private String idCardTypeName;

    /**
     * 认证状态
     */
    @Excel(name = "认证状态")
    private String authStatus;

    /**
     * 认证类别
     */
    @Excel(name = "认证类别")
    private String authType;

    /**
     * 身份证护照面照片
     */
    @Excel(name = "身份证护照面照片")
    private String idCardFront;

    /**
     * 身份证护照反面照片
     */
    @Excel(name = "身份证护照反面照片")
    private String idCardContrary;

    /**
     * 版本
     */
    @Excel(name = "版本")
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 审核状态 审核类型是身份证时 无审核状态
     */
    private String checkStatus;

    /**
     * 审核意见 核类型是身份证时 无审核意见
     */
    private String checkOpinion;

    /*
     * 国籍
     */
    private String countryName;

    public AuthInfo() {
    }

    public AuthInfo(Long authId, String checkStatus, String checkOpinion) {
        this.authId = authId;
        this.checkStatus = checkStatus;
        this.checkOpinion = checkOpinion;
        this.authStatus = TdConstants.CHECK_STATUS_TG.equals(checkStatus) ? Constants.AUTH_STATUS_PASS : Constants.AUTH_STATUS_FAIL;
    }

    public Long getAuthId() {
        return authId;
    }

    public void setAuthId(Long authId) {
        this.authId = authId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setAuthTime(Date authTime) {
        this.authTime = authTime;
    }

    public Date getAuthTime() {
        return authTime;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRealName() {
        return realName;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCardType(String idCardType) {
        this.idCardType = idCardType;
    }

    public String getIdCardType() {
        return idCardType;
    }

    public void setAuthStatus(String authStatus) {
        this.authStatus = authStatus;
    }

    public String getAuthStatus() {
        return authStatus;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getAuthType() {
        return authType;
    }

    public void setIdCardFront(String idCardFront) {
        this.idCardFront = idCardFront;
    }

    public String getIdCardFront() {
        return idCardFront;
    }

    public void setIdCardContrary(String idCardContrary) {
        this.idCardContrary = idCardContrary;
    }

    public String getIdCardContrary() {
        return idCardContrary;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getVersion() {
        return version;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getCheckOpinion() {
        return checkOpinion;
    }

    public void setCheckOpinion(String checkOpinion) {
        this.checkOpinion = checkOpinion;
    }

    public String getIdCardTypeName() {
        return idCardTypeName;
    }

    public void setIdCardTypeName(String idCardTypeName) {
        this.idCardTypeName = idCardTypeName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("authId", getAuthId())
                .append("userId", getUserId())
                .append("authTime", getAuthTime())
                .append("realName", getRealName())
                .append("idCard", getIdCard())
                .append("idCardType", getIdCardType())
                .append("authStatus", getAuthStatus())
                .append("authType", getAuthType())
                .append("idCardFront", getIdCardFront())
                .append("idCardContrary", getIdCardContrary())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
