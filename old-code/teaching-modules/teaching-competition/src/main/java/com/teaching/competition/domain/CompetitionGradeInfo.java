package com.teaching.competition.domain;

import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 成绩表 competition_grade_info
 *
 * @author teaching
 */
public class CompetitionGradeInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 成绩id */
    private Long gradeId;

    /** 赛事系列id */
    private Long competitionSeriesId;

    /** 赛事阶段id */
    private String competitionStageId;

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

    /** 课程id */
    private Long courseId;

    /** 培训项目id */
    private Long trainingProgramId;

    /** 报名用户id */
    private Long memberId;

    /** 用户ID */
    private Long userId;

    /** 成绩来源 */
    private String gradeSource;

    /** 来源数据 */
    private String sourceData;

    /** 分数 */
    @Excel(name = "分数")
    private String score;

    /** 排名 */
    private String ranking;

    /** 团队code */
    private String teamCode;

    /** 手机号 */
    private String phone;

    /** 用户名称 */
    private String userName;

    /** 身份证号 */
    private String idCard;

    /** 邮箱 */
    private String email;

    /** 带队老师id */
    private Long leaderTeacherId;

    /** 带队老师名称 */
    private String leaderTeacherName;

    /** 指导老师名称 */
    private String guideTeacherName;

    /** 学校名称 */
    private String schoolName;

    /** 参赛角色名称 */
    private String competitionRoleName;

    /** 版本 */
    private Long version;

    private String delFlag;

    /**
     * 阶段名称
     */
    private String stageName;

    /**
     * 人员查询条件
     */
    private CertCompetitionApplyInfoCondition filterConditions;

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public Long getGradeId()
    {
        return gradeId;
    }

    public void setGradeId(Long gradeId)
    {
        this.gradeId = gradeId;
    }

    public Long getCompetitionSeriesId()
    {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId)
    {
        this.competitionSeriesId = competitionSeriesId;
    }

    @Size(min = 0, max = 100, message = "赛事阶段id不能超过100个字符")
    public String getCompetitionStageId()
    {
        return competitionStageId;
    }

    public void setCompetitionStageId(String competitionStageId)
    {
        this.competitionStageId = competitionStageId;
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

    public Long getCourseId()
    {
        return courseId;
    }

    public void setCourseId(Long courseId)
    {
        this.courseId = courseId;
    }

    public Long getTrainingProgramId()
    {
        return trainingProgramId;
    }

    public void setTrainingProgramId(Long trainingProgramId)
    {
        this.trainingProgramId = trainingProgramId;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    @Size(min = 0, max = 64, message = "分数不能超过64个字符")
    public String getScore()
    {
        return score;
    }

    public void setScore(String score)
    {
        this.score = score;
    }

    @Size(min = 0, max = 32, message = "排名不能超过32个字符")
    public String getRanking()
    {
        return ranking;
    }

    public void setRanking(String ranking)
    {
        this.ranking = ranking;
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

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Long getLeaderTeacherId()
    {
        return leaderTeacherId;
    }

    public void setLeaderTeacherId(Long leaderTeacherId)
    {
        this.leaderTeacherId = leaderTeacherId;
    }

    public String getCompetitionRoleName()
    {
        return competitionRoleName;
    }

    public void setCompetitionRoleName(String competitionRoleName)
    {
        this.competitionRoleName = competitionRoleName;
    }

    @Size(min = 0, max = 64, message = "成绩来源不能超过64个字符")
    public String getGradeSource()
    {
        return gradeSource;
    }

    public void setGradeSource(String gradeSource)
    {
        this.gradeSource = gradeSource;
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

    public String getSourceData() {
        return sourceData;
    }

    public void setSourceData(String sourceData) {
        this.sourceData = sourceData;
    }

    public CertCompetitionApplyInfoCondition getFilterConditions() {
        return filterConditions;
    }

    public void setFilterConditions(CertCompetitionApplyInfoCondition filterConditions) {
        this.filterConditions = filterConditions;
    }

    public String getLeaderTeacherName() {
        return leaderTeacherName;
    }

    public void setLeaderTeacherName(String leaderTeacherName) {
        this.leaderTeacherName = leaderTeacherName;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
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

    public String getGuideTeacherName() {
        return guideTeacherName;
    }

    public void setGuideTeacherName(String guideTeacherName) {
        this.guideTeacherName = guideTeacherName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("gradeId", getGradeId())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("competitionStageId", getCompetitionStageId())
            .append("competitionTrackId", getCompetitionTrackId())
            .append("secondLevelCode", getSecondLevelCode())
            .append("courseId", getCourseId())
            .append("trainingProgramId", getTrainingProgramId())
            .append("memberId", getMemberId())
            .append("userId", getUserId())
            .append("score", getScore())
            .append("ranking", getRanking())
            .append("teamCode", getTeamCode())
            .append("gradeSource", getGradeSource())
            .append("phone", getPhone())
            .append("userName", getUserName())
            .append("idCard", getIdCard())
            .append("email", getEmail())
            .append("leaderTeacherId", getLeaderTeacherId())
            .append("competitionRoleName", getCompetitionRoleName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("version", getVersion())
            .toString();
    }
}
