package com.teaching.competition.domain;

import lombok.Data;

/**
 * 用户端资源预约取消参数。
 */
@Data
public class CompetitionSceneResourceReservationCancelReq {
    private Long reservationId;
    private String cancelReason;
}
