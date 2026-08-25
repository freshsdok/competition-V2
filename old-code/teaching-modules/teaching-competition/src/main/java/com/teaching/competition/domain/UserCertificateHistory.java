package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 用户证书历史对象 user_certificate_history
 *
 * @author teaching
 * @date 2026-05-13
 */
public class UserCertificateHistory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 用户证书id
     */
    private Long certId;

    /**
     * 证书编号
     */
    @Excel(name = "证书编号")
    private String certCode;

    /**
     * 证书名称
     */
    @Excel(name = "证书名称")
    private String certName;

    /**
     * 证书链接url
     */
    private String certUrl;

    /**
     * 发证日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发证日期", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date issuanceDate;

    /**
     * 有效期截至时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "有效期截至时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date certPeriod;

    /**
     * 获得方式(1大赛获得、2，学习项目  3培训项目  4 证书互通)
     */
    @Excel(name = "获得方式", readConverterExp = "1=大赛获得,2=学习项目,3=培训项目,4=证书互通")
    private String acquireWay;

    /**
     * 赛事系列id
     */
    private Long competitionSeriesId;

    /**
     * 赛事阶段id
     */
    private String competitionStageId;

    /**
     * 赛道code
     */
    private String competitionTrackId;

    /**
     * 组别code
     */
    private String secondLevelCode;

    /**
     * 赛事名称
     */
    @Excel(name = "赛事名称")
    private String competitionName;

    /**
     * 赛道名称
     */
    @Excel(name = "赛道名称")
    private String competitionTrackName;

    /**
     * 组别名称
     */
    @Excel(name = "组别名称")
    private String secondLevelName;

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 培训项目id
     */
    private Long trainingProgramId;

    /**
     * 关联证书互通申请ID
     */
    private Long certExchangeId;

    /**
     * 证书配置id
     */
    private String certConfigId;

    /**
     * 状态(0 无效   1有效)
     */
    @Excel(name = "状态", readConverterExp = "0=无效,1=有效")
    private String certStatus;

    /**
     * 团队code
     */
    private String teamCode;

    /**
     * 奖项名称
     */
    @Excel(name = "奖项名称")
    private String awardsName;

    /**
     * 奖项名称描述
     */
    private String awardsNameDesc;

    /**
     * 年份
     */
    @Excel(name = "年份")
    private String year;

    /**
     * 颁发机构
     */
    @Excel(name = "颁发机构")
    private String orgCode;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 参赛者姓名
     */
    @Excel(name = "参赛者姓名")
    private String userName;

    /**
     * 身份证号
     */
    @Excel(name = "身份证号")
    private String idCard;

    /**
     * 参赛人员
     */
    private String player;

    /**
     * 年限
     */
    private String yearLimit;

    /**
     * 指导教师
     */
    @Excel(name = "指导教师")
    private String guideTeacher;

    /**
     * 学校
     */
    private String school;

    /**
     * 学校名称
     */
    @Excel(name = "学校名称")
    private String schoolName;

    /**
     * 删除标识(0 未删除   1删除)
     */
    private String delFlag;

    /**
     * 版本
     */
    private Long version;

    /**
     * 手机号
     */
    @Excel(name = "手机号")
    private String phone;

    public Long getCertId() {
        return certId;
    }

    public void setCertId(Long certId) {
        this.certId = certId;
    }

    public String getCertCode() {
        return certCode;
    }

    public void setCertCode(String certCode) {
        this.certCode = certCode;
    }

    public String getCertName() {
        return certName;
    }

    public void setCertName(String certName) {
        this.certName = certName;
    }

    public String getCertUrl() {
        return certUrl;
    }

    public void setCertUrl(String certUrl) {
        this.certUrl = certUrl;
    }

    public Date getIssuanceDate() {
        return issuanceDate;
    }

    public void setIssuanceDate(Date issuanceDate) {
        this.issuanceDate = issuanceDate;
    }

    public Date getCertPeriod() {
        return certPeriod;
    }

    public void setCertPeriod(Date certPeriod) {
        this.certPeriod = certPeriod;
    }

    public String getAcquireWay() {
        return acquireWay;
    }

    public void setAcquireWay(String acquireWay) {
        this.acquireWay = acquireWay;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getCompetitionStageId() {
        return competitionStageId;
    }

    public void setCompetitionStageId(String competitionStageId) {
        this.competitionStageId = competitionStageId;
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

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getTrainingProgramId() {
        return trainingProgramId;
    }

    public void setTrainingProgramId(Long trainingProgramId) {
        this.trainingProgramId = trainingProgramId;
    }

    public Long getCertExchangeId() {
        return certExchangeId;
    }

    public void setCertExchangeId(Long certExchangeId) {
        this.certExchangeId = certExchangeId;
    }

    public String getCertConfigId() {
        return certConfigId;
    }

    public void setCertConfigId(String certConfigId) {
        this.certConfigId = certConfigId;
    }

    public String getCertStatus() {
        return certStatus;
    }

    public void setCertStatus(String certStatus) {
        this.certStatus = certStatus;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getAwardsName() {
        return awardsName;
    }

    public void setAwardsName(String awardsName) {
        this.awardsName = awardsName;
    }

    public String getAwardsNameDesc() {
        return awardsNameDesc;
    }

    public void setAwardsNameDesc(String awardsNameDesc) {
        this.awardsNameDesc = awardsNameDesc;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getYearLimit() {
        return yearLimit;
    }

    public void setYearLimit(String yearLimit) {
        this.yearLimit = yearLimit;
    }

    public String getGuideTeacher() {
        return guideTeacher;
    }

    public void setGuideTeacher(String guideTeacher) {
        this.guideTeacher = guideTeacher;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
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
                .append("competitionName", getCompetitionName())
                .append("competitionTrackName", getCompetitionTrackName())
                .append("secondLevelName", getSecondLevelName())
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
                .append("userName", getUserName())
                .append("idCard", getIdCard())
                .append("player", getPlayer())
                .append("yearLimit", getYearLimit())
                .append("guideTeacher", getGuideTeacher())
                .append("school", getSchool())
                .append("schoolName", getSchoolName())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("createBy", getCreateBy())
                .append("updateBy", getUpdateBy())
                .append("delFlag", getDelFlag())
                .append("version", getVersion())
                .append("phone", getPhone())
                .toString();
    }
}
