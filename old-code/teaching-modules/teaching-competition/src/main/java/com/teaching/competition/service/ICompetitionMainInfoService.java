package com.teaching.competition.service;

import com.teaching.system.api.domain.CompetitionDetailInfo;
import com.teaching.system.api.domain.CompetitionMainInfo;
import com.teaching.system.api.domain.CompetitionMainInfoReq;
import com.teaching.system.api.domain.OperationConfig;

import java.util.List;
import java.util.Map;

/**
 * 赛事主数据Service接口
 * 
 * @author teaching
 * @date 2025-10-10
 */
public interface ICompetitionMainInfoService 
{
    /**
     * 查询赛事主数据
     * 
     * @param competitionId 赛事主数据主键
     * @return 赛事主数据
     */
    public List<CompetitionDetailInfo> selectCompetitionDetailInfoByCompetitionId(CompetitionMainInfoReq req);

    /**
     * 查询所有赛事数据
     *
     * @param competitionMainInfo 赛事主数据
     * @return 赛事主数据集合
     */
    public List<CompetitionDetailInfo> selectAllCompetitionDetailInfo(CompetitionMainInfoReq req);

    /**
     * 查询赛事主数据列表
     * 
     * @param competitionMainInfo 赛事主数据
     * @return 赛事主数据集合
     */
    public List<CompetitionMainInfo> selectCompetitionMainInfoList(CompetitionMainInfoReq req);

    /**
     * 获取赛事下拉列表
     * @param req
     * @return
     */
    public List<Map<String,Object>> selectCompetitionMainInfoListInner(CompetitionMainInfoReq req);

    /**
     * 根据赛事名称查询赛事信息
     * @param competitionName
     * @return
     */
    public List<CompetitionMainInfo> selectCompetitionSeriesInfoByCompetitionName(String competitionName);

    /**
     * 获取赛事下拉列表
     * @param req
     * @return
     */
    public List<CompetitionMainInfo> selectCompetitionMainInfoPullDownList(CompetitionMainInfoReq req);

    /**
     * 新增赛事主数据
     * 
     * @param competitionMainInfo 赛事主数据
     * @return 结果
     */
    public CompetitionDetailInfo insertCompetitionMainInfo(CompetitionDetailInfo competitionDetailInfo);

    /**
     * 修改赛事主数据
     * 
     * @param competitionDetailInfo 赛事主数据
     * @return 结果
     */
    public int updateCompetitionMainInfo(CompetitionDetailInfo competitionDetailInfo);

    /**
     * 批量删除赛事主数据
     * 
     * @param infoReq 需要删除的赛事主数据主键集合
     * @return 结果
     */
    public int deleteCompetitionMainInfoByCompetitionIds(CompetitionMainInfoReq infoReq);

    /**
     * 删除赛事主数据信息
     * 
     * @param competitionId 赛事主数据主键
     * @return 结果
     */
    public int deleteCompetitionMainInfoByCompetitionId(Long competitionId);
}
