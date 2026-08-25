package com.teaching.competition.domain;

import java.io.Serializable;

public class AwardPlayerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long memberId;

    // 用户名
    private String userName;

    // 用户id
    private String userId;

    // 手机号
    private String phone;

    // 邮箱
    private String email;

    // 排序序号
    private Integer teamSort;

    private String teamCode;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getTeamSort() {
        return teamSort;
    }

    public void setTeamSort(Integer teamSort) {
        this.teamSort = teamSort;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }
}
