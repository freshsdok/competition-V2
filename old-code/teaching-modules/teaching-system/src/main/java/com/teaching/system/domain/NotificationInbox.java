package com.teaching.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 站内信收件箱 VO（发送主体 + 当前用户阅读状态）
 *
 * @author teaching
 */
public class NotificationInbox {

    /** 发送表主键 */
    private Long id;
    private Long senderUserId;
    private String content;
    private String messageType;
    private Long relatedId;
    private String relatedType;
    private String title;
    private String isBroadcast;
    private String receiverUserIds;
    private Date sendTime;
    private String delFlag;
    private Long orgId;

    /** 当前用户是否已读（0-未读，1-已读） */
    private String isRead;

    /** 当前用户阅读时间 */
    private Date readTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(Long senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public String getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsBroadcast() {
        return isBroadcast;
    }

    public void setIsBroadcast(String isBroadcast) {
        this.isBroadcast = isBroadcast;
    }

    public String getReceiverUserIds() {
        return receiverUserIds;
    }

    public void setReceiverUserIds(String receiverUserIds) {
        this.receiverUserIds = receiverUserIds;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    public Date getReadTime() {
        return readTime;
    }

    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("senderUserId", getSenderUserId())
                .append("title", getTitle())
                .append("messageType", getMessageType())
                .append("isBroadcast", getIsBroadcast())
                .append("sendTime", getSendTime())
                .append("isRead", getIsRead())
                .append("readTime", getReadTime())
                .toString();
    }
}

