package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 赞助企业信息对象 sponsoring_enterprise_info
 * 
 * @author teaching
 * @date 2025-10-13
 */
public class SponsoringEnterpriseInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 企业id */
    private Long enterpriseId;

    /** 企业名称 */
    @Excel(name = "企业名称")
    private String enterpriseName;

    /** 企业简介 */
    @Excel(name = "企业简介")
    private String enterpriseDesc;

    /** 企业LOGO */
    @Excel(name = "企业LOGO")
    private String enterpriseLogo;

    /** 企业联系人 */
    @Excel(name = "企业联系人")
    private String contactPerson;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String phone;

    /** 邮箱 */
    @Excel(name = "邮箱")
    private String email;

    /** 地址 */
    @Excel(name = "地址")
    private String enterpriseAddr;

    /** 官网链接 */
    @Excel(name = "官网链接")
    private String officialWebsiteLink;

    /** 赞助金额 */
    @Excel(name = "赞助金额")
    private Long spopAmount;

    /** 类型 */
    @Excel(name = "类型")
    private String type;

    /** 合作开始时间 */
    @Excel(name = "合作开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date coptStartTime;

    /** 合作结束时间 */
    @Excel(name = "合作结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date coptEndTime;

    /** 展示状态 */
    @Excel(name = "展示状态")
    private String displayStatus;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag = "0";

    /** 数据权限用户id */
    @Excel(name = "数据权限用户id")
    private Long userId;

    /** 数据权限机构id */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    /** 赞助金额开始 */
    private Long spopAmountStart;

    /** 赞助金额结束 */
    private Long spopAmountEnd;

    public void setEnterpriseId(Long enterpriseId) 
    {
        this.enterpriseId = enterpriseId;
    }

    public Long getEnterpriseId() 
    {
        return enterpriseId;
    }

    public void setEnterpriseName(String enterpriseName) 
    {
        this.enterpriseName = enterpriseName;
    }

    public String getEnterpriseName() 
    {
        return enterpriseName;
    }

    public void setEnterpriseDesc(String enterpriseDesc) 
    {
        this.enterpriseDesc = enterpriseDesc;
    }

    public String getEnterpriseDesc() 
    {
        return enterpriseDesc;
    }

    public void setEnterpriseLogo(String enterpriseLogo) 
    {
        this.enterpriseLogo = enterpriseLogo;
    }

    public String getEnterpriseLogo() 
    {
        return enterpriseLogo;
    }

    public void setContactPerson(String contactPerson) 
    {
        this.contactPerson = contactPerson;
    }

    public String getContactPerson() 
    {
        return contactPerson;
    }

    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() 
    {
        return phone;
    }

    public void setEmail(String email) 
    {
        this.email = email;
    }

    public String getEmail() 
    {
        return email;
    }

    public void setEnterpriseAddr(String enterpriseAddr) 
    {
        this.enterpriseAddr = enterpriseAddr;
    }

    public String getEnterpriseAddr() 
    {
        return enterpriseAddr;
    }

    public void setOfficialWebsiteLink(String officialWebsiteLink) 
    {
        this.officialWebsiteLink = officialWebsiteLink;
    }

    public String getOfficialWebsiteLink() 
    {
        return officialWebsiteLink;
    }

    public void setSpopAmount(Long spopAmount) 
    {
        this.spopAmount = spopAmount;
    }

    public Long getSpopAmount() 
    {
        return spopAmount;
    }

    public void setType(String type) 
    {
        this.type = type;
    }

    public String getType() 
    {
        return type;
    }

    public Date getCoptStartTime() {
        return coptStartTime;
    }

    public void setCoptStartTime(Date coptStartTime) {
        this.coptStartTime = coptStartTime;
    }

    public Date getCoptEndTime() {
        return coptEndTime;
    }

    public void setCoptEndTime(Date coptEndTime) {
        this.coptEndTime = coptEndTime;
    }

    public void setDisplayStatus(String displayStatus)
    {
        this.displayStatus = displayStatus;
    }

    public String getDisplayStatus() 
    {
        return displayStatus;
    }

    public void setVersion(Long version) 
    {
        this.version = version;
    }

    public Long getVersion() 
    {
        return version;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setOrgId(Long orgId) 
    {
        this.orgId = orgId;
    }

    public Long getOrgId() 
    {
        return orgId;
    }

    public Long getSpopAmountStart() {
        return spopAmountStart;
    }

    public void setSpopAmountStart(Long spopAmountStart) {
        this.spopAmountStart = spopAmountStart;
    }

    public Long getSpopAmountEnd() {
        return spopAmountEnd;
    }

    public void setSpopAmountEnd(Long spopAmountEnd) {
        this.spopAmountEnd = spopAmountEnd;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("enterpriseId", getEnterpriseId())
            .append("enterpriseName", getEnterpriseName())
            .append("enterpriseDesc", getEnterpriseDesc())
            .append("enterpriseLogo", getEnterpriseLogo())
            .append("contactPerson", getContactPerson())
            .append("phone", getPhone())
            .append("email", getEmail())
            .append("enterpriseAddr", getEnterpriseAddr())
            .append("officialWebsiteLink", getOfficialWebsiteLink())
            .append("spopAmount", getSpopAmount())
            .append("type", getType())
            .append("coptStartTime", getCoptStartTime())
            .append("coptEndTime", getCoptEndTime())
            .append("displayStatus", getDisplayStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("userId", getUserId())
            .append("orgId", getOrgId())
            .toString();
    }
}
