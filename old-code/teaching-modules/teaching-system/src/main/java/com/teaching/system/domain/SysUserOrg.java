package com.teaching.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 用户机构关联关系对象 sys_user_org
 * 
 * @author teaching
 * @date 2025-10-24
 */
public class SysUserOrg extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 关联关系id */
    private Long relaId;

    /** 用户id */
    @Excel(name = "用户id")
    private Long userId;

    /** 机构id */
    @Excel(name = "机构id")
    private Long orgId;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag;

    public void setRelaId(Long relaId) 
    {
        this.relaId = relaId;
    }

    public Long getRelaId() 
    {
        return relaId;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("relaId", getRelaId())
            .append("userId", getUserId())
            .append("orgId", getOrgId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
