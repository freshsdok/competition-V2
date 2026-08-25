package com.teaching.system.api.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.annotation.Excel.ColumnType;
import com.teaching.common.core.annotation.Excel.Type;
import com.teaching.common.core.constant.UserConstants;
import com.teaching.common.core.annotation.Excels;
import com.teaching.common.core.web.domain.BaseEntity;
import com.teaching.common.core.xss.Xss;

/**
 * 用户对象 sys_user
 *
 * @author teaching
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 用户ID */
//    @Excel(name = "用户序号", type = Type.EXPORT, cellType = ColumnType.NUMERIC, prompt = "用户编号")
    private Long userId;

    /** 机构编号 */
    private Long orgId;

    /** 用户账号 */
    @Excel(name = "用户账号")
    private String userName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Excel(name = "姓名")
    private String realName;

    /** 用户性别 */
    @Excel(name = "性别", readConverterExp = "0=男,1=女,2=未知")
    private String sex;

    /**
     * 用户类型
     */
    @Excel(name = "用户类型", readConverterExp = "0=管理员,2=C端用户")
    private String userType;

    /** 手机号码 */
    @Excel(name = "手机号码", cellType = ColumnType.TEXT)
    private String phonenumber;

    /** 用户昵称 */
//    @Excel(name = "用户名称")
    private String nickName;

    /** 用户邮箱 */
    @Excel(name = "邮箱")
    private String email;

    /**
     * 用户来源 管理端创建admin，pc注册pc，系统导入import
     */
    @Excel(name = "用户来源", readConverterExp = "admin=管理端创建,pc=PC注册,import=系统导入")
    private String userSources;

    //学校名称
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Excel(name = "所在学校")
    private String schoolName;

    /**
     * 所在学院
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Excel(name = "所在学院")
    private String institute;

    /**
     * 职务
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Excel(name = "职务")
    private String position;

    /** 用户头像地址 */
    private String avatar;

    /** 头像名称 */
    private String avatarName;

    /** 个人简介 */
    private String briefIntr;

    /** 密码 */
    private String password;

    /** 账号状态（0正常 1停用） */
    @Excel(name = "账号状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 最后登录IP */
//    @Excel(name = "最后登录IP", type = Type.EXPORT)
    private String loginIp;

    /** 最后登录时间 */
//    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Type.EXPORT)
    private Date loginDate;

    /** 密码最后更新时间 */
    private Date pwdUpdateDate;

    /** 机构对象 */
    /*@Excels({
        @Excel(name = "机构名称", targetAttr = "orgName", type = Type.EXPORT),
        @Excel(name = "机构负责人", targetAttr = "responsiblePer", type = Type.EXPORT)
    })*/
    private SysOrg org;


    //学校id
    private String school;

    /** 角色对象 */
    private List<SysRole> roles;

    /** 角色组 */
    private Long[] roleIds;

    /** 机构组 */
    private Long[] orgIds;

    /** 角色ID */
    private Long roleId;

    @Excel(name = "实名认证状态", readConverterExp = "1=未实名认证,2=实名认证待提交,3=实名认证待审核,4=人脸识别中,5=实名认证通过,6=实名认证失败")
    private String authStatus;

    /**
     * 实名认证信息
     */
    private AuthInfo authInfo;

    private List<IdentityInfo> identityInfoList = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Excel(name = "身份认证类型")
    private String identityTypes;

    /**
     * 身份信息切换后单个
     */
    private IdentityInfo identityInfo;

    /**
     * 身份类型
     */
    private String certificationType;


    /**
     * 短信验证码
     */
    private String msgCode;

    /**
     * 微信号
     */
    private String wxCode;

    private Set<String> permissions;

    /**
     * 同一个人主用户id
     */
    private String sysUserId;

    /**
     * 未支付订单数
     */
    private String noPayOrderNum;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    //导出类型 all全部不筛选，filter导出筛选过的数据
    private String exportType;

    /**
     * 微信用户的openId
     */
    private String openId;

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public String getIdentityTypes() {
        return identityTypes;
    }

    public void setIdentityTypes(String identityTypes) {
        this.identityTypes = identityTypes;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position.trim();
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getUserSources() {
        return userSources;
    }

    public void setUserSources(String userSources) {
        this.userSources = userSources;
    }

    public String getMsgCode() {
        return msgCode;
    }

    public void setMsgCode(String msgCode) {
        this.msgCode = msgCode;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }


    public AuthInfo getAuthInfo() {
        return authInfo;
    }

    public void setAuthInfo(AuthInfo authInfo) {
        this.authInfo = authInfo;
    }

    public SysUser()
    {

    }

    public SysUser(Long userId)
    {
        this.userId = userId;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public boolean isAdmin()
    {
        return isAdmin(this.userId);
    }

    public static boolean isAdmin(Long userId)
    {
        return UserConstants.isAdmin(userId);
    }

    @Xss(message = "用户昵称不能包含脚本字符")
    @Size(min = 0, max = 30, message = "用户昵称长度不能超过30个字符")
    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }

    @Xss(message = "用户账号不能包含脚本字符")
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 0, max = 30, message = "用户账号长度不能超过30个字符")
    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    @Email(message = "邮箱格式不正确")
    @Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    @Size(min = 0, max = 11, message = "手机号码长度不能超过11个字符")
    public String getPhonenumber()
    {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber)
    {
        this.phonenumber = phonenumber;
    }

    public String getSex()
    {
        return sex;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getLoginIp()
    {
        return loginIp;
    }

    public void setLoginIp(String loginIp)
    {
        this.loginIp = loginIp;
    }

    public Date getLoginDate()
    {
        return loginDate;
    }

    public void setLoginDate(Date loginDate)
    {
        this.loginDate = loginDate;
    }

    public Date getPwdUpdateDate()
    {
        return pwdUpdateDate;
    }

    public void setPwdUpdateDate(Date pwdUpdateDate) {
        this.pwdUpdateDate = pwdUpdateDate;
    }

    public List<SysRole> getRoles()
    {
        return roles;
    }

    public void setRoles(List<SysRole> roles)
    {
        this.roles = roles;
    }

    public Long[] getRoleIds()
    {
        return roleIds;
    }

    public void setRoleIds(Long[] roleIds)
    {
        this.roleIds = roleIds;
    }

    public Long[] getOrgIds() {
        return orgIds;
    }

    public void setOrgIds(Long[] orgIds) {
        this.orgIds = orgIds;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public String getAuthStatus() {
        return authStatus;
    }

    public void setAuthStatus(String authStatus) {
        this.authStatus = authStatus;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public IdentityInfo getIdentityInfo() {
        return identityInfo;
    }

    public void setIdentityInfo(IdentityInfo identityInfo) {
        this.identityInfo = identityInfo;
    }

    public List<IdentityInfo> getIdentityInfoList() {
        return identityInfoList;
    }

    public void setIdentityInfoList(List<IdentityInfo> identityInfoList) {
        this.identityInfoList = identityInfoList;
    }

    public String getAvatarName() {
        return avatarName;
    }

    public void setAvatarName(String avatarName) {
        this.avatarName = avatarName;
    }

    public String getBriefIntr() {
        return briefIntr;
    }

    public void setBriefIntr(String briefIntr) {
        this.briefIntr = briefIntr;
    }

    public SysOrg getOrg() {
        return org;
    }

    public void setOrg(SysOrg org) {
        this.org = org;
    }

    public String getCertificationType() {
        return certificationType;
    }

    public void setCertificationType(String certificationType) {
        this.certificationType = certificationType;
    }

    public String getWxCode() {
        return wxCode;
    }

    public void setWxCode(String wxCode) {
        this.wxCode = wxCode;
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
        this.schoolName = schoolName.trim();
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public String getNoPayOrderNum() {
        return noPayOrderNum;
    }

    public void setNoPayOrderNum(String noPayOrderNum) {
        this.noPayOrderNum = noPayOrderNum;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getSysUserId() {
        return sysUserId;
    }

    public void setSysUserId(String sysUserId) {
        this.sysUserId = sysUserId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("userId", getUserId())
            .append("orgId", getOrgId())
            .append("userName", getUserName())
            .append("nickName", getNickName())
            .append("email", getEmail())
            .append("phonenumber", getPhonenumber())
            .append("sex", getSex())
            .append("avatar", getAvatar())
            .append("password", getPassword())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("loginIp", getLoginIp())
            .append("loginDate", getLoginDate())
            .append("pwdUpdateDate", getPwdUpdateDate())
            .append("authStatus", getAuthStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
                .append("wxCode", getWxCode())
            .toString();
    }
}
