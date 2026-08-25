package com.teaching.competition.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 现场一证多权旁路扫码结果。
 */
@Data
public class CompetitionSceneOneCardVerifyResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private String operationResult;
    private String resultMessage;
    private Boolean duplicate;
    private Boolean alreadyDone;
    private CompetitionSceneOneCardCredentialSummary credential;
    private CompetitionSceneOneCardCredentialSummary delegateCredential;
    private String operatorRole;
    private String operatorRoleLabel;
    private String targetRole;
    private String targetRoleLabel;
    private List<CompetitionSceneOneCardAction> allowedActions;
    private List<CompetitionSceneOneCardAction> competitionActions;
    private List<CompetitionSceneOneCardScheduleActionGroup> scheduleActionGroups;
    private String matrixMessage;
    private CompetitionSceneSubjectOperationState reportState;
    private CompetitionSceneSubjectOperationState materialState;
    private CompetitionSceneSubjectOperationState waitingState;
}
