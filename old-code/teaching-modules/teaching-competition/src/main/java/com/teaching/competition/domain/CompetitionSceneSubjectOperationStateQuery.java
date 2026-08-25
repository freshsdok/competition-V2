package com.teaching.competition.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 主体操作状态查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneSubjectOperationStateQuery extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long competitionSeriesId;
    private String scopeType;
    private Long scopeRefId;
    private String subjectType;
    private String subjectCode;
    private String operationType;
    private String operationStatus;
    private Long credentialId;
    private Integer deleted;
}

