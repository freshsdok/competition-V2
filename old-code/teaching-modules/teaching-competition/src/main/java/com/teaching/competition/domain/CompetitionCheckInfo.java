package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 校验项对象 competition_check_info
 *
 * @author teaching
 * @date 2025-12-18
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompetitionCheckInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 校验项id
     */
    private Long checkItemId;

    /**
     * 校验项名称
     */
    @Excel(name = "校验项名称")
    private String checkItemName;

    /**
     * 校验方法
     */
    @Excel(name = "校验方法")
    private String function;

    /**
     * 版本
     */
    @Excel(name = "版本")
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag;

    public CompetitionCheckInfo() {
    }

    public CompetitionCheckInfo(Long checkItemId, String checkItemName, String function) {
        this.checkItemId = checkItemId;
        this.checkItemName = checkItemName;
        this.function = function;
    }

    public void setCheckItemId(Long checkItemId) {
        this.checkItemId = checkItemId;
    }

    public Long getCheckItemId() {
        return checkItemId;
    }

    public void setCheckItemName(String checkItemName) {
        this.checkItemName = checkItemName;
    }

    public String getCheckItemName() {
        return checkItemName;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getFunction() {
        return function;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getVersion() {
        return version;
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
                .append("checkItemId", getCheckItemId())
                .append("checkItemName", getCheckItemName())
                .append("remark", getRemark())
                .append("function", getFunction())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
