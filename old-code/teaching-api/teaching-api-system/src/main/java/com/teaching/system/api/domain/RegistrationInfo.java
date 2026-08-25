package com.teaching.system.api.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistrationInfo {
    private String memberId;
    private String competitionSeriesId;
    private String competitionType;
    private String userId;
    private String userName;
    private String employeeCode;
    private String idCard;
    private String phone;
    private String email;
    private String orgId;
    private String orgName;
    private String profession;
    private String classInfo;
    private String guideTeacher;
    private String registrationTime;
    private String checkStatus;
    private String realNameAuthStatus;
    private String payStatus;
    private String teamName;
    private String joinType;
    private String gapScore;
    private String competitionTrackId;
    private String competitionTrackType;
    private String secondLevelCode;
    private String guideTeacherPhone;
    private String guideTeacherEmail;
    private String leaderTeacherId;
    private String leaderTeacher;
    private String leaderTeacherPhone;
    private String school;
    private String schoolName;
    private String province;
    private String provinceName;
    private String competitionQuestion;
    private String nationality;
    private String nationalityName;
    private String department;
    private String departmentName;
    private String sex;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private String version;
    private String delFlag;
    private String keyWord;

    private String competitionRoleName;

    public String getCompetitionRoleName() {
        return competitionRoleName;
    }

    public void setCompetitionRoleName(String competitionRoleName) {
        this.competitionRoleName = competitionRoleName;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(String competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(String competitionType) {
        this.competitionType = competitionType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
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

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(String classInfo) {
        this.classInfo = classInfo;
    }

    public String getGuideTeacher() {
        return guideTeacher;
    }

    public void setGuideTeacher(String guideTeacher) {
        this.guideTeacher = guideTeacher;
    }

    public String getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(String registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getRealNameAuthStatus() {
        return realNameAuthStatus;
    }

    public void setRealNameAuthStatus(String realNameAuthStatus) {
        this.realNameAuthStatus = realNameAuthStatus;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getJoinType() {
        return joinType;
    }

    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }

    public String getGapScore() {
        return gapScore;
    }

    public void setGapScore(String gapScore) {
        this.gapScore = gapScore;
    }

    public String getCompetitionTrackId() {
        return competitionTrackId;
    }

    public void setCompetitionTrackId(String competitionTrackId) {
        this.competitionTrackId = competitionTrackId;
    }

    public String getCompetitionTrackType() {
        return competitionTrackType;
    }

    public void setCompetitionTrackType(String competitionTrackType) {
        this.competitionTrackType = competitionTrackType;
    }

    public String getSecondLevelCode() {
        return secondLevelCode;
    }

    public void setSecondLevelCode(String secondLevelCode) {
        this.secondLevelCode = secondLevelCode;
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

    public String getLeaderTeacherId() {
        return leaderTeacherId;
    }

    public void setLeaderTeacherId(String leaderTeacherId) {
        this.leaderTeacherId = leaderTeacherId;
    }

    public String getLeaderTeacher() {
        return leaderTeacher;
    }

    public void setLeaderTeacher(String leaderTeacher) {
        this.leaderTeacher = leaderTeacher;
    }

    public String getLeaderTeacherPhone() {
        return leaderTeacherPhone;
    }

    public void setLeaderTeacherPhone(String leaderTeacherPhone) {
        this.leaderTeacherPhone = leaderTeacherPhone;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCompetitionQuestion() {
        return competitionQuestion;
    }

    public void setCompetitionQuestion(String competitionQuestion) {
        this.competitionQuestion = competitionQuestion;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getNationalityName() {
        return nationalityName;
    }

    public void setNationalityName(String nationalityName) {
        this.nationalityName = nationalityName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }
}
