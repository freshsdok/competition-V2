package com.teaching.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 短信模板目标对象 message_template_target_table
 * 
 * @author teaching
 * @date 2025-12-22
 */
public class MessageTemplateTargetTable extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 关联表主键 */
    private Long id;

    /** 源表id */
    @Excel(name = "源表id")
    private Long msgId;

    /** 目标表 */
    @Excel(name = "目标表")
    private String targetTable;

    /** 目标表名称 */
    @Excel(name = "目标表名称")
    private String targetTableName;

    /** 目标表字段 */
    @Excel(name = "目标表字段")
    private String targetColumn;

    /** 目标表字段名称 */
    @Excel(name = "目标表字段名称")
    private String targetColumnName;

    /** 条件字段 */
    @Excel(name = "条件字段")
    private String conditionColumn;

    /** 条件字段名称 */
    @Excel(name = "条件字段名称")
    private String conditionColumnName;

    private String conditionColumn2;

    /** 字段顺序 */
    @Excel(name = "字段顺序")
    private Long orderNum;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setMsgId(Long msgId) 
    {
        this.msgId = msgId;
    }

    public Long getMsgId() 
    {
        return msgId;
    }

    public void setTargetTable(String targetTable) 
    {
        this.targetTable = targetTable;
    }

    public String getTargetTable() 
    {
        return targetTable;
    }

    public void setTargetTableName(String targetTableName) 
    {
        this.targetTableName = targetTableName;
    }

    public String getTargetTableName() 
    {
        return targetTableName;
    }

    public String getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(String targetColumn) {
        this.targetColumn = targetColumn;
    }

    public String getTargetColumnName() {
        return targetColumnName;
    }

    public void setTargetColumnName(String targetColumnName) {
        this.targetColumnName = targetColumnName;
    }

    public void setConditionColumn(String conditionColumn)
    {
        this.conditionColumn = conditionColumn;
    }

    public String getConditionColumn() 
    {
        return conditionColumn;
    }

    public void setConditionColumnName(String conditionColumnName) 
    {
        this.conditionColumnName = conditionColumnName;
    }

    public String getConditionColumnName() 
    {
        return conditionColumnName;
    }

    public void setOrderNum(Long orderNum) 
    {
        this.orderNum = orderNum;
    }

    public Long getOrderNum() 
    {
        return orderNum;
    }

    public void setVersion(Long version) 
    {
        this.version = version;
    }

    public Long getVersion() 
    {
        return version;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    public String getConditionColumn2() {
        return conditionColumn2;
    }

    public void setConditionColumn2(String conditionColumn2) {
        this.conditionColumn2 = conditionColumn2;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("msgId", getMsgId())
            .append("targetTable", getTargetTable())
            .append("targetTableName", getTargetTableName())
            .append("targetColumn", getTargetColumn())
            .append("targetColumnName", getTargetColumnName())
            .append("conditionColumn", getConditionColumn())
            .append("conditionColumnName", getConditionColumnName())
                .append("conditionColumn2", getConditionColumn2())
            .append("orderNum", getOrderNum())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
