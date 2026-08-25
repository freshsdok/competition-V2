package com.teaching.competition.domain;

import lombok.Data;

/**
 * 修改现场设备资源状态请求。
 */
@Data
public class CompetitionSceneResourceStatusReq {
    private Long resourceId;
    private String resourceStatus;
}
