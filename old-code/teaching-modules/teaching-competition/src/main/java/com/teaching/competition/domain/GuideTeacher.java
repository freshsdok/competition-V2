package com.teaching.competition.domain;

import java.io.Serializable;

public class GuideTeacher implements Serializable {

    private static final long serialVersionUID = 1L;

    // 姓名
    private String userName;

    //  联系电话
    private String phone;
    // 邮箱
    private String email;

    // 教师名称
    private String guideTeacherName;

    // 教师手机号
    private String guideTeacherPhone;

    // 教师邮箱
    private String guideTeacherEmail;

    // 角色
    private String competitionRoleName;

    // 团队排序
    private Integer teamSort;

    public String getGuideTeacherName() {
        return guideTeacherName;
    }

    public void setGuideTeacherName(String guideTeacherName) {
        this.guideTeacherName = guideTeacherName;
    }

    public String getGuideTeacherPhone() {
        return guideTeacherPhone;
    }

    public void setGuideTeacherPhone(String guideTeacherPhone) {
        this.guideTeacherPhone = guideTeacherPhone;
    }

    public String getGuideTeacherEmail() {
        return guideTeacherEmail;
    }

    public void setGuideTeacherEmail(String guideTeacherEmail) {
        this.guideTeacherEmail = guideTeacherEmail;
    }

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

    public String getCompetitionRoleName() {
        return competitionRoleName;
    }

    public void setCompetitionRoleName(String competitionRoleName) {
        this.competitionRoleName = competitionRoleName;
    }

    public Integer getTeamSort() {
        return teamSort;
    }

    public void setTeamSort(Integer teamSort) {
        this.teamSort = teamSort;
    }
}
