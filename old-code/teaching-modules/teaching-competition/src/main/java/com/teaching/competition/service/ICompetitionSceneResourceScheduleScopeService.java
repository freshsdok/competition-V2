package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneResourceScheduleScope;

import java.util.List;

/**
 * 赛事现场资源允许预约赛场范围Service接口。
 */
public interface ICompetitionSceneResourceScheduleScopeService {

    List<CompetitionSceneResourceScheduleScope> listByScheduleResourceId(Long scheduleResourceId);

    List<Long> listAllowedScheduleIds(Long scheduleResourceId);

    boolean existsAllowedSchedule(Long scheduleResourceId, Long allowedScheduleId);

    CompetitionSceneResourceScheduleScope addManualBindSchedule(Long scheduleResourceId,
                                                                Long resourceId,
                                                                Long allowedScheduleId);

    int removeManualBindSchedule(Long scheduleResourceId, Long allowedScheduleId);

    CompetitionSceneResourceScheduleScope ensureManualBindSchedule(Long scheduleResourceId,
                                                                   Long resourceId,
                                                                   Long allowedScheduleId);

    List<CompetitionSceneResourceScheduleScope> batchEnsureManualBindSchedules(Long scheduleResourceId,
                                                                               Long resourceId,
                                                                               List<Long> allowedScheduleIds);
}
