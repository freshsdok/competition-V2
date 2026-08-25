package com.teaching.system.api.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 赛事赞助企业关联关系对象 competition_enterprise_rela
 * 
 * @author teaching
 * @date 2025-10-11
 */
public class CompetitionEnterpriseRela extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 关联关系id */
    private String relaId;

    /** 赛事系列id(个人参赛) */
    @Excel(name = "赛事系列id(个人参赛)")
    private Long competitionSeriesId;

    /** 赛事赛道配置id */
    private Long competitionTrackConfigId;

    /** 企业id */
    @Excel(name = "企业id")
    private Long enterpriseId;

    /** 企业id */
    @Excel(name = "企业名称")
    private String enterpriseName;

    /** 合作结束时间 */
    @Excel(name = "合作结束时间")
    private String coptEndTime;

    /** 合作开始时间 */
    @Excel(name = "合作开始时间")
    private String coptStartTime;

    /** 赞助金额 */
    @Excel(name = "赞助金额")
    private Long spopAmount;

    /** 合作类型 */
    @Excel(name = "合作类型")
    private String cooperationType;

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

    public String getRelaId() {
        return relaId;
    }

    public void setRelaId(String relaId) {
        this.relaId = relaId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId)
    {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getCompetitionSeriesId() 
    {
        return competitionSeriesId;
    }

    public Long getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(Long enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public void setCoptEndTime(String coptEndTime)
    {
        this.coptEndTime = coptEndTime;
    }

    public String getCoptEndTime() 
    {
        return coptEndTime;
    }

    public void setCoptStartTime(String coptStartTime) 
    {
        this.coptStartTime = coptStartTime;
    }

    public String getCoptStartTime() 
    {
        return coptStartTime;
    }

    public void setSpopAmount(Long spopAmount) 
    {
        this.spopAmount = spopAmount;
    }

    public Long getSpopAmount() 
    {
        return spopAmount;
    }

    public void setCooperationType(String cooperationType) 
    {
        this.cooperationType = cooperationType;
    }

    public String getCooperationType() 
    {
        return cooperationType;
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

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public Long getCompetitionTrackConfigId() {
        return competitionTrackConfigId;
    }

    public void setCompetitionTrackConfigId(Long competitionTrackConfigId) {
        this.competitionTrackConfigId = competitionTrackConfigId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("relaId", getRelaId())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("competitionTrackConfigId", getCompetitionTrackConfigId())
            .append("enterpriseId", getEnterpriseId())
            .append("coptEndTime", getCoptEndTime())
            .append("coptStartTime", getCoptStartTime())
            .append("spopAmount", getSpopAmount())
            .append("cooperationType", getCooperationType())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("userId", getUserId())
            .append("orgId", getOrgId())
            .append("enterpriseName", getEnterpriseName())
            .toString();
    }
}
