package com.teaching.competition.domain;

import lombok.Data;

/**
 * 用户端资源预约提交参数。
 */
@Data
public class CompetitionSceneResourceReservationReq {
    private Long slotId;
    private String idempotencyKey;
}
