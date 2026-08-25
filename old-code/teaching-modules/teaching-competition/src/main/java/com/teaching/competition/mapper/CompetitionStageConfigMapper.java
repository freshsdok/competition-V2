package com.teaching.competition.mapper;

import com.teaching.system.api.domain.CompetitionStageConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事阶段配置Mapper接口
 * 
 * @author teaching
 * @date 2025-10-11
 */
public interface CompetitionStageConfigMapper 
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

    /**
     * 新增赛事阶段配置
     * 
     * @param stageConfigList 赛事阶段配置
     * @return 结果
     */
    public int insertCompetitionStageConfig(@Param("stageConfigList") List<CompetitionStageConfig> stageConfigList);

    /**
     * 修改赛事阶段配置
     * 
     * @param competitionStageConfig 赛事阶段配置
     * @return 结果
     */
    public int updateCompetitionStageConfig(CompetitionStageConfig competitionStageConfig);

    /**
     * 删除赛事阶段配置
     * 
     * @param competitionSeriesId 赛事阶段配置主键
     * @return 结果
     */
    public int deleteCompetitionStageConfigBySeriesId(Long competitionSeriesId);

    /**
     * 批量删除赛事阶段配置
     * 
     * @param competitionSeriesIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionStageConfigByStageIds(Long[] competitionSeriesIds);
}
