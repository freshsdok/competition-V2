package com.teaching.content.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 数据源信息对象 data_source_info
 *
 * @author teaching
 * @date 2025-10-13
 */
public class DataSourceInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 数据源id
     */
    private Long dataId;

    /**
     * 数据源名称
     */
    @NotBlank(message = "数据源名称不能为空")
    @Excel(name = "数据源名称")
    private String dataName;

    /**
     * 类型（API / 静态数据）
     */
    @Excel(name = "类型", readConverterExp = "API/静态数据")
    private String type;

    /**
     * 接口地址
     */
    @NotBlank(message = "接口地址不能为空")
    @Excel(name = "接口地址")
    private String interfaceUrl;

    /**
     * 请求方法（GET/POST 等）
     */
    @Excel(name = "请求方法", readConverterExp = "GET/POST,等")
    private String requestMode;

    /**
     * 请求参数
     */
    @Excel(name = "请求参数")
    private String parameter;

    /**
     * 请求头
     */
    @Excel(name = "请求头")
    private String header;

    /**
     * 版本
     */
    @Excel(name = "版本")
    private Long version;
    /**
     * 是否是系统内置（默认N：否，Y：是）
     */
    private String isBuiltIn;

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

    public String getIsBuiltIn() {
        return isBuiltIn;
    }

    public void setIsBuiltIn(String isBuiltIn) {
        this.isBuiltIn = isBuiltIn;
    }

    public void setDataId(Long dataId) {
        this.dataId = dataId;
    }

    public Long getDataId() {
        return dataId;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public String getDataName() {
        return dataName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setInterfaceUrl(String interfaceUrl) {
        this.interfaceUrl = interfaceUrl;
    }

    public String getInterfaceUrl() {
        return interfaceUrl;
    }

    public void setRequestMode(String requestMode) {
        this.requestMode = requestMode;
    }

    public String getRequestMode() {
        return requestMode;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
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
                .append("dataId", getDataId())
                .append("dataName", getDataName())
                .append("type", getType())
                .append("interfaceUrl", getInterfaceUrl())
                .append("requestMode", getRequestMode())
                .append("paramter", getParameter())
                .append("header", getHeader())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .append("remark", getRemark())
                .append("userId", getUserId())
                .append("orgId", getOrgId())
                .toString();
    }
}
