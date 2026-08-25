package com.teaching.competition.mapper;

import com.teaching.common.core.utils.PageUtils;
import com.teaching.system.api.domain.CompetitionTrackConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛道配置Mapper接口
 * 
 * @author teaching
 * @date 2025-12-01
 */
public interface CompetitionTrackConfigMapper 
{
    /**
     * 查询赛道配置
     * 
     * @param competitionTrackConfigId 赛道配置主键
     * @return 赛道配置
     */
    public CompetitionTrackConfig selectCompetitionTrackConfigByConfigId(CompetitionTrackConfig competitionTrackConfig);

    /**
     * 根据名称查询赛道配置
     *
     * @param competitionTrackConfig 赛道配置
     * @return 赛道配置集合
     */
    public List<CompetitionTrackConfig> selectCompetitionTrackConfigByName(CompetitionTrackConfig competitionTrackConfig);

    /**
     * 查询赛道配置列表
     * 
     * @param competitionTrackConfig 赛道配置
     * @return 赛道配置集合
     */
    public List<CompetitionTrackConfig> selectCompetitionTrackConfigList(CompetitionTrackConfig competitionTrackConfig);

    public List<CompetitionTrackConfig> selectCompetitionTrackConfigAllList(@Param("list") List<String> competitionTrackIdList);

    /**
     * 新增赛道配置
     * 
     * @param competitionTrackConfig 赛道配置
     * @return 结果
     */
    public int insertCompetitionTrackConfig(CompetitionTrackConfig competitionTrackConfig);

    /**
     * 批量新增赛道配置
     *
     * @param competitionTrackConfigs 赛道配置列表
     * @return 结果
     */
    public int batchInsertCompetitionTrackConfig(@Param("list") List<CompetitionTrackConfig> competitionTrackConfigs);

    /**
     * 修改赛道配置
     * 
     * @param competitionTrackConfig 赛道配置
     * @return 结果
     */
    public int updateCompetitionTrackConfig(CompetitionTrackConfig competitionTrackConfig);

    /**
     * 批量修改赛道配置
     *
     * @param competitionTrackConfigList 赛道配置列表
     * @return 结果
     */
    public int batchUpdateCompetitionTrackConfig(@Param("list") List<CompetitionTrackConfig> competitionTrackConfigList);

    /**
     * 删除赛道配置
     * 
     * @param competitionTrackConfigId 赛道配置主键
     * @return 结果
     */
    public int deleteCompetitionTrackConfigByCompetitionTrackConfigId(Long competitionTrackConfigId);

    /**
     * 批量删除赛道配置
     * 
     * @param competitionTrackConfigIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionTrackConfigByCompetitionTrackConfigIds(Long[] competitionTrackConfigIds);
}
