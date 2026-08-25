package com.teaching.system.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 站内信接收表 notification_receiver
 *
 * @author teaching
 */
public class NotificationReceiver extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 消息 ID（关联发送表主键）
     */
    private Long notificationId;

    /**
     * 接收用户 ID
     */
    private Long toUserId;

    /**
     * 是否已读（0-未读，1-已读）
     */
    private String isRead;

    /**
     * 阅读时间
     */
    private Date readTime;

    /**
     * 数据权限机构 id
     */
    private Long orgId;

    /**
     * 删除标识（0-存在，1-删除）
     */
    private String delFlag;

    /**
     * 消息接收时间
     */
    private Date receiveTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
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

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("notificationId", getNotificationId())
                .append("toUserId", getToUserId())
                .append("isRead", getIsRead())
                .append("readTime", getReadTime())
                .append("orgId", getOrgId())
                .append("delFlag", getDelFlag())
                .append("receiveTime", getReceiveTime())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}

