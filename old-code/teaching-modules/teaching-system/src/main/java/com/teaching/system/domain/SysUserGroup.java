package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;
import java.util.Map;

/**
 * 用户组管理对象 sys_user_group
 *
 * @author teaching
 * @date 2026-01-07
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysUserGroup extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户组名称
     */
    @Excel(name = "用户组名称")
    private String name;

    /**
     * 用户组管理员
     */
    @JsonIgnore
    private String groupManager;

    /**
     * 用户组管理员列表
     */
    private List<Map<String, Object>> groupManagerList;


    /**
     * 身份认证类型
     */
    @Excel(name = "身份认证类型")
    private String identifyType;

    /**
     * 关联用户
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String userIds;
    /**
     * 关联用户信息
     */
    private List<Map<String, Object>> userList;

    /**
     * 关联黑名单用户
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String blackUserIds;
    /**
     * 关联黑名单用户信息
     */
    private List<Map<String, Object>> blackUserList;

    /**
     * 描述
     */
    @Excel(name = "描述")
    private String descripe;

    /**
     * 用户组关联赛事关系信息
     */
    private List<SysUserGroupCompetitionRelation> sysUserGroupCompetitionRelationList;

    private String delFlag;

    /**
     * 管理员标记，false不上当前记录的管理员，true为当前记录的管理员。默认为false
     */
    private boolean isAdmin = false;

    private String createUserName;

    /**
     * 用户组关联的用户数
     */
    private Long userIdCount;

    /**
     * 允许的角色名称
     */
    private String allowRoleName;

    public SysUserGroup() {
    }

    public SysUserGroup(Long id) {
        this.id = id;
    }

    public Long getUserIdCount() {
        return userIdCount;
    }

    public void setUserIdCount(Long userIdCount) {
        this.userIdCount = userIdCount;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = (name != null) ? name.trim() : null;
    }

    public String getName() {
        return name;
    }

    public void setGroupManager(String groupManager) {
        this.groupManager = (groupManager != null) ? groupManager.trim() : null;
    }

    public String getGroupManager() {
        return groupManager;
    }


    public void setIdentifyType(String identifyType) {
        this.identifyType = identifyType;
    }

    public String getIdentifyType() {
        return identifyType;
    }

    public void setUserIds(String userIds) {
        this.userIds = userIds;
    }

    public String getUserIds() {
        return userIds;
    }

    public void setDescripe(String descripe) {
        this.descripe = descripe;
    }

    public String getDescripe() {
        return descripe;
    }

    public List<SysUserGroupCompetitionRelation> getSysUserGroupCompetitionRelationList() {
        return sysUserGroupCompetitionRelationList;
    }

    public void setSysUserGroupCompetitionRelationList(List<SysUserGroupCompetitionRelation> sysUserGroupCompetitionRelationList) {
        this.sysUserGroupCompetitionRelationList = sysUserGroupCompetitionRelationList;
    }

    public List<Map<String, Object>> getGroupManagerList() {
        return groupManagerList;
    }

    public void setGroupManagerList(List<Map<String, Object>> groupManagerList) {
        this.groupManagerList = groupManagerList;
    }

    public List<Map<String, Object>> getUserList() {
        return userList;
    }

    public void setUserList(List<Map<String, Object>> userList) {
        this.userList = userList;
    }

    public String getBlackUserIds() {
        return blackUserIds;
    }

    public void setBlackUserIds(String blackUserIds) {
        this.blackUserIds = blackUserIds;
    }

    public List<Map<String, Object>> getBlackUserList() {
        return blackUserList;
    }

    public void setBlackUserList(List<Map<String, Object>> blackUserList) {
        this.blackUserList = blackUserList;
    }

    public String getAllowRoleName() {
        return allowRoleName;
    }

    public void setAllowRoleName(String allowRoleName) {
        this.allowRoleName = allowRoleName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("groupManager", getGroupManager())
                .append("identifyType", getIdentifyType())
                .append("userIds", getUserIds())
                .append("descripe", getDescripe())
                .append("sysUserGroupCompetitionRelationList", getSysUserGroupCompetitionRelationList())
                .toString();
    }
}
