package com.teaching.competition.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 现场一证多权旁路扫码请求。
 */
@Data
public class CompetitionSceneOneCardVerifyReq implements Serializable {
    private static final long serialVersionUID = 1L;

    private String credentialToken;
    private String qrContent;
    private Long credentialId;
    private String actionType;
    private Long currentScheduleId;
    /**
     * 阶段3 pilot测试字段。正式切换前必须改为后端登录态和操作员证件解析。
     */
    private String operatorRole;
    private String actionScene;
    private String deviceId;
    private String deviceInfo;
    private String scanIp;
    private String idempotencyKey;
    private Long delegateCredentialId;
}
