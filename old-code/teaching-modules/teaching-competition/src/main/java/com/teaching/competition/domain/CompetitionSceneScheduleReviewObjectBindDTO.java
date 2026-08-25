package com.teaching.competition.domain;

import lombok.Data;

import java.util.List;

/**
 * 赛场安排绑定评审对象入参。
 */
@Data
public class CompetitionSceneScheduleReviewObjectBindDTO {
    private List<Long> reviewObjectIds;
}
