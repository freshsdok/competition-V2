package com.teaching.competition.mapper;

import com.teaching.system.api.domain.CompetitionTrackInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事赛道配置Mapper接口
 * 
 * @author teaching
 * @date 2025-11-17
 */
public interface CompetitionTrackInfoMapper 
{
    /**
     * 查询赛事赛道配置
     * 
     * @param competitionTrackId 赛事赛道配置主键
     * @return 赛事赛道配置
     */
    public CompetitionTrackInfo selectCompetitionTrackInfoByCompetitionTrackId(String competitionTrackId);

    public CompetitionTrackInfo selectCompetitionTrackInfoByTrackId(Long trackId);

    /**
     * 根据赛道名称查询赛道信息
     * @param competitionTrackName
     * @return
     */
    public List<CompetitionTrackInfo> selectCompetitionTrackInfoByCompetitionTrackName(CompetitionTrackInfo competitionTrackInfo);

    // pc端获取赛道及赛道组别配置
    public List<CompetitionTrackInfo> selectCompetitionTrackInfoByUser(CompetitionTrackInfo competitionTrackInfo);

    /**
     * 根据赛事系列id查询赛道信息
     * @param competitionSeriesId
     * @return
     */
    public List<CompetitionTrackInfo> selectCompetitionTrackInfoByCompetitionSeriesId(Long competitionSeriesId);


    public List<CompetitionTrackInfo> selectCompetitionTrackInfo(Long competitionSeriesId,String checkStatus);

    /**
     * 校验赛事系列下赛道信息是否存在
     * @param competitionSeriesId
     * @return
     */
    public int checkCompetitionTrackInfoByCompetitionSeriesId(@Param("competitionSeriesId") Long competitionSeriesId,@Param("competitionTrackName") String competitionTrackName);

    /**
     * 校验赛道名称是否唯一
     * @param competitionTrackName
     * @param competitionSeriesId
     * @return
     */
    public int checkCompetitionTrackInfoUnique(@Param("competitionTrackName") String competitionTrackName, @Param("competitionSeriesId") Long competitionSeriesId);

    /**
     * 查询赛事赛道配置列表
     * 
     * @param competitionTrackInfo 赛事赛道配置
     * @return 赛事赛道配置集合
     */
    public List<CompetitionTrackInfo> selectCompetitionTrackInfoList(CompetitionTrackInfo competitionTrackInfo);

    /**
     * 新增赛事赛道配置
     * 
     * @param competitionTrackInfo 赛事赛道配置
     * @return 结果
     */
    public int insertCompetitionTrackInfo(@Param("competitionTrackInfoList") List<CompetitionTrackInfo> competitionTrackInfoList);

    /**
     * 修改赛事赛道配置
     * 
     * @param competitionTrackInfo 赛事赛道配置
     * @return 结果
     */
    public int updateCompetitionTrackInfo(CompetitionTrackInfo competitionTrackInfo);

    /**
     * 删除赛事赛道配置
     * 
     * @param competitionTrackId 赛事赛道配置主键
     * @return 结果
     */
    public int deleteCompetitionTrackInfoByCompetitionTrackId(String competitionTrackId);

    /**
     * 批量删除赛事赛道配置
     * 
     * @param competitionTrackIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionTrackInfoByCompetitionTrackIds(String[] competitionTrackIds);
}
