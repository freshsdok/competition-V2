package com.teaching.system.api.domain;

/**
 * 站内信发送 DTO（供其他微服务通过 Feign 调用 system 模块使用）
 */
public class NotificationSendDTO {

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 消息类型（字典 notification_message_type：1-流程待办 2-系统通知 3-活动通知 4-课程活动 5-通用消息）
     */
    private String messageType;

    /**
     * 关联业务 ID（可选）
     */
    private Long relatedId;

    /**
     * 关联业务类型（如：FLOWABLE_TASK）
     */
    private String relatedType;

    /**
     * 接收人用户ID列表（逗号分隔）
     */
    private String receiverUserIds;

    /**
     * 数据权限机构 id
     */
    private Long orgId;

    /**
     * 发送人ID（0 表示系统）
     */
    private Long senderUserId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getReceiverUserIds() {
        return receiverUserIds;
    }

    public void setReceiverUserIds(String receiverUserIds) {
        this.receiverUserIds = receiverUserIds;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(Long senderUserId) {
        this.senderUserId = senderUserId;
    }
}

