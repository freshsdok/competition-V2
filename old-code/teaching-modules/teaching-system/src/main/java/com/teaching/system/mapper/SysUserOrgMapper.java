package com.teaching.system.mapper;

import java.util.List;
import com.teaching.system.domain.SysUserOrg;

/**
 * 用户机构关联关系Mapper接口
 * 
 * @author teaching
 * @date 2025-10-24
 */
public interface SysUserOrgMapper 
{
    /**
     * 查询用户机构关联关系
     * 
     * @param relaId 用户机构关联关系主键
     * @return 用户机构关联关系
     */
    public SysUserOrg selectSysUserOrgByRelaId(Long relaId);

    /**
     * 查询用户机构关联关系列表
     * 
     * @param sysUserOrg 用户机构关联关系
     * @return 用户机构关联关系集合
     */
    public List<SysUserOrg> selectSysUserOrgList(SysUserOrg sysUserOrg);

    /**
     * 新增用户机构关联关系
     * 
     * @param sysUserOrg 用户机构关联关系
     * @return 结果
     */
    public int insertSysUserOrg(SysUserOrg sysUserOrg);

    /**
     * 修改用户机构关联关系
     * 
     * @param sysUserOrg 用户机构关联关系
     * @return 结果
     */
    public int updateSysUserOrg(SysUserOrg sysUserOrg);

    /**
     * 批量新增用户机构关联关系
     *
     * @param sysUserOrgList 用户机构关联关系列表
     * @return 结果
     */
    public int batchUserOrg(List<SysUserOrg> sysUserOrgList);

    /**
     * 删除用户机构关联关系
     * 
     * @param relaId 用户机构关联关系主键
     * @return 结果
     */
    public int deleteSysUserOrgByUserId(Long relaId);

    /**
     * 批量删除用户机构关联关系
     * 
     * @param relaIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysUserOrgByUserIds(Long[] relaIds);
}
