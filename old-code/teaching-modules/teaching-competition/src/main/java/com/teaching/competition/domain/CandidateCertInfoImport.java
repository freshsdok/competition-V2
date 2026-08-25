package com.teaching.competition.domain;

import com.teaching.common.core.annotation.Excel;

/**
 * 候选人信息导入实体类
 *
 * @author teaching
 */
public class CandidateCertInfoImport {

    /** 参赛者姓名 */
    @Excel(name = "参赛者姓名")
    private String userName;

    /** 联系电话 */
//    @Excel(name = "联系电话")
    private String phone;

    /** 邮箱 */
//    @Excel(name = "邮箱")
    private String email;

    /** 身份证号 */
    @Excel(name = "身份证号")
    private String idCard;

    /** 赛事名称 */
    private String competitionName;

    /** 赛道名称 */
    private String competitionTrackName;

    /** 组别名称 */
    private String secondLevelName;

    /** 团队名称 */
    private String teamName;

    /** 参赛角色 */
    private String competitionRoleName;

    /** 带队老师姓名 */
    private String leaderTeacherName;

    /** 指导教师姓名 */
    private String guideTeacherName;

    /** 成绩分数 */
    private String score;

    /** 参赛人来源 */
    private String playerSources;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard != null ? idCard.trim() : null;
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

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getCompetitionRoleName() {
        return competitionRoleName;
    }

    public void setCompetitionRoleName(String competitionRoleName) {
        this.competitionRoleName = competitionRoleName;
    }

    public String getLeaderTeacherName() {
        return leaderTeacherName;
    }

    public void setLeaderTeacherName(String leaderTeacherName) {
        this.leaderTeacherName = leaderTeacherName;
    }

    public String getGuideTeacherName() {
        return guideTeacherName;
    }

    public void setGuideTeacherName(String guideTeacherName) {
        this.guideTeacherName = guideTeacherName;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getPlayerSources() {
        return playerSources;
    }

    public void setPlayerSources(String playerSources) {
        this.playerSources = playerSources;
    }
}
