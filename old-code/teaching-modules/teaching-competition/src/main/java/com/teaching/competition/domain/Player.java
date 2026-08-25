package com.teaching.competition.domain;

import com.teaching.common.core.annotation.Excel;

import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;
    // 姓名
    private String userName;
    // 身份证号
    private String idCard;
    //  联系电话
    private String phone;
    // 邮箱
    private String email;
    // 性别
    private String sex;
    // 学级
    private String classInfo;
    // 专业
    private String profession;
    // 国籍
    private String nationalityName;
    // 院系
    private String departmentName;
    // 学校名称
    private String schoolName;
    // 单位名称（报名快照）
    private String companyName;
    // 机构名称（报名快照）
    private String orgNameSnapshot;
    // 工号
    private String employeeCode;
    // 角色
    private String competitionRoleName;
    // 证件类型
    private String idCardType;
    // 提示语
    private String message;
    // 顺序
    private Integer teamSort;

//    public Player(String userName, String idCard, String phone, String email, String sex, String classInfo, String profession, String nationalityName, String departmentName) {
//        this.userName = userName;
//        this.idCard = idCard;
//        this.phone = phone;
//        this.email = email;
//        this.sex = sex;
//        this.classInfo = classInfo;
//        this.profession = profession;
//        this.nationalityName = nationalityName;
//        this.departmentName = departmentName;
//    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(String classInfo) {
        this.classInfo = classInfo;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getNationalityName() {
        return nationalityName;
    }

    public void setNationalityName(String nationalityName) {
        this.nationalityName = nationalityName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getOrgNameSnapshot() {
        return orgNameSnapshot;
    }

    public void setOrgNameSnapshot(String orgNameSnapshot) {
        this.orgNameSnapshot = orgNameSnapshot;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getCompetitionRoleName() {
        return competitionRoleName;
    }

    public void setCompetitionRoleName(String competitionRoleName) {
        this.competitionRoleName = competitionRoleName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIdCardType() {
        return idCardType;
    }

    public void setIdCardType(String idCardType) {
        this.idCardType = idCardType;
    }

    public Integer getTeamSort() {
        return teamSort;
    }

    public void setTeamSort(Integer teamSort) {
        this.teamSort = teamSort;
    }
}
