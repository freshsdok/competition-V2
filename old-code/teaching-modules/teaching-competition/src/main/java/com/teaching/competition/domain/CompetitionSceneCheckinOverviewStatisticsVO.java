package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 签到概览统计。
 */
@Data
public class CompetitionSceneCheckinOverviewStatisticsVO {
    private Long totalPersonCount = 0L;
    private Long signedPersonCount = 0L;
    private Long unsignedPersonCount = 0L;
    private BigDecimal checkinRate = BigDecimal.ZERO;
    private Long scheduleCount = 0L;
    private Long warningScheduleCount = 0L;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastCheckinTime;

    private List<CompetitionSceneCheckinRankItemVO> lowRateRank;
    private List<CompetitionSceneCheckinTimeGroupVO> startTimeGroups;
}
