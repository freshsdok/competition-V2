package com.teaching.system.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * 评审任务分配组信息对象 review_task_allot_group
 *
 * @author teaching
 * @date 2026-04-09
 */
public class ReviewTaskAllotGroup extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 分配组id
     */
    private Long reviewGroupId;

    /**
     * 分配组名称
     */
    @Excel(name = "分配组名称")
    private String allotGroupName;

    /**
     * 版本
     */
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 评审任务分配组关联关系列表
     */
    private List<ReviewTaskAllotGroupRelation> reviewTaskAllotGroupRelationList;

    // 任务id集合
    private List<Long> reviewIdList;

    public Long getReviewGroupId() {
        return reviewGroupId;
    }

    public void setReviewGroupId(Long reviewGroupId) {
        this.reviewGroupId = reviewGroupId;
    }

    public String getAllotGroupName() {
        return allotGroupName;
    }

    public void setAllotGroupName(String allotGroupName) {
        this.allotGroupName = allotGroupName;
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

    public List<ReviewTaskAllotGroupRelation> getReviewTaskAllotGroupRelationList() {
        return reviewTaskAllotGroupRelationList;
    }

    public void setReviewTaskAllotGroupRelationList(List<ReviewTaskAllotGroupRelation> reviewTaskAllotGroupRelationList) {
        this.reviewTaskAllotGroupRelationList = reviewTaskAllotGroupRelationList;
    }

    public List<Long> getReviewIdList() {
        return reviewIdList;
    }

    public void setReviewIdList(List<Long> reviewIdList) {
        this.reviewIdList = reviewIdList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("reviewGroupId", getReviewGroupId())
                .append("allotGroupName", getAllotGroupName())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
