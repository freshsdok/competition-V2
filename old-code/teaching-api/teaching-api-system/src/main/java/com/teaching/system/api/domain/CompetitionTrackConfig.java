package com.teaching.system.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.List;

/**
 * 赛道配置对象 competition_track_config
 *
 * @author teaching
 * @date 2025-12-01
 */
public class CompetitionTrackConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 赛道配置id */
    private Long competitionTrackConfigId;

    /** 赛道code */
    @Excel(name = "赛道code")
    private String competitionTrackId;

    /** 赛道类型 */
    private String competitionTrackType;

    /** 二级编码 */
    private String secondLevelCode;

    /** 二级编码名称 */
    private String secondLevelName;

    /** 赛事赛道配置信息 */
    private CompetitionConfig competitionConfig;

    /** 赛道赞助企业配置信息 */
    private List<CompetitionEnterpriseRela> competitionTrackEnterpriseList;

    // 审核状态
    private String checkStatus;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag = "0";

    /** 赛事id */
    private Long competitionSeriesId;

    /**
     * 校验包id
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long checkPackageId;

    public Long getCheckPackageId() {
        return checkPackageId;
    }

    public void setCheckPackageId(Long checkPackageId) {
        this.checkPackageId = checkPackageId;
    }

    public void setCompetitionTrackConfigId(Long competitionTrackConfigId)
    {
        this.competitionTrackConfigId = competitionTrackConfigId;
    }

    public Long getCompetitionTrackConfigId()
    {
        return competitionTrackConfigId;
    }

    public void setCompetitionTrackId(String competitionTrackId)
    {
        this.competitionTrackId = competitionTrackId;
    }

    public String getCompetitionTrackId()
    {
        return competitionTrackId;
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

    public CompetitionConfig getCompetitionConfig() {
        return competitionConfig;
    }

    public void setCompetitionConfig(CompetitionConfig competitionConfig) {
        this.competitionConfig = competitionConfig;
    }

    public List<CompetitionEnterpriseRela> getCompetitionTrackEnterpriseList() {
        return competitionTrackEnterpriseList;
    }

    public void setCompetitionTrackEnterpriseList(List<CompetitionEnterpriseRela> competitionTrackEnterpriseList) {
        this.competitionTrackEnterpriseList = competitionTrackEnterpriseList;
    }

    public String getCompetitionTrackType() {
        return competitionTrackType;
    }

    public void setCompetitionTrackType(String competitionTrackType) {
        this.competitionTrackType = competitionTrackType;
    }

    public String getSecondLevelCode() {
        return secondLevelCode;
    }

    public void setSecondLevelCode(String secondLevelCode) {
        this.secondLevelCode = secondLevelCode;
    }

    public String getSecondLevelName() {
        return secondLevelName;
    }

    public void setSecondLevelName(String secondLevelName) {
        this.secondLevelName = secondLevelName;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("competitionTrackConfigId", getCompetitionTrackConfigId())
            .append("competitionTrackId", getCompetitionTrackId())
                .append("competitionTrackType", getCompetitionTrackType())
                .append("secondLevelCode", getSecondLevelCode())
                .append("secondLevelName", getSecondLevelName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
