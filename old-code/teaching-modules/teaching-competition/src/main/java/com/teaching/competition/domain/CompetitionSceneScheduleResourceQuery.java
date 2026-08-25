package com.teaching.competition.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 大赛现场赛场资源布置查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneScheduleResourceQuery extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long scheduleId;
    private Long scheduleResourceId;
    private Long resourceId;
    private Long eventId;
    private String resourceName;
    private String resourceType;
    private String bookingStatus;
}
