package com.teaching.competition.service;

import com.teaching.system.api.domain.CompetitionTrackConfig;
import com.teaching.system.api.domain.CompetitionTrackInfo;
import com.teaching.system.api.domain.CompetitionTrackInfoEntity;

import java.util.List;

/**
 * 赛事赛道配置Service接口
 * 
 * @author teaching
 * @date 2025-11-17
 */
public interface ICompetitionTrackInfoService 
{
    /**
     * 查询赛事赛道配置
     * 
     * @param competitionTrackId 赛事赛道配置主键
     * @return 赛事赛道配置
     */
    public CompetitionTrackInfo selectCompetitionTrackInfoByTrackId(Long trackId);

    public CompetitionTrackInfo selectCompetitionTrackInfoByCompetitionTrackId(String competitionTrackId);

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
    public CompetitionTrackConfig insertCompetitionTrackInfo(CompetitionTrackInfoEntity competitionTrackInfoEntity);

    /**
     * 修改赛事赛道配置
     * 
     * @param competitionTrackInfo 赛事赛道配置
     * @return 结果
     */
    public int updateCompetitionTrackInfo(CompetitionTrackInfo competitionTrackInfo);

    /**
     * 修改赛事赛道审核状态
     *
     * @param competitionTrackInfo 赛事赛道配置
     * @return 结果
     */
    public int updateCompetitionTrackStatus(CompetitionTrackInfo competitionTrackInfo);

    /**
     * 批量删除赛事赛道配置
     * 
     * @param competitionTrackIds 需要删除的赛事赛道配置主键集合
     * @return 结果
     */
    public int deleteCompetitionTrackInfoByCompetitionTrackIds(String[] competitionTrackIds);

    /**
     * 删除赛事赛道配置信息
     * 
     * @param competitionTrackId 赛事赛道配置主键
     * @return 结果
     */
    public int deleteCompetitionTrackInfoByCompetitionTrackId(String competitionTrackId);
}
