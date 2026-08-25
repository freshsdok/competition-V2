package com.teaching.system.service;

import java.util.List;
import java.util.Set;

import com.teaching.system.api.domain.SysRoleMenu;
import com.teaching.system.api.domain.SysUser;

/**
 * 权限信息 服务层
 * 
 * @author teaching
 */
public interface ISysPermissionService
{
    /**
     * 获取角色数据权限
     * 
     * @param userId 用户Id
     * @return 角色权限信息
     */
    public Set<String> getRolePermission(SysUser user);

    /**
     * 获取菜单数据权限
     * 
     * @param userId 用户Id
     * @return 菜单权限信息
     */
    public Set<String> getMenuPermission(SysUser user);

    public void getRoleMenuList(SysUser user);
}
