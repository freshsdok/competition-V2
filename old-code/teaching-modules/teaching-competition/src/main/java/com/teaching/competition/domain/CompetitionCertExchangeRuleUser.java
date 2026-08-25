package com.teaching.competition.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import com.teaching.system.api.domain.CertConfigInfo;

import java.util.List;

public class CompetitionCertExchangeRuleUser extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 赛证互通规则id */
    private Long ruleId;

    /** 赛事系列id */
    private Long competitionSeriesId;

    /** 赛道code */
    private String competitionTrackId;

    /** 组别code */
    private String secondLevelCode;

    /** 申请条件说明 */
    private String applyDesc;

    /** 规则名称 */
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

    /** 新增赛证互通规则目标证书 */
    private List<CertConfigInfo> targetCertList;

    /** 用户可选源证书 */
    private List<CertConfigInfo> originCertList;

    /** 赛证互通规则明细列表 */
    private List<CertExchangeRuleDetail> detailList;

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getCompetitionTrackId() {
        return competitionTrackId;
    }

    public void setCompetitionTrackId(String competitionTrackId) {
        this.competitionTrackId = competitionTrackId;
    }

    public String getSecondLevelCode() {
        return secondLevelCode;
    }

    public void setSecondLevelCode(String secondLevelCode) {
        this.secondLevelCode = secondLevelCode;
    }

    public String getApplyDesc() {
        return applyDesc;
    }

    public void setApplyDesc(String applyDesc) {
        this.applyDesc = applyDesc;
    }

    public String getRulerName() {
        return rulerName;
    }

    public void setRulerName(String rulerName) {
        this.rulerName = rulerName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Long getSort() {
        return sort;
    }

    public void setSort(Long sort) {
        this.sort = sort;
    }

    public String getCertConditions() {
        return certConditions;
    }

    public void setCertConditions(String certConditions) {
        this.certConditions = certConditions;
    }

    public String getIsTope() {
        return isTope;
    }

    public void setIsTope(String isTope) {
        this.isTope = isTope;
    }

    public String getRulerStatus() {
        return rulerStatus;
    }

    public void setRulerStatus(String rulerStatus) {
        this.rulerStatus = rulerStatus;
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

    public List<CertExchangeRuleDetail> getDetailList() {
        return detailList;
    }

    public void setDetailList(List<CertExchangeRuleDetail> detailList) {
        this.detailList = detailList;
    }
}
