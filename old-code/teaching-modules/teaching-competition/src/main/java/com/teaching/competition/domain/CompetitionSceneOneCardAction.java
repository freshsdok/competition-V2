package com.teaching.competition.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 现场一证多权旁路扫码动作。
 */
@Data
public class CompetitionSceneOneCardAction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String actionType;
    private String actionLabel;
    private String actionKind;
    private Boolean enabled;
    private String status;
    private Boolean alreadyDone;
    private String message;
    private Long scheduleId;
    private Long grantId;
    private String scheduleName;
    private String scheduleTime;
    private String scheduleLocation;
}
