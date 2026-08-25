package com.teaching.system.domain;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 专家审阅备注信息记录对象 expert_review_notes
 *
 * @author teaching
 * @date 2026-04-24
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpertReviewNotes extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * processed_relation表id
     */
    @Excel(name = "processed_relation表id")
    private Long processedRelationId;

    /**
     * 专家id
     */
    @Excel(name = "专家id")
    private Long expertId;

    /**
     * 描述
     */
    @Excel(name = "描述")
    private String describe;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private String delFlag;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setProcessedRelationId(Long processedRelationId) {
        this.processedRelationId = processedRelationId;
    }

    public Long getProcessedRelationId() {
        return processedRelationId;
    }

    public void setExpertId(Long expertId) {
        this.expertId = expertId;
    }

    public Long getExpertId() {
        return expertId;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getDescribe() {
        return describe;
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
                .append("processedRelationId", getProcessedRelationId())
                .append("expertId", getExpertId())
                .append("describe", getDescribe())
                .append("delFlag", getDelFlag())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
