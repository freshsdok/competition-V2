package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionPromotedInfo;

import java.util.List;

/**
 * 赛事晋级Service接口
 *
 * @author teaching
 * @date 2026-05-19
 */
public interface ICompetitionPromotedInfoService {
    /**
     * 查询赛事晋级
     *
     * @param promotedId 赛事晋级主键
     * @return 赛事晋级
     */
    public CompetitionPromotedInfo selectCompetitionPromotedInfoByPromotedId(Long promotedId);

    /**
     * 查询赛事晋级列表
     *
     * @param competitionPromotedInfo 赛事晋级
     * @return 赛事晋级集合
     */
    public List<CompetitionPromotedInfo> selectCompetitionPromotedInfoList(CompetitionPromotedInfo competitionPromotedInfo);

    /**
     * 新增赛事晋级
     *
     * @param competitionPromotedInfo 赛事晋级
     * @return 结果
     */
    public int insertCompetitionPromotedInfo(CompetitionPromotedInfo competitionPromotedInfo);

    /**
     * 修改赛事晋级
     *
     * @param competitionPromotedInfo 赛事晋级
     * @return 结果
     */
    public int updateCompetitionPromotedInfo(CompetitionPromotedInfo competitionPromotedInfo);

    /**
     * 批量删除赛事晋级
     *
     * @param promotedIds 需要删除的赛事晋级主键集合
     * @return 结果
     */
    public int deleteCompetitionPromotedInfoByPromotedIds(Long[] promotedIds);

    /**
     * 删除赛事晋级信息
     *
     * @param promotedId 赛事晋级主键
     * @return 结果
     */
    public int deleteCompetitionPromotedInfoByPromotedId(Long promotedId);
}
