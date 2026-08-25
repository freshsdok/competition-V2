package com.teaching.system.mapper;

import java.util.List;
import java.util.Map;

import com.teaching.system.api.domain.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.teaching.system.domain.SysMenu;

/**
 * 菜单表 数据层
 *
 * @author teaching
 */
@Mapper
public interface SysMenuMapper
{
    /**
     * 查询系统菜单列表
     *
     * @param menu 菜单信息
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuList(SysMenu menu);

    /**
     * 查询系统菜单列表 pc
     * @param menu
     * @return
     */
    public List<SysMenu> selectPcMenuList(SysMenu menu);

    /**
     * pc menuList
     * @param menu
     * @return
     */
    public List<SysMenu> selectMenuPcList(SysMenu menu);

    /**
     * 根据用户所有权限
     *
     * @return 权限列表
     */
    public List<String> selectMenuPerms();

    /**
     * 根据用户查询系统菜单列表
     *
     * @param menu 菜单信息
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuListByUserId(SysMenu menu);

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    public List<String> selectMenuPermsByRoleId(Long roleId);

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public List<String> selectMenuPermsByUserId(Long userId);

    /**
     * 根据用户ID查询菜单
     *
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuTreeAll();

    /**
     * 根据用户ID查询菜单
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    public List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId 角色ID
     * @param menuCheckStrictly 菜单树选择项是否关联显示
     * @return 选中菜单列表
     */
    public List<SysRoleMenu> selectMenuListByRoleId(@Param("roleId") Long roleId, @Param("menuCheckStrictly") boolean menuCheckStrictly);

    /**
     * 根据菜单ID查询信息
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    public SysMenu selectMenuById(Long menuId);

    /**
     * 是否存在菜单子节点
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    public int hasChildByMenuId(Long menuId);

    /**
     * 新增菜单信息
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public int insertMenu(SysMenu menu);

    /**
     * 修改菜单信息
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public int updateMenu(SysMenu menu);

    /**
     * 删除菜单管理信息
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    public int deleteMenuById(Long menuId);

    /**
     * 校验菜单名称是否唯一
     *
     * @param menuName 菜单名称
     * @param parentId 父菜单ID
     * @return 结果
     */
    public SysMenu checkMenuNameUnique(@Param("platformType")String platformType,@Param("menuName") String menuName, @Param("parentId") Long parentId);

    /**
     * 根据菜单ID查询信息
     * @param menuId
     * @return
     */
    public Map<String,Object> selectColumnByMenuId(Long menuId);

    /**
     * 根据栏目ID查询详情列表
     * @param columnId
     * @return
     */
    public List<Map<String,Object>> selectDetailByColumnId(Long columnId);

    /**
     * 根据栏目ID查询文件列表
     * @param columnId
     * @return
     */
    public List<Map<String,Object>> selectFileByColumnId(Long columnId);

    /**
     * 根据详情ID查询详情信息
     * @param detailId
     * @return
     */
    public Map<String,Object> selectDetailById(Long detailId);
}
