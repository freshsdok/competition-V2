package com.teaching.system.api.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统机构信息对象 sys_org
 * 
 * @author teaching
 * @date 2025-10-23
 */
public class SysOrg extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 机构id */
    private Long orgId;

    /** 上级机构 */
    private Long parentId;

    /** 机构编码 */
    private String orgCode;

    /** 机构名称 */
    @NotBlank(message = "机构名称不能为空")
    @Size(min = 0, max = 30, message = "机构名称长度不能超过30个字符")
    private String orgName;

    /** 机构类型 */
    private String orgType;

    /** 祖级列表 */
    private String ancestors;

    /** 负责人 */
    private String responsiblePer;

    /** 联系电话 */
    @Size(min = 0, max = 11, message = "联系电话长度不能超过11个字符")
    private String phone;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    private String email;

    /** 地址 */
    private String address;

    /** 排序权重 */
    @NotNull(message = "显示顺序不能为空")
    private String orderNum;

    /** 状态 */
    private String status;

    /** 机构来源(管理端创建用户申请创建) */
    private String orgSource;

    /** 描述 */
    private String description;

    /** 版本 */
    private Long version;

    /** 删除标识 */
    private String delFlag;

    /** 子机构 */
    private List<SysOrg> children = new ArrayList<SysOrg>();

    public void setOrgId(Long orgId) 
    {
        this.orgId = orgId;
    }

    public Long getOrgId() 
    {
        return orgId;
    }

    public void setParentId(Long parentId) 
    {
        this.parentId = parentId;
    }

    public Long getParentId() 
    {
        return parentId;
    }

    public void setOrgCode(String orgCode) 
    {
        this.orgCode = orgCode;
    }

    public String getOrgCode() 
    {
        return orgCode;
    }

    public void setOrgName(String orgName) 
    {
        this.orgName = orgName;
    }

    public String getOrgName() 
    {
        return orgName;
    }

    public void setOrgType(String orgType) 
    {
        this.orgType = orgType;
    }

    public String getOrgType() 
    {
        return orgType;
    }

    public void setAncestors(String ancestors) 
    {
        this.ancestors = ancestors;
    }

    public String getAncestors() 
    {
        return ancestors;
    }

    public void setResponsiblePer(String responsiblePer) 
    {
        this.responsiblePer = responsiblePer;
    }

    public String getResponsiblePer() 
    {
        return responsiblePer;
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

    public void setAddress(String address) 
    {
        this.address = address;
    }

    public String getAddress() 
    {
        return address;
    }

    public void setOrderNum(String orderNum) 
    {
        this.orderNum = orderNum;
    }

    public String getOrderNum() 
    {
        return orderNum;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setOrgSource(String orgSource) 
    {
        this.orgSource = orgSource;
    }

    public String getOrgSource() 
    {
        return orgSource;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
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

    public List<SysOrg> getChildren() {
        return children;
    }

    public void setChildren(List<SysOrg> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("orgId", getOrgId())
            .append("parentId", getParentId())
            .append("orgCode", getOrgCode())
            .append("orgName", getOrgName())
            .append("orgType", getOrgType())
            .append("ancestors", getAncestors())
            .append("responsiblePer", getResponsiblePer())
            .append("phone", getPhone())
            .append("email", getEmail())
            .append("address", getAddress())
            .append("orderNum", getOrderNum())
            .append("status", getStatus())
            .append("orgSource", getOrgSource())
            .append("description", getDescription())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
