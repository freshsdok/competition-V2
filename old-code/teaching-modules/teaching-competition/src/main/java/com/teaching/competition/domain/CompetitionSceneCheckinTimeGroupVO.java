package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 按比赛开始时间分组统计。
 */
@Data
public class CompetitionSceneCheckinTimeGroupVO {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date competitionStartTime;
    private Long scheduleCount = 0L;
    private Long totalPersonCount = 0L;
    private Long signedPersonCount = 0L;
    private Long unsignedPersonCount = 0L;
    private BigDecimal checkinRate = BigDecimal.ZERO;
}
