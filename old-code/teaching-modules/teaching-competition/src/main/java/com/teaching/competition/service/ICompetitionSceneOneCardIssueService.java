package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneOneCardIssueResult;

/**
 * 现场一证多权旁路发证Service接口。
 */
public interface ICompetitionSceneOneCardIssueService {

    CompetitionSceneOneCardIssueResult issueOneCardByTarget(Long targetId);

    CompetitionSceneOneCardIssueResult issueOneCardByScheduleTarget(Long scheduleId, Long targetId);
}
