package com.teaching.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * 文件任务通知安全展示对象，不包含收件人 ID 快照。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileTaskNotificationVo {
    private Long notificationId;
    private Long fileTaskId;
    private String targetType;
    private Integer recipientCount;
    private String title;
    private String content;
    private String status;
    private Long senderUserId;
    private String senderName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sendTime;

    private Long withdrawUserId;
    private String withdrawUserName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date withdrawTime;
}
