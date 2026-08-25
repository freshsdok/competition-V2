package com.teaching.competition.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// 定义队伍和选手实体类
public class Team implements Serializable {
    private static final long serialVersionUID = 1L;
    // 队伍名称
    private String teamName;
    // 队伍编号
    private String teamCode;
    // 赛事赛道名称
    private String competitionTrackName;
    // 参赛组别
    private String secondLevelName;
    // 队伍成员
    private List<Player> players = new ArrayList<>();
    // 指导老师
    private List<GuideTeacher> guideTeachers = new ArrayList<>();
    // 返回前端提示语句
    private String massage;

//    public Team(String teamName, String teamCode, String competitionTrackName, String secondLevelName, List<Player> players, List<GuideTeacher> guideTeachers) {
//        this.teamName = teamName;
//        this.teamCode = teamCode;
//        this.competitionTrackName = competitionTrackName;
//        this.secondLevelName = secondLevelName;
//        this.players = players;
//        this.guideTeachers = guideTeachers;
//    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public void addGuideTeacher(GuideTeacher guideTeacher) {
        this.guideTeachers.add(guideTeacher);
    }

    // getters and setters
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }

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

    public List<GuideTeacher> getGuideTeachers() {
        return guideTeachers;
    }

    public void setGuideTeachers(List<GuideTeacher> guideTeachers) {
        this.guideTeachers = guideTeachers;
    }

    public String getMassage() {
        return massage;
    }

    public void setMassage(String massage) {
        this.massage = massage;
    }
}
