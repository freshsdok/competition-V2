package com.teaching.competition.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.mapper.CompetitionConfigMapper;
import com.teaching.competition.mapper.CompetitionEnterpriseRelaMapper;
import com.teaching.competition.mapper.CompetitionTrackConfigMapper;
import com.teaching.competition.mapper.CompetitionTrackInfoMapper;
import com.teaching.competition.service.ICompetitionTrackConfigService;
import com.teaching.competition.util.UUIDUtils;
import com.teaching.system.api.domain.CompetitionConfig;
import com.teaching.system.api.domain.CompetitionEnterpriseRela;
import com.teaching.system.api.domain.CompetitionTrackConfig;
import com.teaching.system.api.domain.CompetitionTrackInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 赛道配置Service业务层处理
 * 
 * @author teaching
 * @date 2025-12-01
 */
@Service
public class CompetitionTrackConfigServiceImpl implements ICompetitionTrackConfigService {
    private static final Logger log = LoggerFactory.getLogger(CompetitionTrackConfigServiceImpl.class);
    @Autowired
    private CompetitionTrackConfigMapper competitionTrackConfigMapper;

    @Autowired
    private CompetitionConfigMapper competitionConfigMapper;

    @Autowired
    private CompetitionEnterpriseRelaMapper competitionEnterpriseRelaMapper;

    @Autowired
    private CompetitionTrackInfoMapper competitionTrackInfoMapper;

    /**
     * 查询赛道配置
     * 
     * @param competitionTrackConfigId 赛道配置主键
     * @return 赛道配置
     */
    @Override
    public CompetitionTrackConfig selectCompetitionTrackConfigByCompetitionTrackConfigId(Long competitionTrackConfigId) {
        CompetitionTrackConfig competitionTrackConfig = new CompetitionTrackConfig();
        competitionTrackConfig.setCompetitionTrackConfigId(competitionTrackConfigId);
        return competitionTrackConfigMapper.selectCompetitionTrackConfigByConfigId(competitionTrackConfig);
    }

    /**
     * 查询赛道配置列表
     * 
     * @param competitionTrackConfig 赛道配置
     * @return 赛道配置
     */
    @Override
    public List<CompetitionTrackConfig> selectCompetitionTrackConfigList(CompetitionTrackConfig competitionTrackConfig) {
        return competitionTrackConfigMapper.selectCompetitionTrackConfigList(competitionTrackConfig);
    }

    /**
     * 新增赛道配置
     * 
     * @param competitionTrackConfig 赛道配置
     * @return 结果
     */
    @Transactional
    @Override
    public int insertCompetitionTrackConfig(CompetitionTrackConfig competitionTrackConfig) {
        if(StringUtils.isNotEmpty(competitionTrackConfig.getCompetitionTrackId())){
            CompetitionTrackInfo competitionTrackInfo = new CompetitionTrackInfo();
            competitionTrackInfo.setUpdateTime(DateUtils.getNowDate());
            competitionTrackInfo.setCompetitionTrackId(competitionTrackConfig.getCompetitionTrackId());
            competitionTrackInfo.setCheckStatus(Constants.NO_CHECK);
            competitionTrackInfoMapper.updateCompetitionTrackInfo(competitionTrackInfo);
        }
        if (Objects.nonNull(competitionTrackConfig.getCompetitionTrackConfigId())) {
            competitionTrackConfig.setUpdateTime(DateUtils.getNowDate());
            competitionTrackConfigMapper.updateCompetitionTrackConfig(competitionTrackConfig);
        }
        // 二级分类配置信息保存
        CompetitionConfig competitionConfig = competitionTrackConfig.getCompetitionConfig();
        if (Objects.isNull(competitionConfig)) {
            log.info("二级分类配置信息入参为空");
            return 1;
        }
        competitionConfig.setCompetitionTrackConfigId(competitionTrackConfig.getCompetitionTrackConfigId());
        CompetitionConfig competitionConfigInfo =
                competitionConfigMapper.selectCompetitionConfigByTrackConfigId(competitionTrackConfig.getCompetitionTrackConfigId());
        if (Objects.isNull(competitionConfigInfo)) {
            competitionConfig.setCreateTime(DateUtils.getNowDate());
            competitionConfigMapper.insertCompetitionConfig(competitionConfig);
        } else {
            competitionConfig.setUpdateTime(DateUtils.getNowDate());
            competitionConfigMapper.updateCompetitionConfig(competitionConfig);
        }
        // 赞助企业配置信息保存
        List<CompetitionEnterpriseRela> createEnterpriseConfigList = new ArrayList<>();
        List<CompetitionEnterpriseRela> updateEnterpriseConfigList = new ArrayList<>();
        List<CompetitionEnterpriseRela> enterpriseConfigList = competitionTrackConfig.getCompetitionTrackEnterpriseList();
        if (CollectionUtils.isNotEmpty(enterpriseConfigList)) {
            enterpriseConfigList.stream().forEach(enterpriseConfig -> {
                enterpriseConfig.setCompetitionTrackConfigId(competitionTrackConfig.getCompetitionTrackConfigId());
                if (Objects.nonNull(enterpriseConfig.getRelaId())) {
                    enterpriseConfig.setUpdateTime(DateUtils.getNowDate());
                    updateEnterpriseConfigList.add(enterpriseConfig);
                } else {
                    enterpriseConfig.setRelaId(UUIDUtils.getUUID());
                    enterpriseConfig.setCreateTime(DateUtils.getNowDate());
                    createEnterpriseConfigList.add(enterpriseConfig);
                }
            });
            if (CollectionUtils.isNotEmpty(createEnterpriseConfigList)) {
                competitionEnterpriseRelaMapper.insertCompetitionEnterpriseRela(createEnterpriseConfigList);
            }
            if (CollectionUtils.isNotEmpty(updateEnterpriseConfigList)) {
                competitionEnterpriseRelaMapper.batchUpdateCompetitionEnterpriseRela(updateEnterpriseConfigList);
            }
        }
        return 1;
    }

    /**
     * 修改赛道配置
     * 
     * @param competitionTrackConfig 赛道配置
     * @return 结果
     */
    @Override
    public int updateCompetitionTrackConfig(CompetitionTrackConfig competitionTrackConfig) {
        competitionTrackConfig.setUpdateTime(DateUtils.getNowDate());
        return competitionTrackConfigMapper.updateCompetitionTrackConfig(competitionTrackConfig);
    }

    /**
     * 批量删除赛道配置
     * 
     * @param competitionTrackConfigIds 需要删除的赛道配置主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionTrackConfigByCompetitionTrackConfigIds(Long[] competitionTrackConfigIds) {
        competitionConfigMapper.deleteCompetitionConfigByConfigIds(competitionTrackConfigIds);
        competitionEnterpriseRelaMapper.deleteCompetitionEnterpriseRelaByConfigIds(competitionTrackConfigIds);
        return competitionTrackConfigMapper.deleteCompetitionTrackConfigByCompetitionTrackConfigIds(competitionTrackConfigIds);
    }

    /**
     * 删除赛道配置信息
     * 
     * @param competitionTrackConfigId 赛道配置主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionTrackConfigByCompetitionTrackConfigId(Long competitionTrackConfigId) {
        return competitionTrackConfigMapper.deleteCompetitionTrackConfigByCompetitionTrackConfigId(competitionTrackConfigId);
    }
}
