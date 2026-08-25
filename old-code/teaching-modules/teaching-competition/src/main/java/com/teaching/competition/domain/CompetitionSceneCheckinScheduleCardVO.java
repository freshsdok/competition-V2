package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 签到概览赛场卡片。
 */
@Data
public class CompetitionSceneCheckinScheduleCardVO {
    private Long scheduleId;
    private String scheduleName;
    private Long competitionSeriesId;
    private String competitionName;
    private String competitionTrackId;
    private String competitionTrackName;
    private String secondLevelCode;
    private String secondLevelName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date competitionStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date competitionEndTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkinStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkinEndTime;

    private String checkinLocation;
    private String competitionLocation;
    private Long totalPersonCount = 0L;
    private Long signedPersonCount = 0L;
    private Long unsignedPersonCount = 0L;
    private BigDecimal checkinRate = BigDecimal.ZERO;
    private Long memberTotalCount = 0L;
    private Long memberSignedCount = 0L;
    private Long teamTotalCount = 0L;
    private Long completedTeamCount = 0L;
    private Long partialTeamCount = 0L;
    private Long unsignedTeamCount = 0L;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastCheckinTime;

    private String scheduleStatus;
    private String checkinStatus;
    private String warningLevel;
    private String warningMessage;
}
