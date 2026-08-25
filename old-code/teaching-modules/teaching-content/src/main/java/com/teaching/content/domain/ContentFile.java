package com.teaching.content.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 内容文件对象 content_file
 *
 * @author teaching
 * @date 2025-11-21
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentFile extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 栏目ID
     */
    @Excel(name = "栏目ID")
    @NotNull(message = "栏目ID不能为空")
    private Long columnId;

    /**
     * 文件名称
     */
    @Excel(name = "文件名称")
    @NotBlank(message = "文件名称不能为空")
    private String fileName;

    /**
     * 文件URL
     */
    @Excel(name = "文件URL")
    @NotBlank(message = "文件URL不能为空")
    private String fileUrl;

    /**
     * 文件类型
     */
    @Excel(name = "文件类型")
    private String fileType;

    /**
     * 文件大小（字节）
     */
    @Excel(name = "文件大小")
    private Long fileSize;

    /**
     * 显示顺序
     */
    @Excel(name = "显示顺序")
    private Integer orderNum;

    /**
     * 状态（0正常 1停用）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getColumnId() {
        return columnId;
    }

    public void setColumnId(Long columnId) {
        this.columnId = columnId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("fileId", getFileId())
                .append("columnId", getColumnId())
                .append("fileName", getFileName())
                .append("fileUrl", getFileUrl())
                .append("fileType", getFileType())
                .append("fileSize", getFileSize())
                .append("orderNum", getOrderNum())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .append("delFlag", getDelFlag())
                .toString();
    }
}


