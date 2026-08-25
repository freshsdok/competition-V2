package com.teaching.competition.domain;

import lombok.Data;

/**
 * 大赛现场资源预约主体。
 */
@Data
public class CompetitionSceneReservationSubject {
    private Long competitionSeriesId;
    private Long scheduleId;
    private Long targetId;
    private String subjectType;
    private String subjectCode;
    private String teamCode;
    private String teamName;
    private Long userId;
    private String userName;
    private String roleCode;
    private String groupCode;
    private String groupName;
    private Integer participantCount;
}
