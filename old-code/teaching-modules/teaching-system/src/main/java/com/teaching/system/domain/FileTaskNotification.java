package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 文件上传任务通知。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FileTaskNotification extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long fileTaskId;
    private String targetType;
    private Long targetUserId;
    private String recipientUserIds;
    private Integer recipientCount;
    private String title;
    private String content;
    private String status;
    private Long senderUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime;

    private Long withdrawUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date withdrawTime;

    /** 查询展示字段，不持久化。 */
    private String senderName;
    private String withdrawUserName;
}
