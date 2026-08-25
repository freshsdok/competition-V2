package com.teaching.competition.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.competition.domain.CompetitionCheckInfo;
import com.teaching.competition.mapper.CompetitionCheckInfoMapper;
import com.teaching.competition.service.ICompetitionCheckInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 校验项Service业务层处理
 *
 * @author teaching
 * @date 2025-12-18
 */
@Service
public class CompetitionCheckInfoServiceImpl implements ICompetitionCheckInfoService {
    @Autowired
    private CompetitionCheckInfoMapper competitionCheckInfoMapper;

    /**
     * 查询校验项
     *
     * @param checkItemId 校验项主键
     * @return 校验项
     */
    @Override
    public CompetitionCheckInfo selectCompetitionCheckInfoByCheckItemId(Long checkItemId) {
        return competitionCheckInfoMapper.selectCompetitionCheckInfoByCheckItemId(checkItemId);
    }

    /**
     * 查询校验项列表
     *
     * @param competitionCheckInfo 校验项
     * @return 校验项
     */
    @Override
    public List<CompetitionCheckInfo> selectCompetitionCheckInfoList(CompetitionCheckInfo competitionCheckInfo) {
        return competitionCheckInfoMapper.selectCompetitionCheckInfoList(competitionCheckInfo);
    }

    /**
     * 新增校验项
     *
     * @param competitionCheckInfo 校验项
     * @return 结果
     */
    @Override
    public int insertCompetitionCheckInfo(CompetitionCheckInfo competitionCheckInfo) {
        competitionCheckInfo.setCreateTime(DateUtils.getNowDate());
        return competitionCheckInfoMapper.insertCompetitionCheckInfo(competitionCheckInfo);
    }

    /**
     * 修改校验项
     *
     * @param competitionCheckInfo 校验项
     * @return 结果
     */
    @Override
    public int updateCompetitionCheckInfo(CompetitionCheckInfo competitionCheckInfo) {
        competitionCheckInfo.setUpdateTime(DateUtils.getNowDate());
        return competitionCheckInfoMapper.updateCompetitionCheckInfo(competitionCheckInfo);
    }

    /**
     * 批量删除校验项
     *
     * @param checkItemIds 需要删除的校验项主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionCheckInfoByCheckItemIds(Long[] checkItemIds) {
        return competitionCheckInfoMapper.deleteCompetitionCheckInfoByCheckItemIds(checkItemIds);
    }

    /**
     * 删除校验项信息
     *
     * @param checkItemId 校验项主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionCheckInfoByCheckItemId(Long checkItemId) {
        return competitionCheckInfoMapper.deleteCompetitionCheckInfoByCheckItemId(checkItemId);
    }
}
