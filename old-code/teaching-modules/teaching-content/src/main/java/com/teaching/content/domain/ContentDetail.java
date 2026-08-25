package com.teaching.content.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 内容详情对象 content_detail
 *
 * @author teaching
 * @date 2025-11-21
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentDetail extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 详情ID
     */
    private Long detailId;

    /**
     * 栏目ID
     */
    @Excel(name = "栏目ID")
    @NotNull(message = "栏目ID不能为空")
    private Long columnId;

    /**
     * 详情标题
     */
    @Excel(name = "详情标题")
    private String detailTitle;

    /**
     * 详情内容（富文本）
     */
    @Excel(name = "详情内容")
    private String detailContent;

    /**
     * 详情图片
     */
    @Excel(name = "详情图片")
    private String detailImage;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Long getDetailId() {
        return detailId;
    }

    public void setDetailId(Long detailId) {
        this.detailId = detailId;
    }

    public Long getColumnId() {
        return columnId;
    }

    public void setColumnId(Long columnId) {
        this.columnId = columnId;
    }

    public String getDetailTitle() {
        return detailTitle;
    }

    public void setDetailTitle(String detailTitle) {
        this.detailTitle = detailTitle;
    }

    public String getDetailContent() {
        return detailContent;
    }

    public void setDetailContent(String detailContent) {
        this.detailContent = detailContent;
    }

    public String getDetailImage() {
        return detailImage;
    }

    public void setDetailImage(String detailImage) {
        this.detailImage = detailImage;
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
                .append("detailId", getDetailId())
                .append("columnId", getColumnId())
                .append("detailTitle", getDetailTitle())
                .append("detailContent", getDetailContent())
                .append("detailImage", getDetailImage())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .append("delFlag", getDelFlag())
                .toString();
    }
}


