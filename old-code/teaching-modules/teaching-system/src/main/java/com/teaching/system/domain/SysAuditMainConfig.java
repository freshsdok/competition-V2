package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * 系统审核配置对象 sys_audit_main_cofig
 *
 * @author teaching
 * @date 2025-10-15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysAuditMainConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 审核流程id
     */
    private Long auditId;

    /**
     * 审核流程标题
     */
    @Excel(name = "审核流程标题")
    @NotBlank(message = "审核流程标题不能为空")
    private String auditTitle;

    /**
     * 审核流程类型
     */
    @Excel(name = "审核流程类型")
    @NotBlank(message = "审核流程类型不能为空")
    private String auditType;

    /**
     * 流程描述
     */
    @Excel(name = "流程描述")
    private String flowDesc;

    /**
     * 是否启用0未启用，1已启用
     */
    @Excel(name = "是否启用")
    private String isEnable;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 数据权限用户id
     */
    @Excel(name = "数据权限用户id")
    private Long userId;
    /**
     * 版本号
     */
    private Long version;

    /**
     * 数据权限机构id
     */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    /**
     * 审核流程环节配置信息
     */
    private List<SysAuditConfig> sysAuditConfigList;

    /**
     * 审核流程使用次数
     */
    private Long usedNum;
    /**
     * 审核流程正在审批次数
     */
    private Long checkingNum;

    public SysAuditMainConfig() {
    }

    public SysAuditMainConfig(String delFlag) {
        this.delFlag = delFlag;
    }

    public Long getUsedNum() {
        return usedNum;
    }

    public void setUsedNum(Long usedNum) {
        this.usedNum = usedNum;
    }

    public Long getCheckingNum() {
        return checkingNum;
    }

    public void setCheckingNum(Long checkingNum) {
        this.checkingNum = checkingNum;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditTitle(String auditTitle) {
        this.auditTitle = auditTitle;
    }

    public String getAuditTitle() {
        return auditTitle;
    }

    public void setAuditType(String auditType) {
        this.auditType = auditType;
    }

    public String getAuditType() {
        return auditType;
    }

    public void setFlowDesc(String flowDesc) {
        this.flowDesc = flowDesc;
    }

    public String getFlowDesc() {
        return flowDesc;
    }

    public void setIsEnable(String isEnable) {
        this.isEnable = isEnable;
    }

    public String getIsEnable() {
        return isEnable;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public List<SysAuditConfig> getSysAuditConfigList() {
        return sysAuditConfigList;
    }

    public void setSysAuditConfigList(List<SysAuditConfig> sysAuditConfigList) {
        this.sysAuditConfigList = sysAuditConfigList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("auditId", getAuditId())
                .append("auditTitle", getAuditTitle())
                .append("auditType", getAuditType())
                .append("flowDesc", getFlowDesc())
                .append("isEnable", getIsEnable())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("delFlag", getDelFlag())
                .append("userId", getUserId())
                .append("orgId", getOrgId())
                .append("sysAuditConfigList", getSysAuditConfigList())
                .toString();
    }
}
