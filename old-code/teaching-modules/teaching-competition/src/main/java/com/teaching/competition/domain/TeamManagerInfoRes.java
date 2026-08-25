package com.teaching.competition.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.annotation.Excels;
import com.teaching.system.api.domain.CompetitionApplyInfo;

import java.io.Serializable;
import java.util.List;

public class TeamManagerInfoRes implements Serializable {

    @Excel(name = "队伍ID")
    private String teamCode;

    @Excel(name = "赛道")
    private String competitionTrackName;

    @Excel(name = "组别/赛项")
    private String secondLevelName;

    @Excel(name = "队名")
    private String teamName;

    /** 带队老师姓名 */
    @Excel(name = "带队教师")
    private String leaderTeacherName;

//    @Excels({
//            @Excel(name = "姓名", targetAttr = "userName", type = Excel.Type.EXPORT),
//            @Excel(name = "性别", targetAttr = "sex", type = Excel.Type.EXPORT),
//            @Excel(name = "手机号", targetAttr = "phone", type = Excel.Type.EXPORT),
//            @Excel(name = "邮箱", targetAttr = "email", type = Excel.Type.EXPORT),
//            @Excel(name = "身份证", targetAttr = "idCard", type = Excel.Type.EXPORT),
//            @Excel(name = "学校", targetAttr = "schoolName", type = Excel.Type.EXPORT),
//            @Excel(name = "角色", targetAttr = "competitionRoleName", type = Excel.Type.EXPORT)
//    })
    private List<CompetitionApplyInfo> competitionApplyInfoList;

    private String registrationTime;

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
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

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(String registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getLeaderTeacherName() {
        return leaderTeacherName;
    }

    public void setLeaderTeacherName(String leaderTeacherName) {
        this.leaderTeacherName = leaderTeacherName;
    }

    public List<CompetitionApplyInfo> getCompetitionApplyInfoList() {
        return competitionApplyInfoList;
    }

    public void setCompetitionApplyInfoList(List<CompetitionApplyInfo> competitionApplyInfoList) {
        this.competitionApplyInfoList = competitionApplyInfoList;
    }
}
