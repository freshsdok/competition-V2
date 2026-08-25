package com.teaching.competition.mapper;

import com.teaching.system.api.domain.CompetitionAwardsConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事奖项设置Mapper接口
 * 
 * @author teaching
 * @date 2025-10-11
 */
public interface CompetitionAwardsConfigMapper 
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
    public int insertCompetitionAwardsConfig(@Param("awardsConfigList") List<CompetitionAwardsConfig> competitionAwardsConfigs);

    /**
     * 修改赛事奖项设置
     * 
     * @param competitionAwardsConfig 赛事奖项设置
     * @return 结果
     */
    public int updateCompetitionAwardsConfig(CompetitionAwardsConfig competitionAwardsConfig);

    /**
     * 删除赛事奖项设置
     * 
     * @param competitionSeriesId 赛事奖项设置主键
     * @return 结果
     */
    public int deleteCompetitionAwardsConfigBySeriesId(Long competitionSeriesId);

    /**
     * 批量删除赛事奖项设置
     * 
     * @param competitionSeriesIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionAwardsConfigByAwardsIds(Long[] awardsIds);
}
