package com.teaching.system.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.validator.constraints.Length;

import java.util.Date;
import java.util.List;

/**
 * 页面管理信息对象 page_manager_info
 *
 * @author teaching
 * @date 2025-10-14
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageManagerInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 页面id
     */
    private Long pageId;

    /**
     * 展示平台 字典display_platform
     */
    @NotBlank(message = "展示平台不能为空")
    private String displayPlatform;

    /**
     * 页面标题
     */
    @Excel(name = "页面标题")
    private String pageTitle;

    /**
     * URL
     */
    @Excel(name = "URL")
    private String url;

    /**
     * 描述
     */
    @Excel(name = "描述")
    @Length(max = 500, message = "描述长度不能超过500字")
    private String pageDesc;

    /**
     * 内容
     */
    @Excel(name = "内容")
//    @NotNull(message = "内容不能为空")
    private String pageContent;

    /**
     * SEO 配置（标题、描述、关键词）
     */
    @Excel(name = "SEO 配置", readConverterExp = "标=题、描述、关键词")
    private String seoConfig;

    /**
     * 发布状态（草稿 / 已发布 / 已下架）
     */
    @Excel(name = "发布状态", readConverterExp = "草稿,已发布,已下架")
    private String publishStatus;

    /**
     * 审核状态( 待审核 / 审核中 / 审核通过 / 审核驳回)
     */
    @Excel(name = "审核状态( 待审核 / 审核中 / 审核通过 / 审核驳回)")
    private String checkStatus;
    /**
     * 审核原因
     */
    private String applyReason;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发布时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date publishTime;

    /**
     * 排序权重
     */
    @Excel(name = "排序权重")
    private Long sortWeight;

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
     * 数据权限用户id
     */
    @Excel(name = "数据权限用户id")
    private Long userId;

    /**
     * 数据权限机构id
     */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    /**
     * 当前版本是否是生效版本 Y是,N不是
     */
    private String publishVersion;
    /**
     * 页面上组件和数据源的关联关系
     */
    private List<ComponentDataSourceRela> relaList;

    public PageManagerInfo() {
    }

    public PageManagerInfo(Long pageId, String checkStatus) {
        this.pageId = pageId;
        this.checkStatus = checkStatus;
    }

    public PageManagerInfo(Long pageId, String checkStatus, String applyReason) {
        this.pageId = pageId;
        this.applyReason = applyReason;
        this.checkStatus = checkStatus;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public PageManagerInfo(String pageContent) {
        this.pageContent = pageContent;
    }

    public @NotBlank(message = "展示平台不能为空") String getDisplayPlatform() {
        return displayPlatform;
    }

    public void setDisplayPlatform(@NotBlank(message = "展示平台不能为空") String displayPlatform) {
        this.displayPlatform = displayPlatform;
    }

    public String getPublishVersion() {
        return publishVersion;
    }

    public void setPublishVersion(String publishVersion) {
        this.publishVersion = publishVersion;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public Long getPageId() {
        return pageId;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public @Length(max = 500, message = "描述长度不能超过500字") String getPageDesc() {
        return pageDesc;
    }

    public void setPageDesc(@Length(max = 500, message = "描述长度不能超过500字") String pageDesc) {
        this.pageDesc = pageDesc;
    }

    public void setPageContent(String pageContent) {
        this.pageContent = pageContent;
    }

    public String getPageContent() {
        return pageContent;
    }

    public void setSeoConfig(String seoConfig) {
        this.seoConfig = seoConfig;
    }

    public String getSeoConfig() {
        return seoConfig;
    }

    public void setPublishStatus(String publishStatus) {
        this.publishStatus = publishStatus;
    }

    public String getPublishStatus() {
        return publishStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setSortWeight(Long sortWeight) {
        this.sortWeight = sortWeight;
    }

    public Long getSortWeight() {
        return sortWeight;
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

    public List<ComponentDataSourceRela> getRelaList() {
        return relaList;
    }

    public void setRelaList(List<ComponentDataSourceRela> relaList) {
        this.relaList = relaList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("pageId", getPageId())
                .append("pageTitle", getPageTitle())
                .append("url", getUrl())
                .append("pageDesc", getPageDesc())
                .append("pageContent", getPageContent())
                .append("seoConfig", getSeoConfig())
                .append("publishStatus", getPublishStatus())
                .append("checkStatus", getCheckStatus())
                .append("publishTime", getPublishTime())
                .append("sortWeight", getSortWeight())
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
