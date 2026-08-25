package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneCheckinOverviewQuery;
import com.teaching.competition.domain.CompetitionSceneCheckinOverviewStatisticsVO;
import com.teaching.competition.domain.CompetitionSceneCheckinPersonVO;
import com.teaching.competition.domain.CompetitionSceneCheckinScheduleCardVO;
import com.teaching.competition.domain.CompetitionSceneCheckinScheduleDetailVO;

import java.util.List;

/**
 * 签到概览Service。
 */
public interface ICompetitionSceneCheckinOverviewService {

    CompetitionSceneCheckinOverviewStatisticsVO selectStatistics(CompetitionSceneCheckinOverviewQuery query);

    List<CompetitionSceneCheckinScheduleCardVO> selectScheduleCards(CompetitionSceneCheckinOverviewQuery query);

    CompetitionSceneCheckinScheduleDetailVO selectScheduleDetail(Long scheduleId);

    List<CompetitionSceneCheckinPersonVO> selectPersons(CompetitionSceneCheckinOverviewQuery query);
}
