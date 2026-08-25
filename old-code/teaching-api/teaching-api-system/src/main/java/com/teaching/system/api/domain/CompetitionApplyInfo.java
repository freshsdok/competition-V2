package com.teaching.system.api.domain;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 赛事申请报名信息对象 competition_apply_info
 *
 * @author teaching
 * @date 2025-10-13
 */
public class CompetitionApplyInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 报名id */
    private Long memberId;

    private String memberIds;

    /** 赛事id */
    private Long competitionId;

    /** 赛事系列id(个人参赛) */
    private Long competitionSeriesId;

    /** 用户id */
    private Long userId;

    /** 赛事赛道id */
    private String competitionTrackId;

    /** 赛事赛道类型 */
    private String competitionTrackType;

    /** 赛事赛道二级id */
    private String secondLevelCode;

//    @Excel(name = "*赛题")
    private String competitionQuestion;

    /** 团队名称 */
    @Excel(name = "队名")
    private String teamName;

    /** 团队编号 */
    @Excel(name = "队伍ID（用于识别是否一个队伍）")
    private String teamCode;

    private String teamCodes;

    /** 团队队员及指导教师顺序 */
    private Integer teamSort;

    /** 参赛姓名 */
    @Excel(name = "姓名")
    private String userName;
    /** 性别 */
    @Excel(name = "性别")
    private String sex;

    /** 联系电话 */
    @Excel(name = "手机号")
    private String phone;

    /** 邮箱 */
    @Excel(name = "邮箱")
    private String email;

    /** 身份证号 */
    @Excel(name = "身份证号")
//    @Sensitive(desensitizedType = DesensitizedType.ID_CARD)
    private String idCard;
    //证件类型
    private String idCardType;

    /** 学校名称 */
    @Excel(name = "学校")
    private String schoolName;

    /** 参赛角色 */
    @Excel(name = "角色")
    private String competitionRoleName;

    /** 参赛角色赛选条件 */
    private String competitionRoleNameReq;

    @Excel(name = "省份")
    private String provinceName;

    /** 赛事赛道name */
    @Excel(name = "赛道")
    private String competitionTrackName;

    /** 赛事赛道二级name */
    @Excel(name = "组别")
    private String secondLevelName;

//    @Excel(name = "*团队编号",type = Excel.Type.IMPORT)
    private String teamExcelId;

    /** 学号/工号 */
    private String employeeCode;

    /** 机构id */
    private Long orgId;


    private String orgName;

    /** 报名时填写的单位名称快照 */
    private String companyName;

    /** 报名时填写的机构名称快照 */
    private String orgNameSnapshot;

    /** 国籍名称 */
//    @Excel(name = "*国籍")
    private String nationalityName;

    /** 院系名称 */
//    @Excel(name = "*院系")
    private String departmentName;

    /** 年级 */
//    @Excel(name = "*学级")
    private String classInfo;

    /** 专业 /部门 */
//    @Excel(name = "*专业名称")
    private String profession;

    /** 指导老师 支持可以多个,逗号隔开 */
    protected String guideTeacher;

    /** 指导老师名称 */
    protected String guideTeacherName;

    private String guideTeacherPhone;

    private String guideTeacherEmail;

    /** 带队老师id */
    private Long leaderTeacherId;

    /** 带队老师名称 */
    private String leaderTeacher;

    /** 带队老师手机号 */
    private String leaderTeacherPhone;

    /** 带队老师邮箱 */
    private String leaderTeacherEmail;

    /** 带队老师名称搜索条件 */
    private String leaderTeacherName;

    private List<Long> teacherIdList;

    /** 学校 */
    private String school;

    /** 省份 */
    private String province;

    /** 国籍 */
    private String nationality;

    /** 院系 */
    private String department;

    /** GAP评分 */
    protected String gapScore;

    /** 报名费 */
    private String fee;

    /** 查询审核状态是否包含拒绝状态 */
    private String checkStatusContain;

    /** 发票状态 */
    private String invoiceStatus;

    private List<String> teamCodeList;

    /** 导出类型 */
    private String exportType;

    /** 队员或指导教师人员更改信息次数（剩余次数） */
    private Long applyInfoChangeOperateCount = 0L;

    // 用户信息是否一致标识
    private String userInfoFlag = "0";
    // 用户信息不一致的旧数据
    private Map userInfoDateList;

    //给前端提示语
    private String message;

    private String awardsName;

    private String guiderTeacherName;

    /** 团队成员列表 */
    private List<TeamMemberRela> teamMemberRelaList;

    /** 队员变更历史数据 */
    private List<Map<String, Object>> teamMemberOldDateList;

    public CompetitionApplyInfo() {
    }

    public CompetitionApplyInfo(Long memberId, String checkStatus) {
        this.memberId = memberId;
        this.checkStatus = checkStatus;
    }

    /** 报名时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date registrationTime;

    /** 审核状态（待审核 / 已通过 / 已拒绝） */
    private String checkStatus;

    /** 实名认证状态（未认证 / 待提交 / 待审核 / 人脸识别中 / 认证通过 / 认证失败） */
    private String realNameAuthStatus;

    /** 支付状态 */
    private String payStatus;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名成功时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /** 报名发生变更时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名更新时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date changeUpdateTime;

    /** 报名发生变更时间 */
    @Excel(name = "已退赛")
    private String retiredFlag;

    /** 版本 */
    private Long version;

    /** 删除标识 */
    private String delFlag = "0";

    /** 报名开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date registrationStartTime;

    /** 报名结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date registrationEndTime;

   /** 赛事名称 */
    private String competitionName;

    /** 赛事类型 */
    private String competitionType;

    /** 参赛方式 */
    private String joinType;

    /** 奖品公示id */
    private Long awardPublicityId;

    public void setMemberId(Long memberId)
    {
        this.memberId = memberId;
    }

    public Long getMemberId()
    {
        return memberId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId)
    {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getCompetitionSeriesId()
    {
        return competitionSeriesId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getUserName()
    {
        return userName;
    }

    public Integer getTeamSort() {
        return teamSort;
    }

    public void setTeamSort(Integer teamSort) {
        this.teamSort = teamSort;
    }

    public void setEmployeeCode(String employeeCode)
    {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeCode()
    {
        return employeeCode;
    }

    public void setIdCard(String idCard)
    {
        this.idCard = idCard;
    }

    public String getIdCard()
    {
        return idCard;
    }

    public Long getAwardPublicityId() {
        return awardPublicityId;
    }

    public String getAwardsName() {
        return awardsName;
    }

    public void setAwardsName(String awardsName) {
        this.awardsName = awardsName;
    }

    public String getGuiderTeacherName() {
        return guiderTeacherName;
    }

    public void setGuiderTeacherName(String guiderTeacherName) {
        this.guiderTeacherName = guiderTeacherName;
    }

    public void setAwardPublicityId(Long awardPublicityId) {
        this.awardPublicityId = awardPublicityId;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getEmail()
    {
        return email;
    }

    public void setOrgId(Long orgId)
    {
        this.orgId = orgId;
    }

    public Long getOrgId()
    {
        return orgId;
    }

    public void setProfession(String profession)
    {
        this.profession = profession;
    }

    public String getProfession()
    {
        return profession;
    }

    public String getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(String classInfo) {
        this.classInfo = classInfo;
    }

    public void setGuideTeacher(String guideTeacher)
    {
        this.guideTeacher = guideTeacher;
    }

    public String getGuideTeacher()
    {
        return guideTeacher;
    }

    public void setRegistrationTime(Date registrationTime)
    {
        this.registrationTime = registrationTime;
    }

    public Date getRegistrationTime()
    {
        return registrationTime;
    }

    public void setCheckStatus(String checkStatus)
    {
        this.checkStatus = checkStatus;
    }

    public String getCheckStatus()
    {
        return checkStatus;
    }

    public void setRealNameAuthStatus(String realNameAuthStatus)
    {
        this.realNameAuthStatus = realNameAuthStatus;
    }

    public String getRealNameAuthStatus()
    {
        return realNameAuthStatus;
    }

    public void setPayStatus(String payStatus)
    {
        this.payStatus = payStatus;
    }

    public String getPayStatus()
    {
        return payStatus;
    }

    public Date getPayTime()
    {
        return payTime;
    }

    public void setPayTime(Date payTime)
    {
        this.payTime = payTime;
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

    public Date getRegistrationStartTime() {
        return registrationStartTime;
    }

    public void setRegistrationStartTime(Date registrationStartTime) {
        this.registrationStartTime = registrationStartTime;
    }

    public Date getRegistrationEndTime() {
        return registrationEndTime;
    }

    public void setRegistrationEndTime(Date registrationEndTime) {
        this.registrationEndTime = registrationEndTime;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId) {
        this.competitionId = competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(String competitionType) {
        this.competitionType = competitionType;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
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

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
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

    public String getSecondLevelName() {
        return secondLevelName;
    }

    public void setSecondLevelName(String secondLevelName) {
        this.secondLevelName = secondLevelName;
    }

    public String getGuideTeacherName() {
        return guideTeacherName;
    }

    public void setGuideTeacherName(String guideTeacherName) {
        this.guideTeacherName = guideTeacherName;
    }

    public String getCheckStatusContain() {
        return checkStatusContain;
    }

    public void setCheckStatusContain(String checkStatusContain) {
        this.checkStatusContain = checkStatusContain;
    }

    public List<TeamMemberRela> getTeamMemberRelaList() {
        return teamMemberRelaList;
    }

    public void setTeamMemberRelaList(List<TeamMemberRela> teamMemberRelaList) {
        this.teamMemberRelaList = teamMemberRelaList;
    }

    public String getCompetitionTrackName() {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName) {
        this.competitionTrackName = competitionTrackName;
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

    public Long getLeaderTeacherId() {
        return leaderTeacherId;
    }

    public void setLeaderTeacherId(Long leaderTeacherId) {
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

    public String getCompetitionRoleName() {
        return competitionRoleName;
    }

    public void setCompetitionRoleName(String competitionRoleName) {
        this.competitionRoleName = competitionRoleName;
    }

    public String getTeamExcelId() {
        return teamExcelId;
    }

    public void setTeamExcelId(String teamExcelId) {
        this.teamExcelId = teamExcelId;
    }

    public String getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(String memberIds) {
        this.memberIds = memberIds;
    }

    public String getTeamCodes() {
        return teamCodes;
    }

    public void setTeamCodes(String teamCodes) {
        this.teamCodes = teamCodes;
    }

    public List<String> getTeamCodeList() {
        return teamCodeList;
    }

    public void setTeamCodeList(List<String> teamCodeList) {
        this.teamCodeList = teamCodeList;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public List<Long> getTeacherIdList() {
        return teacherIdList;
    }

    public void setTeacherIdList(List<Long> teacherIdList) {
        this.teacherIdList = teacherIdList;
    }

    public String getLeaderTeacherName() {
        return leaderTeacherName;
    }

    public void setLeaderTeacherName(String leaderTeacherName) {
        this.leaderTeacherName = leaderTeacherName;
    }

    public String getCompetitionRoleNameReq() {
        return competitionRoleNameReq;
    }

    public void setCompetitionRoleNameReq(String competitionRoleNameReq) {
        this.competitionRoleNameReq = competitionRoleNameReq;
    }

    public String getLeaderTeacherEmail() {
        return leaderTeacherEmail;
    }

    public void setLeaderTeacherEmail(String leaderTeacherEmail) {
        this.leaderTeacherEmail = leaderTeacherEmail;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public Long getApplyInfoChangeOperateCount() {
        return applyInfoChangeOperateCount;
    }

    public void setApplyInfoChangeOperateCount(Long applyInfoChangeOperateCount) {
        this.applyInfoChangeOperateCount = applyInfoChangeOperateCount;
    }

    public String getUserInfoFlag() {
        return userInfoFlag;
    }

    public void setUserInfoFlag(String userInfoFlag) {
        this.userInfoFlag = userInfoFlag;
    }

    public String getIdCardType() {
        return idCardType;
    }

    public void setIdCardType(String idCardType) {
        this.idCardType = idCardType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map getUserInfoDateList() {
        return userInfoDateList;
    }

    public void setUserInfoDateList(Map userInfoDateList) {
        this.userInfoDateList = userInfoDateList;
    }

    public List<Map<String, Object>> getTeamMemberOldDateList() {
        return teamMemberOldDateList;
    }

    public void setTeamMemberOldDateList(List<Map<String, Object>> teamMemberOldDateList) {
        this.teamMemberOldDateList = teamMemberOldDateList;
    }

    public Date getChangeUpdateTime() {
        return changeUpdateTime;
    }

    public void setChangeUpdateTime(Date changeUpdateTime) {
        this.changeUpdateTime = changeUpdateTime;
    }

    public String getRetiredFlag() {
        return retiredFlag;
    }

    public void setRetiredFlag(String retiredFlag) {
        this.retiredFlag = retiredFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("memberId", getMemberId())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("userId", getUserId())
            .append("userName", getUserName())
            .append("employeeCode", getEmployeeCode())
            .append("idCard", getIdCard())
            .append("phone", getPhone())
            .append("email", getEmail())
            .append("orgId", getOrgId())
            .append("profession", getProfession())
            .append("classInfo", getClassInfo())
            .append("guideTeacher", getGuideTeacher())
            .append("registrationTime", getRegistrationTime())
            .append("checkStatus", getCheckStatus())
            .append("realNameAuthStatus", getRealNameAuthStatus())
            .append("payStatus", getPayStatus())
            .append("payTime", getPayTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("teamName", getTeamName())
                .append("gapScore", getGapScore())
                .append("joinType", getJoinType())
                .append("guideTeacher", getGuideTeacher())
                .append("teamCode", getTeamCode())
                .append("teamSort", getTeamSort())
                .append("competitionTrackName", getCompetitionTrackName())
                .append("competitionTrackId", getCompetitionTrackId())
                .append("competitionTrackType", getCompetitionTrackType())
                .append("secondLevelCode", getSecondLevelCode())
                .append("secondLevelName", getSecondLevelName())
            .toString();
    }
}
