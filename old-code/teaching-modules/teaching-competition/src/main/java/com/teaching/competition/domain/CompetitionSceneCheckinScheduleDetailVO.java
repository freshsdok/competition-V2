package com.teaching.competition.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 赛场签到详情。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneCheckinScheduleDetailVO extends CompetitionSceneCheckinScheduleCardVO {
    private List<CompetitionSceneCheckinTeamVO> teams;
}
