package com.teaching.competition.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.constant.TdConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.mapper.CompetitionConfigMapper;
import com.teaching.competition.mapper.CompetitionEnterpriseRelaMapper;
import com.teaching.competition.mapper.CompetitionTrackConfigMapper;
import com.teaching.competition.mapper.CompetitionTrackInfoMapper;
import com.teaching.competition.service.ICompetitionTrackInfoService;
import com.teaching.competition.util.UUIDUtils;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 赛事赛道配置Service业务层处理
 *
 * @author teaching
 * @date 2025-11-17
 */
@Service
public class CompetitionTrackInfoServiceImpl implements ICompetitionTrackInfoService
{
    @Autowired
    private CompetitionTrackInfoMapper competitionTrackInfoMapper;

    @Autowired
    private RemoteUserService userService;

    @Autowired
    private CompetitionTrackConfigMapper competitionTrackConfigMapper;

    @Autowired
    private CompetitionConfigMapper competitionConfigMapper;

    @Autowired
    private CompetitionEnterpriseRelaMapper competitionEnterpriseRelaMapper;

    @Autowired
    private OrderService orderService;

    /**
     * 查询赛事赛道配置
     *
     * @param trackId 赛事赛道配置主键
     * @return 赛事赛道配置
     */
    @Override
    public CompetitionTrackInfo selectCompetitionTrackInfoByTrackId(Long trackId) {
        return competitionTrackInfoMapper.selectCompetitionTrackInfoByTrackId(trackId);
    }

    @Override
    public CompetitionTrackInfo selectCompetitionTrackInfoByCompetitionTrackId(String competitionTrackId) {
        return competitionTrackInfoMapper.selectCompetitionTrackInfoByCompetitionTrackId(competitionTrackId);
    }

    /**
     * 查询赛事赛道配置列表
     *
     * @param competitionTrackInfo 赛事赛道配置
     * @return 赛事赛道配置
     */
    @Override
    public List<CompetitionTrackInfo> selectCompetitionTrackInfoList(CompetitionTrackInfo competitionTrackInfo) {
        List<CompetitionTrackInfo> competitionTrackInfos = competitionTrackInfoMapper.selectCompetitionTrackInfoList(competitionTrackInfo);
        if(CollectionUtils.isNotEmpty(competitionTrackInfos)){
            // 获取审核意见
            competitionTrackInfos.stream().forEach(competitionTrack-> {
                competitionTrack.setApplyReason(orderService.innerGetCheckOpinion(TdConstants.AUDIT_FLOW_TYPE_RACETRACK, competitionTrack.getTrackId(), SecurityConstants.INNER).getData());
            });
        }
        return competitionTrackInfos;
    }

    /**
     * 新增赛事赛道配置
     *
     * @param competitionTrackInfoEntity 赛事赛道配置
     * @return 结果
     */
    @Override
    @Transactional
    public CompetitionTrackConfig insertCompetitionTrackInfo(CompetitionTrackInfoEntity competitionTrackInfoEntity) {
        CompetitionTrackInfo competitionTrackInfo = new CompetitionTrackInfo();
        competitionTrackInfo.setCheckPackageId(competitionTrackInfoEntity.getCheckPackageId());
        competitionTrackInfo.setCompetitionSeriesId(competitionTrackInfoEntity.getCompetitionSeriesId());
        competitionTrackInfo.setCompetitionTrackType(competitionTrackInfoEntity.getCompetitionTrackType());
        competitionTrackInfo.setCompetitionTrackName(competitionTrackInfoEntity.getCompetitionTrackName());
        competitionTrackInfo.setCheckStatus(Constants.NO_CHECK);
        if(StringUtils.isNotEmpty(competitionTrackInfoEntity.getCompetitionTrackId())){
            competitionTrackInfo.setUpdateTime(DateUtils.getNowDate());
            competitionTrackInfo.setCompetitionTrackId(competitionTrackInfoEntity.getCompetitionTrackId());
            competitionTrackInfoMapper.updateCompetitionTrackInfo(competitionTrackInfo);
        } else {
            String competitionTrackId = "SD_" + UUIDUtils.getRandomCode();
            competitionTrackInfo.setCreateTime(DateUtils.getNowDate());
            competitionTrackInfo.setCompetitionTrackId(competitionTrackId);
            // 创建赛道名称做唯一性校验,同一个赛事不能创建相同赛道
            if(StringUtils.isEmpty(competitionTrackInfoEntity.getCompetitionTrackName())){
                throw new GlobalException("赛道名称不能为空");
            }
            int count = competitionTrackInfoMapper.checkCompetitionTrackInfoUnique
                    (competitionTrackInfoEntity.getCompetitionTrackName(), competitionTrackInfoEntity.getCompetitionSeriesId());
            if(count > 0){
                throw new GlobalException("该赛事下赛道名称已存在");
            }
            competitionTrackInfoMapper.insertCompetitionTrackInfo(Arrays.asList(competitionTrackInfo));
        }
        CompetitionTrackConfig competitionTrackConfig = new CompetitionTrackConfig();
        competitionTrackConfig.setCompetitionTrackId(competitionTrackInfo.getCompetitionTrackId());
        competitionTrackConfig.setCompetitionTrackType(competitionTrackInfoEntity.getCompetitionTrackType());
        competitionTrackConfig.setSecondLevelName(competitionTrackInfoEntity.getSecondLevelName());
        if(Objects.isNull(competitionTrackInfoEntity.getCompetitionTrackConfigId())){
            competitionTrackConfig.setCompetitionTrackType(competitionTrackInfo.getCompetitionTrackType());
            competitionTrackConfig.setSecondLevelCode("CT_"+UUIDUtils.getRandomCode());
            competitionTrackConfig.setCreateTime(DateUtils.getNowDate());
            competitionTrackConfigMapper.insertCompetitionTrackConfig(competitionTrackConfig);
        } else {
            competitionTrackConfig.setCompetitionTrackConfigId(competitionTrackInfoEntity.getCompetitionTrackConfigId());
            competitionTrackConfig.setUpdateTime(DateUtils.getNowDate());
            competitionTrackConfigMapper.updateCompetitionTrackConfig(competitionTrackConfig);
        }
        // 新增审核任务
//        orderService.innerAddAuditTask(TdConstants.AUDIT_FLOW_TYPE_RACETRACK, competitionTrackInfo.getTrackId(),SecurityConstants.INNER);
        return competitionTrackConfig;
    }

    /**
     * 修改赛事赛道配置
     *
     * @param competitionTrackInfo 赛事赛道配置
     * @return 结果
     */
    @Override
    public int updateCompetitionTrackInfo(CompetitionTrackInfo competitionTrackInfo) {
        competitionTrackInfo.setUpdateTime(DateUtils.getNowDate());
        return competitionTrackInfoMapper.updateCompetitionTrackInfo(competitionTrackInfo);
    }

    @Override
    public int updateCompetitionTrackStatus(CompetitionTrackInfo competitionTrackInfo) {
        competitionTrackInfo.setUpdateTime(DateUtils.getNowDate());
        return competitionTrackInfoMapper.updateCompetitionTrackInfo(competitionTrackInfo);
    }

    /**
     * 批量删除赛事赛道配置
     *
     * @param competitionTrackIds 需要删除的赛事赛道配置主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionTrackInfoByCompetitionTrackIds(String[] competitionTrackIds) {
        return competitionTrackInfoMapper.deleteCompetitionTrackInfoByCompetitionTrackIds(competitionTrackIds);
    }

    /**
     * 删除赛事赛道配置信息
     *
     * @param competitionTrackId 赛事赛道配置主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteCompetitionTrackInfoByCompetitionTrackId(String competitionTrackId) {
        // 删除赛道所有配置信息
        CompetitionTrackInfo competitionTrackInfo = competitionTrackInfoMapper.selectCompetitionTrackInfoByCompetitionTrackId(competitionTrackId);
        if(Objects.nonNull(competitionTrackInfo)){
            List<CompetitionTrackConfig> competitionTrackConfigList = competitionTrackInfo.getCompetitionTrackConfigList();
            if(CollectionUtils.isNotEmpty(competitionTrackConfigList)){
                Long[] competitionTrackConfigIds =
                        competitionTrackConfigList.stream().map(CompetitionTrackConfig::getCompetitionTrackConfigId).toArray(Long[]::new);
                competitionTrackConfigMapper.deleteCompetitionTrackConfigByCompetitionTrackConfigIds(competitionTrackConfigIds);
                competitionConfigMapper.deleteCompetitionConfigByConfigIds(competitionTrackConfigIds);
                competitionEnterpriseRelaMapper.deleteCompetitionEnterpriseRelaByConfigIds(competitionTrackConfigIds);
            }
        }
        return competitionTrackInfoMapper.deleteCompetitionTrackInfoByCompetitionTrackId(competitionTrackId);
    }
}
