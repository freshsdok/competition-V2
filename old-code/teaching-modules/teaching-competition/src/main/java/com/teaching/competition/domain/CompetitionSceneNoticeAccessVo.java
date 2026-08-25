package com.teaching.competition.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前用户的通知身份或现场访问范围。
 */
@Data
public class CompetitionSceneNoticeAccessVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String accessType;
    private Long competitionSeriesId;
    private Long scheduleId;
    private Long targetId;
    private Long memberId;
}

