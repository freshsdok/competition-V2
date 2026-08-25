package com.teaching.system.api.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 身份认证信息对象 identity_info
 *
 * @author teaching
 * @date 2025-10-13
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IdentityInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long authId;

    /**
     * 用户id
     */
    @Excel(name = "用户id")
    private Long userId;

    /**
     * 学校
     */
    @Excel(name = "学校")
    private String school;
    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 专业
     */
    @Excel(name = "专业")
    private String specialty;

    /**
     * 年级
     */
    @Excel(name = "年级")
    private String classInfo;

    /**
     * 入学年份
     */
    private String enrollmentYear;

    /**
     * 学生证
     */
    @Excel(name = "学生证")
    private String studentCardId;

    /**
     * 学院
     */
    @Excel(name = "学院")
    private String institute;

    /**
     * 带队老师
     */
    private Long teamLeader;

    /**
     * 所属机构id
     */
    @Excel(name = "所属机构id")
    private Long orgId;

    /**
     * 职位
     */
    @Excel(name = "职位")
    private String position;

    /**
     * 学号 / 工号
     */
    @Excel(name = "学号 / 工号")
    private String employeeCode;

    /**
     * 入职时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "入职时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date jobTime;

    /**
     * 工作证
     */
    @Excel(name = "工作证")
    private String workCardUrl;

    /**
     * 教师职务
     */
    @Excel(name = "教师职务")
    private String post;

    /**
     * 审批文号
     */
    @Excel(name = "审批文号")
    private String apprDocNumber;

    /**
     * 统一社会信用代码
     */
    @Excel(name = "统一社会信用代码")
    private String creditIdent;

    /**
     * 学校资质备案证书
     */
    @Excel(name = "学校资质备案证书")
    private String schoolCertUrl;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 营业执照号码
     */
    @Excel(name = "营业执照号码")
    private String bussLicenseNum;

    /**
     * 法人姓名
     */
    @Excel(name = "法人姓名")
    private String legalPersName;

    /**
     * 法人身份证号
     */
    @Excel(name = "法人身份证号")
    private String legalIdCard;

    /**
     * 营业执照
     */
    @Excel(name = "营业执照")
    private String bussLicenseUrl;

    /**
     * 身份认证类型
     */
    private String certificationType;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 学生证照片名称
     */
    private String studentCardName;

    /**
     * 工作证照片名称
     */
    private String workCardName;

    /**
     * 学校资质备案证书照片名称
     */
    private String schoolCertName;

    /**
     * 营业执照照片名称
     */
    private String bussLicenseName;

    /**
     * 审核状态
     */
    private String checkStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date identityTime;

    /**
     * 版本
     */
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag = "0";

    /**
     * 是否为外籍学生
     */
    private String isForeignStudent;

    /**
     * 驳回原因
     */
    private String refusalReasons;

    public IdentityInfo() {
    }

    public IdentityInfo(Long authId, String checkStatus) {
        this.authId = authId;
        this.checkStatus = checkStatus;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getRefusalReasons() {
        return refusalReasons;
    }

    public void setRefusalReasons(String refusalReasons) {
        this.refusalReasons = refusalReasons;
    }

    public String getIsForeignStudent() {
        return isForeignStudent;
    }

    public void setIsForeignStudent(String isForeignStudent) {
        this.isForeignStudent = isForeignStudent;
    }

    public Long getTeamLeader() {
        return teamLeader;
    }

    public void setTeamLeader(Long teamLeader) {
        this.teamLeader = teamLeader;
    }

    public void setAuthId(Long authId) {
        this.authId = authId;
    }

    public Long getAuthId() {
        return authId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getSchool() {
        return school;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(String classInfo) {
        this.classInfo = classInfo;
    }

    public void setStudentCardId(String studentCardId) {
        this.studentCardId = studentCardId;
    }

    public String getStudentCardId() {
        return studentCardId;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getInstitute() {
        return institute;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPosition() {
        return position;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setJobTime(Date jobTime) {
        this.jobTime = jobTime;
    }

    public Date getJobTime() {
        return jobTime;
    }

    public void setWorkCardUrl(String workCardUrl) {
        this.workCardUrl = workCardUrl;
    }

    public String getWorkCardUrl() {
        return workCardUrl;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getPost() {
        return post;
    }

    public void setApprDocNumber(String apprDocNumber) {
        this.apprDocNumber = apprDocNumber;
    }

    public String getApprDocNumber() {
        return apprDocNumber;
    }

    public void setCreditIdent(String creditIdent) {
        this.creditIdent = creditIdent;
    }

    public String getCreditIdent() {
        return creditIdent;
    }

    public void setSchoolCertUrl(String schoolCertUrl) {
        this.schoolCertUrl = schoolCertUrl;
    }

    public String getSchoolCertUrl() {
        return schoolCertUrl;
    }

    public void setBussLicenseNum(String bussLicenseNum) {
        this.bussLicenseNum = bussLicenseNum;
    }

    public String getBussLicenseNum() {
        return bussLicenseNum;
    }

    public void setLegalPersName(String legalPersName) {
        this.legalPersName = legalPersName;
    }

    public String getLegalPersName() {
        return legalPersName;
    }

    public void setLegalIdCard(String legalIdCard) {
        this.legalIdCard = legalIdCard;
    }

    public String getLegalIdCard() {
        return legalIdCard;
    }

    public void setBussLicenseUrl(String bussLicenseUrl) {
        this.bussLicenseUrl = bussLicenseUrl;
    }

    public String getBussLicenseUrl() {
        return bussLicenseUrl;
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

    public String getCertificationType() {
        return certificationType;
    }

    public void setCertificationType(String certificationType) {
        this.certificationType = certificationType;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getStudentCardName() {
        return studentCardName;
    }

    public void setStudentCardName(String studentCardName) {
        this.studentCardName = studentCardName;
    }

    public String getWorkCardName() {
        return workCardName;
    }

    public void setWorkCardName(String workCardName) {
        this.workCardName = workCardName;
    }

    public String getSchoolCertName() {
        return schoolCertName;
    }

    public void setSchoolCertName(String schoolCertName) {
        this.schoolCertName = schoolCertName;
    }

    public String getBussLicenseName() {
        return bussLicenseName;
    }

    public void setBussLicenseName(String bussLicenseName) {
        this.bussLicenseName = bussLicenseName;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Date getIdentityTime() {
        return identityTime;
    }

    public void setIdentityTime(Date identityTime) {
        this.identityTime = identityTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEnrollmentYear() {
        return enrollmentYear;
    }

    public void setEnrollmentYear(String enrollmentYear) {
        this.enrollmentYear = enrollmentYear;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("authId", getAuthId())
                .append("userId", getUserId())
                .append("school", getSchool())
                .append("specialty", getSpecialty())
                .append("classInfo", getClassInfo())
                .append("studentCardId", getStudentCardId())
                .append("institute", getInstitute())
                .append("orgId", getOrgId())
                .append("position", getPosition())
                .append("employeeCode", getEmployeeCode())
                .append("jobTime", getJobTime())
                .append("workCardUrl", getWorkCardUrl())
                .append("post", getPost())
                .append("apprDocNumber", getApprDocNumber())
                .append("creditIdent", getCreditIdent())
                .append("schoolCertUrl", getSchoolCertUrl())
                .append("bussLicenseNum", getBussLicenseNum())
                .append("legalPersName", getLegalPersName())
                .append("legalIdCard", getLegalIdCard())
                .append("bussLicenseUrl", getBussLicenseUrl())
                .append("realName", getRealName())
                .append("idCard", getIdCard())
                .append("certificationType", getCertificationType())
                .append("studentCardName", getStudentCardName())
                .append("workCardName", getWorkCardName())
                .append("schoolCertName", getSchoolCertName())
                .append("bussLicenseName", getBussLicenseName())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .append("certificationType", getCertificationType())
                .append("checkStatus", getCheckStatus())
                .append("companyName", getCompanyName())
                .append("identityTime", getIdentityTime())
                .append("userName", getUserName())
                .toString();
    }
}
