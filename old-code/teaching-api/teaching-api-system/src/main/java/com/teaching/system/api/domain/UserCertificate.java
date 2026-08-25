package com.teaching.system.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 用户证书表 user_certificate
 *
 * @author teaching
 */
public class UserCertificate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 用户证书id */
    private Long certId;

    /** 证书名称 */
    private String certName;

    /** 证书链接url */
    private String certUrl;

    /** 发证日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
//    @Excel(name = "发证日期", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date issuanceDate;

    /** 有效期截至时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @Excel(name = "有效期截至时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date certPeriod;

    /** 获得方式(1大赛获得、2，学习项目  3培训项目  4 证书互通) */
//    @Excel(name = "获得方式", readConverterExp = "1=大赛获得,2=学习项目,3=培训项目,4=证书互通")
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

    private String schoolId;

    @Excel(name = "参赛单位")
    private String schoolName;

    /** 赛事名称 */
    @Excel(name = "赛事名称")
    private String competitionName;

    /** 赛道名称 */
    @Excel(name = "赛道名称")
    private String competitionTrackName;

    /** 组别名称 */
    @Excel(name = "组别名称")
    private String secondLevelName;

    @Excel(name = "阶段")
    private String competitionStageName;

    /** 课程id */
    private Long courseId;

    /** 培训项目id */
    private Long trainingProgramId;

    /** 关联证书互通申请ID */
    private Long certExchangeId;

    /** 状态(0 无效   1有效) */
//    @Excel(name = "状态", readConverterExp = "0=无效,1=有效")
    private String certStatus;

    /** 团队code */
    private String teamCode;

    private String teamName;

    /** 参赛选手名称 */
    @Excel(name = "参赛选手")
    private String player;

    /** 导师名称 */
    @Excel(name = "指导教师")
    private String guideTeacher;

    /** 奖项名称 */
    @Excel(name = "奖项名称")
    private String awardsName;

    /** 颁发机构 */
//    @Excel(name = "颁发机构")
    private String orgCode;

    /** 颁发机构名称 */
    private String orgName;

    /** 证书编号 */
    @Excel(name = "证书编号")
    private String certCode;

    /** 年份 */
    private String year;

    /** 证书配置id */
    private Long certConfigId;

    /** 用户id */
    private Long userId;

    /** 身份证号 */
    private String idCard;

    /** 用户名 */
    private String userName;

    /** 成员id */
    private Long memberId;

    /** 目标证书分值 */
    private String targetCertScore;

    /**
     * 用户证书可申请得状态
     */
    private String applyStatus;

    /**
     * 用户证书可申请得状态描述
     */
    private String applyStatusDes;

    /**
     * 金额
     */
    private Integer certAmount;

    /**
     * 颁发开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date issuanceStartTime;

    /**
     * 颁发结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date issuanceEndTime;

    // 关键词
    private String keyWords;

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

    @Size(min = 0, max = 255, message = "颁发机构不能超过255个字符")
    public String getOrgCode()
    {
        return orgCode;
    }

    public void setOrgCode(String orgCode)
    {
        this.orgCode = orgCode;
    }

    public String getYear()
    {
        return year;
    }

    public void setYear(String year)
    {
        this.year = year;
    }

    public Long getCertConfigId()
    {
        return certConfigId;
    }

    public void setCertConfigId(Long certConfigId)
    {
        this.certConfigId = certConfigId;
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

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getGuideTeacher() {
        return guideTeacher;
    }

    public void setGuideTeacher(String guideTeacher) {
        this.guideTeacher = guideTeacher;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCompetitionSeriesName() {
        return competitionSeriesName;
    }

    public void setCompetitionSeriesName(String competitionSeriesName) {
        this.competitionSeriesName = competitionSeriesName;
    }

    public String getCompetitionStageName() {
        return competitionStageName;
    }

    public void setCompetitionStageName(String competitionStageName) {
        this.competitionStageName = competitionStageName;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Size(min = 0, max = 100, message = "身份证号不能超过100个字符")
    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    @Size(min = 0, max = 100, message = "用户名不能超过100个字符")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getTargetCertScore() {
        return targetCertScore;
    }

    public void setTargetCertScore(String targetCertScore) {
        this.targetCertScore = targetCertScore;
    }

    public Integer getCertAmount() {
        return certAmount;
    }

    public void setCertAmount(Integer certAmount) {
        this.certAmount = certAmount;
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

    public Date getIssuanceStartTime() {
        return issuanceStartTime;
    }

    public void setIssuanceStartTime(Date issuanceStartTime) {
        this.issuanceStartTime = issuanceStartTime;
    }

    public Date getIssuanceEndTime() {
        return issuanceEndTime;
    }

    public void setIssuanceEndTime(Date issuanceEndTime) {
        this.issuanceEndTime = issuanceEndTime;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
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
            .append("certStatus", getCertStatus())
            .append("teamCode", getTeamCode())
            .append("awardsName", getAwardsName())
            .append("orgCode", getOrgCode())
            .append("year", getYear())
            .append("certConfigId", getCertConfigId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("version", getVersion())
                .append("competitionName", getCompetitionName())
                .append("competitionTrackName", getCompetitionTrackName())
                .append("secondLevelName", getSecondLevelName())
                .append("teamName", getTeamName())
                .append("player", getPlayer())
                .append("guideTeacher", getGuideTeacher())
                .append("schoolId", getSchoolId())
            .append("schoolName", getSchoolName())
            .append("idCard", getIdCard())
            .append("userName", getUserName())
            .toString();
    }
}
