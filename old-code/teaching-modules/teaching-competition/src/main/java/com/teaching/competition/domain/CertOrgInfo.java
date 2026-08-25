package com.teaching.competition.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 证书颁发机构表 cert_org_info
 * 
 * @author teaching
 */
public class CertOrgInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 机构id */
    private Long orgId;

    /** 机构名称 */
    @Excel(name = "机构名称")
    private String orgName;

    /** 机构编号 */
    @Excel(name = "机构编号")
    private String orgCode;

    /** 机构地址 */
    @Excel(name = "机构地址")
    private String addr;

    /** 证书外部链接名称 */
    @Excel(name = "证书外部链接名称")
    private String certLinkName;

    /** 证书外部链接URL */
    @Excel(name = "证书外部链接URL")
    private String certLinkUrl;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    private String delFlag;

    public Long getOrgId()
    {
        return orgId;
    }

    public void setOrgId(Long orgId)
    {
        this.orgId = orgId;
    }

    @NotBlank(message = "机构名称不能为空")
    @Size(min = 0, max = 255, message = "机构名称不能超过255个字符")
    public String getOrgName()
    {
        return orgName;
    }

    public void setOrgName(String orgName)
    {
        this.orgName = orgName;
    }

    @Size(min = 0, max = 255, message = "机构编号不能超过255个字符")
    public String getOrgCode()
    {
        return orgCode;
    }

    public void setOrgCode(String orgCode)
    {
        this.orgCode = orgCode;
    }

    @Size(min = 0, max = 255, message = "机构地址不能超过255个字符")
    public String getAddr()
    {
        return addr;
    }

    public void setAddr(String addr)
    {
        this.addr = addr;
    }

    public String getCertLinkName()
    {
        return certLinkName;
    }

    public void setCertLinkName(String certLinkName)
    {
        this.certLinkName = certLinkName;
    }

    public String getCertLinkUrl()
    {
        return certLinkUrl;
    }

    public void setCertLinkUrl(String certLinkUrl)
    {
        this.certLinkUrl = certLinkUrl;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("orgId", getOrgId())
            .append("orgName", getOrgName())
            .append("orgCode", getOrgCode())
            .append("addr", getAddr())
            .append("certLinkName", getCertLinkName())
            .append("certLinkUrl", getCertLinkUrl())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("version", getVersion())
            .toString();
    }
}