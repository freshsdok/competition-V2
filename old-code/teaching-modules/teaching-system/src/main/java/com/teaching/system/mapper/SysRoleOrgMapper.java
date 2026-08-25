package com.teaching.system.mapper;

import java.util.List;

import com.teaching.system.domain.SysRoleDept;
import com.teaching.system.domain.SysRoleOrg;

/**
 * 角色和机构关联Mapper接口
 * 
 * @author teaching
 * @date 2025-10-24
 */
public interface SysRoleOrgMapper 
{
    /**
     * 查询角色和机构关联
     * 
     * @param roleId 角色和机构关联主键
     * @return 角色和机构关联
     */
    public SysRoleOrg selectSysRoleOrgByRoleId(Long roleId);

    /**
     * 查询角色和机构关联列表
     * 
     * @param sysRoleOrg 角色和机构关联
     * @return 角色和机构关联集合
     */
    public List<SysRoleOrg> selectSysRoleOrgList(SysRoleOrg sysRoleOrg);

    /**
     * 新增角色和机构关联
     * 
     * @param sysRoleOrg 角色和机构关联
     * @return 结果
     */
    public int insertSysRoleOrg(SysRoleOrg sysRoleOrg);

    /**
     * 修改角色和机构关联
     * 
     * @param sysRoleOrg 角色和机构关联
     * @return 结果
     */
    public int updateSysRoleOrg(SysRoleOrg sysRoleOrg);

    /**
     * 删除角色和机构关联
     * 
     * @param roleId 角色和机构关联主键
     * @return 结果
     */
    public int deleteSysRoleOrgByRoleId(Long roleId);

    /**
     * 批量删除角色和机构关联
     * 
     * @param roleIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysRoleOrgByRoleIds(Long[] roleIds);

    /**
     * 批量新增角色机构信息
     *
     * @param roleOrgList 角色机构列表
     * @return 结果
     */
    public int batchRoleOrg(List<SysRoleOrg> roleOrgList);
}
