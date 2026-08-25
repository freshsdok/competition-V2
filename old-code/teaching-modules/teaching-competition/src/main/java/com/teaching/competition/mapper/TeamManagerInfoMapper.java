package com.teaching.competition.mapper;

import com.teaching.competition.domain.UserCompetitionApplyInfoTeam;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.TeamManagerInfo;
import com.teaching.system.api.domain.TeamManagerInfoAwardsInfo;
import com.teaching.system.api.domain.TeamManagerInfoAwardsUserInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 团队管理Mapper接口
 * 
 * @author teaching
 * @date 2025-10-13
 */
public interface TeamManagerInfoMapper 
{
    /**
     * 查询团队管理
     * 
     * @param teamCode 团队管理主键
     * @return 团队管理
     */
    public TeamManagerInfo selectTeamManagerInfoByTeamCode(@Param("teamId") Long teamId,@Param("teamCode") String teamCode);

    public List<UserCompetitionApplyInfoTeam> selectCompetitionTeamInfoListByPCUserId(Map param);

    /**
     * 获取团队管理信息
     * @param teamLeaderId
     * @return
     */
    public TeamManagerInfo selectTeamMemberRelaByTeamLeaderId(Long teamLeaderId,Long competitionSeriesId);

    /**
     * 获取赛事团队信息
     * @param competitionSeriesId
     * @return
     */
    public List<TeamManagerInfo> selectCompetitionTeam(TeamManagerInfo teamManagerInfo);


    public List<TeamManagerInfo> selectTeamManagerInfoExportList(CompetitionApplyInfo competitionApplyInfo);

    /**
     * 查询用户申请团队信息
      * @param competitionSeriesId
     * @return
     */
    public List<TeamManagerInfo> selectTeamInfoByUserId(@Param("userId") Long userId,@Param("competitionSeriesId") Long competitionSeriesId);

    /**
     * 查询团队管理列表
     * 
     * @param teamManagerInfo 团队管理
     * @return 团队管理集合
     */
    public List<TeamManagerInfo> selectTeamManagerInfoList(TeamManagerInfo teamManagerInfo);

    public List<TeamManagerInfoAwardsInfo> selectTeamManagerInfoAwardsExportList(CompetitionApplyInfo competitionApplyInfo);

    public List<TeamManagerInfoAwardsUserInfo> selectTeamManagerInfoAwardsExportUserList(CompetitionApplyInfo competitionApplyInfo);

    /**
     * 新增团队管理
     * 
     * @param teamManagerInfo 团队管理
     * @return 结果
     */
    public int insertTeamManagerInfo(TeamManagerInfo teamManagerInfo);

    /**
     * 批量插入团队管理
     * @param teamManagerInfoList
     * @return
     */
    public int batchInsertTeamManagerInfo(@Param("list") List<TeamManagerInfo> teamManagerInfoList);

    /**
     * 修改团队管理
     * 
     * @param teamManagerInfo 团队管理
     * @return 结果
     */
    public int updateTeamManagerInfo(TeamManagerInfo teamManagerInfo);

    public int bindTeacherContestantUser(@Param("teamCode") String teamCode, @Param("userId") Long userId);

    /**
     * 删除团队管理
     * 
     * @param teamCode 团队管理主键
     * @return 结果
     */
    public int deleteTeamManagerInfoByTeamCode(String teamCode);

    public int deleteRetaTeamManagerInfoByTeamCode(String teamCode);

    /**
     * 批量删除团队管理
     * 
     * @param teamCodes 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTeamManagerInfoByTeamCodes(String[] teamCodes);

    /**
     * 批量删除团队管理
     *
     * @param competitionSeriesId 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTeamManagerInfoByCompetitionSeriesId(Long competitionSeriesId);
}
