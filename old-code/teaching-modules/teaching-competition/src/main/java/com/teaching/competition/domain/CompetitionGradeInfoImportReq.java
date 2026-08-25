package com.teaching.competition.domain;

import java.io.Serializable;

public class CompetitionGradeInfoImportReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 赛事系列id(个人参赛) */
    private Long competitionSeriesId;

    /** 赛事赛道id */
    private String competitionTrackId;

    /** 赛事赛道二级id */
    private String secondLevelCode;

    /** 赛事阶段id */
    private String competitionStageId;

    private boolean updateSupport;

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
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

    public boolean isUpdateSupport() {
        return updateSupport;
    }

    public void setUpdateSupport(boolean updateSupport) {
        this.updateSupport = updateSupport;
    }

    public String getCompetitionStageId() {
        return competitionStageId;
    }

    public void setCompetitionStageId(String competitionStageId) {
        this.competitionStageId = competitionStageId;
    }
}
