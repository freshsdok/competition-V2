package com.teaching.competition.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户成绩信息对象 user_grade_info
 * 
 * @author teaching
 * @date 2025-10-22
 */
public class UserGradeInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 成绩id */
    private Long gradeId;

    /** 赛事系列id */
    @Excel(name = "赛事系列id")
    private Long competitionSeriesId;

    /** 赛事阶段id */
    private String stageId;

    /** 用户id */
    @Excel(name = "用户id")
    private Long userId;

    /** 团队id */
    @Excel(name = "团队id")
    private String teamCode;

    /** 排名 */
    @Excel(name = "排名")
    private String ranks;

    /** 是否获奖 */
    @Excel(name = "是否获奖")
    private String isAward;

    /** 奖项名称 */
    @Excel(name = "奖项名称")
    private String awardsName;

    /** 奖项金额 */
    private BigDecimal awardsMoney;

    /** 证书地址 */
    @Excel(name = "证书地址")
    private String certificateUrl;

    /** 分析结果 */
    @Excel(name = "分析结果")
    private String analyseResult;

    /** 最终得分 */
    private String score;

    /** 赛事名称 */
    private String competitionName;

    /** 用户名称 */
    private String userName;

    /** 团队名称 */
    private String teamName;

    /** 版本 */
    private Long version;

    /** 删除标识 */
    private String delFlag;

    /** 数据权限机构id */
    private Long orgId;

    /** 晋级采用方式 */
    private String advanceType;

    /** 是否晋级 */
    private String isAdvance;

    /** 组别 */
    private String groupClassify;

    /** 赛事赛道名称 */
    private String competitionTrackName;

    private List<UserGradeInfo> userGradeInfoList;

    public void setGradeId(Long gradeId) 
    {
        this.gradeId = gradeId;
    }

    public Long getGradeId() 
    {
        return gradeId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) 
    {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getCompetitionSeriesId() 
    {
        return competitionSeriesId;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setTeamCode(String teamCode) 
    {
        this.teamCode = teamCode;
    }

    public String getTeamCode() 
    {
        return teamCode;
    }

    public void setRanks(String ranks) 
    {
        this.ranks = ranks;
    }

    public String getRanks() 
    {
        return ranks;
    }

    public void setIsAward(String isAward) 
    {
        this.isAward = isAward;
    }

    public String getIsAward() 
    {
        return isAward;
    }

    public void setAwardsName(String awardsName) 
    {
        this.awardsName = awardsName;
    }

    public String getAwardsName() 
    {
        return awardsName;
    }

    public void setCertificateUrl(String certificateUrl) 
    {
        this.certificateUrl = certificateUrl;
    }

    public String getCertificateUrl() 
    {
        return certificateUrl;
    }

    public void setAnalyseResult(String analyseResult) 
    {
        this.analyseResult = analyseResult;
    }

    public String getAnalyseResult() 
    {
        return analyseResult;
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

    public BigDecimal getAwardsMoney() {
        return awardsMoney;
    }

    public void setAwardsMoney(BigDecimal awardsMoney) {
        this.awardsMoney = awardsMoney;
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

    public String getAdvanceType() {
        return advanceType;
    }

    public void setAdvanceType(String advanceType) {
        this.advanceType = advanceType;
    }

    public String getIsAdvance() {
        return isAdvance;
    }

    public void setIsAdvance(String isAdvance) {
        this.isAdvance = isAdvance;
    }

    public String getStageId() {
        return stageId;
    }

    public void setStageId(String stageId) {
        this.stageId = stageId;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public List<UserGradeInfo> getUserGradeInfoList() {
        return userGradeInfoList;
    }

    public void setUserGradeInfoList(List<UserGradeInfo> userGradeInfoList) {
        this.userGradeInfoList = userGradeInfoList;
    }

    public String getGroupClassify() {
        return groupClassify;
    }

    public void setGroupClassify(String groupClassify) {
        this.groupClassify = groupClassify;
    }

    public String getCompetitionTrackName() {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName) {
        this.competitionTrackName = competitionTrackName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("gradeId", getGradeId())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("userId", getUserId())
            .append("teamCode", getTeamCode())
            .append("ranks", getRanks())
            .append("isAward", getIsAward())
            .append("awardsName", getAwardsName())
            .append("certificateUrl", getCertificateUrl())
            .append("analyseResult", getAnalyseResult())
                .append("groupClassify", getGroupClassify())
                .append("competitionTrackName", getCompetitionTrackName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("orgId", getOrgId())
            .append("score", getScore())
            .toString();
    }
}
