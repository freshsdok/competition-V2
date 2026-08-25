package com.teaching.system.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

/**
 * 专家组对象 review_specialist_group_info
 *
 * @author teaching
 * @date 2026-04-09
 */
public class ReviewSpecialistGroupInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 专家组id
     */
    private Long groupId;

    /**
     * 专家组名称
     */
    @Excel(name = "专家组名称")
    private String groupName;

    /**
     * 专家组分配状态
     */
    @Excel(name = "专家组分配状态")
    private String allotStatus;

    /**
     * 版本
     */
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 专家组与专家关联关系列表
     */
    private List<ReviewGroupSpecialistRelation> reviewGroupSpecialistRelationList;

    // 专家组id集合
    private List<Long> specialistUserIdList;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAllotStatus() {
        return allotStatus;
    }

    public void setAllotStatus(String allotStatus) {
        this.allotStatus = allotStatus;
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

    public List<ReviewGroupSpecialistRelation> getReviewGroupSpecialistRelationList() {
        return reviewGroupSpecialistRelationList;
    }

    public void setReviewGroupSpecialistRelationList(List<ReviewGroupSpecialistRelation> reviewGroupSpecialistRelationList) {
        this.reviewGroupSpecialistRelationList = reviewGroupSpecialistRelationList;
    }

    public List<Long> getSpecialistUserIdList() {
        return specialistUserIdList;
    }

    public void setSpecialistUserIdList(List<Long> specialistUserIdList) {
        this.specialistUserIdList = specialistUserIdList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("groupId", getGroupId())
                .append("groupName", getGroupName())
                .append("allotStatus", getAllotStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
