package com.teaching.competition.service;

import com.teaching.system.api.domain.CompetitionTrackConfig;

import java.util.List;

/**
 * 赛道配置Service接口
 * 
 * @author teaching
 * @date 2025-12-01
 */
public interface ICompetitionTrackConfigService 
{
    /**
     * 查询赛道配置
     * 
     * @param competitionTrackConfigId 赛道配置主键
     * @return 赛道配置
     */
    public CompetitionTrackConfig selectCompetitionTrackConfigByCompetitionTrackConfigId(Long competitionTrackConfigId);

    /**
     * 查询赛道配置列表
     * 
     * @param competitionTrackConfig 赛道配置
     * @return 赛道配置集合
     */
    public List<CompetitionTrackConfig> selectCompetitionTrackConfigList(CompetitionTrackConfig competitionTrackConfig);

    /**
     * 新增赛道配置
     * 
     * @param competitionTrackConfig 赛道配置
     * @return 结果
     */
    public int insertCompetitionTrackConfig(CompetitionTrackConfig competitionTrackConfig);

    /**
     * 修改赛道配置
     * 
     * @param competitionTrackConfig 赛道配置
     * @return 结果
     */
    public int updateCompetitionTrackConfig(CompetitionTrackConfig competitionTrackConfig);

    /**
     * 批量删除赛道配置
     * 
     * @param competitionTrackConfigIds 需要删除的赛道配置主键集合
     * @return 结果
     */
    public int deleteCompetitionTrackConfigByCompetitionTrackConfigIds(Long[] competitionTrackConfigIds);

    /**
     * 删除赛道配置信息
     * 
     * @param competitionTrackConfigId 赛道配置主键
     * @return 结果
     */
    public int deleteCompetitionTrackConfigByCompetitionTrackConfigId(Long competitionTrackConfigId);
}
