package com.teaching.competition.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 赛事现场资源允许预约赛场范围批量绑定请求。
 */
@Data
public class CompetitionSceneResourceScheduleScopeBatchReq implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long scheduleResourceId;
    private Long resourceId;
    private List<Long> allowedScheduleIds;
}
