package com.teaching.competition.mapper;

import com.teaching.system.api.domain.CompetitionConfig;

import java.util.List;

/**
 * 赛事配置Mapper接口
 * 
 * @author teaching
 * @date 2025-10-13
 */
public interface CompetitionConfigMapper 
{
    /**
     * 查询赛事配置
     * 
     * @param configId 赛事配置主键
     * @return 赛事配置
     */
    public CompetitionConfig selectCompetitionConfigByConfigId(Long configId);

    /**
     * 根据赛道配置id查询赛事配置
     *
     * @param competitionTrackConfigId 赛道配置id
     * @return 赛事配置
     */
    public CompetitionConfig selectCompetitionConfigByTrackConfigId(Long competitionTrackConfigId);

    /**
     * 查询赛事配置列表
     * 
     * @param competitionSeriesId 赛事配置
     * @return 赛事配置集合
     */
    public List<CompetitionConfig> selectCompetitionConfigList(Long competitionSeriesId);

    /**
     * 新增赛事配置
     * 
     * @param competitionConfig 赛事配置
     * @return 结果
     */
    public int insertCompetitionConfig(CompetitionConfig competitionConfig);

    /**
     * 修改赛事配置
     * 
     * @param competitionConfig 赛事配置
     * @return 结果
     */
    public int updateCompetitionConfig(CompetitionConfig competitionConfig);

    /**
     * 删除赛事配置
     * 
     * @param configId 赛事配置主键
     * @return 结果
     */
    public int deleteCompetitionConfigByConfigId(Long configId);

    /**
     * 批量删除赛事配置
     * 
     * @param competitionSeriesIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionConfigByConfigIds(Long[] competitionSeriesIds);
}
