package com.teaching.system.mapper;

import com.teaching.system.api.domain.SysOrg;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统机构信息Mapper接口
 * 
 * @author teaching
 * @date 2025-10-23
 */
public interface SysOrgMapper 
{
    /**
     * 查询系统机构信息
     * 
     * @param orgId 系统机构信息主键
     * @return 系统机构信息
     */
    public SysOrg selectSysOrgByOrgId(Long orgId);

    public SysOrg selectSysOrgByOrgName(String orgName);


    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId 角色ID
     * @param orgCheckStrictly 部门树选择项是否关联显示
     * @return 选中部门列表
     */
    public List<Long> selectSysOrgListByRoleId(@Param("roleId") Long roleId, @Param("orgCheckStrictly") boolean orgCheckStrictly);

    /**
     * 查询系统机构信息列表
     * 
     * @param sysOrg 系统机构信息
     * @return 系统机构信息集合
     */
    public List<SysOrg> selectSysOrgList(SysOrg sysOrg);

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
     * 删除系统机构信息
     * 
     * @param orgId 系统机构信息主键
     * @return 结果
     */
    public int deleteSysOrgByOrgId(Long orgId);

    /**
     * 批量删除系统机构信息
     * 
     * @param orgIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysOrgByOrgIds(Long[] orgIds);

    /**
     * 校验机构名称是否唯一
     *
     * @param orgName 机构名称
     * @param parentId 父部门ID
     * @return 结果
     */
    SysOrg checkOrgNameUnique(String orgName, Long parentId);

    /**
     * 查询机构是否存在用户
     *
     * @param orgId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    int checkOrgExistUser(Long orgId);

    /**
     * 根据ID是否存在字部门
     *
     * @param orgId 部门ID
     * @return 子部门数
     */
    int hasChildByOrgId(Long orgId);

    /**
     * 根据ID查询所有子部门（正常状态）
     *
     * @param orgId 机构id
     * @return 子部门数
     */
    int selectNormalChildrenOrgById(Long orgId);

    /**
     * 根据ID查询所有子部门
     *
     * @param orgId 机构id
     * @return 子部门数
     */
    List<SysOrg> selectChildrenOrgById(Long orgId);

    /**
     * 修改子元素关系
     *
     * @param children 子元素
     * @return 结果
     */
    int updateOrgChildren(@Param("orgs") List<SysOrg> children);

    /**
     * 修改机构状态
     *
     * @param orgIds 机构id
     * @return 父部门ID集合
     */
    int updateOrgStatusNormal(Long[] orgIds);
}
