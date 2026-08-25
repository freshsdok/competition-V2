package com.teaching.competition.domain;

import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 赛证互通规则明细表 cert_exchange_rule_detail
 *
 * @author teaching
 */
public class CertExchangeRuleDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细id */
    private Long detailId;

    /** 赛证互通规则id */
    private Long ruleId;

    /** 源证书配置id */
    private Long originCertConfigId;

    /** 源证书编号 */
    @Excel(name = "源证书编号")
    private String originCertCode;

    /** 源证书名称 */
    @Excel(name = "源证书名称")
    private String originCertName;

    /** 源证书分值 */
    @Excel(name = "源证书分值")
    private String originCertScore;

    /** 目标证书配置id */
    private Long targetCertConfigId;

    /** 目标证书编号 */
    @Excel(name = "目标证书编号")
    private String targetCertCode;

    /** 目标证书名称 */
    @Excel(name = "目标证书名称")
    private String targetCertName;

    /** 目标证书分值 */
    @Excel(name = "目标证书分值")
    private String targetCertScore;

    /** 状态(0 禁用 1可用) */
    @Excel(name = "状态", readConverterExp = "0=禁用,1=可用")
    private String ruleStatus;

    /** 证书互换年份 */
    private String year;

    /** 源证书年限 */
    @Excel(name = "源证书年限")
    private String originYearLimit;

    /** 排序 */
    @Excel(name = "排序")
    private Long sort;

    /** 版本 */
    private Long version;

    private String delFlag;

    public Long getDetailId()
    {
        return detailId;
    }

    public void setDetailId(Long detailId)
    {
        this.detailId = detailId;
    }

    public Long getRuleId()
    {
        return ruleId;
    }

    public void setRuleId(Long ruleId)
    {
        this.ruleId = ruleId;
    }

    @Size(min = 0, max = 255, message = "源证书编号不能超过255个字符")
    public String getOriginCertCode()
    {
        return originCertCode;
    }

    public void setOriginCertCode(String originCertCode)
    {
        this.originCertCode = originCertCode;
    }

    public Long getOriginCertConfigId()
    {
        return originCertConfigId;
    }

    public void setOriginCertConfigId(Long originCertConfigId)
    {
        this.originCertConfigId = originCertConfigId;
    }

    @Size(min = 0, max = 255, message = "源证书名称不能超过255个字符")
    public String getOriginCertName()
    {
        return originCertName;
    }

    public void setOriginCertName(String originCertName)
    {
        this.originCertName = originCertName;
    }

    @Size(min = 0, max = 100, message = "源证书分值不能超过100个字符")
    public String getOriginCertScore()
    {
        return originCertScore;
    }

    public void setOriginCertScore(String originCertScore)
    {
        this.originCertScore = originCertScore;
    }

    @Size(min = 0, max = 255, message = "目标证书编号不能超过255个字符")
    public String getTargetCertCode()
    {
        return targetCertCode;
    }

    public void setTargetCertCode(String targetCertCode)
    {
        this.targetCertCode = targetCertCode;
    }

    public Long getTargetCertConfigId()
    {
        return targetCertConfigId;
    }

    public void setTargetCertConfigId(Long targetCertConfigId)
    {
        this.targetCertConfigId = targetCertConfigId;
    }

    @Size(min = 0, max = 255, message = "目标证书名称不能超过255个字符")
    public String getTargetCertName()
    {
        return targetCertName;
    }

    public void setTargetCertName(String targetCertName)
    {
        this.targetCertName = targetCertName;
    }

    @Size(min = 0, max = 100, message = "目标证书分值不能超过100个字符")
    public String getTargetCertScore()
    {
        return targetCertScore;
    }

    public void setTargetCertScore(String targetCertScore)
    {
        this.targetCertScore = targetCertScore;
    }

    @Size(min = 0, max = 64, message = "状态不能超过64个字符")
    public String getRuleStatus()
    {
        return ruleStatus;
    }

    public void setRuleStatus(String ruleStatus)
    {
        this.ruleStatus = ruleStatus;
    }

    @Size(min = 0, max = 32, message = "年份不能超过32个字符")
    public String getYear()
    {
        return year;
    }

    public void setYear(String year)
    {
        this.year = year;
    }

    public String getOriginYearLimit() {
        return originYearLimit;
    }

    public void setOriginYearLimit(String originYearLimit) {
        this.originYearLimit = originYearLimit;
    }

    public Long getSort()
    {
        return sort;
    }

    public void setSort(Long sort)
    {
        this.sort = sort;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("detailId", getDetailId())
            .append("ruleId", getRuleId())
            .append("originCertConfigId", getOriginCertConfigId())
            .append("originCertCode", getOriginCertCode())
            .append("originCertName", getOriginCertName())
            .append("originCertScore", getOriginCertScore())
            .append("targetCertConfigId", getTargetCertConfigId())
            .append("targetCertCode", getTargetCertCode())
            .append("targetCertName", getTargetCertName())
            .append("targetCertScore", getTargetCertScore())
            .append("ruleStatus", getRuleStatus())
            .append("year", getYear())
            .append("originYearLimit", getOriginYearLimit())
            .append("sort", getSort())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("version", getVersion())
            .toString();
    }
}
