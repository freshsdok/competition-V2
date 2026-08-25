package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

/**
 * 赛事晋级对象 competition_promoted_info
 *
 * @author teaching
 * @date 2026-05-19
 */
public class CompetitionPromotedInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 赛事晋级主键id
     */
    private Long promotedId;

    /**
     * 赛事系列id
     */
    @Excel(name = "赛事系列id")
    private Long competitionSeriesId;

    // 赛事名称
    private String competitionName;

    // 赛事届数名称
    private String competitionSeriesName;
    /**
     * 领队老师id
     */
    private Long leaderTeacherId;

    /**
     * 报名开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名开始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date applyStartTime;

    /**
     * 报名结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名结束时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date applyEndTime;

    /**
     * 赛事晋级提示语
     */
    @Excel(name = "赛事晋级提示语")
    private String promotedHint;

    /**
     * 晋级赛报名费用
     */
    @Excel(name = "晋级赛报名费用")
    private String fee;

    /**
     * 版本
     */
    @Excel(name = "版本")
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 赛事报名状态 未开始/报名中/已结束
     */
    private String competitionApplyStatus;

    // 晋级队伍数量
    private Integer teamNum;

    // 已报名的队伍数量
    private Integer applyTeamNum;


    private List<String> teamCodes;


    public Long getLeaderTeacherId() {
        return leaderTeacherId;
    }

    public void setLeaderTeacherId(Long leaderTeacherId) {
        this.leaderTeacherId = leaderTeacherId;
    }

    public void setPromotedId(Long promotedId) {
        this.promotedId = promotedId;
    }

    public Long getPromotedId() {
        return promotedId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setApplyStartTime(Date applyStartTime) {
        this.applyStartTime = applyStartTime;
    }

    public Date getApplyStartTime() {
        return applyStartTime;
    }

    public void setApplyEndTime(Date applyEndTime) {
        this.applyEndTime = applyEndTime;
    }

    public Date getApplyEndTime() {
        return applyEndTime;
    }

    public void setPromotedHint(String promotedHint) {
        this.promotedHint = promotedHint;
    }

    public String getPromotedHint() {
        return promotedHint;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getFee() {
        return fee;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getVersion() {
        return version;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public String getCompetitionApplyStatus() {
        return competitionApplyStatus;
    }

    public void setCompetitionApplyStatus(String competitionApplyStatus) {
        this.competitionApplyStatus = competitionApplyStatus;
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

    public Integer getTeamNum() {
        return teamNum;
    }

    public void setTeamNum(Integer teamNum) {
        this.teamNum = teamNum;
    }

    public Integer getApplyTeamNum() {
        return applyTeamNum;
    }

    public void setApplyTeamNum(Integer applyTeamNum) {
        this.applyTeamNum = applyTeamNum;
    }

    public List<String> getTeamCodes() {
        return teamCodes;
    }

    public void setTeamCodes(List<String> teamCodes) {
        this.teamCodes = teamCodes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("promotedId", getPromotedId())
                .append("competitionSeriesId", getCompetitionSeriesId())
                .append("applyStartTime", getApplyStartTime())
                .append("applyEndTime", getApplyEndTime())
                .append("promotedHint", getPromotedHint())
                .append("fee", getFee())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
