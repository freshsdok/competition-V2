package com.teaching.competition.domain;

import lombok.Data;

/**
 * 修改赛场资源布置预约发布状态请求。
 */
@Data
public class CompetitionSceneScheduleResourceStatusReq {
    private Long scheduleResourceId;
    private String bookingStatus;
}
