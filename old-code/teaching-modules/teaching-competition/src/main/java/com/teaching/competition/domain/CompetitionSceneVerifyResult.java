package com.teaching.competition.domain;

import lombok.Data;

import java.util.List;

/**
 * 现场扫码核验结果。
 */
@Data
public class CompetitionSceneVerifyResult {
    private String operationResult;
    private String resultMessage;
    private String applyCheckResult;
    private String scheduleCheckResult;
    private String identityCheckResult;
    private Boolean duplicate;
    private CompetitionSceneCredential credential;
    private String operatorRole;
    private String operatorRoleLabel;
    private String targetRole;
    private String targetRoleLabel;
    private List<CompetitionSceneScanAction> availableActions;
    private List<CompetitionSceneScanAction> competitionActions;
    private List<CompetitionSceneScheduleActionGroup> scheduleActionGroups;
    private Boolean reviewEntryAvailable;
    private String reviewEntryMessage;
    private String matrixMessage;
    private CompetitionSceneSubjectOperationState reportState;
    private CompetitionSceneSubjectOperationState materialState;
    private CompetitionSceneSubjectOperationState waitingState;
    private CompetitionSceneCredential delegateCredential;
}
