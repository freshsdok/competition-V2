package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 站内信发送表 notification_sender
 *
 * @author teaching
 */
public class NotificationSender extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 发送人ID（0 表示系统发送）
     */
    @JsonAlias("sender_user_id")
    private Long senderUserId;

    /**
     * 内容
     */
    private String content;

    /**
     * 消息类型
     */
    @JsonAlias("message_type")
    private String messageType;

    /**
     * 关联业务 ID
     */
    @JsonAlias("related_id")
    private Long relatedId;

    /**
     * 关联业务类型
     */
    @JsonAlias("related_type")
    private String relatedType;

    /**
     * 标题
     */
    private String title;

    /**
     * 是否广播消息（0-否，1-是）
     */
    @JsonAlias("is_broadcast")
    private String isBroadcast;

    /**
     * 接收人用户ID列表（逗号分隔，如：1,2,3）
     */
    @JsonAlias("receiver_user_ids")
    private String receiverUserIds;

    /**
     * 发送时间
     */
    @JsonAlias("send_time")
    private Date sendTime;

    /**
     * 删除标识（0-存在，1-删除）
     */
    private String delFlag;

    /**
     * 数据权限机构 id
     */
    @JsonAlias("org_id")
    private Long orgId;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("senderUserId", getSenderUserId())
                .append("content", getContent())
                .append("messageType", getMessageType())
                .append("relatedId", getRelatedId())
                .append("relatedType", getRelatedType())
                .append("title", getTitle())
                .append("isBroadcast", getIsBroadcast())
                .append("receiverUserIds", getReceiverUserIds())
                .append("sendTime", getSendTime())
                .append("delFlag", getDelFlag())
                .append("orgId", getOrgId())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

