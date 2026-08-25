package com.teaching.competition.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 赛事作品对象 competition_works
 *
 * @author teaching
 * @date 2025-10-22
 */
public class CompetitionWorks extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 作品id
     */
    private Long worksId;

    /**
     * 竞赛id
     */
    @Excel(name = "竞赛id")
    private Long competitionSeriesId;

    /**
     * 赛事阶段id
     */
    @Excel(name = "赛事阶段id")
    private String stageId;

    /**
     * 赛事阶段名称
     */
    private String stageName;

    /**
     * 作品名称
     */
    @Excel(name = "作品名称")
    private String worksName;

    /**
     * 作品url
     */
    @Excel(name = "作品url")
    private String worksUrl;

    /**
     * 作品评分
     */
    @Excel(name = "作品评分")
    private String worksScore;

    /**
     * 用户id
     */
    @Excel(name = "用户id")
    private Long userId;

    /**
     * 团队code
     */
    @Excel(name = "团队code")
    private String teamCode;

    /**
     * 作品排名
     */
    @Excel(name = "作品排名")
    private String worksRank;

    /**
     * 作品说明
     */
    @Excel(name = "作品说明")
    private String worksDesc;

    /**
     * 作品评审状态
     */
    @Excel(name = "作品评审状态")
    private String worksStatus;

    /**
     * 是否晋级
     */
    private String isAdvance;

    /**
     * 奖项名称
     */
    private String awardsName;

    /**
     * 奖项金额
     */
    private BigDecimal awardsMoney;

    /**
     * 证书地址
     */
    @Excel(name = "证书地址")
    private String certificateUrl;

    /**
     * 赛事名称
     */
    private String competitionName;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 团队名称
     */
    private String teamName;

    // 获奖名单
    private List<UserGradeInfo> awardsList;

    /**
     * 是否评分
     */
    private String isWorksScore;

    /**
     * 版本
     */
    @Excel(name = "版本")
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag = "0";

    /**
     * 数据权限机构id
     */
    private Long orgId;

    /**
     * 专家打分作品id集合
     */
    private String worksIds;


    /**
     * 赛事赛道名称
     */
    private String competitionTrackName;

    /**
     * 赛事组别
     */
    private String groupClassify;

    /**
     * 抽取码
     */
    private String extractionCode;

    public void setWorksId(Long worksId) {
        this.worksId = worksId;
    }

    public Long getWorksId() {
        return worksId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setStageId(String stageId) {
        this.stageId = stageId;
    }

    public String getStageId() {
        return stageId;
    }

    public void setWorksName(String worksName) {
        this.worksName = worksName;
    }

    public String getWorksName() {
        return worksName;
    }

    public void setWorksUrl(String worksUrl) {
        this.worksUrl = worksUrl;
    }

    public String getWorksUrl() {
        return worksUrl;
    }

    public void setWorksScore(String worksScore) {
        this.worksScore = worksScore;
    }

    public String getWorksScore() {
        return worksScore;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setWorksRank(String worksRank) {
        this.worksRank = worksRank;
    }

    public String getWorksRank() {
        return worksRank;
    }

    public void setWorksDesc(String worksDesc) {
        this.worksDesc = worksDesc;
    }

    public String getWorksDesc() {
        return worksDesc;
    }

    public void setWorksStatus(String worksStatus) {
        this.worksStatus = worksStatus;
    }

    public String getWorksStatus() {
        return worksStatus;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getVersion() {
        return version;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public String getIsAdvance() {
        return isAdvance;
    }

    public void setIsAdvance(String isAdvance) {
        this.isAdvance = isAdvance;
    }

    public String getAwardsName() {
        return awardsName;
    }

    public void setAwardsName(String awardsName) {
        this.awardsName = awardsName;
    }

    public BigDecimal getAwardsMoney() {
        return awardsMoney;
    }

    public void setAwardsMoney(BigDecimal awardsMoney) {
        this.awardsMoney = awardsMoney;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public List<UserGradeInfo> getAwardsList() {
        return awardsList;
    }

    public void setAwardsList(List<UserGradeInfo> awardsList) {
        this.awardsList = awardsList;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getIsWorksScore() {
        return isWorksScore;
    }

    public void setIsWorksScore(String isWorksScore) {
        this.isWorksScore = isWorksScore;
    }

    public String getWorksIds() {
        return worksIds;
    }

    public void setWorksIds(String worksIds) {
        this.worksIds = worksIds;
    }

    public String getCompetitionTrackName() {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName) {
        this.competitionTrackName = competitionTrackName;
    }

    public String getGroupClassify() {
        return groupClassify;
    }

    public void setGroupClassify(String groupClassify) {
        this.groupClassify = groupClassify;
    }

    public String getExtractionCode() {
        return extractionCode;
    }

    public void setExtractionCode(String extractionCode) {
        this.extractionCode = extractionCode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("worksId", getWorksId())
                .append("competitionSeriesId", getCompetitionSeriesId())
                .append("stageId", getStageId())
                .append("stageName", getStageName())
                .append("worksName", getWorksName())
                .append("worksUrl", getWorksUrl())
                .append("worksScore", getWorksScore())
                .append("userId", getUserId())
                .append("teamCode", getTeamCode())
                .append("worksRank", getWorksRank())
                .append("worksDesc", getWorksDesc())
                .append("worksStatus", getWorksStatus())
                .append("isAdvance", getIsAdvance())
                .append("awardsName", getAwardsName())
                .append("awardsMoney", getAwardsMoney())
                .append("competitionTrackName", getCompetitionTrackName())
                .append("groupClassify", getGroupClassify())
                .append("certificateUrl", getCertificateUrl())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .append("orgId", getOrgId())
                .toString();
    }
}
