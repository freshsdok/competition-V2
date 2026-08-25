package com.teaching.system.domain;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 文件配置对象 file_task_config
 * 
 * @author teaching
 * @date 2026-01-07
 */
public class FileTaskConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 任务id */
    @Excel(name = "任务id")
    private Long taskId;

    /** 任务类型 */
    @Excel(name = "任务类型")
    private String taskType;

    /** 文件名称 */
    @Excel(name = "文件名称")
    private String fileName;

    /** 大小限制（MB） */
    @Excel(name = "大小限制", readConverterExp = "M=B")
    private String fileSize;

    /** 允许上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "允许上传时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date uploadStart;

    /** 上传截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "上传截止时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date uploadEnd;

    /** 上传模板url/下载的文件本体 */
    @Excel(name = "上传模板url/下载的文件本体")
    private String tempFile;

    /** 模板文件名称 */
    private String tempFileName;

    /** 允许的文件类型 */
    @Excel(name = "允许的文件类型")
    private String fileType;

    /** 上传须知 */
    @Excel(name = "上传须知")
    private String annoucement;

    /** 是否永久有效 */
    @Excel(name = "是否永久有效")
    private Boolean isPerminate;

    /** 删除标志（0代表存在 1代表删除） */
    private String delFlag;

    private FileUploadRecord fileUploadRecord;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setTaskType(String taskType) 
    {
        this.taskType = taskType;
    }

    public String getTaskType() 
    {
        return taskType;
    }

    public void setFileName(String fileName) 
    {
        this.fileName = fileName;
    }

    public String getFileName() 
    {
        return fileName;
    }

    public void setFileSize(String fileSize) 
    {
        this.fileSize = fileSize;
    }

    public String getFileSize() 
    {
        return fileSize;
    }

    public void setUploadStart(Date uploadStart) 
    {
        this.uploadStart = uploadStart;
    }

    public Date getUploadStart() 
    {
        return uploadStart;
    }

    public void setUploadEnd(Date uploadEnd) 
    {
        this.uploadEnd = uploadEnd;
    }

    public Date getUploadEnd() 
    {
        return uploadEnd;
    }

    public void setTempFile(String tempFile) 
    {
        this.tempFile = tempFile;
    }

    public String getTempFile() 
    {
        return tempFile;
    }

    public void setFileType(String fileType) 
    {
        this.fileType = fileType;
    }

    public String getFileType() 
    {
        return fileType;
    }

    public void setAnnoucement(String annoucement) 
    {
        this.annoucement = annoucement;
    }

    public String getAnnoucement() 
    {
        return annoucement;
    }

    public Boolean getPerminate() {
        return isPerminate;
    }

    public void setPerminate(Boolean perminate) {
        isPerminate = perminate;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    public FileUploadRecord getFileUploadRecord() {
        return fileUploadRecord;
    }

    public void setFileUploadRecord(FileUploadRecord fileUploadRecord) {
        this.fileUploadRecord = fileUploadRecord;
    }

    public String getTempFileName() {
        return tempFileName;
    }

    public void setTempFileName(String tempFileName) {
        this.tempFileName = tempFileName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("taskId", getTaskId())
            .append("taskType", getTaskType())
            .append("fileName", getFileName())
            .append("fileSize", getFileSize())
            .append("uploadStart", getUploadStart())
            .append("uploadEnd", getUploadEnd())
                .append("tempFileName", getTempFileName())
            .append("tempFile", getTempFile())
            .append("fileType", getFileType())
            .append("annoucement", getAnnoucement())
            .append("isPerminate", getPerminate())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
