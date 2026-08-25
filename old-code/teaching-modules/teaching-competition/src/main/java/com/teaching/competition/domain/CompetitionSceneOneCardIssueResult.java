package com.teaching.competition.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 现场一证多权旁路发证结果。
 */
@Data
public class CompetitionSceneOneCardIssueResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long credentialId;
    private Long grantId;
    private Boolean reusedCredential;
    private Boolean reusedGrant;
    private Boolean alreadyGranted;
    private Long competitionSeriesId;
    private String subjectType;
    private String subjectCode;
    private String credentialType;
    private String roleCode;
    private Long scheduleId;
    private Long targetId;
}
