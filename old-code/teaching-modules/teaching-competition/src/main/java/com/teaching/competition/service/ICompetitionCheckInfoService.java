package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionCheckInfo;

import java.util.List;

/**
 * 校验项Service接口
 *
 * @author teaching
 * @date 2025-12-18
 */
public interface ICompetitionCheckInfoService {
    /**
     * 查询校验项
     *
     * @param checkItemId 校验项主键
     * @return 校验项
     */
    public CompetitionCheckInfo selectCompetitionCheckInfoByCheckItemId(Long checkItemId);

    /**
     * 查询校验项列表
     *
     * @param competitionCheckInfo 校验项
     * @return 校验项集合
     */
    public List<CompetitionCheckInfo> selectCompetitionCheckInfoList(CompetitionCheckInfo competitionCheckInfo);

    /**
     * 新增校验项
     *
     * @param competitionCheckInfo 校验项
     * @return 结果
     */
    public int insertCompetitionCheckInfo(CompetitionCheckInfo competitionCheckInfo);

    /**
     * 修改校验项
     *
     * @param competitionCheckInfo 校验项
     * @return 结果
     */
    public int updateCompetitionCheckInfo(CompetitionCheckInfo competitionCheckInfo);

    /**
     * 批量删除校验项
     *
     * @param checkItemIds 需要删除的校验项主键集合
     * @return 结果
     */
    public int deleteCompetitionCheckInfoByCheckItemIds(Long[] checkItemIds);

    /**
     * 删除校验项信息
     *
     * @param checkItemId 校验项主键
     * @return 结果
     */
    public int deleteCompetitionCheckInfoByCheckItemId(Long checkItemId);
}
