package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.domain.CompetitionPromotedInfo;
import com.teaching.competition.mapper.CompetitionPromotedApplyInfoMapper;
import com.teaching.competition.mapper.CompetitionPromotedInfoMapper;
import com.teaching.competition.service.ICompetitionPromotedInfoService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 赛事晋级Service业务层处理
 *
 * @author teaching
 * @date 2026-05-19
 */
@Service
public class CompetitionPromotedInfoServiceImpl implements ICompetitionPromotedInfoService {
    @Autowired
    private CompetitionPromotedInfoMapper competitionPromotedInfoMapper;

    @Autowired
    private CompetitionPromotedApplyInfoMapper competitionPromotedApplyInfoMapper;

    /**
     * 查询赛事晋级
     *
     * @param promotedId 赛事晋级主键
     * @return 赛事晋级
     */
    @Override
    public CompetitionPromotedInfo selectCompetitionPromotedInfoByPromotedId(Long promotedId) {
        CompetitionPromotedInfo competitionPromotedInfo =
                competitionPromotedInfoMapper.selectCompetitionPromotedInfoByPromotedId(promotedId);
        if (!Objects.isNull(competitionPromotedInfo)) {
            if (Objects.nonNull(competitionPromotedInfo.getApplyStartTime()) && Objects.nonNull(competitionPromotedInfo.getApplyEndTime())) {
                if (competitionPromotedInfo.getApplyStartTime().getTime() <= System.currentTimeMillis()
                        && competitionPromotedInfo.getApplyEndTime().getTime() >= System.currentTimeMillis()) {
                    // 报名中
                    competitionPromotedInfo.setCompetitionApplyStatus("1");
                } else if (competitionPromotedInfo.getApplyStartTime().getTime() > System.currentTimeMillis()) {
                    // 报名未开始
                    competitionPromotedInfo.setCompetitionApplyStatus("0");
                } else {
                    // 报名已结束
                    competitionPromotedInfo.setCompetitionApplyStatus("2");
                }
            } else if (Objects.isNull(competitionPromotedInfo.getApplyStartTime())) {
                // 报名未开始
                competitionPromotedInfo.setCompetitionApplyStatus("0");
            } else if (Objects.nonNull(competitionPromotedInfo.getApplyStartTime()) && Objects.isNull(competitionPromotedInfo.getApplyEndTime())) {
                // 报名中
                competitionPromotedInfo.setCompetitionApplyStatus("1");
            }
            competitionPromotedInfo.setTeamNum(competitionPromotedApplyInfoMapper.getCompetitionPromotedTeamNum(competitionPromotedInfo.getCompetitionSeriesId(), null));
            competitionPromotedInfo.setApplyTeamNum(competitionPromotedApplyInfoMapper.getCompetitionPromotedIsApplyInfoTeamNum(competitionPromotedInfo.getCompetitionSeriesId(), null));
        }
        return competitionPromotedInfo;
    }

    /**
     * 查询赛事晋级列表
     *
     * @param competitionPromotedInfo 赛事晋级
     * @return 赛事晋级
     */
    @Override
    public List<CompetitionPromotedInfo> selectCompetitionPromotedInfoList(CompetitionPromotedInfo competitionPromotedInfo) {
        List<CompetitionPromotedInfo> competitionPromotedInfoList =
                competitionPromotedInfoMapper.selectCompetitionPromotedInfoList(competitionPromotedInfo);
        if (CollectionUtils.isNotEmpty(competitionPromotedInfoList)) {
            competitionPromotedInfoList.forEach(competitionPromotedInfoRes -> {
                if (Objects.nonNull(competitionPromotedInfoRes.getApplyStartTime()) && Objects.nonNull(competitionPromotedInfoRes.getApplyEndTime())) {
                    if (competitionPromotedInfoRes.getApplyStartTime().getTime() <= System.currentTimeMillis()
                            && competitionPromotedInfoRes.getApplyEndTime().getTime() >= System.currentTimeMillis()) {
                        // 报名中
                        competitionPromotedInfoRes.setCompetitionApplyStatus("1");
                    } else if (competitionPromotedInfoRes.getApplyStartTime().getTime() > System.currentTimeMillis()) {
                        // 报名未开始
                        competitionPromotedInfoRes.setCompetitionApplyStatus("0");
                    } else {
                        // 报名已结束
                        competitionPromotedInfoRes.setCompetitionApplyStatus("2");
                    }
                } else if (Objects.isNull(competitionPromotedInfoRes.getApplyStartTime())) {
                    // 报名未开始
                    competitionPromotedInfoRes.setCompetitionApplyStatus("0");
                } else if (Objects.nonNull(competitionPromotedInfoRes.getApplyStartTime()) && Objects.isNull(competitionPromotedInfoRes.getApplyEndTime())) {
                    // 报名中
                    competitionPromotedInfoRes.setCompetitionApplyStatus("1");
                }
                competitionPromotedInfoRes.setTeamNum(competitionPromotedApplyInfoMapper.getCompetitionPromotedTeamNum(competitionPromotedInfoRes.getCompetitionSeriesId(), competitionPromotedInfo.getLeaderTeacherId()));
                competitionPromotedInfoRes.setApplyTeamNum(competitionPromotedApplyInfoMapper.getCompetitionPromotedIsApplyInfoTeamNum(competitionPromotedInfoRes.getCompetitionSeriesId(), competitionPromotedInfo.getLeaderTeacherId()));
            });
        }
        return competitionPromotedInfoList;
    }

    /**
     * 新增赛事晋级
     *
     * @param competitionPromotedInfo 赛事晋级
     * @return 结果
     */
    @Override
    public int insertCompetitionPromotedInfo(CompetitionPromotedInfo competitionPromotedInfo) {
        competitionPromotedInfo.setCreateTime(DateUtils.getNowDate());
        return competitionPromotedInfoMapper.insertCompetitionPromotedInfo(competitionPromotedInfo);
    }

    /**
     * 修改赛事晋级
     *
     * @param competitionPromotedInfo 赛事晋级
     * @return 结果
     */
    @Override
    public int updateCompetitionPromotedInfo(CompetitionPromotedInfo competitionPromotedInfo) {
        competitionPromotedInfo.setUpdateTime(DateUtils.getNowDate());
        competitionPromotedInfo.setCreateBy(SecurityUtils.getLoginUser().getSysUser().getNickName());
        return competitionPromotedInfoMapper.updateCompetitionPromotedInfo(competitionPromotedInfo);
    }

    /**
     * 批量删除赛事晋级
     *
     * @param promotedIds 需要删除的赛事晋级主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionPromotedInfoByPromotedIds(Long[] promotedIds) {
        return competitionPromotedInfoMapper.deleteCompetitionPromotedInfoByPromotedIds(promotedIds);
    }

    /**
     * 删除赛事晋级信息
     *
     * @param promotedId 赛事晋级主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionPromotedInfoByPromotedId(Long promotedId) {
        CompetitionPromotedInfo competitionPromotedInfo = competitionPromotedInfoMapper.selectCompetitionPromotedInfoByPromotedId(promotedId);
        // 存在报名人数则不删除，否则删除
        if (competitionPromotedApplyInfoMapper.
                getCompetitionPromotedIsApplyInfoTeamNum(competitionPromotedInfo.getCompetitionSeriesId(), null) > 0) {
            throw new GlobalException("该赛事已报名，请勿删除！");
        }
        competitionPromotedApplyInfoMapper.logicalDelCompetitionPromotedApplyInfoByCompetitionSeriesId(competitionPromotedInfo.getCompetitionSeriesId());
        return competitionPromotedInfoMapper.deleteCompetitionPromotedInfoByPromotedId(promotedId);
    }
}
