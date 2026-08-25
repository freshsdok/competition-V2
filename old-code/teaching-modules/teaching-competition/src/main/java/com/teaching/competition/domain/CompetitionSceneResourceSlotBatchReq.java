package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 批量生成大赛现场设备资源预约时段请求。
 */
@Data
public class CompetitionSceneResourceSlotBatchReq {
    private Long scheduleResourceId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Integer slotDurationMinutes;
    private Integer deviceCapacity;
    private String slotStatus;

    /** 批量生成后写入每个 slot 的允许预约组别。空集合表示不限组别。 */
    private List<CompetitionSceneResourceSlotGroupScope> allowedGroups;
}
