package com.teaching.system.service;

import com.teaching.system.domain.SysUserGroup;

import java.util.List;
import java.util.Map;

/**
 * 用户组管理Service接口
 *
 * @author teaching
 * @date 2026-01-07
 */
public interface ISysUserGroupService {
    /**
     * 查询用户组管理
     *
     * @param id 用户组管理主键
     * @return 用户组管理
     */
    public SysUserGroup selectSysUserGroupById(Long id);

    /**
     * 查询用户组管理列表
     *
     * @param sysUserGroup 用户组管理
     * @return 用户组管理集合
     */
    public List<SysUserGroup> selectSysUserGroupList(SysUserGroup sysUserGroup);

    /**
     * 新增用户组管理
     *
     * @param sysUserGroup 用户组管理
     * @return 结果
     */
    public int insertSysUserGroup(SysUserGroup sysUserGroup);

    /**
     * 修改用户组管理
     *
     * @param sysUserGroup 用户组管理
     * @return 结果
     */
    public int updateSysUserGroup(SysUserGroup sysUserGroup);

    /**
     * 批量删除用户组管理
     *
     * @param ids 需要删除的用户组管理主键集合
     * @return 结果
     */
    public int deleteSysUserGroupByIds(Long[] ids);

    /**
     * 删除用户组管理信息
     *
     * @param id 用户组管理主键
     * @return 结果
     */
    public int deleteSysUserGroupById(Long id);

    /**
     * 根据用户组id查询下面的用户信息
     * @param groupId
     * @return
     */
    public List<Map<String,Object>> getUserByUserGroup(Long groupId);

    /**
     * 根据用户组ids查询，并保持ids的顺序
     * @param userGroupIds
     * @return
     */
    public List<SysUserGroup> getSysUserGroupByIds(List<Long> userGroupIds);
}
