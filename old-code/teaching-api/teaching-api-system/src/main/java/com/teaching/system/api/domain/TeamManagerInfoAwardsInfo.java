package com.teaching.system.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.annotation.Excels;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class TeamManagerInfoAwardsInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 获奖id */
    private Long awardsId;

    /** 团队id */
    private Long teamId;

    /** 团队code */
    @Excel(name = "队伍ID")
    private String teamCode;

    /** 团队codes集合 */
    private String teamCodes;

    /** 赛事赛道名称 */
    @Excel(name = "赛道")
    private String competitionTrackName;

    @Excel(name = "组别/赛项")
    private String secondLevelName;


    @Excel(name = "奖项名称")
    private String awardsName;

    /** 团队名称 */
    @Excel(name = "队名")
    private String teamName;

    /** 类型（比赛团队） */
    private String teamType;

    /** 队长信息id */
    private Long teamLeaderId;

    /** 描述 */
    private String teamDesc;

    /** 状态 */
    private String checkStatus;

    /** 关联比赛code */

    private Long competitionSeriesId;

    private String competitionSeriesName;

    /** 赛事赛道id */
    private String competitionTrackId;

    /** 赛事赛道二级id */
    private String secondLevelCode;

    private List<Long> teacherIdList;

    private String guideTeacher;

    /** 支付状态 */
    private String payStatus;

    /** 邀请记录 */
    private String invitationRecord;


    /** 团队人数 */
    private String teamNum;

    /** 版本 */
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

    /** 团队人数区间 */
    private Integer teamNumStart;

    /** 团队人数区间 */
    private Integer teamNumEnd;

    /** 创建时间区间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createStartTime;

    /** 创建时间区间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createEndTime;

    /**  赛事主id */
    private Long competitionId;

    private String competitionType;

    /** 队长标识 */
    private Boolean captainFlag;

    /** 团队成员审核状态 */
    private String memberCheckStatus;
    private String fee;
    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @Excel(name = "支付时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    @Excel(name = "学校")
    private String schoolName;

    @Excel(name = "省份")
    private String provinceName;

    /** 带队老师姓名搜搜条件 */
    @Excel(name = "报名用户姓名")
    private String leaderTeacherName;

    /** 带队老师 */
    private String leaderTeacher;

    /** 带队老师手机号 */
    @Excel(name = "报名用户手机")
    private String leaderTeacherPhone;

    /** 带队老师邮箱 */
    @Excel(name = "报名用户邮箱")
    private String leaderTeacherEmail;

    private String guiderTeacherName;

    // 团队变更旧数据
    private List<Map<String, Object>> teamManagerInfoOldData;

    /** 团队下报名人信息 */
    private List<CompetitionApplyInfo> applyInfoList;
    // 暂定6名队员
    @Excels({
            @Excel(name = "姓名1", targetAttr = "userName", type = Excel.Type.EXPORT),
            @Excel(name = "性别1", targetAttr = "sex", type = Excel.Type.EXPORT),
            @Excel(name = "手机号1", targetAttr = "phone", type = Excel.Type.EXPORT),
            @Excel(name = "邮箱1", targetAttr = "email", type = Excel.Type.EXPORT),
            @Excel(name = "身份证号1", targetAttr = "idCard", type = Excel.Type.EXPORT)
    })
    private CompetitionApplyInfo competitionApplyInfoOne;

    @Excels({
            @Excel(name = "姓名2", targetAttr = "userName", type = Excel.Type.EXPORT),
            @Excel(name = "性别2", targetAttr = "sex", type = Excel.Type.EXPORT),
            @Excel(name = "手机号2", targetAttr = "phone", type = Excel.Type.EXPORT),
            @Excel(name = "邮箱2", targetAttr = "email", type = Excel.Type.EXPORT),
            @Excel(name = "身份证号2", targetAttr = "idCard", type = Excel.Type.EXPORT)
    })
    private CompetitionApplyInfo competitionApplyInfoTwo;

    @Excels({
            @Excel(name = "姓名3", targetAttr = "userName", type = Excel.Type.EXPORT),
            @Excel(name = "性别3", targetAttr = "sex", type = Excel.Type.EXPORT),
            @Excel(name = "手机号3", targetAttr = "phone", type = Excel.Type.EXPORT),
            @Excel(name = "邮箱3", targetAttr = "email", type = Excel.Type.EXPORT),
            @Excel(name = "身份证号3", targetAttr = "idCard", type = Excel.Type.EXPORT)
    })
    private CompetitionApplyInfo competitionApplyInfoThree;

    @Excels({
            @Excel(name = "姓名4", targetAttr = "userName", type = Excel.Type.EXPORT),
            @Excel(name = "性别4", targetAttr = "sex", type = Excel.Type.EXPORT),
            @Excel(name = "手机号4", targetAttr = "phone", type = Excel.Type.EXPORT),
            @Excel(name = "邮箱4", targetAttr = "email", type = Excel.Type.EXPORT),
            @Excel(name = "身份证号4", targetAttr = "idCard", type = Excel.Type.EXPORT)
    })
    private CompetitionApplyInfo competitionApplyInfoFour;

    @Excels({
            @Excel(name = "姓名5", targetAttr = "userName", type = Excel.Type.EXPORT),
            @Excel(name = "性别5", targetAttr = "sex", type = Excel.Type.EXPORT),
            @Excel(name = "手机号5", targetAttr = "phone", type = Excel.Type.EXPORT),
            @Excel(name = "邮箱5", targetAttr = "email", type = Excel.Type.EXPORT),
            @Excel(name = "身份证号5", targetAttr = "idCard", type = Excel.Type.EXPORT)
    })
    private CompetitionApplyInfo competitionApplyInfoFive;

    @Excels({
            @Excel(name = "姓名6", targetAttr = "userName", type = Excel.Type.EXPORT),
            @Excel(name = "性别6", targetAttr = "sex", type = Excel.Type.EXPORT),
            @Excel(name = "手机号6", targetAttr = "phone", type = Excel.Type.EXPORT),
            @Excel(name = "邮箱6", targetAttr = "email", type = Excel.Type.EXPORT),
            @Excel(name = "身份证号6", targetAttr = "idCard", type = Excel.Type.EXPORT)
    })
    private CompetitionApplyInfo competitionApplyInfoSix;

    @Excels({
            @Excel(name = "教师姓名1", targetAttr = "userName", type = Excel.Type.EXPORT),
            @Excel(name = "教师手机号1", targetAttr = "phone", type = Excel.Type.EXPORT),
//            @Excel(name = "教师邮箱1", targetAttr = "email", type = Excel.Type.EXPORT),
    })
    private CompetitionApplyInfo competitionApplyInfoTeacherOne;

    @Excels({
            @Excel(name = "教师姓名2", targetAttr = "userName", type = Excel.Type.EXPORT),
            @Excel(name = "教师手机号2", targetAttr = "phone", type = Excel.Type.EXPORT),
//            @Excel(name = "教师邮箱2", targetAttr = "email", type = Excel.Type.EXPORT),
    })
    private CompetitionApplyInfo competitionApplyInfoTeacherTwo;

    /** 团队成员列表 */
    private List<TeamMemberRela> teamMemberRelaList;

    private List<TeamMemberRela> guidTeacherList;

    public Long getAwardsId() {
        return awardsId;
    }

    public void setAwardsId(Long awardsId) {
        this.awardsId = awardsId;
    }

    public String getAwardsName() {
        return awardsName;
    }

    public void setAwardsName(String awardsName) {
        this.awardsName = awardsName;
    }

    public void setTeamCode(String teamCode)
    {
        this.teamCode = teamCode;
    }

    public String getTeamCode()
    {
        return teamCode;
    }

    public void setTeamName(String teamName)
    {
        this.teamName = teamName;
    }

    public String getTeamName()
    {
        return teamName;
    }

    public void setTeamType(String teamType)
    {
        this.teamType = teamType;
    }

    public String getTeamType()
    {
        return teamType;
    }

    public void setTeamLeaderId(Long teamLeaderId)
    {
        this.teamLeaderId = teamLeaderId;
    }

    public Long getTeamLeaderId()
    {
        return teamLeaderId;
    }

    public void setTeamDesc(String teamDesc)
    {
        this.teamDesc = teamDesc;
    }

    public String getTeamDesc()
    {
        return teamDesc;
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

    public Long getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId) {
        this.competitionId = competitionId;
    }

    public void setInvitationRecord(String invitationRecord)
    {
        this.invitationRecord = invitationRecord;
    }

    public String getInvitationRecord()
    {
        return invitationRecord;
    }

    public void setTeamNum(String teamNum)
    {
        this.teamNum = teamNum;
    }

    public String getTeamNum()
    {
        return teamNum;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setOrgId(Long orgId)
    {
        this.orgId = orgId;
    }

    public Long getOrgId()
    {
        return orgId;
    }

    public String getCaptainName() {
        return captainName;
    }

    public void setCaptainName(String captainName) {
        this.captainName = captainName;
    }

    public Integer getTeamNumStart() {
        return teamNumStart;
    }

    public void setTeamNumStart(Integer teamNumStart) {
        this.teamNumStart = teamNumStart;
    }

    public Integer getTeamNumEnd() {
        return teamNumEnd;
    }

    public void setTeamNumEnd(Integer teamNumEnd) {
        this.teamNumEnd = teamNumEnd;
    }

    public Date getCreateStartTime() {
        return createStartTime;
    }

    public void setCreateStartTime(Date createStartTime) {
        this.createStartTime = createStartTime;
    }

    public Date getCreateEndTime() {
        return createEndTime;
    }

    public void setCreateEndTime(Date createEndTime) {
        this.createEndTime = createEndTime;
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

    public List<TeamMemberRela> getTeamMemberRelaList() {
        return teamMemberRelaList;
    }

    public void setTeamMemberRelaList(List<TeamMemberRela> teamMemberRelaList) {
        this.teamMemberRelaList = teamMemberRelaList;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(String competitionType) {
        this.competitionType = competitionType;
    }

    public Boolean getCaptainFlag() {
        return captainFlag;
    }

    public void setCaptainFlag(Boolean captainFlag) {
        this.captainFlag = captainFlag;
    }

    public String getMemberCheckStatus() {
        return memberCheckStatus;
    }

    public void setMemberCheckStatus(String memberCheckStatus) {
        this.memberCheckStatus = memberCheckStatus;
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

    public String getCompetitionTrackName() {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName) {
        this.competitionTrackName = competitionTrackName;
    }

    public String getLeaderTeacher() {
        return leaderTeacher;
    }

    public void setLeaderTeacher(String leaderTeacher) {
        this.leaderTeacher = leaderTeacher;
    }

    public String getCompetitionSeriesName() {
        return competitionSeriesName;
    }

    public void setCompetitionSeriesName(String competitionSeriesName) {
        this.competitionSeriesName = competitionSeriesName;
    }

    public String getSecondLevelName() {
        return secondLevelName;
    }

    public void setSecondLevelName(String secondLevelName) {
        this.secondLevelName = secondLevelName;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public String getTeamCodes() {
        return teamCodes;
    }

    public void setTeamCodes(String teamCodes) {
        this.teamCodes = teamCodes;
    }

    public List<TeamMemberRela> getGuidTeacherList() {
        return guidTeacherList;
    }

    public void setGuidTeacherList(List<TeamMemberRela> guidTeacherList) {
        this.guidTeacherList = guidTeacherList;
    }

    public List<CompetitionApplyInfo> getApplyInfoList() {
        return applyInfoList;
    }

    public void setApplyInfoList(List<CompetitionApplyInfo> applyInfoList) {
        this.applyInfoList = applyInfoList;
    }

    public CompetitionApplyInfo getCompetitionApplyInfoOne() {
        return competitionApplyInfoOne;
    }

    public void setCompetitionApplyInfoOne(CompetitionApplyInfo competitionApplyInfoOne) {
        this.competitionApplyInfoOne = competitionApplyInfoOne;
    }

    public CompetitionApplyInfo getCompetitionApplyInfoTwo() {
        return competitionApplyInfoTwo;
    }

    public void setCompetitionApplyInfoTwo(CompetitionApplyInfo competitionApplyInfoTwo) {
        this.competitionApplyInfoTwo = competitionApplyInfoTwo;
    }

    public CompetitionApplyInfo getCompetitionApplyInfoThree() {
        return competitionApplyInfoThree;
    }

    public void setCompetitionApplyInfoThree(CompetitionApplyInfo competitionApplyInfoThree) {
        this.competitionApplyInfoThree = competitionApplyInfoThree;
    }

    public CompetitionApplyInfo getCompetitionApplyInfoFour() {
        return competitionApplyInfoFour;
    }

    public void setCompetitionApplyInfoFour(CompetitionApplyInfo competitionApplyInfoFour) {
        this.competitionApplyInfoFour = competitionApplyInfoFour;
    }

    public CompetitionApplyInfo getCompetitionApplyInfoFive() {
        return competitionApplyInfoFive;
    }

    public void setCompetitionApplyInfoFive(CompetitionApplyInfo competitionApplyInfoFive) {
        this.competitionApplyInfoFive = competitionApplyInfoFive;
    }

    public CompetitionApplyInfo getCompetitionApplyInfoSix() {
        return competitionApplyInfoSix;
    }

    public void setCompetitionApplyInfoSix(CompetitionApplyInfo competitionApplyInfoSix) {
        this.competitionApplyInfoSix = competitionApplyInfoSix;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getLeaderTeacherName() {
        return leaderTeacherName;
    }

    public void setLeaderTeacherName(String leaderTeacherName) {
        this.leaderTeacherName = leaderTeacherName;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    public List<Long> getTeacherIdList() {
        return teacherIdList;
    }

    public void setTeacherIdList(List<Long> teacherIdList) {
        this.teacherIdList = teacherIdList;
    }

    public String getGuideTeacher() {
        return guideTeacher;
    }

    public void setGuideTeacher(String guideTeacher) {
        this.guideTeacher = guideTeacher;
    }

    public CompetitionApplyInfo getCompetitionApplyInfoTeacherOne() {
        return competitionApplyInfoTeacherOne;
    }

    public void setCompetitionApplyInfoTeacherOne(CompetitionApplyInfo competitionApplyInfoTeacherOne) {
        this.competitionApplyInfoTeacherOne = competitionApplyInfoTeacherOne;
    }

    public CompetitionApplyInfo getCompetitionApplyInfoTeacherTwo() {
        return competitionApplyInfoTeacherTwo;
    }

    public void setCompetitionApplyInfoTeacherTwo(CompetitionApplyInfo competitionApplyInfoTeacherTwo) {
        this.competitionApplyInfoTeacherTwo = competitionApplyInfoTeacherTwo;
    }

    public String getLeaderTeacherPhone() {
        return leaderTeacherPhone;
    }

    public void setLeaderTeacherPhone(String leaderTeacherPhone) {
        this.leaderTeacherPhone = leaderTeacherPhone;
    }

    public String getLeaderTeacherEmail() {
        return leaderTeacherEmail;
    }

    public void setLeaderTeacherEmail(String leaderTeacherEmail) {
        this.leaderTeacherEmail = leaderTeacherEmail;
    }

    public List<Map<String, Object>> getTeamManagerInfoOldData() {
        return teamManagerInfoOldData;
    }

    public void setTeamManagerInfoOldData(List<Map<String, Object>> teamManagerInfoOldData) {
        this.teamManagerInfoOldData = teamManagerInfoOldData;
    }

    public String getGuiderTeacherName() {
        return guiderTeacherName;
    }

    public void setGuiderTeacherName(String guiderTeacherName) {
        this.guiderTeacherName = guiderTeacherName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("teamId", getTeamId())
                .append("teamCode", getTeamCode())
                .append("teamName", getTeamName())
                .append("teamType", getTeamType())
                .append("teamLeaderId", getTeamLeaderId())
                .append("teamDesc", getTeamDesc())
                .append("checkStatus", getCheckStatus())
                .append("competitionSeriesId", getCompetitionSeriesId())
                .append("invitationRecord", getInvitationRecord())
                .append("teamNum", getTeamNum())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .append("userId", getUserId())
                .append("orgId", getOrgId())
                .append("captainName", getCaptainName())
                .append("competitionType", getCompetitionType())
                .append("competitionTrackId", getCompetitionTrackId())
                .append("secondLevelCode", getSecondLevelCode())
                .append("competitionTrackName", getCompetitionTrackName())
                .append("leaderTeacher", getLeaderTeacher())
                .append("payStatus", getPayStatus())
                .toString();
    }

}
