package com.teaching.competition.service;

import com.teaching.competition.domain.UserApplyTeam;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.TeamManagerInfo;
import com.teaching.system.api.domain.TeamManagerInfoAwardsInfo;
import com.teaching.system.api.domain.TeamManagerInfoAwardsUserInfo;

import java.util.List;

/**
 * 团队管理Service接口
 * 
 * @author teaching
 * @date 2025-10-13
 */
public interface ITeamManagerInfoService 
{
    /**
     * 查询团队管理
     * 
     * @param teamCode 团队管理主键
     * @return 团队管理
     */
    public TeamManagerInfo selectTeamManagerInfoByTeamCode(Long teamId,String teamCode);

    /**
     * 查询团队管理列表
     * 
     * @param teamManagerInfo 团队管理
     * @return 团队管理集合
     */
    public List<TeamManagerInfo> selectTeamManagerInfoList(TeamManagerInfo teamManagerInfo);

    public List<TeamManagerInfo> selectTeamManagerInfoListExport(CompetitionApplyInfo competitionApplyInfo);

    public List<TeamManagerInfoAwardsInfo> selectTeamManagerInfoAwardsExportList(CompetitionApplyInfo competitionApplyInfo);

    public List<TeamManagerInfoAwardsUserInfo> selectTeamManagerInfoAwardsExportPCList(CompetitionApplyInfo competitionApplyInfo);

    /**
     * 查询团队管理列表内部调用
     *
     * @param teamManagerInfo 团队管理
     * @return 团队管理集合
     */
    public List<TeamManagerInfo> selectInnerTeamManagerInfoList(TeamManagerInfo teamManagerInfo);

    /**
     * 查询团队信息
     *
     * @param teamManagerInfo 团队管理
     * @return 团队管理集合
     */
    public List<TeamManagerInfo> selectTeamManagerInfoListByUserId(TeamManagerInfo teamManagerInfo);

    /**
     * 新增团队管理
     * 
     * @param teamManagerInfo 团队管理
     * @return 结果
     */
    public int insertTeamManagerInfo(TeamManagerInfo teamManagerInfo);

    /**
     * 团队申请报名
     *
     * @param teamMemberRela 团队管理
     * @return 结果
     */
    public int applyJoinTeam(UserApplyTeam userApplyTeam);

    /**
     * 修改团队管理
     * 
     * @param teamManagerInfo 团队管理
     * @return 结果
     */
    public int updateTeamManagerInfo(TeamManagerInfo teamManagerInfo);

    /**
     * 批量删除团队管理
     * 
     * @param teamCodes 需要删除的团队管理主键集合
     * @return 结果
     */
    public int deleteTeamManagerInfoByTeamCodes(String[] teamCodes);

    /**
     * 删除团队管理信息
     * 
     * @param teamCode 团队管理主键
     * @return 结果
     */
    public int deleteTeamManagerInfoByTeamCode(String teamCode);

    public TeamManagerInfo selectTeamMemberList(Long userId, Long competitionSeriesId);

    public List<TeamManagerInfo> selectTeamManagerInfo(TeamManagerInfo teamManagerInfo);

    public int updateTeamManagerStatus(TeamManagerInfo teamManagerInfo);
}
