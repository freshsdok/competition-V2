package com.teaching.content.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 组件库信息对象 component_library_info
 *
 * @author teaching
 * @date 2025-10-13
 */
public class ComponentLibraryInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 组件id
     */
    private String componentId;

    /**
     * 展示平台 字典display_platform
     */
    @NotBlank(message = "展示平台不能为空")
    private String displayPlatform;

    /**
     * 组件名称
     */
    @Excel(name = "组件名称")
    @NotBlank(message = "组件名称不能为空")
    private String componentName;

    /**
     * 分类
     */
    @Excel(name = "分类")
    private String componentClassify;

    /**
     * 描述
     */
    @Excel(name = "描述")
    private String componentDesc;

    /**
     * 组件标识
     */
    @NotBlank(message = "组件标识不能为空")
    @Excel(name = "组件标识")
    private String componentLogotype;

    /**
     * 组件json
     */
//    @NotBlank(message = "组件json不能为空")
    @Excel(name = "组件json")
    private String componentJson;

    /**
     * 预览图片
     */
    @Excel(name = "预览图片")
    private String imageUrl;

    /**
     * 使用说明
     */
    @Excel(name = "使用说明")
    private String useDirection;

    /**
     * 状态（1启用 /0 禁用）
     */
    @Excel(name = "状态", readConverterExp = "1=启用,0=禁用")
    private String status;

    /**
     * 版本
     */
    @Excel(name = "版本")
    private Long version;

    /**
     * 删除标识(0存在，1删除)
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

    public String getDisplayPlatform() {
        return displayPlatform;
    }

    public void setDisplayPlatform(String displayPlatform) {
        this.displayPlatform = displayPlatform;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentClassify(String componentClassify) {
        this.componentClassify = componentClassify;
    }

    public String getComponentClassify() {
        return componentClassify;
    }

    public void setComponentDesc(String componentDesc) {
        this.componentDesc = componentDesc;
    }

    public String getComponentDesc() {
        return componentDesc;
    }

    public void setComponentLogotype(String componentLogotype) {
        this.componentLogotype = componentLogotype;
    }

    public String getComponentLogotype() {
        return componentLogotype;
    }

    public void setComponentJson(String componentJson) {
        this.componentJson = componentJson;
    }

    public String getComponentJson() {
        return componentJson;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setUseDirection(String useDirection) {
        this.useDirection = useDirection;
    }

    public String getUseDirection() {
        return useDirection;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
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
                .append("componentId", getComponentId())
                .append("componentName", getComponentName())
                .append("componentClassfy", getComponentClassify())
                .append("componentDesc", getComponentDesc())
                .append("componentLogotype", getComponentLogotype())
                .append("componentJson", getComponentJson())
                .append("imageUrl", getImageUrl())
                .append("useDirection", getUseDirection())
                .append("status", getStatus())
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
