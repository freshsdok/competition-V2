package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 赛事现场赛场安排。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneSchedule extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long scheduleId;
    private String scheduleName;
    private Long competitionSeriesId;
    private String competitionName;
    private String competitionStageId;
    private String competitionStageName;
    private String competitionTrackId;
    private String competitionTrackName;
    private String secondLevelCode;
    private String secondLevelName;
    private String credentialType;
    private String configDimension;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reportStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reportEndTime;
    private String reportLocation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date contestStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date contestEndTime;
    private String contestLocation;
    private String contestRoom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date waitingStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date waitingEndTime;
    private String waitingLocation;
    private String waitingGroupCode;
    private String waitingGroupName;

    private String materialLocation;
    private String notice;
    private String status;
    private Long version;
    private String delFlag;
}
