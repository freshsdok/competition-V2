package com.teaching.system.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.TableField;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 商户参数配置（支付和发票）对象 merchant_param_config
 * 
 * @author teaching
 * @date 2025-12-23
 */
public class MerchantParamConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 公司名称 */
    @Excel(name = "公司名称")
    private String merName;

    /** 商户号 */
    @Excel(name = "商户号")
    private String merId;

    /** 收银员 */
    @Excel(name = "收银员")
    private String feeUserId;

    /** 终端id */
    @Excel(name = "终端id")
    private String termId;

    /** 支付有效时长(秒) */
    @Excel(name = "支付有效时长(秒)")
    private Long payValidTime;

    /** 支付APPID */
    @Excel(name = "支付APPID")
    private String payAppId;

    /** 支付appSecret */
    @Excel(name = "支付appSecret")
    private String payAppSecret;

    /** 支付私钥 */
    @Excel(name = "支付私钥")
    private String payPrivateKey;

    /** 支付公钥 */
    @Excel(name = "支付公钥")
    private String payPublicKey;

    /** 发票appKey */
    @Excel(name = "发票appKey")
    private String invoiceAppKey;

    /** 发票appSecret */
    @Excel(name = "发票appSecret")
    private String invoiceAppSecret;

    /** 发票accessToken */
    private String invoiceAccessToken;

    /** 税号 */
    @Excel(name = "税号")
    private String taxNum;

    /** 分机号 */
    @Excel(name = "分机号")
    private String extension;

    /** 税率 */
    @Excel(name = "税率")
    private BigDecimal taxRate;

    /** 开票人 */
    @Excel(name = "开票人")
    private String clerk;

    /** 审核人 */
    @Excel(name = "审核人")
    private String checker;

    /** 开户行 */
    @Excel(name = "开户行")
    private String bank;

    /** 卡号 */
    @Excel(name = "卡号")
    private String account;

    /** 开户行地址 */
    @Excel(name = "开户行地址")
    private String address;

    /** 作用范围 */
    @Excel(name = "作用范围")
    private String workScope;

    /** 开票内容信息 */
    @Excel(name = "开票内容信息")
    private String invoiceContent;

    /** 开启状态(0-关闭，1-开启) */
    @Excel(name = "开启状态(0-关闭，1-开启)")
    private Long status;

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

    /**
     * 作用范围列表
     */
    @TableField(exist = false)
    List<MerchantWorkScope> workScopeList;

    /**
     * 发票内容列表
     */
    @TableField(exist = false)
    List<Map<String,String>> contentMapList;


    /**
     * 接口发票内容参数
     */
    @TableField(exist = false)
    List<String> contentList;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setMerName(String merName) 
    {
        this.merName = merName;
    }

    public String getMerName() 
    {
        return merName;
    }

    public void setMerId(String merId) 
    {
        this.merId = merId;
    }

    public String getMerId() 
    {
        return merId;
    }

    public void setFeeUserId(String feeUserId) 
    {
        this.feeUserId = feeUserId;
    }

    public String getFeeUserId() 
    {
        return feeUserId;
    }

    public void setTermId(String termId) 
    {
        this.termId = termId;
    }

    public String getTermId() 
    {
        return termId;
    }

    public void setPayValidTime(Long payValidTime) 
    {
        this.payValidTime = payValidTime;
    }

    public Long getPayValidTime() 
    {
        return payValidTime;
    }

    public void setPayAppId(String payAppId) 
    {
        this.payAppId = payAppId;
    }

    public String getPayAppId() 
    {
        return payAppId;
    }

    public void setPayAppSecret(String payAppSecret) 
    {
        this.payAppSecret = payAppSecret;
    }

    public String getPayAppSecret() 
    {
        return payAppSecret;
    }

    public void setPayPrivateKey(String payPrivateKey) 
    {
        this.payPrivateKey = payPrivateKey;
    }

    public String getPayPrivateKey() 
    {
        return payPrivateKey;
    }

    public void setPayPublicKey(String payPublicKey) 
    {
        this.payPublicKey = payPublicKey;
    }

    public String getPayPublicKey() 
    {
        return payPublicKey;
    }

    public void setInvoiceAppKey(String invoiceAppKey) 
    {
        this.invoiceAppKey = invoiceAppKey;
    }

    public String getInvoiceAppKey() 
    {
        return invoiceAppKey;
    }

    public void setInvoiceAppSecret(String invoiceAppSecret) 
    {
        this.invoiceAppSecret = invoiceAppSecret;
    }

    public String getInvoiceAppSecret() 
    {
        return invoiceAppSecret;
    }

    public String getInvoiceAccessToken() {
        return invoiceAccessToken;
    }

    public void setInvoiceAccessToken(String invoiceAccessToken) {
        this.invoiceAccessToken = invoiceAccessToken;
    }

    public void setTaxNum(String taxNum)
    {
        this.taxNum = taxNum;
    }

    public String getTaxNum() 
    {
        return taxNum;
    }

    public void setExtension(String extension) 
    {
        this.extension = extension;
    }

    public String getExtension() 
    {
        return extension;
    }

    public void setTaxRate(BigDecimal taxRate) 
    {
        this.taxRate = taxRate;
    }

    public BigDecimal getTaxRate() 
    {
        return taxRate;
    }

    public void setClerk(String clerk) 
    {
        this.clerk = clerk;
    }

    public String getClerk() 
    {
        return clerk;
    }

    public void setChecker(String checker) 
    {
        this.checker = checker;
    }

    public String getChecker() 
    {
        return checker;
    }

    public void setBank(String bank) 
    {
        this.bank = bank;
    }

    public String getBank() 
    {
        return bank;
    }

    public void setAccount(String account) 
    {
        this.account = account;
    }

    public String getAccount() 
    {
        return account;
    }

    public void setAddress(String address) 
    {
        this.address = address;
    }

    public String getAddress() 
    {
        return address;
    }

    public void setWorkScope(String workScope) 
    {
        this.workScope = workScope;
    }

    public String getWorkScope() 
    {
        return workScope;
    }

    public void setInvoiceContent(String invoiceContent) 
    {
        this.invoiceContent = invoiceContent;
    }

    public String getInvoiceContent() 
    {
        return invoiceContent;
    }

    public void setStatus(Long status) 
    {
        this.status = status;
    }

    public Long getStatus() 
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

    public List<MerchantWorkScope> getWorkScopeList() {
        return workScopeList;
    }

    public void setWorkScopeList(List<MerchantWorkScope> workScopeList) {
        this.workScopeList = workScopeList;
    }

    public List<Map<String, String>> getContentMapList() {
        return contentMapList;
    }

    public void setContentMapList(List<Map<String, String>> contentMapList) {
        this.contentMapList = contentMapList;
    }

    public List<String> getContentList() {
        return contentList;
    }

    public void setContentList(List<String> contentList) {
        this.contentList = contentList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("merName", getMerName())
            .append("merId", getMerId())
            .append("feeUserId", getFeeUserId())
            .append("termId", getTermId())
            .append("payValidTime", getPayValidTime())
            .append("payAppId", getPayAppId())
            .append("payAppSecret", getPayAppSecret())
            .append("payPrivateKey", getPayPrivateKey())
            .append("payPublicKey", getPayPublicKey())
            .append("invoiceAppKey", getInvoiceAppKey())
            .append("invoiceAppSecret", getInvoiceAppSecret())
            .append("taxNum", getTaxNum())
            .append("extension", getExtension())
            .append("taxRate", getTaxRate())
            .append("clerk", getClerk())
            .append("checker", getChecker())
            .append("bank", getBank())
            .append("account", getAccount())
            .append("address", getAddress())
            .append("workScope", getWorkScope())
            .append("invoiceContent", getInvoiceContent())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("userId", getUserId())
            .append("orgId", getOrgId())
            .toString();
    }
}
