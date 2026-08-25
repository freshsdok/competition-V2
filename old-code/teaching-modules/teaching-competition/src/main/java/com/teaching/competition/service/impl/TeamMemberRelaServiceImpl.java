package com.teaching.competition.service.impl;

import java.util.List;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.mapper.TeamMemberRelaMapper;
import com.teaching.competition.service.ITeamMemberRelaService;
import com.teaching.system.api.domain.TeamMemberRela;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 团队关联关系Service业务层处理
 * 
 * @author teaching
 * @date 2025-12-25
 */
@Service
public class TeamMemberRelaServiceImpl implements ITeamMemberRelaService
{
    @Autowired
    private TeamMemberRelaMapper teamMemberRelaMapper;

    /**
     * 查询团队关联关系
     * 
     * @param relaId 团队关联关系主键
     * @return 团队关联关系
     */
    @Override
    public TeamMemberRela selectTeamMemberRelaByRelaId(Long userId,String teamCode)
    {
        return teamMemberRelaMapper.selectTeamMemberRelaByRelaId(userId,teamCode);
    }

    /**
     * 查询团队关联关系列表
     * 
     * @param teamMemberRela 团队关联关系
     * @return 团队关联关系
     */
    @Override
    public List<TeamMemberRela> selectTeamMemberRelaList(TeamMemberRela teamMemberRela)
    {
        return teamMemberRelaMapper.selectTeamMemberRelaList(teamMemberRela);
    }

    /**
     * 新增团队关联关系
     * 
     * @param teamMemberRela 团队关联关系
     * @return 结果
     */
    @Override
    public int insertTeamMemberRela(TeamMemberRela teamMemberRela)
    {
        teamMemberRela.setCreateTime(DateUtils.getNowDate());
        return teamMemberRelaMapper.insertTeamMemberRela(teamMemberRela);
    }

    /**
     * 修改团队关联关系
     * 
     * @param teamMemberRela 团队关联关系
     * @return 结果
     */
    @Override
    public int updateTeamMemberRela(TeamMemberRela teamMemberRela)
    {
        teamMemberRela.setUpdateTime(DateUtils.getNowDate());
        return teamMemberRelaMapper.updateTeamMemberRela(teamMemberRela);
    }

    /**
     * 批量删除团队关联关系
     * 
     * @param teamCodes 需要删除的团队关联关系主键
     * @return 结果
     */
    @Override
    public int deleteTeamMemberRelaByRelaIds(String[] teamCodes)
    {
        return teamMemberRelaMapper.deleteTeamMemberRelaByTeamCodes(teamCodes);
    }

    /**
     * 删除团队关联关系信息
     * 
     * @param relaId 团队关联关系主键
     * @return 结果
     */
    @Override
    public int deleteTeamMemberRelaByRelaId(Long relaId)
    {
        return teamMemberRelaMapper.deleteTeamMemberRelaByRelaId(relaId);
    }
}
