package com.teaching.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 对账单对账记录对象 order_statement_record
 * 
 * @author teaching
 * @date 2025-10-27
 */
public class OrderStatementRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键id */
    private Long id;

    /** 对账日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "对账日期", width = 30, dateFormat = "yyyy-MM-dd")
    private String billDate;

    /** 对账文件名称 */
    @Excel(name = "对账文件名称")
    private String statementFileName;

    /** 订单id */
    @Excel(name = "订单id")
    private String orderId;

    /** 订单类型（消费/退货） */
    @Excel(name = "订单类型", readConverterExp = "消=费/退货")
    private String orderType;

    /** 对账金额 */
    @Excel(name = "对账金额")
    private BigDecimal amount;

    /** 对账状态 */
    @Excel(name = "对账状态")
    private String status;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag;

    /** 数据权限机构id */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setBillDate(String billDate)
    {
        this.billDate = billDate;
    }

    public String getBillDate()
    {
        return billDate;
    }

    public void setStatementFileName(String statementFileName) 
    {
        this.statementFileName = statementFileName;
    }

    public String getStatementFileName() 
    {
        return statementFileName;
    }

    public void setOrderId(String orderId) 
    {
        this.orderId = orderId;
    }

    public String getOrderId() 
    {
        return orderId;
    }

    public void setOrderType(String orderType) 
    {
        this.orderType = orderType;
    }

    public String getOrderType() 
    {
        return orderType;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
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

    public void setOrgId(Long orgId) 
    {
        this.orgId = orgId;
    }

    public Long getOrgId() 
    {
        return orgId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("billDate", getBillDate())
            .append("statementFileName", getStatementFileName())
            .append("orderId", getOrderId())
            .append("orderType", getOrderType())
            .append("amount", getAmount())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("orgId", getOrgId())
            .toString();
    }
}
