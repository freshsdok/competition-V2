package com.teaching.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 教师导入临时对象 teacher_tmp_info
 * 
 * @author teaching
 * @date 2025-12-19
 */
public class TeacherTmpInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 用户id */
    private String id;

    /** 用户名 */
    @Excel(name = "用户名")
    private String userName;

    /** 密码 */
    @Excel(name = "密码")
    private String pwd;

    /** 手机号 */
    @Excel(name = "手机号")
    private String phone;

    /** 真实姓名 */
    @Excel(name = "真实姓名")
    private String realName;

    /** 身份证号 */
    @Excel(name = "身份证号")
    private String idCard;

    /** 邮箱 */
    @Excel(name = "邮箱")
    private String email;

    /** 角色 */
    @Excel(name = "角色")
    private String role;

    /** 省份 */
    @Excel(name = "省份")
    private String province;

    /** 学校名称 */
    @Excel(name = "学校名称")
    private String schoolName;

    /** 学院 */
    @Excel(name = "学院")
    private String institute;

    /** 职务 */
    @Excel(name = "职务")
    private String position;

    /** 职称 */
    @Excel(name = "职称")
    private String professionalTitle;

    /** 注册时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "注册时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date registTime;

    /** 备注 */
    @Excel(name = "备注")
    private String reamke;

    /** 教师资格证路径 */
    @Excel(name = "教师资格证路径")
    private String teacherUrl;

    /** 账号状态 */
    @Excel(name = "账号状态")
    private String status;

    public void setId(String id) 
    {
        this.id = id;
    }

    public String getId() 
    {
        return id;
    }

    public void setUserName(String userName) 
    {
        this.userName = userName;
    }

    public String getUserName() 
    {
        return userName;
    }

    public void setPwd(String pwd) 
    {
        this.pwd = pwd;
    }

    public String getPwd() 
    {
        return pwd;
    }

    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() 
    {
        return phone;
    }

    public void setRealName(String realName) 
    {
        this.realName = realName;
    }

    public String getRealName() 
    {
        return realName;
    }

    public void setIdCard(String idCard) 
    {
        this.idCard = idCard;
    }

    public String getIdCard() 
    {
        return idCard;
    }

    public void setEmail(String email) 
    {
        this.email = email;
    }

    public String getEmail() 
    {
        return email;
    }

    public void setRole(String role) 
    {
        this.role = role;
    }

    public String getRole() 
    {
        return role;
    }

    public void setProvince(String province) 
    {
        this.province = province;
    }

    public String getProvince() 
    {
        return province;
    }

    public void setSchoolName(String schoolName) 
    {
        this.schoolName = schoolName;
    }

    public String getSchoolName() 
    {
        return schoolName;
    }

    public void setInstitute(String institute) 
    {
        this.institute = institute;
    }

    public String getInstitute() 
    {
        return institute;
    }

    public void setPosition(String position) 
    {
        this.position = position;
    }

    public String getPosition() 
    {
        return position;
    }

    public void setProfessionalTitle(String professionalTitle) 
    {
        this.professionalTitle = professionalTitle;
    }

    public String getProfessionalTitle() 
    {
        return professionalTitle;
    }

    public void setRegistTime(Date registTime) 
    {
        this.registTime = registTime;
    }

    public Date getRegistTime() 
    {
        return registTime;
    }

    public void setReamke(String reamke) 
    {
        this.reamke = reamke;
    }

    public String getReamke() 
    {
        return reamke;
    }

    public void setTeacherUrl(String teacherUrl) 
    {
        this.teacherUrl = teacherUrl;
    }

    public String getTeacherUrl() 
    {
        return teacherUrl;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userName", getUserName())
            .append("pwd", getPwd())
            .append("phone", getPhone())
            .append("realName", getRealName())
            .append("idCard", getIdCard())
            .append("email", getEmail())
            .append("role", getRole())
            .append("province", getProvince())
            .append("schoolName", getSchoolName())
            .append("institute", getInstitute())
            .append("position", getPosition())
            .append("professionalTitle", getProfessionalTitle())
            .append("registTime", getRegistTime())
            .append("reamke", getReamke())
            .append("teacherUrl", getTeacherUrl())
            .append("status", getStatus())
            .toString();
    }
}
