package com.teaching.competition.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 候选人证书表 candidate_cert_info
 *
 * @author teaching
 */
public class CandidateCertInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 候选人id */
    private Long candidateId;

    /** 证书配置id */
    private Long certConfigId;

    /** 参赛者姓名 */
    @Excel(name = "参赛者姓名")
    private String userName;

    /** 来源类型 */
    @Excel(name = "来源类型", readConverterExp = "import=数据导入,competition=赛事报名")
    private String sourceType;

    @Excel(name = "来源数据")
    private String sourceData;

    /** 参赛人来源 */
    private String playerSources;

    /** 参赛者id */
    private Long userId;

    /** 报名用户id */
//    @NotNull(message = "报名用户id不能为空")
    private Long memberId;

    private String memberIds;

    /** 手机号 */
    @Excel(name = "联系电话")
    private String phone;

    /** 邮箱 */
    @Excel(name = "邮箱")
    private String email;
    /**
     * 学校
     */
    @Excel(name = "学校")
    private String schoolName;
    /**
     * 专业
     */
    @Excel(name = "专业")
    private String profession;

    /** 身份证号码 */
    @Excel(name = "身份证号")
    private String idCard;

    /** 赛事系列id */
    private Long competitionSeriesId;

    /** 赛道code */
    private String competitionTrackId;

    /** 组别code */
    private String secondLevelCode;

    /** 赛事名称 */
    private String competitionName;

    /** 赛道名称 */
    private String competitionTrackName;

    /** 组别名称 */
    private String secondLevelName;

    /** 团队code */
//    @NotNull(message = "报名用户团队编号不能为空")
    private String teamCode;

    /** 团队名称 */
    private String teamName;

    /** 带队老师姓名 */
    @Excel(name = "带队老师姓名")
    private String leaderTeacherName;

    /** 参赛角色 */
    @Excel(name = "参赛角色")
    private String competitionRoleName;

    /** 带队老师id */
    private Long leaderTeacherId;


    /** 指导教师id */
    private Long guideTeacherId;

    /** 指导教师姓名 */
    private String guideTeacherName;

    /** 成绩分数 */
    @Excel(name = "成绩分数")
    private String score;

    /** 是否选中 */
    private Integer isCheck;

    /** 版本 */
    private Long version;

    private String delFlag;


    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public Long getCandidateId()
    {
        return candidateId;
    }

    public void setCandidateId(Long candidateId)
    {
        this.candidateId = candidateId;
    }

    public Long getCertConfigId()
    {
        return certConfigId;
    }

    public void setCertConfigId(Long certConfigId)
    {
        this.certConfigId = certConfigId;
    }

    @Size(min = 0, max = 100, message = "参赛者姓名不能超过100个字符")
    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    @Size(min = 0, max = 32, message = "手机号不能超过32个字符")
    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    @Size(min = 0, max = 100, message = "邮箱不能超过100个字符")
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Long getCompetitionSeriesId()
    {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId)
    {
        this.competitionSeriesId = competitionSeriesId;
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

    @Size(min = 0, max = 255, message = "赛事名称不能超过255个字符")
    public String getCompetitionName()
    {
        return competitionName;
    }

    public void setCompetitionName(String competitionName)
    {
        this.competitionName = competitionName;
    }

    @Size(min = 0, max = 255, message = "赛道名称不能超过255个字符")
    public String getCompetitionTrackName()
    {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName)
    {
        this.competitionTrackName = competitionTrackName;
    }

    @Size(min = 0, max = 255, message = "组别名称不能超过255个字符")
    public String getSecondLevelName()
    {
        return secondLevelName;
    }

    public void setSecondLevelName(String secondLevelName)
    {
        this.secondLevelName = secondLevelName;
    }

    @Size(min = 0, max = 255, message = "团队code不能超过255个字符")
    public String getTeamCode()
    {
        return teamCode;
    }

    public void setTeamCode(String teamCode)
    {
        this.teamCode = teamCode;
    }

    @Size(min = 0, max = 255, message = "团队名称不能超过255个字符")
    public String getTeamName()
    {
        return teamName;
    }

    public void setTeamName(String teamName)
    {
        this.teamName = teamName;
    }

    @Size(min = 0, max = 100, message = "参赛角色不能超过100个字符")
    public String getCompetitionRoleName()
    {
        return competitionRoleName;
    }

    public void setCompetitionRoleName(String competitionRoleName)
    {
        this.competitionRoleName = competitionRoleName;
    }

    public Long getLeaderTeacherId()
    {
        return leaderTeacherId;
    }

    public void setLeaderTeacherId(Long leaderTeacherId)
    {
        this.leaderTeacherId = leaderTeacherId;
    }

    @Size(min = 0, max = 100, message = "带队老师姓名不能超过100个字符")
    public String getLeaderTeacherName()
    {
        return leaderTeacherName;
    }

    public void setLeaderTeacherName(String leaderTeacherName)
    {
        this.leaderTeacherName = leaderTeacherName;
    }

    public Long getGuideTeacherId()
    {
        return guideTeacherId;
    }

    public void setGuideTeacherId(Long guideTeacherId)
    {
        this.guideTeacherId = guideTeacherId;
    }

    @Size(min = 0, max = 100, message = "指导教师姓名不能超过100个字符")
    public String getGuideTeacherName()
    {
        return guideTeacherName;
    }

    public void setGuideTeacherName(String guideTeacherName)
    {
        this.guideTeacherName = guideTeacherName;
    }

    @Size(min = 0, max = 100, message = "成绩分数不能超过100个字符")
    public String getScore()
    {
        return score;
    }

    public void setScore(String score)
    {
        this.score = score;
    }

    public Integer getIsCheck()
    {
        return isCheck;
    }

    public void setIsCheck(Integer isCheck)
    {
        this.isCheck = isCheck;
    }

    @Size(min = 0, max = 64, message = "来源类型不能超过64个字符")
    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
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

    public String getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(String memberIds) {
        this.memberIds = memberIds;
    }

    public String getSourceData() {
        return sourceData;
    }

    public void setSourceData(String sourceData) {
        this.sourceData = sourceData;
    }

    @Size(min = 0, max = 255, message = "参赛人来源不能超过255个字符")
    public String getPlayerSources() {
        return playerSources;
    }

    public void setPlayerSources(String playerSources) {
        this.playerSources = playerSources;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("candidateId", getCandidateId())
            .append("certConfigId", getCertConfigId())
            .append("userName", getUserName())
            .append("userId", getUserId())
            .append("memberId", getMemberId())
            .append("phone", getPhone())
            .append("email", getEmail())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("competitionTrackId", getCompetitionTrackId())
            .append("secondLevelCode", getSecondLevelCode())
            .append("competitionName", getCompetitionName())
            .append("competitionTrackName", getCompetitionTrackName())
            .append("secondLevelName", getSecondLevelName())
            .append("teamCode", getTeamCode())
            .append("teamName", getTeamName())
            .append("competitionRoleName", getCompetitionRoleName())
            .append("leaderTeacherId", getLeaderTeacherId())
            .append("leaderTeacherName", getLeaderTeacherName())
            .append("guideTeacherId", getGuideTeacherId())
            .append("guideTeacherName", getGuideTeacherName())
            .append("score", getScore())
            .append("isCheck", getIsCheck())
            .append("sourceType", getSourceType())
            .append("playerSources", getPlayerSources())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("version", getVersion())
                .append("sourceData", getSourceData())
                .append("idCard", getIdCard())
            .toString();
    }
}
