package com.teaching.competition.domain;

import lombok.Data;

/**
 * 现场扫码后可执行动作。
 */
@Data
public class CompetitionSceneScanAction {
    private String actionType;
    private String actionLabel;
    private String actionKind;
    private Boolean enabled;
    private String status;
    private String message;
    private Long scheduleId;
    private Long targetCredentialId;
    private String scheduleName;
    private String scheduleTime;
    private String scheduleLocation;
}
