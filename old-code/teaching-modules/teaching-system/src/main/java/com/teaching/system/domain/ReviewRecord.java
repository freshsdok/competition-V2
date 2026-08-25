package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 专家文件阅读状态记录对象 review_record
 *
 * @author teaching
 * @date 2026-04-27
 */
public class ReviewRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 专家id
     */
    @Excel(name = "专家id")
    private Long expertId;

    /**
     * 文件id：review_processed_relation表主键
     */
    @Excel(name = "文件id：review_processed_relation表主键")
    private Long fileId;

    /**
     * 审阅状态（0未审阅，1已审阅）
     */
    @Excel(name = "审阅状态", readConverterExp = "0=未审阅，1已审阅")
    private String reviewStatus;

    /**
     * 审阅完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "审阅完成时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date reviewTime;

    /**
     * 最后阅读位置标记
     */
    @Excel(name = "最后阅读位置标记")
    private String lastPage;

    /**
     * 删除标识
     */
    private String delFlag;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setExpertId(Long expertId) {
        this.expertId = expertId;
    }

    public Long getExpertId() {
        return expertId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewTime(Date reviewTime) {
        this.reviewTime = reviewTime;
    }

    public Date getReviewTime() {
        return reviewTime;
    }

    public void setLastPage(String lastPage) {
        this.lastPage = lastPage;
    }

    public String getLastPage() {
        return lastPage;
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
                .append("expertId", getExpertId())
                .append("fileId", getFileId())
                .append("reviewStatus", getReviewStatus())
                .append("reviewTime", getReviewTime())
                .append("lastPage", getLastPage())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
