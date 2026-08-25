package com.teaching.competition.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 现场一证多权旁路扫码证件安全摘要。
 */
@Data
public class CompetitionSceneOneCardCredentialSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long credentialId;
    private String credentialNo;
    private String credentialType;
    private String credentialName;
    private String issueChannel;
    private String scopeType;
    private Long scopeRefId;
    private Long competitionSeriesId;
    private String competitionName;
    private String subjectType;
    private String subjectCode;
    private Long userId;
    private String userName;
    private String teamCode;
    private String teamName;
    private String competitionRoleName;
}
