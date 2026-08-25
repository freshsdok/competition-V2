package com.teaching.competition.domain;

import com.teaching.system.api.domain.CertConfigInfo;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.List;

/**
 * 赛证互通规则表 competition_cert_exchange_rule
 *
 * @author teaching
 */
public class CompetitionCertExchangeRule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 赛证互通规则id */
    private Long ruleId;

    /** 赛事系列id */
    private Long competitionSeriesId;

    /** 赛事阶段id */
    private String competitionStageId;

    /** 赛道code */
    private String competitionTrackId;

    private String competitionName;

    private String competitionStageName;

    private String competitionTrackName;

    private String secondLevelName;

    private String competitionSeriesName;

    // 源证书名称
    private String originCertName;
    // 目标证书名称
    private String targetCertName;

    private String targetCertificate;

    //来源类型
    private String certSource;

    /** 组别code */
    @Size(min = 0, max = 64, message = "组别code不能超过64个字符")
    private String secondLevelCode;

    /** 申请条件说明 */
    @Excel(name = "申请条件说明")
    private String applyDesc;

    /** 规则名称 */
    @Excel(name = "规则名称")
    private String rulerName;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Long sort;

    /** 证书条件 */
    private String certConditions;

    /** 是否置顶 0-否 1-是 */
    private String isTope;

    /** 规则状态 0-禁用 1-启用 */
    private String rulerStatus;

    /** 关键字（用于查询） */
    private String keyWord;

    /** 版本 */
    private Long version;

    private String delFlag;

    /** 订单数量*/
    private Integer orderNum;

    /** 新增赛证互通规则目标证书 */
    private List<CertConfigInfo> targetCertList;

    /** 新增赛证互通规则源证书 */
    private List<CertConfigInfo> originCertList;

    /** 赛证互通规则明细列表 */
    private List<CertExchangeRuleDetail> detailList;

    public Long getRuleId()
    {
        return ruleId;
    }

    public void setRuleId(Long ruleId)
    {
        this.ruleId = ruleId;
    }

    public Long getCompetitionSeriesId()
    {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId)
    {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getCompetitionStageId()
    {
        return competitionStageId;
    }

    public void setCompetitionStageId(String competitionStageId)
    {
        this.competitionStageId = competitionStageId;
    }

    public String getCompetitionSeriesName() {
        return competitionSeriesName;
    }

    public void setCompetitionSeriesName(String competitionSeriesName) {
        this.competitionSeriesName = competitionSeriesName;
    }

    @Size(min = 0, max = 64, message = "赛道code不能超过64个字符")
    public String getCompetitionTrackId()
    {
        return competitionTrackId;
    }

    public void setCompetitionTrackId(String competitionTrackId)
    {
        this.competitionTrackId = competitionTrackId;
    }

    @Size(min = 0, max = 64, message = "组别code不能超过64个字符")
    public String getSecondLevelCode()
    {
        return secondLevelCode;
    }

    public void setSecondLevelCode(String secondLevelCode)
    {
        this.secondLevelCode = secondLevelCode;
    }

    public String getApplyDesc()
    {
        return applyDesc;
    }

    public void setApplyDesc(String applyDesc)
    {
        this.applyDesc = applyDesc;
    }

    public String getRulerName()
    {
        return rulerName;
    }

    public void setRulerName(String rulerName)
    {
        this.rulerName = rulerName;
    }

    public String getIcon()
    {
        return icon;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public Long getSort()
    {
        return sort;
    }

    public void setSort(Long sort)
    {
        this.sort = sort;
    }

    public String getCertConditions()
    {
        return certConditions;
    }

    public void setCertConditions(String certConditions)
    {
        this.certConditions = certConditions;
    }

    public String getIsTope()
    {
        return isTope;
    }

    public void setIsTope(String isTope)
    {
        this.isTope = isTope;
    }

    public String getRulerStatus()
    {
        return rulerStatus;
    }

    public void setRulerStatus(String rulerStatus)
    {
        this.rulerStatus = rulerStatus;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
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

    public List<CertExchangeRuleDetail> getDetailList() {
        return detailList;
    }

    public void setDetailList(List<CertExchangeRuleDetail> detailList) {
        this.detailList = detailList;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
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

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompetitionStageName() {
        return competitionStageName;
    }

    public void setCompetitionStageName(String competitionStageName) {
        this.competitionStageName = competitionStageName;
    }

    public String getCompetitionTrackName() {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName) {
        this.competitionTrackName = competitionTrackName;
    }

    public String getSecondLevelName() {
        return secondLevelName;
    }

    public void setSecondLevelName(String secondLevelName) {
        this.secondLevelName = secondLevelName;
    }

    public String getOriginCertName() {
        return originCertName;
    }

    public void setOriginCertName(String originCertName) {
        this.originCertName = originCertName;
    }

    public String getTargetCertName() {
        return targetCertName;
    }

    public void setTargetCertName(String targetCertName) {
        this.targetCertName = targetCertName;
    }

    public String getTargetCertificate() {
        return targetCertName;
    }

    public void setTargetCertificate(String targetCertificate) {
        this.targetCertificate = targetCertificate;
        this.targetCertName = targetCertificate;
    }

    public String getCertSource() {
        return certSource;
    }

    public void setCertSource(String certSource) {
        this.certSource = certSource;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("ruleId", getRuleId())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("competitionStageId", getCompetitionStageId())
            .append("competitionTrackId", getCompetitionTrackId())
            .append("secondLevelCode", getSecondLevelCode())
            .append("applyDesc", getApplyDesc())
            .append("rulerName", getRulerName())
            .append("icon", getIcon())
            .append("sort", getSort())
            .append("certConditions", getCertConditions())
            .append("isTope", getIsTope())
            .append("rulerStatus", getRulerStatus())
            .append("keyWord", getKeyWord())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("version", getVersion())
            .append("detailList", getDetailList())
            .toString();
    }
}
