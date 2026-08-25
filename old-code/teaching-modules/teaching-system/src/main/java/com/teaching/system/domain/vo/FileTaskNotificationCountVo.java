package com.teaching.system.domain.vo;

import lombok.Data;

/**
 * 用户在文件任务上的有效通知数。
 */
@Data
public class FileTaskNotificationCountVo {
    private Long fileTaskId;
    private Integer notificationCount;
}
