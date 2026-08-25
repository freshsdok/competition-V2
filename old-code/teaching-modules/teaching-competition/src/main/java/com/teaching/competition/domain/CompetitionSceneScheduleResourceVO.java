package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 大赛现场赛场资源布置展示对象。
 */
@Data
public class CompetitionSceneScheduleResourceVO {
    private Long scheduleResourceId;
    private Long scheduleId;
    private Long resourceId;
    private Long eventId;
    private String deploymentLocation;
    private Integer deployedDeviceCount;

    /** 每台设备工位数。 */
    private Integer workstationsPerDevice;

    /** 部署总工位数，由部署设备数乘以每台设备工位数得到。 */
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
    private String createBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private String updateBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String scheduleName;
    private String competitionName;
    private String resourceCode;
    private String resourceName;
    private String resourceType;
    private String resourceStatus;
    private String brandModel;
}
