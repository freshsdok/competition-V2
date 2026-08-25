package com.teaching.system.api.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 团队关联关系对象 team_member_rela
 *
 * @author teaching
 * @date 2025-10-13
 */
public class TeamMemberRela extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 关联关系id */
    private Long relaId;

    /** 团队id集合 */
    private String relaIds;

    /** 团队code */
    @Excel(name = "团队code")
    private String teamCode;

    // 团队code集合
    private String teamCodes;

    /** 加入团队状态 */
    private String checkStatus;

    /** 用户名 */
    private String userName;

    /** 创建用户id */
    private Long userId;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag = "0";

    /** 数据权限机构id */
    private Long orgId;

    /** 用户信息 */
    private SysUser sysUser;

    /** 报名金额 */
    private String fee;

    private String phone;

    private String email;

    private String sex;

    private String orgName;

    private String idCard;
    private String nickName;
    private String teamRole;
    private String instructor;
    private String instructorPhone;
    private String instructorEmail;

    public void setRelaId(Long relaId)
    {
        this.relaId = relaId;
    }

    public Long getRelaId()
    {
        return relaId;
    }

    public void setTeamCode(String teamCode)
    {
        this.teamCode = teamCode;
    }

    public String getTeamCode()
    {
        return teamCode;
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

    public SysUser getSysUser() {
        return sysUser;
    }

    public void setSysUser(SysUser sysUser) {
        this.sysUser = sysUser;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getInstructorPhone() {
        return instructorPhone;
    }

    public void setInstructorPhone(String instructorPhone) {
        this.instructorPhone = instructorPhone;
    }

    public String getInstructorEmail() {
        return instructorEmail;
    }

    public void setInstructorEmail(String instructorEmail) {
        this.instructorEmail = instructorEmail;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getTeamRole() {
        return teamRole;
    }

    public void setTeamRole(String teamRole) {
        this.teamRole = teamRole;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getTeamCodes() {
        return teamCodes;
    }

    public void setTeamCodes(String teamCodes) {
        this.teamCodes = teamCodes;
    }

    public String getRelaIds() {
        return relaIds;
    }

    public void setRelaIds(String relaIds) {
        this.relaIds = relaIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("relaId", getRelaId())
            .append("teamCode", getTeamCode())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("userId", getUserId())
            .append("orgId", getOrgId())
            .append("checkStatus", getCheckStatus())
            .append("userName", getUserName())
                .append("phone", getPhone())
                .append("email", getEmail())
                .append("sex", getSex())
                .append("orgName", getOrgName())
            .toString();
    }
}
