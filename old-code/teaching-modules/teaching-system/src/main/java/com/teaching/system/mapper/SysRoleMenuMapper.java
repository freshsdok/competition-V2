package com.teaching.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.teaching.system.api.domain.SysRoleMenu;

/**
 * 角色与菜单关联表 数据层
 * 
 * @author teaching
 */
@Mapper
public interface SysRoleMenuMapper
{
    /**
     * 查询菜单使用数量
     * 
     * @param menuId 菜单ID
     * @return 结果
     */
    public int checkMenuExistRole(Long menuId);

    /**
     * 通过角色ID删除角色和菜单关联
     * 
     * @param roleId 角色ID
     * @return 结果
     */
    public int deleteRoleMenuByRoleId(Long roleId);

    /**
     * 批量删除角色菜单关联信息
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteRoleMenu(Long[] ids);


    /**
     * 查询角色和菜单关联
     *
     * @param roleId 角色和菜单关联主键
     * @return 角色和菜单关联
     */
    public List<SysRoleMenu> selectSysRoleMenuByRoleId(Long roleId);

    /**
     * 批量新增角色和菜单关联
     *
     * @param roleMenuList 角色和菜单关联
     * @return 结果
     */
    public int insertSysRoleMenu(List<SysRoleMenu> roleMenuList);

    /**
     * 修改角色和菜单关联
     *
     * @param sysRoleMenu 角色和菜单关联
     * @return 结果
     */
    public int updateSysRoleMenu(SysRoleMenu sysRoleMenu);
}
