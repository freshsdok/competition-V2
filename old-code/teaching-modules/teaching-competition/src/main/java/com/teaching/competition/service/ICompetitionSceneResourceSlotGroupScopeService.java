package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneResourceSlotGroupScope;

import java.util.List;

/**
 * 赛事现场资源预约时段允许组别Service接口。
 */
public interface ICompetitionSceneResourceSlotGroupScopeService {

    List<CompetitionSceneResourceSlotGroupScope> listBySlotId(Long slotId);

    List<CompetitionSceneResourceSlotGroupScope> listByScheduleResourceId(Long scheduleResourceId);

    boolean existsAllowedGroup(Long slotId, String groupCode);

    int replaceSlotGroups(Long slotId, Long scheduleResourceId, List<CompetitionSceneResourceSlotGroupScope> groups);

    int batchReplaceSlotGroups(List<CompetitionSceneResourceSlotGroupScope> groups);

    boolean isSlotGroupAllowed(Long slotId, String groupCode);
}

