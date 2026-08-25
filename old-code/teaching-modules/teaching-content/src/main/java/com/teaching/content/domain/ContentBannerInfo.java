package com.teaching.content.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * banner图管理对象 content_banner_info
 *
 * @author teaching
 * @date 2025-10-22
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentBannerInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 名称
     */
    @Excel(name = "名称")
    private String bannerName;

    /**
     * 地址
     */
    @Excel(name = "地址")
    @NotBlank(message = "地址不能为空")
    private String bannerUrl;

    /**
     * 模块(字典banner_module
     */
    @Excel(name = "模块(字典banner_module")
    private String bannerModule;

    /**
     * 标识
     */
    @Excel(name = "标识")
    private String bannerLogotype;

    /**
     * 描述
     */
    @Excel(name = "描述")
    private String bannerDesc;

    /**
     * 排序号
     */
    private int sortNum;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 数据权限用户id
     */
    private Long userId;

    /**
     * 数据权限机构id
     */
    private Long orgId;

    /**
     * pc/list查的数量
     */
    private int number;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setBannerName(String bannerName) {
        this.bannerName = bannerName;
    }

    public String getBannerName() {
        return bannerName;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerModule(String bannerModule) {
        this.bannerModule = bannerModule;
    }

    public String getBannerModule() {
        return bannerModule;
    }

    public void setBannerLogotype(String bannerLogotype) {
        this.bannerLogotype = bannerLogotype;
    }

    public String getBannerLogotype() {
        return bannerLogotype;
    }

    public void setBannerDesc(String bannerDesc) {
        this.bannerDesc = bannerDesc;
    }

    public String getBannerDesc() {
        return bannerDesc;
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

    public int getSortNum() {
        return sortNum;
    }

    public void setSortNum(int sortNum) {
        this.sortNum = sortNum;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("bannerName", getBannerName())
                .append("bannerUrl", getBannerUrl())
                .append("bannerModule", getBannerModule())
                .append("bannerLogotype", getBannerLogotype())
                .append("bannerDesc", getBannerDesc())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("delFlag", getDelFlag())
                .append("remark", getRemark())
                .append("userId", getUserId())
                .append("orgId", getOrgId())
                .toString();
    }
}
