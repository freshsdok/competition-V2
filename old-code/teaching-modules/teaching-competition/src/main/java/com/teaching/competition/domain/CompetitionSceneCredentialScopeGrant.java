package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 赛事现场证件作用域授权。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneCredentialScopeGrant extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long grantId;
    private Long credentialId;
    private Long competitionSeriesId;
    private String scopeType;
    private Long scopeRefId;
    private String activeGrantKey;
    private String sourceType;
    private Long sourceScheduleId;
    private Long sourceTargetId;
    private String credentialType;
    private String roleCode;
    private String subjectType;
    private String subjectCode;
    private String abilityJson;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validFrom;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validTo;
    private String operationWindowJson;
    private String grantStatus;
    private String grantSnapshotJson;
    private Integer deleted;
}
