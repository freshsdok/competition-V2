package com.teaching.competition.service.impl;

import java.util.List;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.system.api.domain.CompetitionEnterpriseRela;
import com.teaching.competition.service.ICompetitionEnterpriseRelaService;
import com.teaching.competition.mapper.CompetitionEnterpriseRelaMapper;
import com.teaching.competition.util.UUIDUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 赛事赞助企业关联关系Service业务层处理
 * 
 * @author teaching
 * @date 2025-10-11
 */
@Service
public class CompetitionEnterpriseRelaServiceImpl implements ICompetitionEnterpriseRelaService
{
    @Autowired
    private CompetitionEnterpriseRelaMapper competitionEnterpriseRelaMapper;

    /**
     * 查询赛事赞助企业关联关系
     * 
     * @param relaId 赛事赞助企业关联关系主键
     * @return 赛事赞助企业关联关系
     */
    @Override
    public CompetitionEnterpriseRela selectCompetitionEnterpriseRelaByRelaId(Long relaId)
    {
        return competitionEnterpriseRelaMapper.selectCompetitionEnterpriseRelaByRelaId(relaId);
    }

    /**
     * 查询赛事赞助企业关联关系列表
     * 
     * @param competitionSeriesId 赛事赞助企业关联关系
     * @return 赛事赞助企业关联关系
     */
    @Override
    public List<CompetitionEnterpriseRela> selectCompetitionEnterpriseRelaList(Long competitionSeriesId)
    {
        return competitionEnterpriseRelaMapper.selectCompetitionEnterpriseRelaList(competitionSeriesId);
    }

    /**
     * 新增赛事赞助企业关联关系
     * 
     * @param competitionEnterpriseRela 赛事赞助企业关联关系
     * @return 结果
     */
    @Override
    public int insertCompetitionEnterpriseRela(List<CompetitionEnterpriseRela> competitionEnterpriseRelas)
    {
        if(CollectionUtils.isNotEmpty(competitionEnterpriseRelas)){
            for (CompetitionEnterpriseRela competitionEnterpriseRela : competitionEnterpriseRelas){
                competitionEnterpriseRela.setCreateTime(DateUtils.getNowDate());
                competitionEnterpriseRela.setRelaId(UUIDUtils.getUUID());
            }
        }

        return competitionEnterpriseRelaMapper.insertCompetitionEnterpriseRela(competitionEnterpriseRelas);
    }

    /**
     * 修改赛事赞助企业关联关系
     * 
     * @param competitionEnterpriseRela 赛事赞助企业关联关系
     * @return 结果
     */
    @Override
    public int updateCompetitionEnterpriseRela(CompetitionEnterpriseRela competitionEnterpriseRela)
    {
        competitionEnterpriseRela.setUpdateTime(DateUtils.getNowDate());
        return competitionEnterpriseRelaMapper.updateCompetitionEnterpriseRela(competitionEnterpriseRela);
    }

    /**
     * 批量删除赛事赞助企业关联关系
     * 
     * @param relaIds 需要删除的赛事赞助企业关联关系主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionEnterpriseRelaByRelaIds(Long[] relaIds)
    {
        return competitionEnterpriseRelaMapper.deleteCompetitionEnterpriseRelaByRelaIds(relaIds);
    }

    /**
     * 删除赛事赞助企业关联关系信息
     * 
     * @param relaId 赛事赞助企业关联关系主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionEnterpriseRelaByRelaId(Long relaId)
    {
        return competitionEnterpriseRelaMapper.deleteCompetitionEnterpriseRelaBySeriesId(relaId);
    }
}
