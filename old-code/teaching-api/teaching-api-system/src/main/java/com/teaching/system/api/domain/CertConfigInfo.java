package com.teaching.system.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 证书配置表 cert_config_info
 *
 * @author teaching
 */
public class CertConfigInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 证书配置id */
    private Long certConfigId;

    /** 证书配置名称 */
    @Excel(name = "证书配置名称")
    @NotBlank(message = "证书配置名称不能为空")
    private String certConfigName;

    /** 证书来源(1 系统内  2系统外) */
    @Excel(name = "证书来源", readConverterExp = "1=系统内,2=系统外")
    private String certSource;

    /** 赛事系列id */
    @Excel(name = "赛事系列id")
    private Long competitionSeriesId;

    /** 赛事届别id */
    @Excel(name = "赛事届别id")
    private String competitionStageId;

    /** 赛道code */
    @Excel(name = "赛道code")
    private String competitionTrackId;

    /** 组别code */
    @Excel(name = "组别code")
    private String secondLevelCode;

    /** 课程id */
    private Long courseId;

    /** 培训项目id */
    private Long trainingProgramId;

    /** 证书有效期类型(默认1 -永久  2  指定日期) */
    @Excel(name = "有效期类型", readConverterExp = "1=永久,2=指定日期")
    private String certPeriodType;

    /** 证书有效期截止日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "有效期截止日期", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date certPeriodTime;

    /** 证书管理员角色 */
    @Excel(name = "证书管理员角色")
    private String certManagerRole;

    private String certManagerRoleName;

    /** 证书状态(0生效  1过期) */
    @Excel(name = "证书状态", readConverterExp = "0=生效,1=过期")
    private String certStatus;

    /** 外部链接名称 */
    @Excel(name = "外部链接名称")
    private String certLinkName;

    /** 外部链接URL */
    @Excel(name = "外部链接URL")
    private String certLinkUrl;

    /** 颁发机构code */
    private String orgCode;

    @Excel(name = "颁发机构")
    private String orgCodeName;

    /** 奖项名称 */
    @Excel(name = "奖项名称")
    private String awardsName;

    /** 是否关联大赛 */
    private Boolean isCompetition;

    /** 是否关联课程 */
    private Boolean isCourse;

    /** 是否关联培训项目 */
    private Boolean isTrainingProgram;

    /** 赛事系列名称 */
    private String competitionSeriesName;

    /** 赛事名称 */
    private String competitionName;
    /** 赛事id */
    private String competitionId;

    /** 赛道名称 */
    private String competitionTrackName;

    /** 组别名称 */
    private String secondLevelName;

    /** 源证书分值 */
    private String originCertScore;

    /**
     * 源证书年份
     */
    private String year;

    /**
     * 源证书拥有年限
     */
    private String ownYear;

    /** 目标证书分值 */
    private String targetCertScore;

    /**
     * 是否可选源证书
     */
    private Boolean isUserSelect = false;

    /**
     * 用户证书可申请得状态
     */
    private String applyStatus;

    /**
     * 用户证书可申请得状态描述
     */
    private String applyStatusDes;

    /** 版本 */
    private Long version;

    private String delFlag;

    /**
     * 金额
     */
    private BigDecimal certAmount;

    private String competitionStageName;

    public Long getCertConfigId()
    {
        return certConfigId;
    }

    public void setCertConfigId(Long certConfigId)
    {
        this.certConfigId = certConfigId;
    }

    @NotBlank(message = "证书配置名称不能为空")
    @Size(min = 0, max = 255, message = "证书配置名称不能超过255个字符")
    public String getCertConfigName()
    {
        return certConfigName;
    }

    public void setCertConfigName(String certConfigName)
    {
        this.certConfigName = certConfigName;
    }

    public String getCertSource()
    {
        return certSource;
    }

    public void setCertSource(String certSource)
    {
        this.certSource = certSource;
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

    public String getCompetitionTrackId()
    {
        return competitionTrackId;
    }

    public void setCompetitionTrackId(String competitionTrackId)
    {
        this.competitionTrackId = competitionTrackId;
    }

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

    public String getCertPeriodType()
    {
        return certPeriodType;
    }

    public void setCertPeriodType(String certPeriodType)
    {
        this.certPeriodType = certPeriodType;
    }

    public Date getCertPeriodTime()
    {
        return certPeriodTime;
    }

    public void setCertPeriodTime(Date certPeriodTime)
    {
        this.certPeriodTime = certPeriodTime;
    }

    public String getCertManagerRole()
    {
        return certManagerRole;
    }

    public void setCertManagerRole(String certManagerRole)
    {
        this.certManagerRole = certManagerRole;
    }

    public String getCertStatus()
    {
        return certStatus;
    }

    public BigDecimal getCertAmount() {
        return certAmount;
    }

    public void setCertAmount(BigDecimal certAmount) {
        this.certAmount = certAmount;
    }

    public void setCertStatus(String certStatus)
    {
        this.certStatus = certStatus;
    }

    public String getCertLinkName()
    {
        return certLinkName;
    }

    public void setCertLinkName(String certLinkName)
    {
        this.certLinkName = certLinkName;
    }

    public String getCertLinkUrl()
    {
        return certLinkUrl;
    }

    public void setCertLinkUrl(String certLinkUrl)
    {
        this.certLinkUrl = certLinkUrl;
    }

    public String getOrgCode()
    {
        return orgCode;
    }

    public void setOrgCode(String orgCode)
    {
        this.orgCode = orgCode;
    }

    public String getAwardsName()
    {
        return awardsName;
    }

    public void setAwardsName(String awardsName)
    {
        this.awardsName = awardsName;
    }

    public Boolean getIsCompetition()
    {
        return isCompetition;
    }

    public void setIsCompetition(Boolean isCompetition)
    {
        this.isCompetition = isCompetition;
    }

    public Boolean getIsCourse()
    {
        return isCourse;
    }

    public void setIsCourse(Boolean isCourse)
    {
        this.isCourse = isCourse;
    }

    public Boolean getIsTrainingProgram()
    {
        return isTrainingProgram;
    }

    public void setIsTrainingProgram(Boolean isTrainingProgram)
    {
        this.isTrainingProgram = isTrainingProgram;
    }

    public String getCompetitionSeriesName()
    {
        return competitionSeriesName;
    }

    public void setCompetitionSeriesName(String competitionSeriesName)
    {
        this.competitionSeriesName = competitionSeriesName;
    }

    public String getCompetitionName()
    {
        return competitionName;
    }

    public void setCompetitionName(String competitionName)
    {
        this.competitionName = competitionName;
    }

    public String getCompetitionTrackName()
    {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName)
    {
        this.competitionTrackName = competitionTrackName;
    }

    public String getSecondLevelName()
    {
        return secondLevelName;
    }

    public void setSecondLevelName(String secondLevelName)
    {
        this.secondLevelName = secondLevelName;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public Boolean getCompetition() {
        return isCompetition;
    }

    public void setCompetition(Boolean competition) {
        isCompetition = competition;
    }

    public Boolean getCourse() {
        return isCourse;
    }

    public void setCourse(Boolean course) {
        isCourse = course;
    }

    public Boolean getTrainingProgram() {
        return isTrainingProgram;
    }

    public void setTrainingProgram(Boolean trainingProgram) {
        isTrainingProgram = trainingProgram;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getCertManagerRoleName() {
        return certManagerRoleName;
    }

    public void setCertManagerRoleName(String certManagerRoleName) {
        this.certManagerRoleName = certManagerRoleName;
    }

    public String getOrgCodeName() {
        return orgCodeName;
    }

    public void setOrgCodeName(String orgCodeName) {
        this.orgCodeName = orgCodeName;
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

    public String getTargetCertScore() {
        return targetCertScore;
    }

    public void setTargetCertScore(String targetCertScore) {
        this.targetCertScore = targetCertScore;
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

    public Boolean getUserSelect() {
        return isUserSelect;
    }

    public void setUserSelect(Boolean userSelect) {
        isUserSelect = userSelect;
    }

    public String getCompetitionStageName() {
        return competitionStageName;
    }

    public void setCompetitionStageName(String competitionStageName) {
        this.competitionStageName = competitionStageName;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(String competitionId) {
        this.competitionId = competitionId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("certConfigId", getCertConfigId())
            .append("certConfigName", getCertConfigName())
            .append("certSource", getCertSource())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("competitionStageId", getCompetitionStageId())
            .append("competitionTrackId", getCompetitionTrackId())
            .append("secondLevelCode", getSecondLevelCode())
            .append("courseId", getCourseId())
            .append("trainingProgramId", getTrainingProgramId())
            .append("certPeriodType", getCertPeriodType())
            .append("certPeriodTime", getCertPeriodTime())
            .append("certManagerRole", getCertManagerRole())
            .append("certStatus", getCertStatus())
            .append("certLinkName", getCertLinkName())
            .append("certLinkUrl", getCertLinkUrl())
            .append("orgCode", getOrgCode())
            .append("awardsName", getAwardsName())
            .append("isCompetition", getIsCompetition())
            .append("isCourse", getIsCourse())
            .append("isTrainingProgram", getIsTrainingProgram())
            .append("competitionSeriesName", getCompetitionSeriesName())
            .append("competitionName", getCompetitionName())
            .append("competitionTrackName", getCompetitionTrackName())
            .append("secondLevelName", getSecondLevelName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("version", getVersion())
            .toString();
    }
}
