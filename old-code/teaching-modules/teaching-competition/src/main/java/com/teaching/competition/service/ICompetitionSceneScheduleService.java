package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneMatchResult;
import com.teaching.competition.domain.CompetitionSceneScheduleAutoSequenceDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleManualTargetDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleNameSequenceDTO;
import com.teaching.competition.domain.CompetitionSceneSchedulePersonBindDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleReviewObjectBindDTO;
import com.teaching.competition.domain.CompetitionSceneSchedule;
import com.teaching.competition.domain.CompetitionSceneScheduleTarget;
import com.teaching.competition.domain.CompetitionSceneScheduleSyncReviewSessionDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleTargetSequenceDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleTeamBindDTO;

import java.util.List;

/**
 * 赛事现场赛场安排Service接口。
 */
public interface ICompetitionSceneScheduleService {

    CompetitionSceneSchedule selectCompetitionSceneScheduleById(Long scheduleId);

    List<CompetitionSceneSchedule> selectCompetitionSceneScheduleList(CompetitionSceneSchedule schedule);

    int insertCompetitionSceneSchedule(CompetitionSceneSchedule schedule);

    int updateCompetitionSceneSchedule(CompetitionSceneSchedule schedule);

    int deleteCompetitionSceneScheduleByIds(Long[] scheduleIds);

    CompetitionSceneMatchResult matchScheduleTargets(Long scheduleId);

    List<CompetitionSceneScheduleTarget> selectScheduleTargetList(CompetitionSceneScheduleTarget target);

    CompetitionSceneMatchResult insertScheduleTarget(CompetitionSceneScheduleTarget target);

    CompetitionSceneMatchResult insertScheduleTargets(List<CompetitionSceneScheduleTarget> targets);

    CompetitionSceneMatchResult bindReviewObjects(Long scheduleId, CompetitionSceneScheduleReviewObjectBindDTO dto);

    CompetitionSceneMatchResult bindTeams(Long scheduleId, CompetitionSceneScheduleTeamBindDTO dto);

    CompetitionSceneMatchResult bindPersons(Long scheduleId, CompetitionSceneSchedulePersonBindDTO dto);

    CompetitionSceneMatchResult insertManualTarget(Long scheduleId, CompetitionSceneScheduleManualTargetDTO dto);

    CompetitionSceneMatchResult updateTargetSequences(Long scheduleId, List<CompetitionSceneScheduleTargetSequenceDTO> items);

    CompetitionSceneMatchResult autoGenerateTargetSequence(Long scheduleId, CompetitionSceneScheduleAutoSequenceDTO dto);

    CompetitionSceneMatchResult generateTargetSequenceByNames(Long scheduleId, CompetitionSceneScheduleNameSequenceDTO dto);

    CompetitionSceneMatchResult syncTargetsToReviewSession(Long scheduleId, CompetitionSceneScheduleSyncReviewSessionDTO dto);

    int updateScheduleTarget(CompetitionSceneScheduleTarget target);

    int deleteScheduleTargetByIds(Long[] targetIds);
}
