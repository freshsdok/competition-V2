package com.teaching.competition.domain;

import lombok.Data;

import java.util.List;

/**
 * 赛场安排绑定人员入参。
 */
@Data
public class CompetitionSceneSchedulePersonBindDTO {
    private List<String> memberIds;
}
