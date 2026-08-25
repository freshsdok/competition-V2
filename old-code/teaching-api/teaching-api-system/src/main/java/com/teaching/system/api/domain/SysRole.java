package com.teaching.system.api.domain;

import java.util.Date;
import java.util.List;
import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.annotation.Excel.ColumnType;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 角色表 sys_role
 *
 * @author teaching
 */
public class SysRole extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 角色ID */
    @Excel(name = "角色序号", cellType = ColumnType.NUMERIC)
    private Long roleId;

    /** 角色名称 */
    @Excel(name = "角色名称")
    private String roleName;

    /** 角色权限 */
    @Excel(name = "编码")
    private String roleKey;

    /** 角色排序 */
    @Excel(name = "显示顺序")
    private Integer roleSort;

    /** 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示） */
    private boolean menuCheckStrictly;

    /** 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ） */
    private boolean deptCheckStrictly;

    /** 组织树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ） */
    private boolean orgCheckStrictly;

    /** 角色排除标识（0：不排除 1：排除） */
    @Excel(name = "独立角色", readConverterExp = "false=否,true=是")
    private boolean exclusionFlag;

    /** 角色状态（0正常 1停用） */
    @Excel(name = "角色状态", readConverterExp = "0=正常,1=停用")
    private String status;

    @Excel(name = "创建时间",dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 获取权限方式（1.按角色 2.按用户） */
    private String acquMethod;

    /** 角色类型 */
    private String roleType;

    /** 锁定标记（0：未锁定 1：已锁定） */
    private boolean lockFlag;


    /** 独立角色级别 */
    private String exclusionLevel;

    /** 已认证学生标识 */
    private boolean studentFlag;

    /** 已认证教师标识 */
    private boolean teacherFlag;

    /** 已实名认证标识 */
    private boolean authFlag;

    /** 比赛用户标识 */
    private boolean competitionFlag;

    /** 比赛队长标识 */
    private boolean captainFlag;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    /** 用户是否存在此角色标识 默认不存在 */
    private boolean flag = false;

    /** 菜单组 */
    private List<SysRoleMenu> menuIds;

    /** 部门组（数据权限） */
    private Long[] deptIds;

    /** 机构组（数据权限） */
    private Long[] orgIds;

    /** 角色菜单权限 */
    private Set<String> permissions;

    public SysRole()
    {

    }

    public SysRole(Long roleId)
    {
        this.roleId = roleId;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }

    public boolean isAdmin()
    {
        return isAdmin(this.roleId);
    }

    public static boolean isAdmin(Long roleId)
    {
        return roleId != null && 1L == roleId;
    }

    @NotBlank(message = "角色名称不能为空")
    @Size(min = 0, max = 30, message = "角色名称长度不能超过30个字符")
    public String getRoleName()
    {
        return roleName;
    }

    public void setRoleName(String roleName)
    {
        this.roleName = roleName;
    }

    @NotBlank(message = "权限字符不能为空")
    @Size(min = 0, max = 100, message = "权限字符长度不能超过100个字符")
    public String getRoleKey()
    {
        return roleKey;
    }

    public void setRoleKey(String roleKey)
    {
        this.roleKey = roleKey;
    }

    @NotNull(message = "显示顺序不能为空")
    public Integer getRoleSort()
    {
        return roleSort;
    }

    public void setRoleSort(Integer roleSort)
    {
        this.roleSort = roleSort;
    }

    public boolean isMenuCheckStrictly()
    {
        return menuCheckStrictly;
    }

    public void setMenuCheckStrictly(boolean menuCheckStrictly)
    {
        this.menuCheckStrictly = menuCheckStrictly;
    }

    public boolean isDeptCheckStrictly()
    {
        return deptCheckStrictly;
    }

    public void setDeptCheckStrictly(boolean deptCheckStrictly)
    {
        this.deptCheckStrictly = deptCheckStrictly;
    }

    public boolean isOrgCheckStrictly() {
        return orgCheckStrictly;
    }

    public void setOrgCheckStrictly(boolean orgCheckStrictly) {
        this.orgCheckStrictly = orgCheckStrictly;
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

    public boolean isFlag()
    {
        return flag;
    }

    public void setFlag(boolean flag)
    {
        this.flag = flag;
    }

    public List<SysRoleMenu> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<SysRoleMenu> menuIds) {
        this.menuIds = menuIds;
    }

    public Long[] getDeptIds()
    {
        return deptIds;
    }

    public void setDeptIds(Long[] deptIds)
    {
        this.deptIds = deptIds;
    }

    public Set<String> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(Set<String> permissions)
    {
        this.permissions = permissions;
    }

    public String getAcquMethod() {
        return acquMethod;
    }

    public void setAcquMethod(String acquMethod) {
        this.acquMethod = acquMethod;
    }

    public String getRoleType() {
        return roleType;
    }

    public void setRoleType(String roleType) {
        this.roleType = roleType;
    }

    public boolean isLockFlag() {
        return lockFlag;
    }

    public void setLockFlag(boolean lockFlag) {
        this.lockFlag = lockFlag;
    }

    public boolean isExclusionFlag() {
        return exclusionFlag;
    }

    public void setExclusionFlag(boolean exclusionFlag) {
        this.exclusionFlag = exclusionFlag;
    }

    public Long[] getOrgIds() {
        return orgIds;
    }

    public void setOrgIds(Long[] orgIds) {
        this.orgIds = orgIds;
    }

    public String getExclusionLevel() {
        return exclusionLevel;
    }

    public void setExclusionLevel(String exclusionLevel) {
        this.exclusionLevel = exclusionLevel;
    }

    public boolean isStudentFlag() {
        return studentFlag;
    }

    public void setStudentFlag(boolean studentFlag) {
        this.studentFlag = studentFlag;
    }

    public boolean isTeacherFlag() {
        return teacherFlag;
    }

    public void setTeacherFlag(boolean teacherFlag) {
        this.teacherFlag = teacherFlag;
    }

    public boolean isAuthFlag() {
        return authFlag;
    }

    public void setAuthFlag(boolean authFlag) {
        this.authFlag = authFlag;
    }

    public boolean isCompetitionFlag() {
        return competitionFlag;
    }

    public void setCompetitionFlag(boolean competitionFlag) {
        this.competitionFlag = competitionFlag;
    }

    public boolean isCaptainFlag() {
        return captainFlag;
    }

    public void setCaptainFlag(boolean captainFlag) {
        this.captainFlag = captainFlag;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("roleId", getRoleId())
            .append("roleName", getRoleName())
            .append("roleKey", getRoleKey())
            .append("roleSort", getRoleSort())
            .append("menuCheckStrictly", isMenuCheckStrictly())
            .append("deptCheckStrictly", isDeptCheckStrictly())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("acquMethod", getAcquMethod())
            .append("roleType", getRoleType())
            .append("lockFlag", isLockFlag())
            .append("exclusionFlag", isExclusionFlag())
                .append("studentFlag", isStudentFlag())
                .append("teacherFlag", isTeacherFlag())
                .append("authFlag", isAuthFlag())
                .append("competitionFlag", isCompetitionFlag())
                .append("captainFlag", isCaptainFlag())
            .append("orgCheckStrictly", isOrgCheckStrictly())
            .append("exclusionLevel", getExclusionLevel())
            .toString();
    }
}
