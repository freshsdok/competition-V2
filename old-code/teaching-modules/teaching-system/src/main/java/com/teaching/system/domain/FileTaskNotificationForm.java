package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 文件上传任务通知发送表单。
 */
@Data
public class FileTaskNotificationForm {
    private String targetType;
    private Long targetUserId;
    private String title;

    /**
     * UTF-8 富文本的 Base64 值，避免网关先行处理原始 HTML。
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contentBase64;
}
