package com.teaching.competition.service;

import com.teaching.system.api.domain.CompetitionStageConfig;

import java.util.List;

/**
 * 赛事阶段配置Service接口
 * 
 * @author teaching
 * @date 2025-10-11
 */
public interface ICompetitionStageConfigService 
{
    /**
     * 查询赛事阶段配置
     * 
     * @param stageId 赛事阶段配置主键
     * @return 赛事阶段配置
     */
    public CompetitionStageConfig selectCompetitionStageConfigByStageId(String stageId);

    /**
     * 查询赛事阶段配置列表
     * 
     * @param competitionSeriesId 赛事阶段配置
     * @return 赛事阶段配置集合
     */
    public List<CompetitionStageConfig> selectCompetitionStageConfigList(Long competitionSeriesId);

    // 获取赛赛当前处于阶段
    public CompetitionStageConfig selectNowCompetitionStageConfig(Long competitionSeriesId);
    /**
     * 新增赛事阶段配置
     * 
     * @param competitionStageConfigs 赛事阶段配置
     * @return 结果
     */
    public int insertCompetitionStageConfig(List<CompetitionStageConfig> competitionStageConfigs);

    /**
     * 修改赛事阶段配置
     * 
     * @param competitionStageConfig 赛事阶段配置
     * @return 结果
     */
    public int updateCompetitionStageConfig(CompetitionStageConfig competitionStageConfig);

    /**
     * 批量删除赛事阶段配置
     * 
     * @param stageIds 需要删除的赛事阶段配置主键集合
     * @return 结果
     */
    public int deleteCompetitionStageConfigByStageIds(Long[] stageIds);

    /**
     * 删除赛事阶段配置信息
     * 
     * @param stageId 赛事阶段配置主键
     * @return 结果
     */
    public int deleteCompetitionStageConfigByStageId(Long stageId);
}
