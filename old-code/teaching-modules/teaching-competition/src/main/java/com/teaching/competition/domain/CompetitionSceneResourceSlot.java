package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 大赛现场设备资源预约时段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneResourceSlot extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long slotId;
    private Long scheduleResourceId;
    private Long scheduleId;
    private Long resourceId;
    private Long eventId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    private Integer workstationCount;
    private Integer totalDeviceCount;
    private Integer totalWorkstationCount;
    private Integer deviceCapacity;
    private Integer reservedDeviceCount;
    private Integer remainingDeviceCount;
    private Integer workstationCapacity;
    private Integer reservedWorkstationCount;
    private Integer remainingWorkstationCount;
    private String slotStatus;
    private Long version;
    private Integer deleted;

    /** 管理端提交的允许预约组别。为空集合表示不限组别，null 表示不变更。 */
    private List<CompetitionSceneResourceSlotGroupScope> allowedGroups;
}
