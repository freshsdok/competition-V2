package com.teaching.system.api.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 订单信息对象 order_info
 *
 * @author teaching
 * @date 2025-10-17
 */
public class OrderInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @TableField(exist = false)
    private String idStr;

    /** 订单号 */
    private String orderId;

    /** 用户id */
    @NotNull(message = "用户id不能为空")
    private Long userId;
    /** 商品 / 服务名称 */
    @Excel(name = "商品名称")
    @NotBlank(message = "商品名称不能为空")
    private String commodityName;

    /** 商品类型 */
    @NotNull(message = "商品类型不能为空")
    @Excel(name = "商品类型" , readConverterExp = "course=课程,competition=赛事,cert=证书")
    private String commodityType;

    /** 用户姓名 */
    @Excel(name = "购买人")
    private String userName;

    /** 商品服务id */
    @NotBlank(message = "商品id不能为空")
    private String commodityId;

    @Excel(name = "联系方式")
    @TableField(exist = false)
    private String phoneNumber;

    /** 金额 */
    @Excel(name = "金额")
    @NotNull(message = "交易金额不能为空")
    private BigDecimal amount;

    /** 状态（待支付 / 已支付 / 已退款 / 已取消） */
    @Excel(name = "状态", readConverterExp = "pending=待支付,paid=已支付,refunded=已退款,cancelled=已取消,approving=待审核,approve_rejected=审核不通过")
    private String payStatus;

    //ZF：支付宝
    //WX：微信
    //YL：银联
    //EC：数字人民币
    @Excel(name = "支付方式",readConverterExp = "ZF=支付宝,WX=微信,YL=银联,EC=数字人民币")
    private String payMode;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "支付时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    //@Excel(name="是否已开票",readConverterExp = "false=否,true=是")
    @TableField(exist = false)
    public boolean hasInvoiced;

    @Excel(name="开票状态",readConverterExp = "0=未开票完毕,1=已开票")
//    @TableField(exist = false)
    private String invoiceStatus;

    /** 版本 */
    private Long version;

    /** 删除标识 */
    private String delFlag;

    /** 数据权限机构id */
    private Long orgId;

    /**
     * 二维码链接
     */
    private String qrCode;

    /**
     * 三方公司订单号
     */
    private String targetOrderId;

    /** 二维码过期时间 */
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String qrCodeExpireTime;

    /**
     * 退款订单号
     */
    private String refundOrderId;
    /**
     * 退款原因
     */
    private String refundReason;
    /**
     * 退款金额
     */
    private BigDecimal refundAmount;
    /**
     * 退款时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date refundTime;

    /**
     * 是否是最新的订单（0-否，1-是）
     */
    private String lastOrder;

    @Excel(name="创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 支付方式-online-线上转账，offline-线下转账
     * @return
     */
    private String payMethod;

    /**
     * 审核意见
     * @return
     */
    private String auditOpinion;

    /**
     * 转账支付证明文件
     * @return
     */
    private String paymentProofFiles;

    /**
     * 商户号
     */
    private String merId;

    @TableField(exist = false)
    private String merName;

    /**
     * 外部商户订单号
     */
    private String outOrderId;

    /**
     * 平台订单号(招行生成的订单号)
     */
    private String cmbOrderId;

    /**
     * 商户订单号（和orderId一样，订单返回，和本地作对比）
     */
    private String bizOrderId;

    /**
     * 调用远程接口获取订单费用token验证
     */
    @TableField(exist = false)
    private String token;

    /**
     * 赛事id-下单时用
     */
    @TableField(exist = false)
    private Long competitionSeriesId;

    /**
     * 发票地址
     */
    @TableField(exist = false)
    private String cUrl;
    /**
     * 商品详情
     */
    @TableField(exist = false)
    private List<CompetitionApplyInfoVO> competitionList;

    /**
     * 商品数量、单价列表
     */
    @TableField(exist = false)
    private List<NumDetail> numDetailList;

    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date payStartTime;

    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date payEndTime;

    @TableField(exist = false)
    private BigDecimal amountStart;

    @TableField(exist = false)
    private BigDecimal amountEnd;

    /**
     * 学校名称，查询使用
     */
    @TableField(exist = false)
    private String schoolName;

    /**
     * 二级事项id
     */
    private Long eventId;

    /**
     * 订单类型（pay-支付订单，refund-退款单）
     */
    private String orderType;

    /**
     * 退款状态（refunding-退款中，refunded-已退款）
     */
    private String refundStatus;

    /**
     * 原支付订单id(退款单才有)
     */
    private Long payOrderId;

    /**
     * 实际缴费金额（缴费金额-退费金额）
     */
    private BigDecimal relAmount;

    /**
     * 团队成员列表
     */
    @TableField(exist = false)
    private Map<String,String> teamUsers;

    /**
     * 变更类型
     */
    @TableField(exist = false)
    private String changeType;

    /**
     * 是否隐藏取消订单
     */
    @TableField(exist = false)
    private boolean hide;

    // 团队信息
    private String teamInfoList;

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderId(String orderId)
    {
        this.orderId = orderId;
    }

    public String getOrderId()
    {
        return orderId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setCommodityName(String commodityName)
    {
        this.commodityName = commodityName;
    }

    public String getCommodityName()
    {
        return commodityName;
    }

    public void setCommodityId(String commodityId)
    {
        this.commodityId = commodityId;
    }

    public String getCommodityId()
    {
        return commodityId;
    }

    public String getCommodityType() {
        return commodityType;
    }

    public void setCommodityType(String commodityType) {
        this.commodityType = commodityType;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setPayMode(String payMode)
    {
        this.payMode = payMode;
    }

    public String getPayMode()
    {
        return payMode;
    }

    public void setPayStatus(String payStatus)
    {
        this.payStatus = payStatus;
    }

    public String getPayStatus()
    {
        return payStatus;
    }

    public void setPayTime(Date payTime)
    {
        this.payTime = payTime;
    }

    public Date getPayTime()
    {
        return payTime;
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

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getTargetOrderId() {
        return targetOrderId;
    }

    public void setTargetOrderId(String targetOrderId) {
        this.targetOrderId = targetOrderId;
    }

    public String getQrCodeExpireTime() {
        return qrCodeExpireTime;
    }

    public void setQrCodeExpireTime(String qrCodeExpireTime) {
        this.qrCodeExpireTime = qrCodeExpireTime;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRefundOrderId() {
        return refundOrderId;
    }

    public void setRefundOrderId(String refundOrderId) {
        this.refundOrderId = refundOrderId;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Date getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(Date refundTime) {
        this.refundTime = refundTime;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isHasInvoiced() {
        return hasInvoiced;
    }

    public void setHasInvoiced(boolean hasInvoiced) {
        this.hasInvoiced = hasInvoiced;
    }
    public String getLastOrder() {
        return lastOrder;
    }

    public void setLastOrder(String lastOrder) {
        this.lastOrder = lastOrder;
    }


    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(String payMethod) {
        this.payMethod = payMethod;
    }

    public String getAuditOpinion() {
        return auditOpinion;
    }

    public void setAuditOpinion(String auditOpinion) {
        this.auditOpinion = auditOpinion;
    }

    public String getPaymentProofFiles() {
        return paymentProofFiles;
    }

    public void setPaymentProofFiles(String paymentProofFiles) {
        this.paymentProofFiles = paymentProofFiles;
    }

    public List<CompetitionApplyInfoVO> getCompetitionList() {
        return competitionList;
    }

    public void setCompetitionList(List<CompetitionApplyInfoVO> competitionList) {
        this.competitionList = competitionList;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getcUrl() {
        return cUrl;
    }

    public void setcUrl(String cUrl) {
        this.cUrl = cUrl;
    }

    public List<NumDetail> getNumDetailList() {
        return numDetailList;
    }

    public void setNumDetailList(List<NumDetail> numDetailList) {
        this.numDetailList = numDetailList;
    }

    public Date getPayStartTime() {
        return payStartTime;
    }

    public void setPayStartTime(Date payStartTime) {
        this.payStartTime = payStartTime;
    }

    public Date getPayEndTime() {
        return payEndTime;
    }

    public void setPayEndTime(Date payEndTime) {
        this.payEndTime = payEndTime;
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

    public String getMerId() {
        return merId;
    }

    public void setMerId(String merId) {
        this.merId = merId;
    }

    public String getOutOrderId() {
        return outOrderId;
    }

    public void setOutOrderId(String outOrderId) {
        this.outOrderId = outOrderId;
    }

    public String getCmbOrderId() {
        return cmbOrderId;
    }

    public void setCmbOrderId(String cmbOrderId) {
        this.cmbOrderId = cmbOrderId;
    }

    public String getBizOrderId() {
        return bizOrderId;
    }

    public void setBizOrderId(String bizOrderId) {
        this.bizOrderId = bizOrderId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getMerName() {
        return merName;
    }

    public void setMerName(String merName) {
        this.merName = merName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    public Long getPayOrderId() {
        return payOrderId;
    }

    public void setPayOrderId(Long payOrderId) {
        this.payOrderId = payOrderId;
    }

    public Map<String, String> getTeamUsers() {
        return teamUsers;
    }

    public void setTeamUsers(Map<String, String> teamUsers) {
        this.teamUsers = teamUsers;
    }

    public BigDecimal getRelAmount() {
        return relAmount;
    }

    public void setRelAmount(BigDecimal relAmount) {
        this.relAmount = relAmount;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public String getTeamInfoList() {
        return teamInfoList;
    }

    public void setTeamInfoList(String teamInfoList) {
        this.teamInfoList = teamInfoList;
    }

    @Override
    public String toString() {
        return "OrderInfo{" +
                "id=" + id +
                ", orderId='" + orderId + '\'' +
                ", userId=" + userId +
                ", commodityName='" + commodityName + '\'' +
                ", commodityType='" + commodityType + '\'' +
                ", userName='" + userName + '\'' +
                ", commodityId='" + commodityId + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", amount=" + amount +
                ", payStatus='" + payStatus + '\'' +
                ", payMode='" + payMode + '\'' +
                ", payTime=" + payTime +
                ", hasInvoiced=" + hasInvoiced +
                ", invoiceStatus='" + invoiceStatus + '\'' +
                ", version=" + version +
                ", delFlag='" + delFlag + '\'' +
                ", orgId=" + orgId +
                ", qrCode='" + qrCode + '\'' +
                ", targetOrderId='" + targetOrderId + '\'' +
                ", qrCodeExpireTime='" + qrCodeExpireTime + '\'' +
                ", refundOrderId='" + refundOrderId + '\'' +
                ", refundReason='" + refundReason + '\'' +
                ", refundAmount=" + refundAmount +
                ", refundTime=" + refundTime +
                ", lastOrder='" + lastOrder + '\'' +
                ", createTime=" + createTime +
                ", payMethod='" + payMethod + '\'' +
                ", auditOpinion='" + auditOpinion + '\'' +
                ", paymentProofFiles='" + paymentProofFiles + '\'' +
                ", merId='" + merId + '\'' +
                ", merName='" + merName + '\'' +
                ", outOrderId='" + outOrderId + '\'' +
                ", cmbOrderId='" + cmbOrderId + '\'' +
                ", bizOrderId='" + bizOrderId + '\'' +
                ", token='" + token + '\'' +
                ", competitionSeriesId=" + competitionSeriesId +
                ", cUrl='" + cUrl + '\'' +
                ", competitionList=" + competitionList +
                ", numDetailList=" + numDetailList +
                ", payStartTime=" + payStartTime +
                ", payEndTime=" + payEndTime +
                ", amountStart=" + amountStart +
                ", amountEnd=" + amountEnd +
                ", schoolName='" + schoolName + '\'' +
                ", eventId=" + eventId +
                ", orderType='" + orderType + '\'' +
                ", refundStatus='" + refundStatus + '\'' +
                ", payOrderId=" + payOrderId +
                ", relAmount=" + relAmount +
                ", teamUsers=" + teamUsers +
                ", changeType='" + changeType + '\'' +
                '}';
    }
}

