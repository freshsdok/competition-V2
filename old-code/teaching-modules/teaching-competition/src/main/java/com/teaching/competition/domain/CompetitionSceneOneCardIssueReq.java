package com.teaching.competition.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 现场一证多权旁路发证请求。
 */
@Data
public class CompetitionSceneOneCardIssueReq implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long scheduleId;
    private Long targetId;
}
