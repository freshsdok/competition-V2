package com.teaching.system.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 发票信息对象 invoice_info
 * 
 * @author teaching
 * @date 2025-10-28
 */
public class InvoiceInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 订单号 */
    @Excel(name = "订单号")
    private String orderId;

    /** 发票号码 */
    @Excel(name = "发票号码")
    private String invoiceNum;

    /** 发票代码 */
    @Excel(name = "发票代码")
    private String invoiceCode;

    /** 类型（1蓝票 /2 红票） */
    @Excel(name = "类型", readConverterExp = "1=蓝票,/=2,红=票")
    private String invoiceType;

    /** 购方名称（个人姓名或企业信息） */
    @Excel(name = "购方名称", readConverterExp = "个=人姓名或企业信息")
    private String buyerName;

    /** 购方税号（企业需要） */
    @Excel(name = "购方税号", readConverterExp = "企=业需要")
    private String buyerTaxNum;

    /** 企业名称 */
    @Excel(name = "企业名称")
    private String enterpriseName;

    /** 税号 */
    @Excel(name = "税号")
    private String taxCode;

    /** 地址 */
    @Excel(name = "地址")
    private String addr;

    /** 电话 */
    @Excel(name = "电话")
    private String phone;

    /** 银行账户 */
    @Excel(name = "银行账户")
    private String bankAccount;

    /** 开具状态（1开票完成 2开票失败 3开票成功签章失败(电票时)） */
    @Excel(name = "开具状态", readConverterExp = "0-开票中,1=开票完成,2=开票失败,3=开票成功签章失败(电票时)")
    private String issuedStatus;

    /** 开具时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开具时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date issuedTime;

    /** 开票审核状态 */
    @Excel(name = "开票审核状态")
    private String checkStatus;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag;

    /** 数据权限用户id */
    @Excel(name = "数据权限用户id")
    private Long userId;

    /** 数据权限机构id */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    /** 售方税号 */
    @Excel(name = "售方税号")
    private String salerTaxNum;

    /** 售方电话 */
    @Excel(name = "售方电话")
    private String salerTel;

    /** 售方地址 */
    @Excel(name = "售方地址")
    private String salerAddress;

    /** 订单时间 */
    @Excel(name = "订单时间")
    private Date invoiceDate;

    /** 开票员 */
    @Excel(name = "开票员")
    private String clerk;

    /** 买方手机 */
    @Excel(name = "买方手机")
    private String buyerPhone;

    /** 买方邮箱 */
    @Excel(name = "买方邮箱")
    private String buyerEmail;

    /** 开票种类（1 个人/2 企业） */
    @Excel(name = "开票种类", readConverterExp = "1=,个=人/2,企=业")
    private String invoiceClass;

    /** 回传发票信息地址（开票完成、失败） */
    @Excel(name = "回传发票信息地址", readConverterExp = "开=票完成、失败")
    private String callBackUrl;

    /** 发票流水号 */
    @Excel(name = "发票流水号")
    private String invoiceSerialNum;

    /** 发票pdf地址 */
    @Excel(name = "发票pdf地址")
    private String cUrl;

    /** 发票详情地址 */
    @Excel(name = "发票详情地址")
    private String cJpgUrl;

    /**
     * 开票内容
     */
    private String invoiceContent;

    /**
     * 开票内容-名称
     */
    private String invoiceContentName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 开票金额
     */
    private BigDecimal amount;

    /**
     * 失败原因
     */
    private String failReason;

    //发票申请时间-搜索条件
    @TableField(exist = false)
    private Date[] invoiceApplyTime;

    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date applyStartTime;

    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date applyEndTime;

    //开票金额-搜索条件
    @TableField(exist = false)
    private BigDecimal[] invoiceFee;

    @TableField(exist = false)
    private BigDecimal amountStart;

    @TableField(exist = false)
    private BigDecimal amountEnd;

    /**
     * 下单用户明
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 学校名称，查询使用
     */
    @TableField(exist = false)
    private String schoolName;

    /**
     * 商品编码（搜索用）
     */
    @TableField(exist = false)
    private String invoiceGoodsCode;

    @TableField(exist = false)
    private String feeType;


    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setOrderId(String orderId) 
    {
        this.orderId = orderId;
    }

    public String getOrderId() 
    {
        return orderId;
    }

    public void setInvoiceNum(String invoiceNum) 
    {
        this.invoiceNum = invoiceNum;
    }

    public String getInvoiceNum() 
    {
        return invoiceNum;
    }

    public void setInvoiceCode(String invoiceCode) 
    {
        this.invoiceCode = invoiceCode;
    }

    public String getInvoiceCode() 
    {
        return invoiceCode;
    }

    public void setInvoiceType(String invoiceType) 
    {
        this.invoiceType = invoiceType;
    }

    public String getInvoiceType() 
    {
        return invoiceType;
    }

    public void setBuyerName(String buyerName) 
    {
        this.buyerName = buyerName;
    }

    public String getBuyerName() 
    {
        return buyerName;
    }

    public void setBuyerTaxNum(String buyerTaxNum) 
    {
        this.buyerTaxNum = buyerTaxNum;
    }

    public String getBuyerTaxNum() 
    {
        return buyerTaxNum;
    }

    public void setEnterpriseName(String enterpriseName) 
    {
        this.enterpriseName = enterpriseName;
    }

    public String getEnterpriseName() 
    {
        return enterpriseName;
    }

    public void setTaxCode(String taxCode) 
    {
        this.taxCode = taxCode;
    }

    public String getTaxCode() 
    {
        return taxCode;
    }

    public void setAddr(String addr) 
    {
        this.addr = addr;
    }

    public String getAddr() 
    {
        return addr;
    }

    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() 
    {
        return phone;
    }

    public void setBankAccount(String bankAccount) 
    {
        this.bankAccount = bankAccount;
    }

    public String getBankAccount() 
    {
        return bankAccount;
    }

    public void setIssuedStatus(String issuedStatus) 
    {
        this.issuedStatus = issuedStatus;
    }

    public String getIssuedStatus() 
    {
        return issuedStatus;
    }

    public void setIssuedTime(Date issuedTime) 
    {
        this.issuedTime = issuedTime;
    }

    public Date getIssuedTime() 
    {
        return issuedTime;
    }

    public void setCheckStatus(String checkStatus) 
    {
        this.checkStatus = checkStatus;
    }

    public String getCheckStatus() 
    {
        return checkStatus;
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

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setOrgId(Long orgId) 
    {
        this.orgId = orgId;
    }

    public Long getOrgId() 
    {
        return orgId;
    }

    public void setSalerTaxNum(String salerTaxNum) 
    {
        this.salerTaxNum = salerTaxNum;
    }

    public String getSalerTaxNum() 
    {
        return salerTaxNum;
    }

    public void setSalerTel(String salerTel) 
    {
        this.salerTel = salerTel;
    }

    public String getSalerTel() 
    {
        return salerTel;
    }

    public void setSalerAddress(String salerAddress) 
    {
        this.salerAddress = salerAddress;
    }

    public String getSalerAddress() 
    {
        return salerAddress;
    }

    public void setInvoicDate(Date invoicDate)
    {
        this.invoiceDate = invoicDate;
    }

    public Date getInvoicDate()
    {
        return invoiceDate;
    }

    public void setClerk(String clerk) 
    {
        this.clerk = clerk;
    }

    public String getClerk() 
    {
        return clerk;
    }

    public void setBuyerPhone(String buyerPhone) 
    {
        this.buyerPhone = buyerPhone;
    }

    public String getBuyerPhone() 
    {
        return buyerPhone;
    }

    public void setBuyerEmail(String buyerEmail) 
    {
        this.buyerEmail = buyerEmail;
    }

    public String getBuyerEmail() 
    {
        return buyerEmail;
    }

    public void setInvoiceClass(String invoiceClass) 
    {
        this.invoiceClass = invoiceClass;
    }

    public String getInvoiceClass() 
    {
        return invoiceClass;
    }

    public void setCallBackUrl(String callBackUrl) 
    {
        this.callBackUrl = callBackUrl;
    }

    public String getCallBackUrl() 
    {
        return callBackUrl;
    }

    public void setInvoiceSerialNum(String invoiceSerialNum) 
    {
        this.invoiceSerialNum = invoiceSerialNum;
    }

    public String getInvoiceSerialNum() 
    {
        return invoiceSerialNum;
    }

    public void setcUrl(String cUrl) 
    {
        this.cUrl = cUrl;
    }

    public String getcUrl() 
    {
        return cUrl;
    }

    public void setcJpgUrl(String cJpgUrl) 
    {
        this.cJpgUrl = cJpgUrl;
    }

    public String getcJpgUrl() 
    {
        return cJpgUrl;
    }

    public String getInvoiceContent() {
        return invoiceContent;
    }

    public void setInvoiceContent(String invoiceContent) {
        this.invoiceContent = invoiceContent;
    }

    @Override
    public String getRemark() {
        return remark;
    }

    @Override
    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public Date[] getInvoiceApplyTime() {
        return invoiceApplyTime;
    }

    public void setInvoiceApplyTime(Date[] invoiceApplyTime) {
        this.invoiceApplyTime = invoiceApplyTime;
    }

    public BigDecimal[] getInvoiceFee() {
        return invoiceFee;
    }

    public void setInvoiceFee(BigDecimal[] invoiceFee) {
        this.invoiceFee = invoiceFee;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getApplyStartTime() {
        return applyStartTime;
    }

    public void setApplyStartTime(Date applyStartTime) {
        this.applyStartTime = applyStartTime;
    }

    public Date getApplyEndTime() {
        return applyEndTime;
    }

    public void setApplyEndTime(Date applyEndTime) {
        this.applyEndTime = applyEndTime;
    }

    public BigDecimal getAmountStart() {
        return amountStart;
    }

    public void setAmountStart(BigDecimal amountStart) {
        this.amountStart = amountStart;
    }

    public BigDecimal getAmountEnd() {
        return amountEnd;
    }

    public void setAmountEnd(BigDecimal amountEnd) {
        this.amountEnd = amountEnd;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getInvoiceContentName() {
        return invoiceContentName;
    }

    public void setInvoiceContentName(String invoiceContentName) {
        this.invoiceContentName = invoiceContentName;
    }

    public String getInvoiceGoodsCode() {
        return invoiceGoodsCode;
    }

    public void setInvoiceGoodsCode(String invoiceGoodsCode) {
        this.invoiceGoodsCode = invoiceGoodsCode;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("orderId", getOrderId())
            .append("invoiceNum", getInvoiceNum())
            .append("invoiceCode", getInvoiceCode())
            .append("invoiceType", getInvoiceType())
            .append("buyerName", getBuyerName())
            .append("buyerTaxNum", getBuyerTaxNum())
            .append("enterpriseName", getEnterpriseName())
            .append("taxCode", getTaxCode())
            .append("addr", getAddr())
            .append("phone", getPhone())
            .append("bankAccount", getBankAccount())
            .append("issuedStatus", getIssuedStatus())
            .append("issuedTime", getIssuedTime())
            .append("checkStatus", getCheckStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("userId", getUserId())
            .append("orgId", getOrgId())
            .append("salerTaxNum", getSalerTaxNum())
            .append("salerTel", getSalerTel())
            .append("salerAddress", getSalerAddress())
            .append("invoicDate", getInvoicDate())
            .append("clerk", getClerk())
            .append("buyerPhone", getBuyerPhone())
            .append("buyerEmail", getBuyerEmail())
            .append("invoiceClass", getInvoiceClass())
            .append("callBackUrl", getCallBackUrl())
            .append("invoiceSerialNum", getInvoiceSerialNum())
            .append("cUrl", getcUrl())
            .append("cJpgUrl", getcJpgUrl())
            .append("invoiceContent", getInvoiceContent())
            .append("remark", getRemark())
            .toString();
    }
}
