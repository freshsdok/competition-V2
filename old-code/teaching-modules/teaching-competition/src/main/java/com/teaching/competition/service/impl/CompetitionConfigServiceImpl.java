package com.teaching.competition.service.impl;

import java.util.List;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.system.api.domain.CompetitionConfig;
import com.teaching.competition.mapper.CompetitionConfigMapper;
import com.teaching.competition.service.ICompetitionConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 赛事配置Service业务层处理
 * 
 * @author teaching
 * @date 2025-10-13
 */
@Service
public class CompetitionConfigServiceImpl implements ICompetitionConfigService
{
    @Autowired
    private CompetitionConfigMapper competitionConfigMapper;

    /**
     * 查询赛事配置
     * 
     * @param configId 赛事配置主键
     * @return 赛事配置
     */
    @Override
    public CompetitionConfig selectCompetitionConfigByConfigId(Long configId)
    {
        return competitionConfigMapper.selectCompetitionConfigByConfigId(configId);
    }

    /**
     * 查询赛事配置列表
     * 
     * @param competitionConfig 赛事配置
     * @return 赛事配置
     */
    @Override
    public List<CompetitionConfig> selectCompetitionConfigList(CompetitionConfig competitionConfig)
    {
        return competitionConfigMapper.selectCompetitionConfigList(competitionConfig.getCompetitionTrackConfigId());
    }

    /**
     * 新增赛事配置
     * 
     * @param competitionConfig 赛事配置
     * @return 结果
     */
    @Override
    public int insertCompetitionConfig(CompetitionConfig competitionConfig)
    {
        competitionConfig.setCreateTime(DateUtils.getNowDate());
        return competitionConfigMapper.insertCompetitionConfig(competitionConfig);
    }

    /**
     * 修改赛事配置
     * 
     * @param competitionConfig 赛事配置
     * @return 结果
     */
    @Override
    public int updateCompetitionConfig(CompetitionConfig competitionConfig)
    {
        competitionConfig.setUpdateTime(DateUtils.getNowDate());
        return competitionConfigMapper.updateCompetitionConfig(competitionConfig);
    }

    /**
     * 批量删除赛事配置
     * 
     * @param configIds 需要删除的赛事配置主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionConfigByConfigIds(Long[] configIds)
    {
        return competitionConfigMapper.deleteCompetitionConfigByConfigIds(configIds);
    }

    /**
     * 删除赛事配置信息
     * 
     * @param configId 赛事配置主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionConfigByConfigId(Long configId)
    {
        return competitionConfigMapper.deleteCompetitionConfigByConfigId(configId);
    }
}
