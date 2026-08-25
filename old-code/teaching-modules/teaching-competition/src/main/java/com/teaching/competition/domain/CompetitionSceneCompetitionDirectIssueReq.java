package com.teaching.competition.domain;

import lombok.Data;

/**
 * 大赛级现场证件直接发证请求。
 */
@Data
public class CompetitionSceneCompetitionDirectIssueReq {
    private Long competitionSeriesId;
    private String credentialType;
    private String credentialName;
    private String subjectType;
    private String subjectCode;
    private String subjectName;
    private String remark;
}
