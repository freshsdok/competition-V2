package com.teaching.system.api.domain;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 赛事配置对象 competition_config
 * 
 * @author teaching
 * @date 2025-10-13
 */
public class CompetitionConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 赛事配置d */
    private Long configId;

    /** 赛事赛道配置id */
    private Long competitionTrackConfigId;

    /** 参赛方式 */
    @Excel(name = "参赛方式")
    private String joinType;

    private String joinTypeCn;

    /** 是否必须指导老师 */
    @Excel(name = "是否必须指导老师")
    private String isTeacherNess;

    /** 最小组队人数 */
    @Excel(name = "最小组队人数")
    private String minPernNum;

    /** 最大组队人数 */
    @Excel(name = "最大组队人数")
    private String maxPernNum;

    /** 最小指导老师人数 */
    @Excel(name = "最小指导老师人数")
    private String minTeacherNum;

    /** 最大指导老师人数 */
    @Excel(name = "最大指导老师人数")
    private String maxTeacherNum;

    /** 组队规则 */
    private String teamRule;

    /** 是否实名认证 */
    @Excel(name = "是否实名认证")
    private String isRealNameAuth;

    /** 是否必须是学生 */
    @Excel(name = "是否必须是学生")
    private String isStudent;

    /** 是否是国际学生 */
    private String isNationalityStudent;

    /** 年级要求 */
    @Excel(name = "年级要求")
    private String classRequest;

    /** 专业要求 */
    @Excel(name = "专业要求")
    private String professionRequest;

    /** 最低gpa */
    @Excel(name = "最低gpa")
    private String lowestGpa;

    /** 报名开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date applyStartTime;

    /** 报名结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date applyEndTime;

    /** 作品提交方式 */
    @Excel(name = "作品提交方式")
    private String worksSubmitWay;

    /** 作品格式 */
    @Excel(name = "作品格式")
    private String worksFormat;

    /** 作品格式大小 */
    @Excel(name = "作品格式大小")
    private String worksFormatSize;

    /** 作品提交截至日 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "作品提交截至日", width = 30, dateFormat = "yyyy-MM-dd")
    private Date worksSubmitDate;

    /** 作品提交说明 */
    @Excel(name = "作品提交说明")
    private String worksSubmitExplain;

    /** 报名费用 */
    private String fee;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag = "0";

    /** 数据权限用户id */
    @Excel(name = "数据权限用户id")
    private Long userId;

    /** 数据权限机构id */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    public void setConfigId(Long configId) 
    {
        this.configId = configId;
    }

    public Long getConfigId() 
    {
        return configId;
    }

    public Long getCompetitionTrackConfigId() {
        return competitionTrackConfigId;
    }

    public void setCompetitionTrackConfigId(Long competitionTrackConfigId) {
        this.competitionTrackConfigId = competitionTrackConfigId;
    }

    public void setJoinType(String joinType) 
    {
        this.joinType = joinType;
    }

    public String getJoinType() 
    {
        return joinType;
    }

    public void setIsTeacherNess(String isTeacherNess) 
    {
        this.isTeacherNess = isTeacherNess;
    }

    public String getIsTeacherNess() 
    {
        return isTeacherNess;
    }

    public void setMinPernNum(String minPernNum) 
    {
        this.minPernNum = minPernNum;
    }

    public String getMinPernNum() 
    {
        return minPernNum;
    }

    public void setMaxPernNum(String maxPernNum) 
    {
        this.maxPernNum = maxPernNum;
    }

    public String getMaxPernNum() 
    {
        return maxPernNum;
    }

    public void setMinTeacherNum(String minTeacherNum) 
    {
        this.minTeacherNum = minTeacherNum;
    }

    public String getMinTeacherNum() 
    {
        return minTeacherNum;
    }

    public void setMaxTeacherNum(String maxTeacherNum) 
    {
        this.maxTeacherNum = maxTeacherNum;
    }

    public String getMaxTeacherNum() 
    {
        return maxTeacherNum;
    }

    public void setTeamRule(String teamRule) 
    {
        this.teamRule = teamRule;
    }

    public String getTeamRule() 
    {
        return teamRule;
    }

    public String getIsRealNameAuth() {
        return isRealNameAuth;
    }

    public void setIsRealNameAuth(String isRealNameAuth) {
        this.isRealNameAuth = isRealNameAuth;
    }

    public String getIsStudent() {
        return isStudent;
    }

    public void setIsStudent(String isStudent) {
        this.isStudent = isStudent;
    }

    public void setClassRequest(String classRequest)
    {
        this.classRequest = classRequest;
    }

    public String getClassRequest() 
    {
        return classRequest;
    }

    public void setProfessionRequest(String professionRequest) 
    {
        this.professionRequest = professionRequest;
    }

    public String getProfessionRequest() 
    {
        return professionRequest;
    }

    public void setLowestGpa(String lowestGpa) 
    {
        this.lowestGpa = lowestGpa;
    }

    public String getLowestGpa() 
    {
        return lowestGpa;
    }

    public void setApplyStartTime(Date applyStartTime) 
    {
        this.applyStartTime = applyStartTime;
    }

    public Date getApplyStartTime() 
    {
        return applyStartTime;
    }

    public void setApplyEndTime(Date applyEndTime) 
    {
        this.applyEndTime = applyEndTime;
    }

    public Date getApplyEndTime() 
    {
        return applyEndTime;
    }

    public void setWorksSubmitWay(String worksSubmitWay) 
    {
        this.worksSubmitWay = worksSubmitWay;
    }

    public String getWorksSubmitWay() 
    {
        return worksSubmitWay;
    }

    public void setWorksFormat(String worksFormat) 
    {
        this.worksFormat = worksFormat;
    }

    public String getWorksFormat() 
    {
        return worksFormat;
    }

    public void setWorksFormatSize(String worksFormatSize) 
    {
        this.worksFormatSize = worksFormatSize;
    }

    public String getWorksFormatSize() 
    {
        return worksFormatSize;
    }

    public void setWorksSubmitDate(Date worksSubmitDate) 
    {
        this.worksSubmitDate = worksSubmitDate;
    }

    public Date getWorksSubmitDate() 
    {
        return worksSubmitDate;
    }

    public void setWorksSubmitExplain(String worksSubmitExplain) 
    {
        this.worksSubmitExplain = worksSubmitExplain;
    }

    public String getWorksSubmitExplain() 
    {
        return worksSubmitExplain;
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

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setOrgId(Long orgId) 
    {
        this.orgId = orgId;
    }

    public Long getOrgId() 
    {
        return orgId;
    }

    public String getIsNationalityStudent() {
        return isNationalityStudent;
    }

    public void setIsNationalityStudent(String isNationalityStudent) {
        this.isNationalityStudent = isNationalityStudent;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getJoinTypeCn() {
        return joinTypeCn;
    }

    public void setJoinTypeCn(String joinTypeCn) {
        this.joinTypeCn = joinTypeCn;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("competitionTrackConfigId", getCompetitionTrackConfigId())
            .append("joinType", getJoinType())
            .append("isTeacherNess", getIsTeacherNess())
            .append("minPernNum", getMinPernNum())
            .append("maxPernNum", getMaxPernNum())
            .append("minTeacherNum", getMinTeacherNum())
            .append("maxTeacherNum", getMaxTeacherNum())
            .append("teamRule", getTeamRule())
            .append("isRealNameAuth", getIsRealNameAuth())
            .append("isStudent", getIsStudent())
                .append("isNationalityStudent", getIsNationalityStudent())
            .append("classRequest", getClassRequest())
            .append("professionRequest", getProfessionRequest())
            .append("lowestGpa", getLowestGpa())
            .append("applyStartTime", getApplyStartTime())
            .append("applyEndTime", getApplyEndTime())
            .append("worksSubmitWay", getWorksSubmitWay())
            .append("worksFormat", getWorksFormat())
            .append("worksFormatSize", getWorksFormatSize())
            .append("worksSubmitDate", getWorksSubmitDate())
            .append("worksSubmitExplain", getWorksSubmitExplain())
                .append("fee", getFee())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("userId", getUserId())
            .append("orgId", getOrgId())
            .toString();
    }
}
