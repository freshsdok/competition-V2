package com.teaching.system.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 用户证书原表 user_certificate_origin
 *
 * @author teaching
 */
public class UserCertificateOrigin extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 用户证书id */
    private Long certId;

    /** 证书编号 */
    @Excel(name = "证书编号")
    private String certCode;

    /** 证书名称 */
    @Excel(name = "证书名称")
    private String certName;

    /** 证书链接url */
    private String certUrl;

    /** 发证日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date issuanceDate;

    /** 有效期截至时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date certPeriod;

    /** 获得方式(1大赛获得、2，学习项目  3培训项目  4 证书互通) */
    private String acquireWay;

    /** 赛事系列id */
    private Long competitionSeriesId;

    /** 赛事届别id */
    private String competitionStageId;

    /** 赛道code */
    private String competitionTrackId;

    /** 组别code */
    private String secondLevelCode;

    private String competitionSeriesName;

    private String competitionName;

    private String competitionTrackName;

    private String secondLevelName;

    /** 课程id */
    private Long courseId;

    /** 培训项目id */
    private Long trainingProgramId;

    /** 关联证书互通申请ID */
    private Long certExchangeId;

    /** 证书配置id */
    private String certConfigId;

    /** 状态(0 无效   1有效) */
    private String certStatus;

    /** 团队code */
    private String teamCode;

    /** 奖项名称 */
    @Excel(name = "奖项名称")
    private String awardsName;

    /** 奖项名称描述 */
    private String awardsNameDesc;

    /** 年份 */
    private String year;

    /** 颁发机构 */
    private String orgCode;

    /** 用户id */
    private Long userId;

    /** 参赛人员 */
    @Excel(name = "参赛人员")
    private String player;

    /** 用户姓名 */
    @Excel(name = "用户姓名")
    private String userName;

    /** 身份证号 */
    @Excel(name = "身份证号")
    private String idCard;

    /** 指导教师 */
    @Excel(name = "指导教师")
    private String guideTeacher;

    /** 学校 */
    private String school;

    /** 学校名称 */
    private String schoolName;

    /** 年限 */
    private String yearLimit;

    /** 源证书分值 */
    private String originCertScore;

    /**
     * 源证书拥有年限
     */
    private String ownYear;

    /**
     * 用户证书可申请得状态
     */
    private String applyStatus;

    /**
     * 用户证书可申请得状态描述
     */
    private String applyStatusDes;

    /**
     * 用户是否可选择
     */
    private Boolean isUserSelect = false;

    /** 版本 */
    private Long version;

    private String delFlag;

    public Long getCertId()
    {
        return certId;
    }

    public void setCertId(Long certId)
    {
        this.certId = certId;
    }

    @Size(min = 0, max = 255, message = "证书编号不能超过255个字符")
    public String getCertCode()
    {
        return certCode;
    }

    public void setCertCode(String certCode)
    {
        this.certCode = certCode;
    }

    @Size(min = 0, max = 255, message = "证书名称不能超过255个字符")
    public String getCertName()
    {
        return certName;
    }

    public void setCertName(String certName)
    {
        this.certName = certName;
    }

    public String getCertUrl()
    {
        return certUrl;
    }

    public void setCertUrl(String certUrl)
    {
        this.certUrl = certUrl;
    }

    public Date getIssuanceDate()
    {
        return issuanceDate;
    }

    public void setIssuanceDate(Date issuanceDate)
    {
        this.issuanceDate = issuanceDate;
    }

    public Date getCertPeriod()
    {
        return certPeriod;
    }

    public void setCertPeriod(Date certPeriod)
    {
        this.certPeriod = certPeriod;
    }

    @Size(min = 0, max = 64, message = "获得方式不能超过64个字符")
    public String getAcquireWay()
    {
        return acquireWay;
    }

    public void setAcquireWay(String acquireWay)
    {
        this.acquireWay = acquireWay;
    }

    public Long getCompetitionSeriesId()
    {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId)
    {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getCompetitionStageId() {
        return competitionStageId;
    }

    public void setCompetitionStageId(String competitionStageId) {
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

    public Long getCertExchangeId()
    {
        return certExchangeId;
    }

    public void setCertExchangeId(Long certExchangeId)
    {
        this.certExchangeId = certExchangeId;
    }

    @Size(min = 0, max = 100, message = "证书配置id不能超过100个字符")
    public String getCertConfigId()
    {
        return certConfigId;
    }

    public void setCertConfigId(String certConfigId)
    {
        this.certConfigId = certConfigId;
    }

    @Size(min = 0, max = 32, message = "状态不能超过32个字符")
    public String getCertStatus()
    {
        return certStatus;
    }

    public void setCertStatus(String certStatus)
    {
        this.certStatus = certStatus;
    }

    @Size(min = 0, max = 64, message = "团队code不能超过64个字符")
    public String getTeamCode()
    {
        return teamCode;
    }

    public void setTeamCode(String teamCode)
    {
        this.teamCode = teamCode;
    }

    @Size(min = 0, max = 100, message = "奖项名称不能超过100个字符")
    public String getAwardsName()
    {
        return awardsName;
    }

    public void setAwardsName(String awardsName)
    {
        this.awardsName = awardsName;
    }

    @Size(min = 0, max = 100, message = "奖项名称描述不能超过100个字符")
    public String getAwardsNameDesc()
    {
        return awardsNameDesc;
    }

    public void setAwardsNameDesc(String awardsNameDesc)
    {
        this.awardsNameDesc = awardsNameDesc;
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

    @Size(min = 0, max = 255, message = "颁发机构不能超过255个字符")
    public String getOrgCode()
    {
        return orgCode;
    }

    public void setOrgCode(String orgCode)
    {
        this.orgCode = orgCode;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    @Size(min = 0, max = 100, message = "参赛人员不能超过100个字符")
    public String getPlayer()
    {
        return player;
    }

    public void setPlayer(String player)
    {
        this.player = player;
    }

    @Size(min = 0, max = 100, message = "用户姓名不能超过100个字符")
    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    @Size(min = 0, max = 64, message = "身份证号不能超过64个字符")
    public String getIdCard()
    {
        return idCard;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    @Size(min = 0, max = 100, message = "指导教师不能超过100个字符")
    public String getGuideTeacher()
    {
        return guideTeacher;
    }

    public void setGuideTeacher(String guideTeacher)
    {
        this.guideTeacher = guideTeacher;
    }

    @Size(min = 0, max = 100, message = "学校不能超过100个字符")
    public String getSchool()
    {
        return school;
    }

    public void setSchool(String school)
    {
        this.school = school;
    }

    @Size(min = 0, max = 100, message = "学校名称不能超过100个字符")
    public String getSchoolName()
    {
        return schoolName;
    }

    public void setSchoolName(String schoolName)
    {
        this.schoolName = schoolName;
    }

    @Size(min = 0, max = 100, message = "年限不能超过100个字符")
    public String getYearLimit()
    {
        return yearLimit;
    }

    public void setYearLimit(String yearLimit)
    {
        this.yearLimit = yearLimit;
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

    public String getOriginCertScore() {
        return originCertScore;
    }

    public void setOriginCertScore(String originCertScore) {
        this.originCertScore = originCertScore;
    }

    public String getOwnYear() {
        return ownYear;
    }

    public void setOwnYear(String ownYear) {
        this.ownYear = ownYear;
    }

    public Boolean getUserSelect() {
        return isUserSelect;
    }

    public void setUserSelect(Boolean userSelect) {
        isUserSelect = userSelect;
    }

    public String getApplyStatus() {
        return applyStatus;
    }

    public void setApplyStatus(String applyStatus) {
        this.applyStatus = applyStatus;
    }

    public String getApplyStatusDes() {
        return applyStatusDes;
    }

    public void setApplyStatusDes(String applyStatusDes) {
        this.applyStatusDes = applyStatusDes;
    }

    public String getCompetitionSeriesName() {
        return competitionSeriesName;
    }

    public void setCompetitionSeriesName(String competitionSeriesName) {
        this.competitionSeriesName = competitionSeriesName;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("certId", getCertId())
            .append("certCode", getCertCode())
            .append("certName", getCertName())
            .append("certUrl", getCertUrl())
            .append("issuanceDate", getIssuanceDate())
            .append("certPeriod", getCertPeriod())
            .append("acquireWay", getAcquireWay())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("competitionStageId", getCompetitionStageId())
            .append("competitionTrackId", getCompetitionTrackId())
            .append("secondLevelCode", getSecondLevelCode())
            .append("courseId", getCourseId())
            .append("trainingProgramId", getTrainingProgramId())
            .append("certExchangeId", getCertExchangeId())
            .append("certConfigId", getCertConfigId())
            .append("certStatus", getCertStatus())
            .append("teamCode", getTeamCode())
            .append("awardsName", getAwardsName())
            .append("awardsNameDesc", getAwardsNameDesc())
            .append("year", getYear())
            .append("orgCode", getOrgCode())
            .append("userId", getUserId())
            .append("player", getPlayer())
            .append("userName", getUserName())
            .append("idCard", getIdCard())
            .append("guideTeacher", getGuideTeacher())
            .append("school", getSchool())
            .append("schoolName", getSchoolName())
            .append("yearLimit", getYearLimit())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("version", getVersion())
            .toString();
    }
}
