package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 评审文件处理前后对应关系对象 processed_relation
 *
 * @author teaching
 * @date 2026-04-23
 */
public class ProcessedRelation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * file_upload_manager表id
     */
    @Excel(name = "file_upload_manager表id")
    private Long managerId;

    /**
     * file_upload_manager表file_info中fileName值
     */
    @Excel(name = "file_upload_manager表file_info中fileName值")
    private String oldFileName;

    /**
     * file_upload_manager表file_info中downloadLink值
     */
    @Excel(name = "file_upload_manager表file_info中downloadLink值")
    private String oldUrl;

    /**
     * 截取前后几页后的文件名
     */
    @Excel(name = "截取前后几页后的文件名")
    private String newFileName;

    /**
     * 截取前后几页后的url
     */
    @Excel(name = "截取前后几页后的url")
    private String newUrl;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private String delFlag;
    /**
     * 评审状态 (0未审，1已审）
     */
    private String reviewStatus;
    /**
     * 评审时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public Date getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime) {
        this.reviewTime = reviewTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setOldFileName(String oldFileName) {
        this.oldFileName = oldFileName;
    }

    public String getOldFileName() {
        return oldFileName;
    }

    public void setOldUrl(String oldUrl) {
        this.oldUrl = oldUrl;
    }

    public String getOldUrl() {
        return oldUrl;
    }

    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    public String getNewFileName() {
        return newFileName;
    }

    public void setNewUrl(String newUrl) {
        this.newUrl = newUrl;
    }

    public String getNewUrl() {
        return newUrl;
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
                .append("managerId", getManagerId())
                .append("oldFileName", getOldFileName())
                .append("oldUrl", getOldUrl())
                .append("newFileName", getNewFileName())
                .append("newUrl", getNewUrl())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
