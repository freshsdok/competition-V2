package com.teaching.system.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Date;

/**
 * 通知公告信息对象 notice_info (API模块)
 *
 * @author teaching
 * @date 2025-10-27
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 通知公告ID
     */
    private Long noticeId;

    /**
     * 通知公告标题
     */
    private String noticeTitle;

    /**
     * 通知公告内容
     */
    private String noticeContent;

    /**
     * 通知公告摘要
     */
    private String noticeAbstract;

    /**
     * 通知公告类型
     */
    private String noticeType;

    /**
     * 状态：草稿/审核中/审核通过/审核驳回/已发布/已下架（使用资讯状态news_status字典）
     */
    private String noticeStatus;

    /**
     * 通知公告图片
     */
    private String noticeImage;

    /**
     * 通知公告作者
     */
    private String noticeAuthor;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /**
     * 审核状态
     */
    private String checkStatus;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 无参构造函数
     */
    public NoticeInfo() {
    }

    /**
     * 便捷构造函数（用于跨服务调用修改审核状态）
     *
     * @param noticeId    通知公告ID
     * @param checkStatus 审核状态
     */
    public NoticeInfo(Long noticeId, String checkStatus) {
        this.noticeId = noticeId;
        this.checkStatus = checkStatus;
    }

    public Long getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(Long noticeId) {
        this.noticeId = noticeId;
    }

    public String getNoticeTitle() {
        return noticeTitle;
    }

    public void setNoticeTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public String getNoticeContent() {
        return noticeContent;
    }

    public void setNoticeContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }

    public String getNoticeAbstract() {
        return noticeAbstract;
    }

    public void setNoticeAbstract(String noticeAbstract) {
        this.noticeAbstract = noticeAbstract;
    }

    public String getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(String noticeType) {
        this.noticeType = noticeType;
    }

    public String getNoticeStatus() {
        return noticeStatus;
    }

    public void setNoticeStatus(String noticeStatus) {
        this.noticeStatus = noticeStatus;
    }

    public String getNoticeImage() {
        return noticeImage;
    }

    public void setNoticeImage(String noticeImage) {
        this.noticeImage = noticeImage;
    }

    public String getNoticeAuthor() {
        return noticeAuthor;
    }

    public void setNoticeAuthor(String noticeAuthor) {
        this.noticeAuthor = noticeAuthor;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}


