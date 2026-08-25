package com.teaching.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 文件上传任务应交用户。
 */
@Data
public class FileTaskRecipientVo {
    private Long userId;
    private String realName;
    private String userName;
    private String phoneNumber;
    private String schoolName;
    private String userGroupNames;
    private Boolean uploaded;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTime;
}
