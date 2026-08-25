package com.teaching.competition.mapper;

import com.teaching.system.api.domain.CompetitionDetailInfo;
import com.teaching.system.api.domain.CompetitionMainInfo;
import com.teaching.system.api.domain.CompetitionMainInfoReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事主数据Mapper接口
 * 
 * @author teaching
 * @date 2025-10-10
 */
public interface CompetitionMainInfoMapper 
{
    /**
     * 查询赛事主数据
     * 
     * @param competitionMainInfoReq 赛事主数据主键
     * @return 赛事主数据
     */
    public List<CompetitionDetailInfo> selectCompetitionDetailInfoByCompetitionId(CompetitionMainInfoReq competitionMainInfoReq);

    /**
     * 检查赛事名称是否存在
     * @param competitionName
     * @return
     */
    public int checkCompetitionName(@Param("competitionName") String competitionName,@Param("competitionSeriesName") String competitionSeriesName);

    /**
     * 查询赛事主数据列表
     * 
     * @param competitionMainInfoReq 赛事主数据
     * @return 赛事主数据集合
     */
    public List<CompetitionMainInfo> selectCompetitionMainInfoList(CompetitionMainInfoReq competitionMainInfoReq);

    /**
     * 通过赛事名称查询赛事信息
     * @param competitionName
     * @return
     */
    public List<CompetitionMainInfo> selectCompetitionSeriesInfoByCompetitionName(String competitionName);

    // 重复赛事名称，新增赛事界数使用
    public List<CompetitionMainInfo> selectCompetitionInfoByCompetitionName(String competitionName);

    /**
     * 新增赛事主数据
     * 
     * @param competitionMainInfo 赛事主数据
     * @return 结果
     */
    public int insertCompetitionMainInfo(CompetitionMainInfo competitionMainInfo);

    /**
     * 修改赛事主数据
     * 
     * @param competitionMainInfo 赛事主数据
     * @return 结果
     */
    public int updateCompetitionMainInfo(CompetitionMainInfo competitionMainInfo);

    /**
     * 删除赛事主数据
     * 
     * @param competitionId 赛事主数据主键
     * @return 结果
     */
    public int deleteCompetitionMainInfoByCompetitionId(Long competitionId);

    /**
     * 批量删除赛事主数据
     * 
     * @param competitionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionMainInfoByCompetitionIds(Long[] competitionIds);
}
