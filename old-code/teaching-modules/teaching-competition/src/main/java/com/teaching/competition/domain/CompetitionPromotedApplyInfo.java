package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

/**
 * 赛事晋级申请报名信息对象 competition_promoted_apply_info
 *
 * @author teaching
 * @date 2026-05-19
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompetitionPromotedApplyInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 晋级报名id
     */
    private Long applyId;

    /**
     * 赛事系列id
     */
    private Long competitionSeriesId;

    /**
     * 赛事名称
     */
    @Excel(name = "赛事名称")
    private String competitionName;
    /**
     * 赛道名称
     */
    @Excel(name = "赛道名称")
    private String competitionTrackName;
    /**
     * 二级分类名称
     */
    @Excel(name = "组别")
    private String secondLevelName;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 学校名称
     */
    @Excel(name = "学校名称")
    private String schoolName;

    /**
     * 团队code
     */
    @Excel(name = "团队编号")
    private String teamCode;

    /**
     * 团队名称
     */
    @Excel(name = "团队名称")
    private String teamName;

    /**
     * 参赛姓名
     */
    @Excel(name = "参赛姓名")
    private String userName;

    /**
     * /**
     * 专业 /部门
     */
    @Excel(name = "专业")
    private String profession;
    /**
     * 性别
     */
    @Excel(name = "性别")
    private String sex;
    /**
     * 参赛角色
     */
    @Excel(name = "参赛角色")
    private String competitionRoleName;
    /**
     * 学号/工号
     */
    private String employeeCode;

    /**
     * 身份证号
     */
    @Excel(name = "身份证号")
    private String idCard;

    /**
     * 联系电话
     */
    @Excel(name = "联系电话")
    private String phone;

    /**
     * 邮箱
     */
    @Excel(name = "邮箱")
    private String email;
    /**
     * 团队队员及指导教师顺序
     */
    @Excel(name = "排序")
    private Integer teamSort;


    /**
     * 机构id
     */
    private Long orgId;


    /**
     * 年级
     */
    private String classInfo;

    /**
     * 指导老师
     */
    @Excel(name = "指导老师")
    private String guideTeacher;

    /**
     * 指导老师手机号
     */
    @Excel(name = "指导老师手机号")
    private String guideTeacherPhone;

    /**
     * 指导老师邮箱
     */
    @Excel(name = "指导老师邮箱")
    private String guideTeacherEmail;

    /**
     * 报名时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报名时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date registrationTime;

    /**
     * 支付状态
     */
    private String payStatus;



    /**
     * 赛道编码
     */
    private String competitionTrackId;

    /**
     * 赛道二级分类
     */
    private String competitionTrackType;

    /**
     * 赛道二级分类编码(组别、赛道、子课题)
     */
    private String secondLevelCode;



    /**
     * 带队老师id
     */
    private Long leaderTeacherId;

    /**
     * 学校编码
     */
    private String school;

    /**
     * 省份code
     */
    private String province;

    /**
     * 国籍
     */
    private String nationality;



    /**
     * 省份名称
     */
    private String provinceName;

    /**
     * 国籍名称
     */
    private String nationalityName;


    /**
     * 发票状态
     */
    private String invoiceStatus;

    /**
     * 证件类型
     */
    private String idCardType;



    /**
     * 团队报名状态 已报名/未报名
     */
    @Excel(name = "团队报名状态", readConverterExp = "1=已报名,0=未报名")
    private String applyStatus;

    /**
     * 版本
     */
    private Long version;

    /**
     * 报名金额
     */
    private String fee;

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    private String exportType;
    /**
     * 晋级人员信息列表
     */
    private List<PromotedPlayerInfo> promotedPlayerInfoList;

    /**
     * 队员/队长信息列表
     */
    private List<PromotedPlayerInfo> playerInfoList;

    /**
     * 指导老师信息列表
     */
    private List<PromotedPlayerInfo> guideTeacherInfoList;

    /**
     * 删除标识
     */
    private String delFlag;

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public List<PromotedPlayerInfo> getPromotedPlayerInfoList() {
        return promotedPlayerInfoList;
    }

    public void setPromotedPlayerInfoList(List<PromotedPlayerInfo> promotedPlayerInfoList) {
        this.promotedPlayerInfoList = promotedPlayerInfoList;
    }

    public List<PromotedPlayerInfo> getPlayerInfoList() {
        return playerInfoList;
    }

    public void setPlayerInfoList(List<PromotedPlayerInfo> playerInfoList) {
        this.playerInfoList = playerInfoList;
    }

    public List<PromotedPlayerInfo> getGuideTeacherInfoList() {
        return guideTeacherInfoList;
    }

    public void setGuideTeacherInfoList(List<PromotedPlayerInfo> guideTeacherInfoList) {
        this.guideTeacherInfoList = guideTeacherInfoList;
    }

    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }

    public Long getApplyId() {
        return applyId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getProfession() {
        return profession;
    }

    public void setClassInfo(String classInfo) {
        this.classInfo = classInfo;
    }

    public String getClassInfo() {
        return classInfo;
    }

    public void setGuideTeacher(String guideTeacher) {
        this.guideTeacher = guideTeacher;
    }

    public String getGuideTeacher() {
        return guideTeacher;
    }

    public void setGuideTeacherPhone(String guideTeacherPhone) {
        this.guideTeacherPhone = guideTeacherPhone;
    }

    public String getGuideTeacherPhone() {
        return guideTeacherPhone;
    }

    public void setGuideTeacherEmail(String guideTeacherEmail) {
        this.guideTeacherEmail = guideTeacherEmail;
    }

    public String getGuideTeacherEmail() {
        return guideTeacherEmail;
    }

    public void setRegistrationTime(Date registrationTime) {
        this.registrationTime = registrationTime;
    }

    public Date getRegistrationTime() {
        return registrationTime;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public void setCompetitionTrackName(String competitionTrackName) {
        this.competitionTrackName = competitionTrackName;
    }

    public String getCompetitionTrackName() {
        return competitionTrackName;
    }

    public void setCompetitionTrackId(String competitionTrackId) {
        this.competitionTrackId = competitionTrackId;
    }

    public String getCompetitionTrackId() {
        return competitionTrackId;
    }

    public void setCompetitionTrackType(String competitionTrackType) {
        this.competitionTrackType = competitionTrackType;
    }

    public String getCompetitionTrackType() {
        return competitionTrackType;
    }

    public void setSecondLevelCode(String secondLevelCode) {
        this.secondLevelCode = secondLevelCode;
    }

    public String getSecondLevelCode() {
        return secondLevelCode;
    }

    public void setSecondLevelName(String secondLevelName) {
        this.secondLevelName = secondLevelName;
    }

    public String getSecondLevelName() {
        return secondLevelName;
    }

    public void setLeaderTeacherId(Long leaderTeacherId) {
        this.leaderTeacherId = leaderTeacherId;
    }

    public Long getLeaderTeacherId() {
        return leaderTeacherId;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getSchool() {
        return school;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getProvince() {
        return province;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getNationality() {
        return nationality;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setNationalityName(String nationalityName) {
        this.nationalityName = nationalityName;
    }

    public String getNationalityName() {
        return nationalityName;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSex() {
        return sex;
    }

    public void setCompetitionRoleName(String competitionRoleName) {
        this.competitionRoleName = competitionRoleName;
    }

    public String getCompetitionRoleName() {
        return competitionRoleName;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setIdCardType(String idCardType) {
        this.idCardType = idCardType;
    }

    public String getIdCardType() {
        return idCardType;
    }

    public void setTeamSort(Integer teamSort) {
        this.teamSort = teamSort;
    }

    public Integer getTeamSort() {
        return teamSort;
    }

    public void setApplyStatus(String applyStatus) {
        this.applyStatus = applyStatus;
    }

    public String getApplyStatus() {
        return applyStatus;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getVersion() {
        return version;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("applyId", getApplyId())
                .append("competitionSeriesId", getCompetitionSeriesId())
                .append("competitionName", getCompetitionName())
                .append("userId", getUserId())
                .append("teamCode", getTeamCode())
                .append("teamName", getTeamName())
                .append("userName", getUserName())
                .append("employeeCode", getEmployeeCode())
                .append("idCard", getIdCard())
                .append("phone", getPhone())
                .append("email", getEmail())
                .append("orgId", getOrgId())
                .append("profession", getProfession())
                .append("classInfo", getClassInfo())
                .append("guideTeacher", getGuideTeacher())
                .append("guideTeacherPhone", getGuideTeacherPhone())
                .append("guideTeacherEmail", getGuideTeacherEmail())
                .append("registrationTime", getRegistrationTime())
                .append("payStatus", getPayStatus())
                .append("competitionTrackName", getCompetitionTrackName())
                .append("competitionTrackId", getCompetitionTrackId())
                .append("competitionTrackType", getCompetitionTrackType())
                .append("secondLevelCode", getSecondLevelCode())
                .append("secondLevelName", getSecondLevelName())
                .append("leaderTeacherId", getLeaderTeacherId())
                .append("school", getSchool())
                .append("province", getProvince())
                .append("nationality", getNationality())
                .append("schoolName", getSchoolName())
                .append("provinceName", getProvinceName())
                .append("nationalityName", getNationalityName())
                .append("sex", getSex())
                .append("competitionRoleName", getCompetitionRoleName())
                .append("invoiceStatus", getInvoiceStatus())
                .append("idCardType", getIdCardType())
                .append("teamSort", getTeamSort())
                .append("applyStatus", getApplyStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
