package com.teaching.system.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 任务分配专家关联关系对象 review_task_specialist_relation
 *
 * @author teaching
 * @date 2026-04-09
 */
public class ReviewTaskSpecialistRelation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 关联关系id
     */
    private Long relaId;

    /**
     * 评审任务id
     */
    @Excel(name = "评审任务id")
    private Long reviewId;

    /**
     * 专家用户id
     */
    @Excel(name = "专家用户id")
    private Long userId;

    /**
     * 专家用户名称
     */
    private String userName;

    /**
     * 分配状态
     */
    @Excel(name = "分配状态")
    private String allotStatus;

    /**
     * 审阅状态
     */
    @Excel(name = "审阅状态")
    private String reviewStatus;

    /**
     * 审阅时间
     */
    private Date reviewTime;

    /**
     * 版本
     */
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 最后预览到的标记
     */
    private String lastPage;

    public String getLastPage() {
        return lastPage;
    }

    public void setLastPage(String lastPage) {
        this.lastPage = lastPage;
    }

    public Date getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime) {
        this.reviewTime = reviewTime;
    }

    public Long getRelaId() {
        return relaId;
    }

    public void setRelaId(Long relaId) {
        this.relaId = relaId;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAllotStatus() {
        return allotStatus;
    }

    public void setAllotStatus(String allotStatus) {
        this.allotStatus = allotStatus;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("relaId", getRelaId())
                .append("reviewId", getReviewId())
                .append("userId", getUserId())
                .append("allotStatus", getAllotStatus())
                .append("reviewStatus", getReviewStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
