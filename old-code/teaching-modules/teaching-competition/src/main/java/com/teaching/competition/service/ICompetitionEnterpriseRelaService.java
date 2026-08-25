package com.teaching.competition.service;

import com.teaching.system.api.domain.CompetitionEnterpriseRela;

import java.util.List;

/**
 * 赛事赞助企业关联关系Service接口
 * 
 * @author teaching
 * @date 2025-10-11
 */
public interface ICompetitionEnterpriseRelaService 
{
    /**
     * 查询赛事赞助企业关联关系
     * 
     * @param relaId 赛事赞助企业关联关系主键
     * @return 赛事赞助企业关联关系
     */
    public CompetitionEnterpriseRela selectCompetitionEnterpriseRelaByRelaId(Long relaId);

    /**
     * 查询赛事赞助企业关联关系列表
     * 
     * @param competitionSeriesId 赛事赞助企业关联关系
     * @return 赛事赞助企业关联关系集合
     */
    public List<CompetitionEnterpriseRela> selectCompetitionEnterpriseRelaList(Long competitionSeriesId);

    /**
     * 新增赛事赞助企业关联关系
     * 
     * @param competitionEnterpriseRelas 赛事赞助企业关联关系
     * @return 结果
     */
    public int insertCompetitionEnterpriseRela(List<CompetitionEnterpriseRela> competitionEnterpriseRelas);

    /**
     * 修改赛事赞助企业关联关系
     * 
     * @param competitionEnterpriseRela 赛事赞助企业关联关系
     * @return 结果
     */
    public int updateCompetitionEnterpriseRela(CompetitionEnterpriseRela competitionEnterpriseRela);

    /**
     * 批量删除赛事赞助企业关联关系
     * 
     * @param relaIds 需要删除的赛事赞助企业关联关系主键集合
     * @return 结果
     */
    public int deleteCompetitionEnterpriseRelaByRelaIds(Long[] relaIds);

    /**
     * 删除赛事赞助企业关联关系信息
     * 
     * @param relaId 赛事赞助企业关联关系主键
     * @return 结果
     */
    public int deleteCompetitionEnterpriseRelaByRelaId(Long relaId);
}
