package com.teaching.competition.service;

import com.teaching.system.api.domain.CompetitionConfig;

import java.util.List;

/**
 * 赛事配置Service接口
 * 
 * @author teaching
 * @date 2025-10-13
 */
public interface ICompetitionConfigService 
{
    /**
     * 查询赛事配置
     * 
     * @param configId 赛事配置主键
     * @return 赛事配置
     */
    public CompetitionConfig selectCompetitionConfigByConfigId(Long configId);

    /**
     * 查询赛事配置列表
     * 
     * @param competitionConfig 赛事配置
     * @return 赛事配置集合
     */
    public List<CompetitionConfig> selectCompetitionConfigList(CompetitionConfig competitionConfig);

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
     * 批量删除赛事配置
     * 
     * @param configIds 需要删除的赛事配置主键集合
     * @return 结果
     */
    public int deleteCompetitionConfigByConfigIds(Long[] configIds);

    /**
     * 删除赛事配置信息
     * 
     * @param configId 赛事配置主键
     * @return 结果
     */
    public int deleteCompetitionConfigByConfigId(Long configId);
}
