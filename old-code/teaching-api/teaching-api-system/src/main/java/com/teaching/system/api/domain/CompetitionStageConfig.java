package com.teaching.system.api.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 赛事阶段配置对象 competition_stage_config
 * 
 * @author teaching
 * @date 2025-10-11
 */
public class CompetitionStageConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 赛事阶段id */
    private String stageId;

    /** 赛事系列id */
    @Excel(name = "赛事系列id")
    private Long competitionSeriesId;

    /** 阶段名称 */
    @Excel(name = "阶段名称")
    private String stageName;

    /** 评分方式 */
    @Excel(name = "评分方式")
    private String scoreWay;

    /** 晋级人数团队数 */
    @Excel(name = "晋级人数团队数")
    private String promoteNum;

    /** 晋级分数 */
    @Excel(name = "晋级分数")
    private String promoteScore;

    /** 阶段描述 */
    @Excel(name = "阶段描述")
    private String stageDesc;

    /** 阶段开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "阶段开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date stageStartTime;

    /** 阶段结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "阶段结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date stageEndTime;

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

    private Integer sort = 2;

    public String getStageId() {
        return stageId;
    }

    public void setStageId(String stageId) {
        this.stageId = stageId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId)
    {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getCompetitionSeriesId() 
    {
        return competitionSeriesId;
    }

    public void setStageName(String stageName) 
    {
        this.stageName = stageName;
    }

    public String getStageName() 
    {
        return stageName;
    }

    public void setScoreWay(String scoreWay) 
    {
        this.scoreWay = scoreWay;
    }

    public String getScoreWay() 
    {
        return scoreWay;
    }

    public void setPromoteNum(String promoteNum) 
    {
        this.promoteNum = promoteNum;
    }

    public String getPromoteNum() 
    {
        return promoteNum;
    }

    public void setPromoteScore(String promoteScore) 
    {
        this.promoteScore = promoteScore;
    }

    public String getPromoteScore() 
    {
        return promoteScore;
    }

    public void setStageDesc(String stageDesc) 
    {
        this.stageDesc = stageDesc;
    }

    public String getStageDesc() 
    {
        return stageDesc;
    }

    public void setStageStartTime(Date stageStartTime) 
    {
        this.stageStartTime = stageStartTime;
    }

    public Date getStageStartTime() 
    {
        return stageStartTime;
    }

    public void setStageEndTime(Date stageEndTime) 
    {
        this.stageEndTime = stageEndTime;
    }

    public Date getStageEndTime() 
    {
        return stageEndTime;
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

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("stageId", getStageId())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("stageName", getStageName())
            .append("scoreWay", getScoreWay())
            .append("promoteNum", getPromoteNum())
            .append("promoteScore", getPromoteScore())
            .append("stageDesc", getStageDesc())
            .append("stageStartTime", getStageStartTime())
            .append("stageEndTime", getStageEndTime())
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
