package com.teaching.content.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 通知公告信息对象 notice_info
 *
 * @author teaching
 * @date 2025-10-27
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 通知公告ID
     */
    private Long noticeId;

    /**
     * 通知公告标题
     */
    @Excel(name = "通知公告标题")
    @NotBlank(message = "通知公告标题不能为空")
    private String noticeTitle;

    /**
     * 通知公告内容
     */
    @Excel(name = "通知公告内容")
    private String noticeContent;

    /**
     * 通知公告摘要
     */
    @Excel(name = "通知公告摘要")
    @NotBlank(message = "通知公告摘要不能为空")
    private String noticeAbstract;

    /**
     * 通知公告类型
     */
    @Excel(name = "通知公告类型")
    private String noticeType;

    private String type;

    /**
     * 状态：草稿/审核中/审核通过/审核驳回/已发布/已下架（使用资讯状态news_status字典）
     */
    @Excel(name = "状态", readConverterExp = "草稿=草稿,审核中=审核中,审核通过=审核通过,审核驳回=审核驳回,已发布=已发布,已下架=已下架")
    private String noticeStatus;

    /**
     * 通知公告图片
     */
    @Excel(name = "通知公告图片")
    private String noticeImage;

    /**
     * 通知公告作者
     */
    @Excel(name = "通知公告作者")
    private String noticeAuthor;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发布时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "发布时间不能为空")
    private Date publishTime;

    /**
     * 审核状态
     */
    @Excel(name = "审核状态")
    private String checkStatus;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 删除标志（0代表存在 2代表删除）
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

    public void setNoticeId(Long noticeId) {
        this.noticeId = noticeId;
    }

    public Long getNoticeId() {
        return noticeId;
    }

    public void setNoticeTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public String getNoticeTitle() {
        return noticeTitle;
    }

    public void setNoticeContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }

    public String getNoticeContent() {
        return noticeContent;
    }

    public void setNoticeAbstract(String noticeAbstract) {
        this.noticeAbstract = noticeAbstract;
    }

    public String getNoticeAbstract() {
        return noticeAbstract;
    }

    public void setNoticeType(String noticeType) {
        this.noticeType = noticeType;
    }

    public String getNoticeType() {
        return noticeType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNoticeStatus(String noticeStatus) {
        this.noticeStatus = noticeStatus;
    }

    public String getNoticeStatus() {
        return noticeStatus;
    }

    public void setNoticeImage(String noticeImage) {
        this.noticeImage = noticeImage;
    }

    public String getNoticeImage() {
        return noticeImage;
    }

    public void setNoticeAuthor(String noticeAuthor) {
        this.noticeAuthor = noticeAuthor;
    }

    public String getNoticeAuthor() {
        return noticeAuthor;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getVersion() {
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
                .append("noticeId", getNoticeId())
                .append("noticeTitle", getNoticeTitle())
                .append("noticeContent", getNoticeContent())
                .append("noticeAbstract", getNoticeAbstract())
                .append("noticeType", getNoticeType())
                .append("noticeStatus", getNoticeStatus())
                .append("noticeImage", getNoticeImage())
                .append("noticeAuthor", getNoticeAuthor())
                .append("publishTime", getPublishTime())
                .append("checkStatus", getCheckStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .append("userId", getUserId())
                .append("orgId", getOrgId())
                .append("remark", getRemark())
                .toString();
    }
}


