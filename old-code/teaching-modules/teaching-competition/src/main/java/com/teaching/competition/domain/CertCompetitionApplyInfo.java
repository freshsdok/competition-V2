package com.teaching.competition.domain;

import com.teaching.system.api.domain.CompetitionApplyInfo;

import java.util.Map;

public class CertCompetitionApplyInfo extends CompetitionApplyInfo {

    /**
     * 证书来源类型
     */
    private String sourceType;

    /**
     * 用户成绩
     */
    private String userScore;

    /**
     * 是否选中
     */
    private Boolean isSelect =  false;

    /**
     * 证书配置信息ID
     */
    private Long certConfigId;

    /** 排名 */
    private String ranking;

    /**
     * 人员查询条件
     */
    private CertCompetitionApplyInfoCondition filterConditions;

    public String getRanking() {
        return ranking;
    }

    public void setRanking(String ranking) {
        this.ranking = ranking;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getUserScore() {
        return userScore;
    }

    public void setUserScore(String userScore) {
        this.userScore = userScore;
    }

    public CertCompetitionApplyInfoCondition getFilterConditions() {
        return filterConditions;
    }

    public void setFilterConditions(CertCompetitionApplyInfoCondition filterConditions) {
        this.filterConditions = filterConditions;
    }

    public Boolean getSelect() {
        return isSelect;
    }

    public void setSelect(Boolean select) {
        isSelect = select;
    }

    public Long getCertConfigId() {
        return certConfigId;
    }

    public void setCertConfigId(Long certConfigId) {
        this.certConfigId = certConfigId;
    }
}
