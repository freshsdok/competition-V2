package com.teaching.competition.domain;

import lombok.Data;

import java.util.List;

/**
 * 扫大赛证后按赛场聚合的可执行动作和状态。
 */
@Data
public class CompetitionSceneScheduleActionGroup {
    private Long scheduleId;
    private Long targetCredentialId;
    private String scheduleName;
    private String scheduleTime;
    private String scheduleLocation;
    private String reportStatus;
    private String materialStatus;
    private String waitingStatus;
    private String reportTime;
    private String materialTime;
    private String waitingTime;
    private List<CompetitionSceneScanAction> actions;
}
