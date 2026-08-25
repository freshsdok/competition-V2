package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionApplyAllStatus;
import com.teaching.system.api.domain.TeamMemberRela;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 团队关联关系Mapper接口
 * 
 * @author teaching
 * @date 2025-10-20
 */
public interface TeamMemberRelaMapper 
{
    /**
     * 查询团队关联关系
     * 
     * @param relaId 团队关联关系主键
     * @return 团队关联关系
     */
    public TeamMemberRela selectTeamMemberRelaByRelaId(@Param("userId") Long userId,@Param("teamCode") String teamCode);

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
     * 批量新增团队关联关系
     *
     * @param teamMemberRelaList 团队关联关系列表
     * @return 结果
     */
    public int batchInsertTeamMemberRela(@Param("list") List<TeamMemberRela> teamMemberRelaList);

    /**
     * 修改团队关联关系
     * 
     * @param teamMemberRela 团队关联关系
     * @return 结果
     */
    public int updateTeamMemberRela(TeamMemberRela teamMemberRela);

    public int updateTeamMemberRelaUserId(TeamMemberRela teamMemberRela);

    public int bindTeacherContestantUser(@Param("teamCode") String teamCode, @Param("userId") Long userId);

    public int updateTeamMemberRelaByUserName(Map<String,Object> teamMemberRelaMap);

    /**
     * 删除团队关联关系
     * 
     * @param relaId 团队关联关系主键
     * @return 结果
     */
    public int deleteTeamMemberRelaByRelaId(Long relaId);

    public int deleteTeamMemberRelaByTeamCode(String teamCode);

    public int deleteRetaTeamMemberRelaByTeamCode(String teamCode);

    public int deleteTeamMemberRelaByTeamCodeAndUserName(@Param("teamCode")String teamCode,@Param("userName")String userName);

    /**
     * 批量删除团队关联关系
     * 
     * @param relaIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTeamMemberRelaByTeamCodes(@Param("teamCodes") String[] teamCodes);

    public Integer selectTeamMemberRelaCountByTeamCode(@Param("teamCode")String teamCode,@Param("teamRole")String teamRole);


    public int updateTeamStatus(@Param("list") List<TeamMemberRela> teamMemberRelaList);

    public CompetitionApplyAllStatus selectUserTeamStatus(CompetitionApplyAllStatus competitionApplyAllStatus);

    // 查询团队申请发票状态
    public List<TeamMemberRela> queryTeamMemberInvoiceStatus(TeamMemberRela teamMemberRela);
}
