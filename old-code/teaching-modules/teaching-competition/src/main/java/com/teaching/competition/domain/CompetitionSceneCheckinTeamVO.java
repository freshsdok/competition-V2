package com.teaching.competition.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 赛场团队签到统计。
 */
@Data
public class CompetitionSceneCheckinTeamVO {
    private String teamCode;
    private String teamName;
    private Long totalPersonCount = 0L;
    private Long signedPersonCount = 0L;
    private Long unsignedPersonCount = 0L;
    private BigDecimal checkinRate = BigDecimal.ZERO;
    private String teamStatus;
}
