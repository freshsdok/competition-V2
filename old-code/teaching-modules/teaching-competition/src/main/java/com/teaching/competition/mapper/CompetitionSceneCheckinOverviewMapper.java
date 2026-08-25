package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionSceneCheckinOverviewQuery;
import com.teaching.competition.domain.CompetitionSceneCheckinOverviewStatisticsVO;
import com.teaching.competition.domain.CompetitionSceneCheckinPersonVO;
import com.teaching.competition.domain.CompetitionSceneCheckinRankItemVO;
import com.teaching.competition.domain.CompetitionSceneCheckinScheduleCardVO;
import com.teaching.competition.domain.CompetitionSceneCheckinTeamVO;
import com.teaching.competition.domain.CompetitionSceneCheckinTimeGroupVO;

import java.util.List;

/**
 * 签到概览Mapper。
 */
public interface CompetitionSceneCheckinOverviewMapper {

    CompetitionSceneCheckinOverviewStatisticsVO selectStatistics(CompetitionSceneCheckinOverviewQuery query);

    List<CompetitionSceneCheckinRankItemVO> selectLowRateRank(CompetitionSceneCheckinOverviewQuery query);

    List<CompetitionSceneCheckinTimeGroupVO> selectStartTimeGroups(CompetitionSceneCheckinOverviewQuery query);

    List<CompetitionSceneCheckinScheduleCardVO> selectScheduleCards(CompetitionSceneCheckinOverviewQuery query);

    CompetitionSceneCheckinScheduleCardVO selectScheduleCardById(CompetitionSceneCheckinOverviewQuery query);

    List<CompetitionSceneCheckinTeamVO> selectTeams(Long scheduleId);

    List<CompetitionSceneCheckinPersonVO> selectPersons(CompetitionSceneCheckinOverviewQuery query);
}
