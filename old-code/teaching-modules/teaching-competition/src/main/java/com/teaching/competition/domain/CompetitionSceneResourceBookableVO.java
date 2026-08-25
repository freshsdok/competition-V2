package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 用户端可预约资源展示对象。
 */
@Data
public class CompetitionSceneResourceBookableVO {
    private Long scheduleResourceId;
    private Long scheduleId;
    private Long resourceId;
    private Long eventId;
    private Long competitionSeriesId;
    private Long userSourceScheduleId;
    private String scheduleName;
    private String competitionName;
    private String resourceCode;
    private String resourceName;
    private String resourceType;
    private String brandModel;
    private String deploymentLocation;
    private Integer deployedDeviceCount;
    private Integer workstationsPerDevice;
    private Integer totalWorkstations;
    private Integer slotDurationMinutes;
    private Boolean sharedOccupancy;
    private Boolean needOpsConfirm;
    private String opsContactName;
    private String opsContactPhone;
    private String bookingStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bookingOpenTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date bookingCloseTime;
    private String safetyNotice;
    private String attentionNotes;
    private String parameterJson;
    private String usageInstructions;
    private String imageUrls;
    private Integer remainingDeviceCount;
    private Integer remainingWorkstationCount;
    private Long nextSlotId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextEndTime;
    private String subjectType;
    private String subjectCode;
    private String subjectName;
    private String groupCode;
    private String groupName;
    private Integer participantCount;
    private Integer suggestedDeviceCount;
    private Integer coveredWorkstationCount;
    private Boolean hasExistingReservation;
    private CompetitionSceneResourceReservationVO existingReservation;
}
