package com.teaching.system.mapper;

import com.teaching.system.domain.SysUserGroup;
import com.teaching.system.domain.SysUserGroupCompetitionRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户组管理Mapper接口
 *
 * @author teaching
 * @date 2026-01-07
 */
public interface SysUserGroupMapper {
    /**
     * 查询用户组管理
     *
     * @param id 用户组管理主键
     * @return 用户组管理
     */
    public SysUserGroup selectSysUserGroupById(Long id);

    public List<SysUserGroup> selectSysUserGroupByIds(@Param("ids") List<Long> ids);

    public List<SysUserGroup> selectSysUserGroupByUserId(String userId);

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
     * 删除用户组管理
     *
     * @param id 用户组管理主键
     * @return 结果
     */
    public int deleteSysUserGroupById(@Param("id") Long id, @Param("updateBy") String updateBy);

    /**
     * 批量删除用户组管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysUserGroupByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);

    /**
     * 批量删除用户组关联赛事关系
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysUserGroupCompetitionRelationByUserGroupIds(Long[] ids);

    /**
     * 批量新增用户组关联赛事关系
     *
     * @param sysUserGroupCompetitionRelationList 用户组关联赛事关系列表
     * @return 结果
     */
    public int batchSysUserGroupCompetitionRelation(List<SysUserGroupCompetitionRelation> sysUserGroupCompetitionRelationList);


    /**
     * 通过用户组管理主键删除用户组关联赛事关系信息
     *
     * @param id 用户组管理ID
     * @return 结果
     */
    public int deleteSysUserGroupCompetitionRelationByUserGroupId(Long id);

    /**
     * 查询用户组关联赛事关系列表
     *
     * @param groupId 用户组管理ID
     * @return
     */
    public List<SysUserGroupCompetitionRelation> selectSysUserGroupCompetitionRelationList(Long groupId);

    public List<SysUserGroupCompetitionRelation> selectSysUserGroupCompetitionRelationListByUser(Long groupId, Long competitionSeriesId);

    public List<Map<String, Object>> selectSysUserGroupByUserGroupName(String userGroupName);

    /**
     * 查询用户组关联赛事关系列表(某个或全部)
     * @param id
     * @return
     */
    public List<SysUserGroup> selectSysUserGroupAllInfoByIdOrAll(Long id);
}
