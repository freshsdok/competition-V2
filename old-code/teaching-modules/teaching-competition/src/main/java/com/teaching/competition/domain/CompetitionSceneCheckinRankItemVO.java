package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 低签到率赛场排行。
 */
@Data
public class CompetitionSceneCheckinRankItemVO {
    private Long scheduleId;
    private String scheduleName;
    private Long totalPersonCount = 0L;
    private Long signedPersonCount = 0L;
    private Long unsignedPersonCount = 0L;
    private BigDecimal checkinRate = BigDecimal.ZERO;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date competitionStartTime;
}
