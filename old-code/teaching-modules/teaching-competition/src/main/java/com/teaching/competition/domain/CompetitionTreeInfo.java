package com.teaching.competition.domain;

import com.teaching.system.api.domain.CompetitionStageConfig;

import java.util.List;

public class CompetitionTreeInfo {

    /**
     * 赛事id
     */
    private Long competitionId;

    /**
     * 赛事名称
     */
    private String competitionName;

    /**
     * 赛事系列id
     */
    private Long competitionSeriesId;

    /**
     * 赛事系列名称
     */
    private String competitionSeriesName;

    /**
     * 赛事级别
     */
    private Integer sort;

    private List<CompetitionStageConfig> competitionStageConfigList;

    private List<CompetitionChildren> competitionChildren;

    public Long getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId) {
        this.competitionId = competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getCompetitionSeriesName() {
        return competitionSeriesName;
    }

    public void setCompetitionSeriesName(String competitionSeriesName) {
        this.competitionSeriesName = competitionSeriesName;
    }

    public List<CompetitionChildren> getCompetitionChildren() {
        return competitionChildren;
    }

    public void setCompetitionChildren(List<CompetitionChildren> competitionChildren) {
        this.competitionChildren = competitionChildren;
    }

    public List<CompetitionStageConfig> getCompetitionStageConfigList() {
        return competitionStageConfigList;
    }

    public void setCompetitionStageConfigList(List<CompetitionStageConfig> competitionStageConfigList) {
        this.competitionStageConfigList = competitionStageConfigList;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
