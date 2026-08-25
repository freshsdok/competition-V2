package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 赛事现场主体操作状态事实源。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneSubjectOperationState extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long stateId;
    private Long competitionSeriesId;
    private String scopeType;
    private Long scopeRefId;
    private String subjectType;
    private String subjectCode;
    private String operationType;
    private String operationStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operationTime;
    private Long credentialId;
    private Long operatorUserId;
    private String operatorName;
    private Long delegateUserId;
    private String delegateName;
    private Long delegateCredentialId;
    private String delegateRelation;
    private Long lastLogId;
    private Integer deleted;
}

