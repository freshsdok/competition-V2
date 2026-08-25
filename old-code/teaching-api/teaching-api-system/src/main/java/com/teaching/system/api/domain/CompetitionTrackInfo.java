package com.teaching.system.api.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 赛事赛道配置对象 competition_track_info
 *
 * @author teaching
 * @date 2025-11-17
 */
public class CompetitionTrackInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long trackId;

    /** 赛道id */
    private String competitionTrackId;

    /** 赛事id */
    @Excel(name = "赛事id")
    private Long competitionSeriesId;

    /** 赛事名称 */
    private String competitionName;

    /** 赛事界名称 */
    private String competitionSeriesName;

    /** 赛道名称 */
    @Excel(name = "赛道名称")
    private String competitionTrackName;

    // 赛道名称描述
    private String competitionTrackNameDesc;

    /** 赛道设置类型 */
    @Excel(name = "赛道设置类型")
    private String competitionTrackType;

    /** 赛道类型名称 */
    private String competitionTrackTypeName;

    /** 赛道配置列表 */
    private List<CompetitionTrackConfig> competitionTrackConfigList;

    /** 赛道描述 */
    @Excel(name = "赛道描述")
    private String competitionTrackDesc;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 赛事配置审核状态 */
    private String checkStatus;

    /** 申请理由 */
    private String applyReason;

    /** 删除标识 */
    private String delFlag = "0";

    /**
     * 校验包id
     */
    private Long checkPackageId;

    public CompetitionTrackInfo() {
    }

    public CompetitionTrackInfo(Long trackId, String checkStatus) {
        this.trackId = trackId;
        this.checkStatus = checkStatus;
    }

    public Long getCheckPackageId() {
        return checkPackageId;
    }

    public void setCheckPackageId(Long checkPackageId) {
        this.checkPackageId = checkPackageId;
    }

    public void setCompetitionTrackId(String competitionTrackId)
    {
        this.competitionTrackId = competitionTrackId;
    }

    public String getCompetitionTrackId()
    {
        return competitionTrackId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId)
    {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getCompetitionSeriesId()
    {
        return competitionSeriesId;
    }

    public void setCompetitionTrackName(String competitionTrackName)
    {
        this.competitionTrackName = competitionTrackName;
    }

    public String getCompetitionTrackName()
    {
        return competitionTrackName;
    }

    public String getCompetitionTrackType() {
        return competitionTrackType;
    }

    public void setCompetitionTrackType(String competitionTrackType) {
        this.competitionTrackType = competitionTrackType;
    }

    public void setCompetitionTrackDesc(String competitionTrackDesc)
    {
        this.competitionTrackDesc = competitionTrackDesc;
    }

    public String getCompetitionTrackDesc()
    {
        return competitionTrackDesc;
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

    public String getCompetitionTrackNameDesc() {
        return competitionTrackNameDesc;
    }

    public void setCompetitionTrackNameDesc(String competitionTrackNameDesc) {
        this.competitionTrackNameDesc = competitionTrackNameDesc;
    }

    public List<CompetitionTrackConfig> getCompetitionTrackConfigList() {
        return competitionTrackConfigList;
    }

    public void setCompetitionTrackConfigList(List<CompetitionTrackConfig> competitionTrackConfigList) {
        this.competitionTrackConfigList = competitionTrackConfigList;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompetitionSeriesName() {
        return competitionSeriesName;
    }

    public void setCompetitionSeriesName(String competitionSeriesName) {
        this.competitionSeriesName = competitionSeriesName;
    }

    public String getCompetitionTrackTypeName() {
        return competitionTrackTypeName;
    }

    public void setCompetitionTrackTypeName(String competitionTrackTypeName) {
        this.competitionTrackTypeName = competitionTrackTypeName;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("trackId", getTrackId())
            .append("competitionTrackId", getCompetitionTrackId())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("competitionTrackName", getCompetitionTrackName())
            .append("competitionTrackType", getCompetitionTrackType())
            .append("competitionTrackTypeName", getCompetitionTrackTypeName())
            .append("competitionTrackDesc", getCompetitionTrackDesc())
                .append("checkStatus", getCheckStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .toString();
    }
}
