package com.teaching.competition.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 现场资源预约记录查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneResourceReservationQuery extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long reservationId;
    private Long slotId;
    private Long scheduleResourceId;
    private Long scheduleId;
    private Long resourceId;
    private Long eventId;
    private String subjectType;
    private String subjectCode;
    private String teamCode;
    private Long userId;
    private Long operatorUserId;
    private String reservationStatus;
}
