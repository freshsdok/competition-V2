package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import com.teaching.system.api.domain.TeamMemberRela;

import java.util.Date;
import java.util.List;

public class UserApplyCompetitionReq extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 团队id */
    private Long teamId;

    /** 团队code */
    private String teamCode;

    /** 团队名称 */
    @Excel(name = "团队名称")
    private String teamName;

    /** 类型（比赛团队） */
    @Excel(name = "类型", readConverterExp = "比赛团队")
    private String teamType;

    /** 队长信息id */
    @Excel(name = "队长信息id")
    private Long teamLeaderId;

    /** 描述 */
    @Excel(name = "描述")
    private String teamDesc;

    /** 状态 */
    @Excel(name = "状态")
    private String checkStatus;

    /** 关联比赛code */
    @Excel(name = "关联比赛code")
    private Long competitionSeriesId;

    /** 邀请记录 */
    @Excel(name = "邀请记录")
    private String invitationRecord;

    /** 指导老师 */
    @Excel(name = "指导老师")
    private String guideTeacher;

    /** 团队人数 */
    @Excel(name = "团队人数")
    private String teamNum;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag = "0";

    /** 数据权限用户id */
    private Long userId;

    /** 数据权限机构id */
    private Long orgId;

    /** 队长名称 */
    private String captainName;

    /** 学号 */
    private String employeeCode;

    /** 比赛名称 */
    private String competitionName;


    /**  赛事主id */
    private Long competitionId;

    /** 团队成员列表 */
    private List<TeamMemberRela> teamMemberRelaList;

    /** GAP评分 */
    private String gapScore;

    /** 赛事赛道名称 */
    private String competitionTrackName;

    /** 赛事赛道id */
    private String competitionTrackId;

    /** 二级分类编码 */
    private String secondLevelCode;

    /** 参赛姓名 */
    @Excel(name = "参赛者姓名")
    private String userName;

    /** 身份证号 */
    @Excel(name = "身份证号")
    private String idCard;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String phone;

    /** 邮箱 */
    @Excel(name = "邮箱")
    private String email;

    /** 机构名称 */
    @Excel(name = "所属机构")
    private String orgName;

    /** 专业 /部门 */
    @Excel(name = "专业 /部门")
    private String profession;

    /** 年级 */
    @Excel(name = "年级")
    private String classInfo;

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamType() {
        return teamType;
    }

    public void setTeamType(String teamType) {
        this.teamType = teamType;
    }

    public Long getTeamLeaderId() {
        return teamLeaderId;
    }

    public void setTeamLeaderId(Long teamLeaderId) {
        this.teamLeaderId = teamLeaderId;
    }

    public String getTeamDesc() {
        return teamDesc;
    }

    public void setTeamDesc(String teamDesc) {
        this.teamDesc = teamDesc;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getInvitationRecord() {
        return invitationRecord;
    }

    public void setInvitationRecord(String invitationRecord) {
        this.invitationRecord = invitationRecord;
    }

    public String getGuideTeacher() {
        return guideTeacher;
    }

    public void setGuideTeacher(String guideTeacher) {
        this.guideTeacher = guideTeacher;
    }

    public String getTeamNum() {
        return teamNum;
    }

    public void setTeamNum(String teamNum) {
        this.teamNum = teamNum;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getCaptainName() {
        return captainName;
    }

    public void setCaptainName(String captainName) {
        this.captainName = captainName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public Long getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId) {
        this.competitionId = competitionId;
    }

    public List<TeamMemberRela> getTeamMemberRelaList() {
        return teamMemberRelaList;
    }

    public void setTeamMemberRelaList(List<TeamMemberRela> teamMemberRelaList) {
        this.teamMemberRelaList = teamMemberRelaList;
    }

    public String getGapScore() {
        return gapScore;
    }

    public void setGapScore(String gapScore) {
        this.gapScore = gapScore;
    }

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

    public String getCompetitionTrackName() {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName) {
        this.competitionTrackName = competitionTrackName;
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
}
