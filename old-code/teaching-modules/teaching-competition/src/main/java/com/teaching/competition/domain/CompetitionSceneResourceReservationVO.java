package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 现场资源预约记录展示对象。
 */
@Data
public class CompetitionSceneResourceReservationVO {
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
    private String createBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private String updateBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String scheduleName;
    private String reservationSourceScheduleName;
    private String competitionName;
    private String resourceName;
    private String resourceType;
    private String brandModel;
    private String deploymentLocation;
    private Integer workstationsPerDevice;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date slotStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date slotEndTime;
    private String slotStatus;
    private Boolean expired;
}
