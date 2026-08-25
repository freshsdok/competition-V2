package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 大赛现场赛场资源布置。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneScheduleResource extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long scheduleResourceId;
    private Long scheduleId;
    private Long resourceId;
    private Long eventId;
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
    private String safetyNoticeOverride;
    private String attentionNotesOverride;
    private String usageInstructionsOverride;
    private String adminRemark;
    private Integer deleted;
}
