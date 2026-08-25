package com.teaching.system.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;
import java.util.List;

/**
 * 赛证互通申请表 competition_cert_exchange_apply
 *
 * @author teaching
 */
public class CompetitionCertExchangeApply extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 申请id */
    private Long applyId;

    /** 学校 */
    private String school;

    @Excel(name = "参赛单位")
    private String schoolName;

    @Excel(name = "队伍ID")
    private String teamCode;
    /** 用户名称 */
    @Excel(name = "参赛选手")
    private String userName;

    /** 赛事名称 */
    private String competitionName;

    /** 赛道名称 */
    private String competitionTrackName;

    /** 组别名称 */
    private String secondLevelName;

    /** 指导教师名称 */
    @Excel(name = "指导教师")
    private String guideTeacherName;

    /** 身份证号 */
    @Excel(name = "身份证号")
    private String idCard;

    /** 源证书名称 */
    @Excel(name = "源证书名称")
    private String originCertName;

    /** 源证书编号(逗号分隔) */
    private String originCertCode;

    /** 源证书id */
    private String originCertId;

    /** 目标证书名称 */
    @Excel(name = "目标证书名称")
    private String targetCertName;

    /** 目标证书id */
    private String targetCertId;

    /** 申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "申请时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date applyTime;

    /** 补缴金额 */
    private String repayAmount;

    /** 关联订单ID */
    private Long orderId;

    /** 状态(0 取消申请    1 已申请未缴费   2 已申请已缴费) */
    private String applyStatus;

    /** 赛证互通规则id */
    private Long ruleId;

    /** 赛证互通规则名称 */
    private String rulerName;

    /** 支付状态 */
    @Excel(name = "订单状态", readConverterExp = "pending=待支付,paid=已支付,refunded=已退款,cancelled=已取消,failed=支付失败")
    private String payStatus;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "支付时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /** 用户id */
    private Long userId;

    /** 发票状态 */
    private String invoiceStatus;

    /** 版本 */
    private Long version;

    private String delFlag;

    /** 新增赛证互通规则目标证书 */
    private List<CertConfigInfo> targetCertList;

    /** 用户可选源证书 */
    private List<CertConfigInfo> originCertList;

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getRulerName() {
        return rulerName;
    }

    public void setRulerName(String rulerName) {
        this.rulerName = rulerName;
    }

    public Long getApplyId()
    {
        return applyId;
    }

    public void setApplyId(Long applyId)
    {
        this.applyId = applyId;
    }

    public Date getApplyTime()
    {
        return applyTime;
    }

    public void setApplyTime(Date applyTime)
    {
        this.applyTime = applyTime;
    }

    @Size(min = 0, max = 100, message = "补缴金额不能超过100个字符")
    public String getRepayAmount()
    {
        return repayAmount;
    }

    public void setRepayAmount(String repayAmount)
    {
        this.repayAmount = repayAmount;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    @Size(min = 0, max = 32, message = "状态不能超过32个字符")
    public String getApplyStatus()
    {
        return applyStatus;
    }

    public void setApplyStatus(String applyStatus)
    {
        this.applyStatus = applyStatus;
    }

    @NotNull(message = "赛证互通规则id不能为空")
    public Long getRuleId()
    {
        return ruleId;
    }

    public void setRuleId(Long ruleId)
    {
        this.ruleId = ruleId;
    }

    @Size(min = 0, max = 32, message = "支付状态不能超过32个字符")
    public String getPayStatus()
    {
        return payStatus;
    }

    public void setPayStatus(String payStatus)
    {
        this.payStatus = payStatus;
    }

    public Date getPayTime()
    {
        return payTime;
    }

    public void setPayTime(Date payTime)
    {
        this.payTime = payTime;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    @Size(min = 0, max = 100, message = "发票状态不能超过100个字符")
    public String getInvoiceStatus()
    {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus)
    {
        this.invoiceStatus = invoiceStatus;
    }

    public String getOriginCertName()
    {
        return originCertName;
    }

    public void setOriginCertName(String originCertName)
    {
        this.originCertName = originCertName;
    }

    public String getOriginCertCode()
    {
        return originCertCode;
    }

    public void setOriginCertCode(String originCertCode)
    {
        this.originCertCode = originCertCode;
    }

    public String getOriginCertId()
    {
        return originCertId;
    }

    public void setOriginCertId(String originCertId)
    {
        this.originCertId = originCertId;
    }

    public String getTargetCertName()
    {
        return targetCertName;
    }

    public void setTargetCertName(String targetCertName)
    {
        this.targetCertName = targetCertName;
    }

    public String getTargetCertId() {
        return targetCertId;
    }

    public void setTargetCertId(String targetCertId) {
        this.targetCertId = targetCertId;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getSchool()
    {
        return school;
    }

    public void setSchool(String school)
    {
        this.school = school;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public String getCompetitionName()
    {
        return competitionName;
    }

    public void setCompetitionName(String competitionName)
    {
        this.competitionName = competitionName;
    }

    public String getCompetitionTrackName()
    {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName)
    {
        this.competitionTrackName = competitionTrackName;
    }

    public String getSecondLevelName()
    {
        return secondLevelName;
    }

    public void setSecondLevelName(String secondLevelName)
    {
        this.secondLevelName = secondLevelName;
    }

    public String getGuideTeacherName()
    {
        return guideTeacherName;
    }

    public void setGuideTeacherName(String guideTeacherName)
    {
        this.guideTeacherName = guideTeacherName;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public List<CertConfigInfo> getTargetCertList() {
        return targetCertList;
    }

    public void setTargetCertList(List<CertConfigInfo> targetCertList) {
        this.targetCertList = targetCertList;
    }

    public List<CertConfigInfo> getOriginCertList() {
        return originCertList;
    }

    public void setOriginCertList(List<CertConfigInfo> originCertList) {
        this.originCertList = originCertList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("applyId", getApplyId())
            .append("applyTime", getApplyTime())
            .append("repayAmount", getRepayAmount())
            .append("orderId", getOrderId())
            .append("applyStatus", getApplyStatus())
            .append("ruleId", getRuleId())
            .append("payStatus", getPayStatus())
            .append("payTime", getPayTime())
            .append("userId", getUserId())
            .append("invoiceStatus", getInvoiceStatus())
            .append("originCertName", getOriginCertName())
            .append("originCertCode", getOriginCertCode())
            .append("originCertId", getOriginCertId())
            .append("targetCertName", getTargetCertName())
            .append("targetCertId", getTargetCertId())
            .append("userName", getUserName())
            .append("school", getSchool())
            .append("idCard", getIdCard())
            .append("competitionName", getCompetitionName())
            .append("competitionTrackName", getCompetitionTrackName())
            .append("secondLevelName", getSecondLevelName())
            .append("guideTeacherName", getGuideTeacherName())
                .append("schoolName", getSchoolName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("version", getVersion())
            .toString();
    }
}
