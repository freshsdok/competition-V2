package com.teaching.competition.domain;

import lombok.Data;

/**
 * 现场扫码核验/确认请求。
 */
@Data
public class CompetitionSceneVerifyReq {
    private String credentialToken;
    private String qrContent;
    private String operationType;
    private Long scheduleId;
    private Long targetCredentialId;
    private String receiverName;
    private String receiverPhone;
    private String receiverIdSuffix;
    private Long subjectUserId;
    private String delegateCredentialToken;
    private String delegateQrContent;
    private String operatorOpenId;
    private String operatorPhone;
    private String scanIp;
    private String deviceInfo;
    private String remark;
}
