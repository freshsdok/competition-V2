package com.teaching.competition.domain;

import lombok.Data;

import java.util.List;

/**
 * 赛场安排绑定团队入参。
 */
@Data
public class CompetitionSceneScheduleTeamBindDTO {
    private List<String> teamCodes;
}
