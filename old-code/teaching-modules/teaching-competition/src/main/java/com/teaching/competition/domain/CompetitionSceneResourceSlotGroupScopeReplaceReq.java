package com.teaching.competition.domain;

import lombok.Data;

import java.util.List;

/**
 * 管理端替换预约时段允许组别请求。
 */
@Data
public class CompetitionSceneResourceSlotGroupScopeReplaceReq {
    private Long slotId;
    private Long scheduleResourceId;
    private List<CompetitionSceneResourceSlotGroupScope> groups;
}
