package com.teaching.competition.service.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.competition.domain.UserGradeInfo;
import com.teaching.competition.mapper.UserGradeInfoMapper;
import com.teaching.system.api.domain.CompetitionAwardsConfig;
import com.teaching.competition.service.ICompetitionAwardsConfigService;
import com.teaching.competition.mapper.CompetitionAwardsConfigMapper;
import com.teaching.competition.util.UUIDUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 赛事奖项设置Service业务层处理
 * 
 * @author teaching
 * @date 2025-10-11
 */
@Service
public class CompetitionAwardsConfigServiceImpl implements ICompetitionAwardsConfigService
{
    @Autowired
    private CompetitionAwardsConfigMapper competitionAwardsConfigMapper;

    @Autowired
    private UserGradeInfoMapper userGradeInfoMapper;

    /**
     * 查询赛事奖项设置
     * 
     * @param awardsId 赛事奖项设置主键
     * @return 赛事奖项设置
     */
    @Override
    public CompetitionAwardsConfig selectCompetitionAwardsConfigByAwardsId(Long awardsId)
    {
        return competitionAwardsConfigMapper.selectCompetitionAwardsConfigByAwardsId(awardsId);
    }

    /**
     * 查询赛事奖项设置列表
     * 
     * @param competitionAwardsConfig 赛事奖项设置
     * @return 赛事奖项设置
     */
    @Override
    public List<CompetitionAwardsConfig> selectCompetitionAwardsConfigList(CompetitionAwardsConfig competitionAwardsConfig) {
        // 统计已设定奖项数量
        UserGradeInfo userGradeInfoRes = new UserGradeInfo();
        userGradeInfoRes.setCompetitionSeriesId(competitionAwardsConfig.getCompetitionSeriesId());
        userGradeInfoRes.setStageId(competitionAwardsConfig.getStageId());
        userGradeInfoRes.setCompetitionTrackName(competitionAwardsConfig.getCompetitionTrackName());
        userGradeInfoRes.setGroupClassify(competitionAwardsConfig.getGroupClassify());
        List<Map<String, Object>> awardsNumList = userGradeInfoMapper.selectUserGradeInfoNum(userGradeInfoRes);
        List<CompetitionAwardsConfig> competitionAwardsConfigs = competitionAwardsConfigMapper.selectCompetitionAwardsConfigList(competitionAwardsConfig);
        if(CollectionUtils.isNotEmpty(competitionAwardsConfigs) && CollectionUtils.isNotEmpty(awardsNumList)){
            competitionAwardsConfigs.stream().forEach(competitionAwards -> {
                awardsNumList.stream().forEach(awardsNum -> {
                    if(awardsNum.get("awardsName").equals(competitionAwards.getAwardsName())){
                        competitionAwards.setAllocatedNum(awardsNum.get("num").toString());
                        int count = Integer.valueOf(awardsNum.get("num").toString());
                        int sum = Integer.valueOf(competitionAwards.getAwardNum());
                        competitionAwards.setUnabsorbedNum(Math.subtractExact(sum, count)+"");
                    }
                });
            });
        }
        if(CollectionUtils.isNotEmpty(competitionAwardsConfigs) && CollectionUtils.isEmpty(awardsNumList)){
            competitionAwardsConfigs.stream().forEach(competitionAwards -> {
                competitionAwards.setAllocatedNum(competitionAwards.getAwardNum());
            });
        }
        return competitionAwardsConfigs;
    }

    /**
     * 新增赛事奖项设置
     * 
     * @param competitionAwardsConfigs 赛事奖项设置
     * @return 结果
     */
    @Override
    public int insertCompetitionAwardsConfig(List<CompetitionAwardsConfig> competitionAwardsConfigs)
    {
        if(CollectionUtils.isNotEmpty(competitionAwardsConfigs)){
            for (CompetitionAwardsConfig competitionAwardsConfig : competitionAwardsConfigs) {
                competitionAwardsConfig.setCreateTime(DateUtils.getNowDate());
                competitionAwardsConfig.setAwardsId(UUIDUtils.getUUID());
            }
        }
        return competitionAwardsConfigMapper.insertCompetitionAwardsConfig(competitionAwardsConfigs);
    }

    /**
     * 修改赛事奖项设置
     * 
     * @param competitionAwardsConfig 赛事奖项设置
     * @return 结果
     */
    @Override
    public int updateCompetitionAwardsConfig(CompetitionAwardsConfig competitionAwardsConfig) {
        competitionAwardsConfig.setUpdateTime(DateUtils.getNowDate());
        return competitionAwardsConfigMapper.updateCompetitionAwardsConfig(competitionAwardsConfig);
    }

    /**
     * 批量删除赛事奖项设置
     * 
     * @param awardsIds 需要删除的赛事奖项设置主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionAwardsConfigByAwardsIds(Long[] awardsIds)
    {
        return competitionAwardsConfigMapper.deleteCompetitionAwardsConfigByAwardsIds(awardsIds);
    }

    /**
     * 删除赛事奖项设置信息
     * 
     * @param awardsId 赛事奖项设置主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionAwardsConfigByAwardsId(Long awardsId)
    {
        return competitionAwardsConfigMapper.deleteCompetitionAwardsConfigBySeriesId(awardsId);
    }
}
