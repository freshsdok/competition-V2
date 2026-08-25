package com.teaching.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.List;

/**
 * 短信模板对象 message_template_source
 * 
 * @author teaching
 * @date 2025-12-22
 */
public class MessageTemplateSource extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 短信类型（验证码、通知短信） */
    @Excel(name = "短信类型", readConverterExp = "验=证码、通知短信")
    private String msgType;

    /** 短信模板名称 */
    @Excel(name = "短信模板名称")
    private String msgName;

    /** 短信模板code */
    @Excel(name = "短信模板code")
    private String msgCode;

    // 参数属性名称
    private String attributeName;

    /** 变量属性表名 */
    @Excel(name = "变量属性表名")
    private String variableTable;

    /** 表名备注 */
    @Excel(name = "表名备注")
    private String variableTableLabel;

    /** 变量属性列名 */
    @Excel(name = "变量属性列名")
    private String variableColumn;

    /** 字段备注 */
    @Excel(name = "字段备注")
    private String variableColumnLabel;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag;

    List<MessageTemplateTargetTable> messageTemplateTargetTableList;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setMsgType(String msgType) 
    {
        this.msgType = msgType;
    }

    public String getMsgType() 
    {
        return msgType;
    }

    public void setMsgName(String msgName) 
    {
        this.msgName = msgName;
    }

    public String getMsgName() 
    {
        return msgName;
    }

    public void setMsgCode(String msgCode) 
    {
        this.msgCode = msgCode;
    }

    public String getMsgCode() 
    {
        return msgCode;
    }

    public void setVariableTable(String variableTable) 
    {
        this.variableTable = variableTable;
    }

    public String getVariableTable() 
    {
        return variableTable;
    }

    public void setVariableTableLabel(String variableTableLabel) 
    {
        this.variableTableLabel = variableTableLabel;
    }

    public String getVariableTableLabel() 
    {
        return variableTableLabel;
    }

    public void setVariableColumn(String variableColumn) 
    {
        this.variableColumn = variableColumn;
    }

    public String getVariableColumn() 
    {
        return variableColumn;
    }

    public void setVariableColumnLabel(String variableColumnLabel) 
    {
        this.variableColumnLabel = variableColumnLabel;
    }

    public String getVariableColumnLabel() 
    {
        return variableColumnLabel;
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

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public List<MessageTemplateTargetTable> getMessageTemplateTargetTableList() {
        return messageTemplateTargetTableList;
    }

    public void setMessageTemplateTargetTableList(List<MessageTemplateTargetTable> messageTemplateTargetTableList) {
        this.messageTemplateTargetTableList = messageTemplateTargetTableList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("msgType", getMsgType())
            .append("msgName", getMsgName())
            .append("msgCode", getMsgCode())
            .append("variableTable", getVariableTable())
            .append("variableTableLabel", getVariableTableLabel())
            .append("variableColumn", getVariableColumn())
            .append("variableColumnLabel", getVariableColumnLabel())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("remark", getRemark())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
                .append("attributeName", getAttributeName())
            .toString();
    }
}
