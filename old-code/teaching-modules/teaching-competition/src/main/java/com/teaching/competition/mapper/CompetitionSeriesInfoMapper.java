package com.teaching.competition.mapper;

import com.teaching.system.api.domain.CompetitionSeriesInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事系列信息Mapper接口
 * 
 * @author teaching
 * @date 2025-10-13
 */
public interface CompetitionSeriesInfoMapper 
{
    /**
     * 查询赛事系列信息
     * 
     * @param competitionSeriesId 赛事系列信息主键
     * @return 赛事系列信息
     */
    public CompetitionSeriesInfo selectCompetitionSeriesInfoByCompetitionSeriesId(@Param("competitionId") Long competitionId,@Param("competitionSeriesId") Long competitionSeriesId);

    /**
     * 查询赛事系列信息列表
     * 
     * @param competitionSeriesInfo 赛事系列信息
     * @return 赛事系列信息集合
     */
    public List<CompetitionSeriesInfo> selectCompetitionSeriesInfoList(CompetitionSeriesInfo competitionSeriesInfo);

    /**
     * 新增赛事系列信息
     * 
     * @param competitionSeriesInfo 赛事系列信息
     * @return 结果
     */
    public int insertCompetitionSeriesInfo(CompetitionSeriesInfo competitionSeriesInfo);

    /**
     * 修改赛事系列信息
     * 
     * @param competitionSeriesInfo 赛事系列信息
     * @return 结果
     */
    public int updateCompetitionSeriesInfo(CompetitionSeriesInfo competitionSeriesInfo);

    /**
     * 删除赛事系列信息
     * 
     * @param competitionSeriesId 赛事系列信息主键
     * @return 结果
     */
    public int deleteCompetitionSeriesInfoByCompetitionSeriesId(Long competitionSeriesId);

    /**
     * 批量删除赛事系列信息
     * 
     * @param competitionSeriesIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionSeriesInfoByCompetitionSeriesIds(Long[] competitionSeriesIds);
}
