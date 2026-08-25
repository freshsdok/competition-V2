package com.teaching.system.api.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 页面、组件、数据源关联关系对象 component_data_source_rela
 *
 * @author teaching
 * @date 2025-10-14
 */
public class ComponentDataSourceRela extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 关联id
     */
    private Long relaId;
    /**
     * 页面id
     */
    private Long pageId;

    /**
     * 组件id
     */
    @Excel(name = "组件id")
    private String componentId;

    /**
     * 数据源id
     */
    @Excel(name = "数据源id")
    private Long dataId;

    /**
     * 删除标识
     */
    private String delFlag;

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public void setRelaId(Long relaId) {
        this.relaId = relaId;
    }

    public Long getRelaId() {
        return relaId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setDataId(Long dataId) {
        this.dataId = dataId;
    }

    public Long getDataId() {
        return dataId;
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
                .append("relaId", getRelaId())
                .append("componentId", getComponentId())
                .append("dataId", getDataId())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
