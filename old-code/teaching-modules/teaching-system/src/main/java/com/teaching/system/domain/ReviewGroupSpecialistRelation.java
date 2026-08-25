package com.teaching.system.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 专家组与专家关联关系对象 review_group_specialist_relation
 *
 * @author teaching
 * @date 2026-04-09
 */
public class ReviewGroupSpecialistRelation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 关联关系id
     */
    private Long groupRelaId;

    /**
     * 专家组id
     */
    @Excel(name = "专家组id")
    private Long groupId;

    /**
     * 专家id
     */
    @Excel(name = "专家id")
    private Long userId;

    @Excel(name = "专家名称")
    private String userName;

    /**
     * 版本
     */
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag;

    public Long getGroupRelaId() {
        return groupRelaId;
    }

    public void setGroupRelaId(Long groupRelaId) {
        this.groupRelaId = groupRelaId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
                .append("groupRelaId", getGroupRelaId())
                .append("groupId", getGroupId())
                .append("userId", getUserId())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
