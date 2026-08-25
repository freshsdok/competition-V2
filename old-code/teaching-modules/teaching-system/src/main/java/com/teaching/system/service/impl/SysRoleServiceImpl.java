package com.teaching.system.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.system.domain.SysRoleOrg;
import com.teaching.system.mapper.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.alibaba.fastjson2.JSON;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.UserConstants;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.SpringUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.core.utils.ip.IpUtils;
import com.teaching.common.datascope.annotation.DataScope;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.RemoteAuditLogService;
import com.teaching.system.api.domain.SysAuditLog;
import com.teaching.system.api.domain.SysRole;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.api.domain.SysRoleMenu;
import com.teaching.system.domain.SysUserRole;
import com.teaching.system.service.ISysRoleService;

/**
 * 角色 业务层处理
 *
 * @author teaching
 */
@Service
public class SysRoleServiceImpl implements ISysRoleService
{
    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired(required = false)
    private RemoteAuditLogService remoteAuditLogService;

    @Autowired
    private SysRoleOrgMapper sysRoleOrgMapper;

    /**
     * 根据条件分页查询角色数据
     *
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    @Override
    @DataScope(orgAlias = "d")
    public List<SysRole> selectRoleList(SysRole role) {
        // 将角色菜单权限
        List<SysRole> sysRoleList = roleMapper.selectRoleList(role);
        for (SysRole sysRole : sysRoleList) {
            List<SysRoleMenu> roleMenuList = roleMenuMapper.selectSysRoleMenuByRoleId(sysRole.getRoleId());
            role.setMenuIds(roleMenuList);
        }
        return sysRoleList;
    }

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    public List<SysRole> selectRolesByUserId(Long userId)
    {
        List<SysRole> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        List<SysRole> roles = selectRoleAll();
        for (SysRole role : roles)
        {
            for (SysRole userRole : userRoles)
            {
                if (role.getRoleId().longValue() == userRole.getRoleId().longValue())
                {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId)
    {
        List<SysRole> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms) {
            if (StringUtils.isNotNull(perm)) {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    @Override
    public List<SysRole> selectRoleAll()
    {
        return SpringUtils.getAopProxy(this).selectRoleList(new SysRole());
    }

    @Override
    public SysRole selectRoleByFlag(SysRole role) {
        return roleMapper.selectRoleByFlag(role);
    }

    /**
     * 根据用户ID获取角色选择框列表
     *
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    @Override
    public List<Long> selectRoleListByUserId(Long userId)
    {
        return roleMapper.selectRoleListByUserId(userId);
    }

    /**
     * 通过角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    @Override
    public SysRole selectRoleById(Long roleId)
    {
        return roleMapper.selectRoleById(roleId);
    }

    /**
     * 校验角色名称是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleNameUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleNameUnique(role.getRoleName());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验角色权限是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public boolean checkRoleKeyUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleKeyUnique(role.getRoleKey());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public boolean checkIndependentRoleUnique(SysRole role) {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole sysRole = roleMapper.checkIndependentRoleUnique(role);
        if (StringUtils.isNotNull(sysRole) && sysRole.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.UNIQUE;
        }
        return UserConstants.NOT_UNIQUE;
    }

    /**
     * 校验角色是否允许操作
     *
     * @param role 角色信息
     */
    @Override
    public void checkRoleAllowed(SysRole role)
    {
        if (StringUtils.isNotNull(role.getRoleId()) && role.isAdmin())
        {
            throw new ServiceException("不允许操作超级管理员角色");
        }
    }

    /**
     * 校验角色是否有数据权限
     *
     * @param roleIds 角色id
     */
    @Override
    public void checkRoleDataScope(Long... roleIds)
    {
        if (!SysUser.isAdmin(SecurityUtils.getUserId()))
        {
            for (Long roleId : roleIds)
            {
                SysRole role = new SysRole();
                role.setRoleId(roleId);
                List<SysRole> roles = SpringUtils.getAopProxy(this).selectRoleList(role);
                if (StringUtils.isEmpty(roles))
                {
                    throw new ServiceException("没有权限访问角色数据！");
                }
            }
        }
    }

    /**
     * 校验是否同时包含独立和非独立角色
     * @param roleIds
     */
    @Override
    public void checkIndependentRoleCoexistence(Long... roleIds) {
        if (roleIds == null || roleIds.length == 0) {
            return;
        }
        List<SysRole> sysRoles = roleMapper.selectRoleByIds(roleIds);
        // 独立角色(exclusionFlag=1)
        Set<SysRole> independentRoles = sysRoles.stream()
                .filter(SysRole::isExclusionFlag)
                .collect(Collectors.toSet());
        // 非独立角色(exclusionFlag=0)
        Set<SysRole> nonIndependentRoles = sysRoles.stream()
                .filter(role -> !role.isExclusionFlag())
                .collect(Collectors.toSet());
        if (!independentRoles.isEmpty() && !nonIndependentRoles.isEmpty()) {
            throw new ServiceException("独立角色和非独立角色不能共存");
        }
        if(!independentRoles.isEmpty() && independentRoles.size() > 1){
            throw new ServiceException("独立角色只能有一个");
        }
    }


    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    public int countUserRoleByRoleId(Long roleId)
    {
        return userRoleMapper.countUserRoleByRoleId(roleId);
    }

    /**
     * 新增保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRole(SysRole role) {
        // 新增角色信息
        // 学生用户角色  比赛用户角色  教师用户角色  比赛队长角色  实名认证角色唯一性校验
        if(role.isAuthFlag() || role.isCompetitionFlag() || role.isTeacherFlag() || role.isCaptainFlag() || role.isStudentFlag()){
            SysRole sysRole = roleMapper.selectRoleByFlag(role);
            if(sysRole != null){
                throw new GlobalException("该角色已存在");
            }
        }
        roleMapper.insertRole(role);
        return insertRoleMenu(role);
    }

    /**
     * 修改保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRole(SysRole role) {
        // 学生用户角色  比赛用户角色  教师用户角色  比赛队长角色  实名认证角色唯一性校验
        if(role.isAuthFlag() || role.isCompetitionFlag() || role.isTeacherFlag() || role.isCaptainFlag() || role.isStudentFlag()){
            SysRole sysRole = roleMapper.selectRoleByFlag(role);
            if(sysRole != null && sysRole.getRoleId().longValue() != role.getRoleId().longValue()){
                throw new GlobalException("该角色已存在");
            }
        }
        // 修改角色信息
        roleMapper.updateRole(role);
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(role.getRoleId());
        return insertRoleMenu(role);
    }

    /**
     * 修改角色状态
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    public int updateRoleStatus(SysRole role)
    {
        return roleMapper.updateRole(role);
    }

    /**
     * 修改数据权限信息
     *
     * @param role 角色信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int authDataScope(SysRole role)
    {
        // 获取修改前的角色信息
        SysRole oldRole = roleMapper.selectRoleById(role.getRoleId());

        // 修改角色信息
        roleMapper.updateRole(role);
        // 删除角色与机构关联
        sysRoleOrgMapper.deleteSysRoleOrgByRoleId(role.getRoleId());
        // 新增角色和机构信息（数据权限）
        int result = insertRoleOrg(role);

        // 记录数据权限修改审计日志
        recordDataScopeAudit(oldRole, role);

        return result;
    }

    /**
     * 新增角色菜单信息
     *
     * @param role 角色对象
     */
    public int insertRoleMenu(SysRole role)
    {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenu> list = new ArrayList<SysRoleMenu>();
        for (SysRoleMenu sysRoleMenu : role.getMenuIds()) {
            sysRoleMenu.setRoleId(role.getRoleId());
            sysRoleMenu.setCreateBy(role.getCreateBy());
            list.add(sysRoleMenu);
        }
        if (list.size() > 0) {
            rows = roleMenuMapper.insertSysRoleMenu(list);
        }
        return rows;
    }

    /**
     * 新增角色机构信息(数据权限)
     *
     * @param role 角色对象
     */
    public int insertRoleOrg(SysRole role)
    {
        int rows = 1;
        // 新增角色与机构（数据权限）管理
        List<SysRoleOrg> list = new ArrayList<SysRoleOrg>();
        for (Long orgId : role.getOrgIds())
        {
            SysRoleOrg rd = new SysRoleOrg();
            rd.setRoleId(role.getRoleId());
            rd.setOrgId(orgId);
            list.add(rd);
        }
        if (list.size() > 0)
        {
            rows = sysRoleOrgMapper.batchRoleOrg(list);
        }
        return rows;
    }

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleById(Long roleId)
    {
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenuByRoleId(roleId);
        // 删除角色与机构关联
        sysRoleOrgMapper.deleteSysRoleOrgByRoleId(roleId);
        return roleMapper.deleteRoleById(roleId);
    }

    /**
     * 批量删除角色信息
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoleByIds(Long[] roleIds)
    {
        for (Long roleId : roleIds)
        {
            checkRoleAllowed(new SysRole(roleId));
            checkRoleDataScope(roleId);
            SysRole role = selectRoleById(roleId);
            if (countUserRoleByRoleId(roleId) > 0)
            {
                throw new ServiceException(String.format("%1$s已分配,不能删除", role.getRoleName()));
            }
        }
        // 删除角色与菜单关联
        roleMenuMapper.deleteRoleMenu(roleIds);
        // 删除角色与机构关联
        sysRoleOrgMapper.deleteSysRoleOrgByRoleIds(roleIds);
        return roleMapper.deleteRoleByIds(roleIds);
    }

    /**
     * 取消授权用户角色
     *
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    @Override
    public int deleteAuthUser(SysUserRole userRole)
    {
        return userRoleMapper.deleteUserRoleInfo(userRole);
    }

    /**
     * 批量取消授权用户角色
     *
     * @param roleId 角色ID
     * @param userIds 需要取消授权的用户数据ID
     * @return 结果
     */
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds)
    {
        return userRoleMapper.deleteUserRoleInfos(roleId, userIds);
    }

    /**
     * 批量选择授权用户角色
     *
     * @param roleId 角色ID
     * @param userIds 需要授权的用户数据ID
     * @return 结果
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds)
    {
        // 新增用户与角色管理
        List<SysUserRole> list = new ArrayList<SysUserRole>();
        for (Long userId : userIds)
        {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return userRoleMapper.batchUserRole(list);
    }

    /**
     * 记录数据权限修改审计日志
     *
     * @param oldRole 修改前的角色信息
     * @param newRole 修改后的角色信息
     */
    private void recordDataScopeAudit(SysRole oldRole, SysRole newRole)
    {
        try
        {
            if (remoteAuditLogService == null)
            {
                return;
            }

            SysAuditLog auditLog = new SysAuditLog();
            auditLog.setAuditType("权限审计");
            auditLog.setAuditCategory("权限变更");
            auditLog.setEventName("修改数据权限");
            auditLog.setEventDesc("修改角色[" + newRole.getRoleName() + "]的数据权限范围");

            // 用户信息
            Long userId = SecurityUtils.getUserId();
            String username = SecurityUtils.getUsername();
            auditLog.setUserId(userId);
            auditLog.setUserName(username);

            // 操作信息
            auditLog.setOperationType("数据权限修改");
            auditLog.setOperationModule("角色管理");

            // 获取请求信息
            HttpServletRequest request = getRequest();
            if (request != null)
            {
                auditLog.setRequestUrl(request.getRequestURI());
                auditLog.setRequestMethod(request.getMethod());
            }

            // 安全信息
            auditLog.setIpAddress(IpUtils.getIpAddr());

            // 数据变更信息
            auditLog.setDataId(String.valueOf(newRole.getRoleId()));
            auditLog.setDataType("角色数据权限");

            // 记录变更前后的数据权限范围
            String oldDataScope = buildDataScopeDesc(oldRole);
            String newDataScope = buildDataScopeDesc(newRole);
            auditLog.setOldValue(oldDataScope);
            auditLog.setNewValue(newDataScope);

            // 风险级别 - 数据权限修改属于中等风险
            auditLog.setRiskLevel("MEDIUM");

            // 审计状态
            auditLog.setAuditStatus("0"); // 待审计
            auditLog.setIsAbnormal("0");
            auditLog.setOperationTime(new Date());

            // 异步保存审计日志
            remoteAuditLogService.saveAuditLog(auditLog, SecurityConstants.INNER);
        }
        catch (Exception e)
        {
            // 记录审计日志失败不影响主业务
        }
    }

    /**
     * 构建数据权限范围描述
     *
     * @param role 角色信息
     * @return 数据权限描述
     */
    private String buildDataScopeDesc(SysRole role)
    {
        if (role == null)
        {
            return "";
        }

        StringBuilder desc = new StringBuilder();
        desc.append("{\"dataScope\":\"");
        List<SysRoleMenu> menuIds = role.getMenuIds();
        if (CollectionUtils.isNotEmpty(menuIds)) {
            for (SysRoleMenu sysRoleMenu : menuIds) {
                desc.append(sysRoleMenu.getMenuId()).append(":");
                String dataScope = sysRoleMenu.getDataScope();
                if ("1".equals(dataScope))
                {
                    desc.append("全部数据权限");
                }
                else if ("2".equals(dataScope))
                {
                    desc.append("自定义数据权限");
                }
                else if ("3".equals(dataScope))
                {
                    desc.append("本机构数据权限");
                }
                else if ("4".equals(dataScope))
                {
                    desc.append("本机构及以下数据权限");
                }
                else if ("5".equals(dataScope))
                {
                    desc.append("仅本人数据权限");
                }
                desc.append(";");
            }
        }

        desc.append("\",\"orgIds\":");
        if (role.getOrgIds() != null && role.getOrgIds().length > 0)
        {
            desc.append(JSON.toJSONString(role.getOrgIds()));
        }
        else
        {
            desc.append("[]");
        }
        desc.append("}");

        return desc.toString();
    }

    /**
     * 获取当前HTTP请求
     *
     * @return HttpServletRequest
     */
    private HttpServletRequest getRequest()
    {
        try
        {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
