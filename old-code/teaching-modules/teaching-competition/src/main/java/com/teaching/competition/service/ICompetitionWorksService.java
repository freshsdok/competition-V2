package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionWorks;

import java.util.List;

/**
 * 赛事作品Service接口
 * 
 * @author teaching
 * @date 2025-10-22
 */
public interface ICompetitionWorksService 
{
    /**
     * 查询赛事作品
     * 
     * @param worksId 赛事作品主键
     * @return 赛事作品
     */
    public CompetitionWorks selectCompetitionWorksByWorksId(Long worksId);

    public List<CompetitionWorks> selectCompetitionWorksByUserId(CompetitionWorks competitionWorks);

    /**
     * 查询赛事作品列表
     * 
     * @param competitionWorks 赛事作品
     * @return 赛事作品集合
     */
    public List<CompetitionWorks> selectCompetitionWorksList(CompetitionWorks competitionWorks);

    /**
     * 查询专家赛事作品列表
     *
     * @param competitionWorks 赛事作品
     * @return 赛事作品集合
     */
    public List<CompetitionWorks> selectSpecialistCompetitionWorksList(CompetitionWorks competitionWorks);

    /**
     * 用户端查询赛事作品列表
     *
     * @param competitionWorks 赛事作品
     * @return 赛事作品集合
     */
    public List<CompetitionWorks> selectCompetitionWorksListByUserId(CompetitionWorks competitionWorks);

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

    public int updateLinkCompetitionWorks(CompetitionWorks competitionWorks);

    public CompetitionWorks selectLinkCompetitionWorksByWorksId(Long worksId);

    /**
     * 批量删除赛事作品
     * 
     * @param worksIds 需要删除的赛事作品主键集合
     * @return 结果
     */
    public int deleteCompetitionWorksByWorksIds(Long[] worksIds);

    /**
     * 删除赛事作品信息
     * 
     * @param worksId 赛事作品主键
     * @return 结果
     */
    public int deleteCompetitionWorksByWorksId(Long worksId);
}
