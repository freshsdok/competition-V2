package com.teaching.competition.service.impl;

import com.teaching.competition.domain.CompetitionSceneCheckinOverviewQuery;
import com.teaching.competition.domain.CompetitionSceneCheckinOverviewStatisticsVO;
import com.teaching.competition.domain.CompetitionSceneCheckinPersonVO;
import com.teaching.competition.domain.CompetitionSceneCheckinScheduleCardVO;
import com.teaching.competition.domain.CompetitionSceneCheckinScheduleDetailVO;
import com.teaching.competition.domain.CompetitionSceneCheckinTeamVO;
import com.teaching.competition.mapper.CompetitionSceneCheckinOverviewMapper;
import com.teaching.competition.service.ICompetitionSceneCheckinOverviewService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 签到概览Service实现。
 */
@Service
public class CompetitionSceneCheckinOverviewServiceImpl implements ICompetitionSceneCheckinOverviewService {

    @Autowired
    private CompetitionSceneCheckinOverviewMapper checkinOverviewMapper;

    @Override
    public CompetitionSceneCheckinOverviewStatisticsVO selectStatistics(CompetitionSceneCheckinOverviewQuery query) {
        CompetitionSceneCheckinOverviewStatisticsVO statistics = checkinOverviewMapper.selectStatistics(query);
        if (statistics == null) {
            statistics = new CompetitionSceneCheckinOverviewStatisticsVO();
        }
        fillStatisticsDefaults(statistics);
        statistics.setLowRateRank(checkinOverviewMapper.selectLowRateRank(query));
        statistics.setStartTimeGroups(checkinOverviewMapper.selectStartTimeGroups(query));
        return statistics;
    }

    @Override
    public List<CompetitionSceneCheckinScheduleCardVO> selectScheduleCards(CompetitionSceneCheckinOverviewQuery query) {
        return checkinOverviewMapper.selectScheduleCards(query);
    }

    @Override
    public CompetitionSceneCheckinScheduleDetailVO selectScheduleDetail(Long scheduleId) {
        CompetitionSceneCheckinOverviewQuery query = new CompetitionSceneCheckinOverviewQuery();
        query.setScheduleId(scheduleId);
        CompetitionSceneCheckinScheduleCardVO card = checkinOverviewMapper.selectScheduleCardById(query);
        if (card == null) {
            return null;
        }
        CompetitionSceneCheckinScheduleDetailVO detail = new CompetitionSceneCheckinScheduleDetailVO();
        BeanUtils.copyProperties(card, detail);
        List<CompetitionSceneCheckinTeamVO> teams = checkinOverviewMapper.selectTeams(scheduleId);
        detail.setTeams(teams == null ? new ArrayList<>() : teams);
        return detail;
    }

    @Override
    public List<CompetitionSceneCheckinPersonVO> selectPersons(CompetitionSceneCheckinOverviewQuery query) {
        return checkinOverviewMapper.selectPersons(query);
    }

    private void fillStatisticsDefaults(CompetitionSceneCheckinOverviewStatisticsVO statistics) {
        if (statistics.getTotalPersonCount() == null) {
            statistics.setTotalPersonCount(0L);
        }
        if (statistics.getSignedPersonCount() == null) {
            statistics.setSignedPersonCount(0L);
        }
        if (statistics.getUnsignedPersonCount() == null) {
            statistics.setUnsignedPersonCount(0L);
        }
        if (statistics.getCheckinRate() == null) {
            statistics.setCheckinRate(BigDecimal.ZERO);
        }
        if (statistics.getScheduleCount() == null) {
            statistics.setScheduleCount(0L);
        }
        if (statistics.getWarningScheduleCount() == null) {
            statistics.setWarningScheduleCount(0L);
        }
    }
}
