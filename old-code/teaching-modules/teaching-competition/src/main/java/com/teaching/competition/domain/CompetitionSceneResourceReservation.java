package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 大赛现场设备资源预约记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneResourceReservation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long reservationId;
    private Long slotId;
    private Long scheduleResourceId;
    private Long scheduleId;
    private Long resourceId;
    private Long eventId;
    private Long competitionSeriesId;
    private Long reservationSourceScheduleId;
    private String subjectType;
    private String subjectCode;
    private String teamCode;
    private Long userId;
    private Long operatorUserId;
    private String operatorName;
    private String groupCode;
    private String groupName;
    private Integer occupyPeopleCount;
    private Integer reservedDeviceCount;
    private Integer reservedWorkstationCount;
    private Integer coveredWorkstationCount;
    private Boolean sharedOccupancySnapshot;
    private Integer workstationCountSnapshot;
    private String activeReservationKey;
    private String reservationStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cancelTime;
    private String cancelReason;
    private String checkStatus;
    private Long checkUserId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkTime;
    private String idempotencyKey;
    private Integer deleted;
}
