package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 签到概览查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneCheckinOverviewQuery extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long competitionSeriesId;
    private Long competitionId;
    private String competitionTrackId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTimeBegin;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTimeEnd;

    private String checkinLocation;
    private String checkinStatus;
    private String warningLevel;
    private Long scheduleId;
    private String teamCode;
    private String keyword;
    private String role;
}
