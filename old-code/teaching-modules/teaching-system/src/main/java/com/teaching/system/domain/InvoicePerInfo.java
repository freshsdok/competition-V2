package com.teaching.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 开票信息记录对象 invoice_per_info
 * 
 * @author teaching
 * @date 2025-12-10
 */
public class InvoicePerInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 发票类型 */
    @Excel(name = "发票类型")
    private String invoiceType;

    /** 发票抬头类型 */
    @Excel(name = "发票抬头类型")
    private String invoiceTitleType;

    /** 企业名称 */
    @Excel(name = "企业名称")
    private String enterpriseName;

    /** 企业id */
    @Excel(name = "企业id")
    private String enterpriseId;

    /** 纳税人识别号 */
    @Excel(name = "纳税人识别号")
    private String taxpayerIdentificationNumber;

    /** 发票内容 */
    @Excel(name = "发票内容")
    private String invoiceConternt;

    /** 接收邮箱 */
    @Excel(name = "接收邮箱")
    private String receiveEmail;

    /** 备注 */
    @Excel(name = "备注")
    private String ramke;

    private String userId;

    private Long version;

    private String delFlag;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setInvoiceType(String invoiceType) 
    {
        this.invoiceType = invoiceType;
    }

    public String getInvoiceType() 
    {
        return invoiceType;
    }

    public void setInvoiceTitleType(String invoiceTitleType) 
    {
        this.invoiceTitleType = invoiceTitleType;
    }

    public String getInvoiceTitleType() 
    {
        return invoiceTitleType;
    }

    public void setEnterpriseName(String enterpriseName) 
    {
        this.enterpriseName = enterpriseName;
    }

    public String getEnterpriseName() 
    {
        return enterpriseName;
    }

    public void setEnterpriseId(String enterpriseId) 
    {
        this.enterpriseId = enterpriseId;
    }

    public String getEnterpriseId() 
    {
        return enterpriseId;
    }

    public void setTaxpayerIdentificationNumber(String taxpayerIdentificationNumber) 
    {
        this.taxpayerIdentificationNumber = taxpayerIdentificationNumber;
    }

    public String getTaxpayerIdentificationNumber() 
    {
        return taxpayerIdentificationNumber;
    }

    public void setInvoiceConternt(String invoiceConternt) 
    {
        this.invoiceConternt = invoiceConternt;
    }

    public String getInvoiceConternt() 
    {
        return invoiceConternt;
    }

    public void setReceiveEmail(String receiveEmail) 
    {
        this.receiveEmail = receiveEmail;
    }

    public String getReceiveEmail() 
    {
        return receiveEmail;
    }

    public void setRamke(String ramke) 
    {
        this.ramke = ramke;
    }

    public String getRamke() 
    {
        return ramke;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("invoiceType", getInvoiceType())
            .append("invoiceTitleType", getInvoiceTitleType())
            .append("enterpriseName", getEnterpriseName())
            .append("enterpriseId", getEnterpriseId())
            .append("taxpayerIdentificationNumber", getTaxpayerIdentificationNumber())
            .append("invoiceConternt", getInvoiceConternt())
            .append("receiveEmail", getReceiveEmail())
            .append("ramke", getRamke())
                .append("userId", getUserId())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
            .toString();
    }
}
