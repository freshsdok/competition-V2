package com.teaching.system.service;

import com.teaching.system.api.domain.SysOrg;
import com.teaching.system.domain.vo.TreeSelect;

import java.util.List;

/**
 * 系统机构信息Service接口
 * 
 * @author teaching
 * @date 2025-10-23
 */
public interface ISysOrgService 
{
    /**
     * 查询系统机构信息
     * 
     * @param orgId 系统机构信息主键
     * @return 系统机构信息
     */
    public SysOrg selectSysOrgByOrgId(Long orgId);


    public List<Long> selectSysOrgListByRoleId(Long roleId);

    /**
     * 查询系统机构信息列表
     * 
     * @param sysOrg 系统机构信息
     * @return 系统机构信息集合
     */
    public List<SysOrg> selectSysOrgList(SysOrg sysOrg);

    /**
     * 查询系统机构信息树列表
     *
     * @param sysOrg 系统机构信息
     * @return 系统机构信息
     */
    List<TreeSelect> selectOrgTreeList(SysOrg sysOrg);

    /****
     * 构建前端所需要树结构
     *
     * @param sysOrgList 系统机构信息
     * @return 树结构列表
     */
    List<TreeSelect> buildOrgTreeSelect(List<SysOrg> sysOrgList);

    /**
     * 构建树结构
     *
     * @param sysOrgList 系统机构信息
     * @return 树结构列表
     */
    List<SysOrg> buildOrgTree(List<SysOrg> sysOrgList);

    /**
     * 新增系统机构信息
     * 
     * @param sysOrg 系统机构信息
     * @return 结果
     */
    public int insertSysOrg(SysOrg sysOrg);

    /**
     * 修改系统机构信息
     * 
     * @param sysOrg 系统机构信息
     * @return 结果
     */
    public int updateSysOrg(SysOrg sysOrg);

    /**
     * 批量删除系统机构信息
     * 
     * @param orgIds 需要删除的系统机构信息主键集合
     * @return 结果
     */
    public int deleteSysOrgByOrgIds(Long[] orgIds);

    /**
     * 删除系统机构信息信息
     * 
     * @param orgId 系统机构信息主键
     * @return 结果
     */
    public int deleteSysOrgByOrgId(Long orgId);

    /**
     * 校验部门名称是否唯一
     *
     * @param orgId 机构信息
     * @return 结果
     */
    void checkOrgDataScope(Long orgId);

    /**
     * 校验部门名称是否唯一
     *
     * @param org 机构信息
     * @return 结果
     */
    boolean checkOrgNameUnique(SysOrg org);

    /**
     * 查询部门是否存在子部门
     *
     * @param orgId 部门ID
     * @return 结果
     */
    int selectNormalChildrenOrgById(Long orgId);

    /**
     * 查询部门是否存在用户
     *
     * @param orgId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    boolean hasChildByOrgId(Long orgId);

    /**
     * 查询部门是否存在用户
     *
     * @param orgId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    boolean checkOrgExistUser(Long orgId);

}
