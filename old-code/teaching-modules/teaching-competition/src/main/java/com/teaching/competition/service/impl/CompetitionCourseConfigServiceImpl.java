package com.teaching.competition.service.impl;

import java.util.List;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.system.api.domain.CompetitionCourseConfig;
import com.teaching.competition.service.ICompetitionCourseConfigService;
import com.teaching.competition.mapper.CompetitionCourseConfigMapper;
import com.teaching.competition.util.UUIDUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 赛事关联课程配置Service业务层处理
 * 
 * @author teaching
 * @date 2025-10-11
 */
@Service
public class CompetitionCourseConfigServiceImpl implements ICompetitionCourseConfigService
{
    @Autowired
    private CompetitionCourseConfigMapper competitionCourseConfigMapper;

    /**
     * 查询赛事关联课程配置
     * 
     * @param courseConfigId 赛事关联课程配置主键
     * @return 赛事关联课程配置
     */
    @Override
    public CompetitionCourseConfig selectCompetitionCourseConfigByCourseConfigId(Long courseConfigId)
    {
        return competitionCourseConfigMapper.selectCompetitionCourseConfigByCourseConfigId(courseConfigId);
    }

    /**
     * 查询赛事关联课程配置列表
     * 
     * @param competitionSeriesId 赛事关联课程配置
     * @return 赛事关联课程配置
     */
    @Override
    public List<CompetitionCourseConfig> selectCompetitionCourseConfigList(Long competitionSeriesId)
    {
        return competitionCourseConfigMapper.selectCompetitionCourseConfigList(competitionSeriesId);
    }

    /**
     * 新增赛事关联课程配置
     * 
     * @param competitionCourseConfigs 赛事关联课程配置
     * @return 结果
     */
    @Override
    public int insertCompetitionCourseConfig(List<CompetitionCourseConfig> competitionCourseConfigs)
    {
        if(CollectionUtils.isNotEmpty(competitionCourseConfigs)){
            for (CompetitionCourseConfig competitionCourseConfig : competitionCourseConfigs){
                competitionCourseConfig.setCreateTime(DateUtils.getNowDate());
                competitionCourseConfig.setCourseConfigId(UUIDUtils.getUUID());
            }
        }
        return competitionCourseConfigMapper.insertCompetitionCourseConfig(competitionCourseConfigs);
    }

    /**
     * 修改赛事关联课程配置
     * 
     * @param competitionCourseConfig 赛事关联课程配置
     * @return 结果
     */
    @Override
    public int updateCompetitionCourseConfig(CompetitionCourseConfig competitionCourseConfig)
    {
        competitionCourseConfig.setUpdateTime(DateUtils.getNowDate());
        return competitionCourseConfigMapper.updateCompetitionCourseConfig(competitionCourseConfig);
    }

    /**
     * 批量删除赛事关联课程配置
     * 
     * @param courseConfigIds 需要删除的赛事关联课程配置主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionCourseConfigByCourseConfigIds(Long[] courseConfigIds)
    {
        return competitionCourseConfigMapper.deleteCompetitionCourseConfigByCourseConfigIds(courseConfigIds);
    }

    /**
     * 删除赛事关联课程配置信息
     * 
     * @param courseConfigId 赛事关联课程配置主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionCourseConfigByCourseConfigId(Long courseConfigId)
    {
        return competitionCourseConfigMapper.deleteCompetitionCourseConfigBySeriesId(courseConfigId);
    }
}
