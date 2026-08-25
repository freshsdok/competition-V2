package com.teaching.competition.mapper;


import com.teaching.competition.domain.CompetitionCheckInfo;

import java.util.List;

/**
 * 校验项Mapper接口
 *
 * @author teaching
 * @date 2025-12-18
 */
public interface CompetitionCheckInfoMapper {
    /**
     * 查询校验项
     *
     * @param checkItemId 校验项主键
     * @return 校验项
     */
    public CompetitionCheckInfo selectCompetitionCheckInfoByCheckItemId(Long checkItemId);

    /**
     * 根据校验项ids查询校验项
     * @param checkItemIds
     * @return
     */
    public List<CompetitionCheckInfo> selectCompetitionCheckInfoByCheckItemIds(List<Long> checkItemIds);

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
     * 删除校验项
     *
     * @param checkItemId 校验项主键
     * @return 结果
     */
    public int deleteCompetitionCheckInfoByCheckItemId(Long checkItemId);

    /**
     * 批量删除校验项
     *
     * @param checkItemIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionCheckInfoByCheckItemIds(Long[] checkItemIds);
}
