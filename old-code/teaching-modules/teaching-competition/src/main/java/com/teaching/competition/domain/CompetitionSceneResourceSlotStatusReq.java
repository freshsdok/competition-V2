package com.teaching.competition.domain;

import lombok.Data;

/**
 * 修改大赛现场设备资源预约时段状态请求。
 */
@Data
public class CompetitionSceneResourceSlotStatusReq {
    private Long slotId;
    private String slotStatus;
}
