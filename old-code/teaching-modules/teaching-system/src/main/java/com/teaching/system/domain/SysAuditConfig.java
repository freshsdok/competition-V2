package com.teaching.system.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 审核流程环节配置对象 sys_audit_config
 *
 * @author teaching
 * @date 2025-10-15
 */
public class SysAuditConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 环节配置id
     */
    private Long configId;

    /**
     * 审核流程配置id
     */
    @Excel(name = "审核流程配置id")
    private Long auditId;

    /**
     * 级别名称
     */
    @Excel(name = "级别名称")
    private String levelName;

    /**
     * 级别排序
     */
    @Excel(name = "级别排序")
    private Long levelSort;

    /**
     * 是否启用0未启用，1已启用
     */
    @Excel(name = "是否开启")
    private String isEnable;

    /**
     * 审核人类型
     */
    @Excel(name = "审核人类型")
    private String checkPersonType;

    /**
     * 审核人机构
     */
    @Excel(name = "审核人机构")
    private String checkPersonOrg;

    /**
     * 审核角色
     */
    @Excel(name = "审核角色")
    private String checkPersonRole;

    /**
     * 审核人
     */
    @Excel(name = "审核人")
    private String checkPerson;

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
     * 数据权限机构id
     */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelSort(Long levelSort) {
        this.levelSort = levelSort;
    }

    public Long getLevelSort() {
        return levelSort;
    }

    public void setIsEnable(String isEnable) {
        this.isEnable = isEnable;
    }

    public String getIsEnable() {
        return isEnable;
    }

    public void setCheckPersonType(String checkPersonType) {
        this.checkPersonType = checkPersonType;
    }

    public String getCheckPersonType() {
        return checkPersonType;
    }

    public void setCheckPersonOrg(String checkPersonOrg) {
        this.checkPersonOrg = checkPersonOrg;
    }

    public String getCheckPersonOrg() {
        return checkPersonOrg;
    }

    public void setCheckPersonRole(String checkPersonRole) {
        this.checkPersonRole = checkPersonRole;
    }

    public String getCheckPersonRole() {
        return checkPersonRole;
    }

    public void setCheckPerson(String checkPerson) {
        this.checkPerson = checkPerson;
    }

    public String getCheckPerson() {
        return checkPerson;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("configId", getConfigId())
                .append("auditId", getAuditId())
                .append("levelName", getLevelName())
                .append("levelSort", getLevelSort())
                .append("isEnable", getIsEnable())
                .append("checkPersonType", getCheckPersonType())
                .append("checkPersonOrg", getCheckPersonOrg())
                .append("checkPersonRole", getCheckPersonRole())
                .append("checkPerson", getCheckPerson())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("delFlag", getDelFlag())
                .append("userId", getUserId())
                .append("orgId", getOrgId())
                .toString();
    }
}
