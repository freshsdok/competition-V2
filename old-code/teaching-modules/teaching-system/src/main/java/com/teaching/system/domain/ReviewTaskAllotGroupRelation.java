package com.teaching.system.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 评审任务分配组关联关系对象 review_task_allot_group_relation
 *
 * @author teaching
 * @date 2026-04-09
 */
public class ReviewTaskAllotGroupRelation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 分配组关联id
     */
    private Long relationId;

    /**
     * 分配组id
     */
    @Excel(name = "分配组id")
    private Long reviewGroupId;

    /**
     * 分配组名称
     */
    private String allotGroupName;

    /**
     * 评审任务id
     */
    @Excel(name = "评审任务id")
    private Long reviewId;

    /**
     * 版本
     */
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag;

    // 任务对象
    private ReviewTaskInfo reviewTaskInfo;

    public Long getRelationId() {
        return relationId;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    public Long getReviewGroupId() {
        return reviewGroupId;
    }

    public void setReviewGroupId(Long reviewGroupId) {
        this.reviewGroupId = reviewGroupId;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
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

    public String getAllotGroupName() {
        return allotGroupName;
    }

    public void setAllotGroupName(String allotGroupName) {
        this.allotGroupName = allotGroupName;
    }

    public ReviewTaskInfo getReviewTaskInfo() {
        return reviewTaskInfo;
    }

    public void setReviewTaskInfo(ReviewTaskInfo reviewTaskInfo) {
        this.reviewTaskInfo = reviewTaskInfo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("relationId", getRelationId())
                .append("reviewGroupId", getReviewGroupId())
                .append("reviewId", getReviewId())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
