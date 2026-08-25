package com.teaching.content.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * 内容栏目对象 content_column
 *
 * @author teaching
 * @date 2025-11-21
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentColumn extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 栏目ID
     */
    private Long columnId;

    /**
     * 栏目名称
     */
    @Excel(name = "栏目名称")
    @NotBlank(message = "栏目名称不能为空")
    private String columnName;

    /**
     * 栏目编码
     */
    @Excel(name = "栏目编码")
    private String columnCode;

    /**
     * 父栏目ID（0表示顶级）
     */
    @Excel(name = "父栏目ID")
    private Long parentId;

    /**
     * 父栏目名称
     */
    private String parentName;

    /**
     * 栏目类型：1-内容列表 2-文件列表 3-详情页 4-文件下载
     */
    @Excel(name = "栏目类型", readConverterExp = "1=内容列表,2=文件列表,3=详情页,4=文件下载")
    @NotBlank(message = "栏目类型不能为空")
    private String columnType;

    /**
     * 关联菜单ID
     */
    @Excel(name = "菜单ID")
    private Long menuId;

    /**
     * 栏目描述
     */
    @Excel(name = "栏目描述")
    private String columnDesc;

    /**
     * 栏目图片
     */
    @Excel(name = "栏目图片")
    private String columnImage;

    /**
     * 显示顺序
     */
    @Excel(name = "显示顺序")
    private Integer orderNum;

    /**
     * 状态（0正常 1停用）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 是否置顶（0否 1是）
     */
    @Excel(name = "是否置顶", readConverterExp = "0=否,1=是")
    private String isTop;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 子栏目
     */
    private List<ContentColumn> children = new ArrayList<ContentColumn>();

    public Long getColumnId() {
        return columnId;
    }

    public void setColumnId(Long columnId) {
        this.columnId = columnId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnCode() {
        return columnCode;
    }

    public void setColumnCode(String columnCode) {
        this.columnCode = columnCode;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public String getColumnDesc() {
        return columnDesc;
    }

    public void setColumnDesc(String columnDesc) {
        this.columnDesc = columnDesc;
    }

    public String getColumnImage() {
        return columnImage;
    }

    public void setColumnImage(String columnImage) {
        this.columnImage = columnImage;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIsTop() {
        return isTop;
    }

    public void setIsTop(String isTop) {
        this.isTop = isTop;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public List<ContentColumn> getChildren() {
        return children;
    }

    public void setChildren(List<ContentColumn> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("columnId", getColumnId())
                .append("columnName", getColumnName())
                .append("columnCode", getColumnCode())
                .append("parentId", getParentId())
                .append("parentName", getParentName())
                .append("columnType", getColumnType())
                .append("menuId", getMenuId())
                .append("columnDesc", getColumnDesc())
                .append("columnImage", getColumnImage())
                .append("orderNum", getOrderNum())
                .append("status", getStatus())
                .append("isTop", getIsTop())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .append("delFlag", getDelFlag())
                .toString();
    }
}


