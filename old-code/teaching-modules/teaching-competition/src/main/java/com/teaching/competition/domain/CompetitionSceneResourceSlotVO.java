package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 大赛现场设备资源预约时段展示对象。
 */
@Data
public class CompetitionSceneResourceSlotVO {
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
    private String createBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private String updateBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private String resourceName;
    private String resourceType;
    private String deploymentLocation;

    /** 每台设备工位数。 */
    private Integer workstationsPerDevice;

    /** 当前时段允许预约的组别名称，未配置时为空。 */
    private List<String> allowedGroupNames;

    /** 不可预约原因，当前用户端第一阶段主要用于兼容展示。 */
    private String disabledReason;
}
