package com.teaching.competition.service.impl;

import java.util.ArrayList;
import java.util.List;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.system.api.domain.CompetitionStageConfig;
import com.teaching.competition.service.ICompetitionStageConfigService;
import com.teaching.competition.mapper.CompetitionStageConfigMapper;
import com.teaching.competition.util.UUIDUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 赛事阶段配置Service业务层处理
 * 
 * @author teaching
 * @date 2025-10-11
 */
@Service
public class CompetitionStageConfigServiceImpl implements ICompetitionStageConfigService
{
    @Autowired
    private CompetitionStageConfigMapper competitionStageConfigMapper;

    /**
     * 查询赛事阶段配置
     * 
     * @param stageId 赛事阶段配置主键
     * @return 赛事阶段配置
     */
    @Override
    public CompetitionStageConfig selectCompetitionStageConfigByStageId(String stageId)
    {
        return competitionStageConfigMapper.selectCompetitionStageConfigByStageId(stageId);
    }

    /**
     * 查询赛事阶段配置列表
     * 
     * @param competitionSeriesId 赛事阶段配置
     * @return 赛事阶段配置
     */
    @Override
    public List<CompetitionStageConfig> selectCompetitionStageConfigList(Long competitionSeriesId)
    {
        if(null != competitionSeriesId){
            return competitionStageConfigMapper.selectCompetitionStageConfigList(competitionSeriesId);
        }
        return new ArrayList<>();
    }

    @Override
    public CompetitionStageConfig selectNowCompetitionStageConfig(Long competitionSeriesId) {
        CompetitionStageConfig stageConfig = new CompetitionStageConfig();
        if(null != competitionSeriesId){
            List<CompetitionStageConfig> competitionStageConfigList =
                    competitionStageConfigMapper.selectCompetitionStageConfigList(competitionSeriesId);
            if(CollectionUtils.isNotEmpty(competitionStageConfigList)){
                competitionStageConfigList.stream().forEach(CompetitionStageConfig ->{
                    if(CompetitionStageConfig.getStageStartTime().getTime()<= System.currentTimeMillis() &&
                            CompetitionStageConfig.getStageEndTime().getTime()>= System.currentTimeMillis()){
                        BeanUtils.copyProperties(CompetitionStageConfig,stageConfig);
                    }
                });
            }
        }
        // 判断阶段是否开始
        if(StringUtils.isNotEmpty(stageConfig.getStageName())){
            return stageConfig;
        }
        return null;
    }

    /**
     * 新增赛事阶段配置
     * 
     * @param competitionStageConfig 赛事阶段配置
     * @return 结果
     */
    @Override
    public int insertCompetitionStageConfig(List<CompetitionStageConfig> stageConfigList)
    {
        if(CollectionUtils.isNotEmpty(stageConfigList)){
            for (CompetitionStageConfig competitionStageConfig:stageConfigList){
                competitionStageConfig.setCreateTime(DateUtils.getNowDate());
                competitionStageConfig.setStageId(UUIDUtils.getUUID());
            }
        }
        return competitionStageConfigMapper.insertCompetitionStageConfig(stageConfigList);
    }

    /**
     * 修改赛事阶段配置
     * 
     * @param competitionStageConfig 赛事阶段配置
     * @return 结果
     */
    @Override
    public int updateCompetitionStageConfig(CompetitionStageConfig competitionStageConfig)
    {
        competitionStageConfig.setUpdateTime(DateUtils.getNowDate());
        return competitionStageConfigMapper.updateCompetitionStageConfig(competitionStageConfig);
    }

    /**
     * 批量删除赛事阶段配置
     * 
     * @param stageIds 需要删除的赛事阶段配置主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionStageConfigByStageIds(Long[] stageIds)
    {
        return competitionStageConfigMapper.deleteCompetitionStageConfigByStageIds(stageIds);
    }

    /**
     * 删除赛事阶段配置信息
     * 
     * @param id 赛事阶段配置主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionStageConfigByStageId(Long id)
    {
        return competitionStageConfigMapper.deleteCompetitionStageConfigBySeriesId(id);
    }
}
