package com.teaching.competition.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.mapper.CompetitionConfigMapper;
import com.teaching.system.api.domain.CompetitionConfig;
import com.teaching.system.api.domain.CompetitionSeriesInfo;
import com.teaching.competition.mapper.CompetitionSeriesInfoMapper;
import com.teaching.competition.service.ICompetitionSeriesInfoService;
import netscape.javascript.JSObject;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;import org.springframework.transaction.annotation.Transactional;


/**
 * 赛事系列信息Service业务层处理
 *
 * @author teaching
 * @date 2025-10-13
 */
@Service
public class CompetitionSeriesInfoServiceImpl implements ICompetitionSeriesInfoService {

    private static final Logger log = LoggerFactory.getLogger(CompetitionSeriesInfoServiceImpl.class);

    @Autowired
    private CompetitionSeriesInfoMapper competitionSeriesInfoMapper;

    @Autowired
    private CompetitionConfigMapper competitionConfigMapper;

    /**
     * 查询赛事系列信息
     *
     * @param competitionSeriesId 赛事系列信息主键
     * @return 赛事系列信息
     */
    @Override
    public CompetitionSeriesInfo selectCompetitionSeriesInfoByCompetitionSeriesId(Long competitionId,Long competitionSeriesId)
    {
        return competitionSeriesInfoMapper.selectCompetitionSeriesInfoByCompetitionSeriesId(competitionId,competitionSeriesId);
    }

    /**
     * 查询赛事系列信息列表
     *
     * @param competitionSeriesInfo 赛事系列信息
     * @return 赛事系列信息
     */
    @Override
    public List<CompetitionSeriesInfo> selectCompetitionSeriesInfoList(CompetitionSeriesInfo competitionSeriesInfo)
    {
        return competitionSeriesInfoMapper.selectCompetitionSeriesInfoList(competitionSeriesInfo);
    }

    /**
     * 新增赛事系列信息
     *
     * @param competitionSeriesInfo 赛事系列信息
     * @return 结果
     */
    @Override
    public int insertCompetitionSeriesInfo(CompetitionSeriesInfo competitionSeriesInfo)
    {
        competitionSeriesInfo.setCreateTime(DateUtils.getNowDate());
        return competitionSeriesInfoMapper.insertCompetitionSeriesInfo(competitionSeriesInfo);
    }

    /**
     * 修改赛事系列信息
     *
     * @param competitionSeriesInfo 赛事系列信息
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int updateCompetitionSeriesInfo(CompetitionSeriesInfo competitionSeriesInfo) {
        log.info("赛事数据参数:"+ JSONObject.toJSONString(competitionSeriesInfo));
        // 查库状态，修改最新状态
        CompetitionSeriesInfo seriesInfo = competitionSeriesInfoMapper.selectCompetitionSeriesInfoByCompetitionSeriesId(competitionSeriesInfo.getCompetitionId(), competitionSeriesInfo.getCompetitionSeriesId());
        log.info("赛事数据:"+ JSONObject.toJSONString(seriesInfo));
        if(Objects.isNull(seriesInfo)){
            throw new GlobalException("不存在赛事信息");
        }
        if(!Constants.COMPETITION_PUBLISH.equals(seriesInfo.getCheckStatus()) && Constants.COMPETITION_RUNNING.equals(competitionSeriesInfo.getCheckStatus())){
            throw new GlobalException("只有已发布状态的赛事可启动");
        }
        if(!Constants.COMPETITION_PUBLISH.equals(seriesInfo.getCheckStatus())){
            if(Constants.COMPETITION_REPEAL_PUBLISH.equals(competitionSeriesInfo.getCheckStatus())){
                throw new GlobalException("只有已发布状态的赛事可撤销");
            }
        }
//        if (Constants.COMPETITION_REPEAL_PUBLISH.equals(competitionSeriesInfo.getCheckStatus())) {
//            // 报名时间开始不可撤销发布
//            List<CompetitionConfig> competitionConfigs =
//                    competitionConfigMapper.selectCompetitionConfigList(competitionSeriesInfo.getCompetitionSeriesId());
//            // 赛事阶段只要存在报名时间开始，则不可撤销发布
//            if(CollectionUtils.isNotEmpty(competitionConfigs)){
//                competitionConfigs.stream().forEach(competitionConfig -> {
//                    if(Objects.nonNull(competitionConfig.getApplyStartTime())){
//                        if(competitionConfig.getApplyStartTime().getTime()<= System.currentTimeMillis()){
//                            throw new GlobalException("赛事报名已开始，请勿撤销发布");
//                        }
//                    }
//                });
//            }
//        }
        // 赛事发布获取发布人及发布时间
//        if(Constants.COMPETITION_PUBLISH.equals(competitionSeriesInfo.getCheckStatus())){
//            competitionSeriesInfo.setPublishPerson(SecurityUtils.getLoginUser().getSysUser().getUserId());
//            competitionSeriesInfo.setPublishTime(DateUtils.getNowDate());
//        }
        competitionSeriesInfo.setUpdateTime(DateUtils.getNowDate());
        return competitionSeriesInfoMapper.updateCompetitionSeriesInfo(competitionSeriesInfo);
    }

    /**
     * 批量删除赛事系列信息
     *
     * @param competitionSeriesIds 需要删除的赛事系列信息主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionSeriesInfoByCompetitionSeriesIds(Long[] competitionSeriesIds)
    {
        return competitionSeriesInfoMapper.deleteCompetitionSeriesInfoByCompetitionSeriesIds(competitionSeriesIds);
    }

    /**
     * 删除赛事系列信息信息
     *
     * @param competitionSeriesId 赛事系列信息主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionSeriesInfoByCompetitionSeriesId(Long competitionSeriesId)
    {
        return competitionSeriesInfoMapper.deleteCompetitionSeriesInfoByCompetitionSeriesId(competitionSeriesId);
    }
}
