package com.teaching.competition.service;

import com.teaching.system.api.domain.TeamMemberRela;

import java.util.List;

/**
 * 团队关联关系Service接口
 * 
 * @author teaching
 * @date 2025-12-25
 */
public interface ITeamMemberRelaService 
{
    /**
     * 查询团队关联关系
     * 
     * @param relaId 团队关联关系主键
     * @return 团队关联关系
     */
    public TeamMemberRela selectTeamMemberRelaByRelaId(Long userId,String teamCode);

    /**
     * 查询团队关联关系列表
     * 
     * @param teamMemberRela 团队关联关系
     * @return 团队关联关系集合
     */
    public List<TeamMemberRela> selectTeamMemberRelaList(TeamMemberRela teamMemberRela);

    /**
     * 新增团队关联关系
     * 
     * @param teamMemberRela 团队关联关系
     * @return 结果
     */
    public int insertTeamMemberRela(TeamMemberRela teamMemberRela);

    /**
     * 修改团队关联关系
     * 
     * @param teamMemberRela 团队关联关系
     * @return 结果
     */
    public int updateTeamMemberRela(TeamMemberRela teamMemberRela);

    /**
     * 批量删除团队关联关系
     * 
     * @param relaIds 需要删除的团队关联关系主键集合
     * @return 结果
     */
    public int deleteTeamMemberRelaByRelaIds(String[] teamCodes);

    /**
     * 删除团队关联关系信息
     * 
     * @param relaId 团队关联关系主键
     * @return 结果
     */
    public int deleteTeamMemberRelaByRelaId(Long relaId);
}
