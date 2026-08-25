package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionWorks;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 赛事作品Mapper接口
 * 
 * @author teaching
 * @date 2025-10-22
 */
public interface CompetitionWorksMapper 
{
    /**
     * 查询赛事作品
     * 
     * @param worksId 赛事作品主键
     * @return 赛事作品
     */
    public CompetitionWorks selectCompetitionWorksByWorksId(Long worksId);

    /**
     * 用户查询赛事作品
     *
     * @param competitionWorks 赛事作品
     * @return 赛事作品
     */
    public CompetitionWorks selectCompetitionWorksByUserId(CompetitionWorks competitionWorks);

    /**
     * 查询赛事作品列表
     * 
     * @param competitionWorks 赛事作品
     * @return 赛事作品集合
     */
    public List<CompetitionWorks> selectCompetitionWorksList(CompetitionWorks competitionWorks);

    // 获取赛事已评分作品列表
    public List<CompetitionWorks> selectCompetitionWorksScore(CompetitionWorks competitionWorks);

    // 获取获奖名单
    public List<CompetitionWorks> selectCompetitionList(CompetitionWorks competitionWorks);

    /**
     * 新增赛事作品
     * 
     * @param competitionWorks 赛事作品
     * @return 结果
     */
    public int insertCompetitionWorks(CompetitionWorks competitionWorks);

    /**
     * 修改赛事作品
     * 
     * @param competitionWorks 赛事作品
     * @return 结果
     */
    public int updateCompetitionWorks(CompetitionWorks competitionWorks);

    int batchUpdateCompetitionWorks(@Param("list") List<CompetitionWorks> list);

    /**
     * 删除赛事作品
     * 
     * @param worksId 赛事作品主键
     * @return 结果
     */
    public int deleteCompetitionWorksByWorksId(Long worksId);

    /**
     * 批量删除赛事作品
     * 
     * @param worksIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionWorksByWorksIds(Long[] worksIds);

    /**
     * 统计用户参加赛事获取总分数
     *
     * @param competitionSeriesId 赛事id
     * @return
     */
    public List<Map<String, Object>> selectCompetitionWorksAdvanceScore(Long competitionSeriesId);
}
