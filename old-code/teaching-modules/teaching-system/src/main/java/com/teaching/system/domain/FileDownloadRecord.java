package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 文件下载记录对象 file_download_record
 *
 * @author teaching
 * @date 2026-01-09
 */
public class FileDownloadRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 下载用户id
     */
    @Excel(name = "下载用户id")
    private Long userId;

    /**
     * 用户名称
     */
    @Excel(name = "用户名称")
    private String userName;
    /**
     * 用户昵称
     */
    @Excel(name = "用户昵称")
    private String nickName;

    /**
     * 总任务id
     */
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 分发任务id
     */
    @Excel(name = "任务id")
    private Long fileTaskId;

    /**
     * 分发任务名称
     */
    @Excel(name = "任务名称")
    private String fileTaskName;

    /**
     * 文件名称
     */
    @Excel(name = "文件名称")
    private String fileName;

    /**
     * 下载时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "下载时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date downloadTime;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private String delFlag;


    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = (taskName != null) ? taskName.trim() : null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserName(String userName) {
        this.userName = (userName != null) ? userName.trim() : null;
    }

    public String getUserName() {
        return userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = (nickName != null) ? nickName.trim() : null;
    }

    public void setFileTaskId(Long fileTaskId) {
        this.fileTaskId = fileTaskId;
    }

    public Long getFileTaskId() {
        return fileTaskId;
    }

    public void setFileTaskName(String fileTaskName) {
        this.fileTaskName = (fileTaskName != null) ? fileTaskName.trim() : null;
    }

    public String getFileTaskName() {
        return fileTaskName;
    }

    public void setFileName(String fileName) {
        this.fileName = (fileName != null) ? fileName.trim() : null;
    }

    public String getFileName() {
        return fileName;
    }

    public void setDownloadTime(Date downloadTime) {
        this.downloadTime = downloadTime;
    }

    public Date getDownloadTime() {
        return downloadTime;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("userId", getUserId())
                .append("userName", getUserName())
                .append("fileTaskId", getFileTaskId())
                .append("fileTaskName", getFileTaskName())
                .append("fileName", getFileName())
                .append("downloadTime", getDownloadTime())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
