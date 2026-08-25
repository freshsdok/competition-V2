package com.teaching.competition.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import com.teaching.system.api.domain.CertConfigInfo;

import java.math.BigDecimal;
import java.util.List;

public class CompetitionCertExchangeRuleUserApply extends BaseEntity {

    /** 赛证互通规则id */
    private Long ruleId;

    /** 新增赛证互通规则目标证书 */
    private List<CertConfigInfo> targetCertList;

    /** 用户可选源证书 */
    private List<CertConfigInfo> originCertList;

    /** 证书总费用 */
    private BigDecimal certAmountSum;

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
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

    public BigDecimal getCertAmountSum() {
        return certAmountSum;
    }

    public void setCertAmountSum(BigDecimal certAmountSum) {
        this.certAmountSum = certAmountSum;
    }
}
