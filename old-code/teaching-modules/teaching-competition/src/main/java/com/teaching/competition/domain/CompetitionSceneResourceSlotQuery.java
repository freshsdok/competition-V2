package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 大赛现场设备资源预约时段查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneResourceSlotQuery extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long scheduleResourceId;
    private Long scheduleId;
    private Long resourceId;
    private Long eventId;
    private String slotStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
}
