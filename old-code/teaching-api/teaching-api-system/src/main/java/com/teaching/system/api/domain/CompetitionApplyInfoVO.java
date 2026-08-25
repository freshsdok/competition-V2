package com.teaching.system.api.domain;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class CompetitionApplyInfoVO {
    /**
     * 单价
     */
    private String fee;
    /**
     * 队员数量 不包含老师
     */
    private int teamSize;
    /**
     * 小计
     */
    private String subtotal;

    /**
     * 团队编号
     */
    private String teamCode;
    /**
     * 团队名称
     */
    private String teamName;
    /**
     * 赛事名称
     */
    private String competitionName;
    /**
     * 赛事系列ID
     */
    private String competitionSeriesId;
    /**
     * 赛事赛道名称
     */
    private String competitionTrackName;
    /**
     * 组别名称
     */
    private String secondLevelName;
    /**
     * 成员子信息
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<RegistrationInfo> registrationInfoList;
    /**
     * 赛事赛道ID
     */
    private String competitionTrackId;
    /**
     * 队员
     */
    private List<RegistrationInfo> playersList;
    /**
     * 执导老师
     */
    private List<RegistrationInfo> instructorList;


    public String getCompetitionTrackId() {
        return competitionTrackId;
    }

    public void setCompetitionTrackId(String competitionTrackId) {
        this.competitionTrackId = competitionTrackId;
    }

    public List<RegistrationInfo> getPlayersList() {
        return playersList;
    }

    public void setPlayersList(List<RegistrationInfo> playersList) {
        this.playersList = playersList;
    }

    public List<RegistrationInfo> getInstructorList() {
        return instructorList;
    }

    public void setInstructorList(List<RegistrationInfo> instructorList) {
        this.instructorList = instructorList;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(String competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
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

    public List<RegistrationInfo> getRegistrationInfoList() {
        return registrationInfoList;
    }

    public void setRegistrationInfoList(List<RegistrationInfo> registrationInfoList) {
        this.registrationInfoList = registrationInfoList;
    }

}
