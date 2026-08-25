package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 赛事现场扫码操作流水。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneOperationLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long credentialId;
    private Long scheduleId;
    private Long targetId;
    private Long competitionSeriesId;
    private String credentialNo;
    private String credentialToken;
    private String operationType;
    private String operationStage;
    private String operationResult;
    private String resultMessage;
    private String applyCheckResult;
    private String scheduleCheckResult;
    private String identityCheckResult;
    private String teamCode;
    private String teamName;
    private Long memberId;
    private Long userId;
    private String userName;
    private String idCardSuffix;
    private String competitionTrackId;
    private String competitionTrackName;
    private String secondLevelCode;
    private String secondLevelName;
    private String receiverName;
    private String receiverPhone;
    private String receiverIdSuffix;
    private Long operatorUserId;
    private String operatorName;
    private String operatorPhone;
    private String operatorOpenId;
    private String scanIp;
    private String deviceInfo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operationTime;
    private String requestPayload;
    private String responsePayload;
    private String applySnapshotJson;
    private Long version;
    private String delFlag;
}
