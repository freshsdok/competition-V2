package com.teaching.competition.mapper;

import com.teaching.system.api.domain.CompetitionEnterpriseRela;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事赞助企业关联关系Mapper接口
 * 
 * @author teaching
 * @date 2025-10-11
 */
public interface CompetitionEnterpriseRelaMapper 
{
    /**
     * 查询赛事赞助企业关联关系
     * 
     * @param relaId 赛事赞助企业关联关系主键
     * @return 赛事赞助企业关联关系
     */
    public CompetitionEnterpriseRela selectCompetitionEnterpriseRelaByRelaId(Long relaId);

    /**
     * 查询赛事赞助企业关联关系列表
     * 
     * @param competitionSeriesId 赛事赞助企业关联关系
     * @return 赛事赞助企业关联关系集合
     */
    public List<CompetitionEnterpriseRela> selectCompetitionEnterpriseRelaList(Long competitionSeriesId);

    /**
     * 新增赛事赞助企业关联关系
     * 
     * @param competitionEnterpriseRelaList 赛事赞助企业关联关系
     * @return 结果
     */
    public int insertCompetitionEnterpriseRela(@Param("enterpriseRelaList") List<CompetitionEnterpriseRela> competitionEnterpriseRelaList);

    /**
     * 修改赛事赞助企业关联关系
     * 
     * @param competitionEnterpriseRela 赛事赞助企业关联关系
     * @return 结果
     */
    public int updateCompetitionEnterpriseRela(CompetitionEnterpriseRela competitionEnterpriseRela);

    /**
     * 批量修改赛道赞助企业
     *
     * @param competitionEnterpriseRelaList 赛事赞助企业关联关系列表
     * @return 批量修改结果
     */
    public int batchUpdateCompetitionEnterpriseRela(@Param("list") List<CompetitionEnterpriseRela> competitionEnterpriseRelaList);

    /**
     * 删除赛事赞助企业关联关系
     * 
     * @param competitionSeriesId 赛事赞助企业关联关系主键
     * @return 结果
     */
    public int deleteCompetitionEnterpriseRelaBySeriesId(Long competitionSeriesId);

    /**
     * 批量删除赛事赞助企业关联关系
     * 
     * @param competitionSeriesIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionEnterpriseRelaByRelaIds(Long[] competitionSeriesIds);

    /**
     * 批量删除赛事赞助企业关联关系
     *
     * @param competitionTrackConfigIds 需要删除的数据主键集合
     * @return 批量删除结果
     */
    public int deleteCompetitionEnterpriseRelaByConfigIds(Long[] competitionTrackConfigIds);
}
