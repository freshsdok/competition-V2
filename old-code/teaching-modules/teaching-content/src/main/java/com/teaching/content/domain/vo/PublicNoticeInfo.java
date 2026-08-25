package com.teaching.content.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Date;

/**
 * 面向匿名访问者的通知公告详情。
 *
 * <p>该对象只包含前台展示所需字段，避免将后台审计、权限和流程字段暴露给公开接口。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicNoticeInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long noticeId;

    private String noticeTitle;

    private String noticeContent;

    private String noticeAbstract;

    private String noticeType;

    private String noticeImage;

    private String noticeAuthor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

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
}
