package com.teaching.competition.service;

import com.teaching.system.api.domain.CompetitionAwardsConfig;

import java.util.List;

/**
 * 赛事奖项设置Service接口
 * 
 * @author teaching
 * @date 2025-10-11
 */
public interface ICompetitionAwardsConfigService 
{
    /**
     * 查询赛事奖项设置
     * 
     * @param awardsId 赛事奖项设置主键
     * @return 赛事奖项设置
     */
    public CompetitionAwardsConfig selectCompetitionAwardsConfigByAwardsId(Long awardsId);

    /**
     * 查询赛事奖项设置列表
     * 
     * @param competitionSeriesId 赛事奖项设置
     * @return 赛事奖项设置集合
     */
    public List<CompetitionAwardsConfig> selectCompetitionAwardsConfigList(CompetitionAwardsConfig competitionAwardsConfig);

    /**
     * 新增赛事奖项设置
     * 
     * @param competitionAwardsConfigs 赛事奖项设置
     * @return 结果
     */
    public int insertCompetitionAwardsConfig(List<CompetitionAwardsConfig> competitionAwardsConfigs);

    /**
     * 修改赛事奖项设置
     * 
     * @param competitionAwardsConfig 赛事奖项设置
     * @return 结果
     */
    public int updateCompetitionAwardsConfig(CompetitionAwardsConfig competitionAwardsConfig);

    /**
     * 批量删除赛事奖项设置
     * 
     * @param awardsIds 需要删除的赛事奖项设置主键集合
     * @return 结果
     */
    public int deleteCompetitionAwardsConfigByAwardsIds(Long[] awardsIds);

    /**
     * 删除赛事奖项设置信息
     * 
     * @param awardsId 赛事奖项设置主键
     * @return 结果
     */
    public int deleteCompetitionAwardsConfigByAwardsId(Long awardsId);
}
