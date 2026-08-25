package com.teaching.competition.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 现场一证多权旁路扫码赛场授权分组。
 */
@Data
public class CompetitionSceneOneCardScheduleActionGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long scheduleId;
    private Long grantId;
    private String scheduleName;
    private String scheduleTime;
    private String scheduleLocation;
    private String waitingStatus;
    private String waitingTime;
    private List<CompetitionSceneOneCardAction> actions;
}
