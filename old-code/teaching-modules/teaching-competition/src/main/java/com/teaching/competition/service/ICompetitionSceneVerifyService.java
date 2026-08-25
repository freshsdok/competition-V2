package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneOperationLog;
import com.teaching.competition.domain.CompetitionSceneVerifyReq;
import com.teaching.competition.domain.CompetitionSceneVerifyResult;

import java.util.List;

/**
 * 赛事现场扫码核验Service接口。
 */
public interface ICompetitionSceneVerifyService {

    CompetitionSceneVerifyResult scan(CompetitionSceneVerifyReq req);

    CompetitionSceneVerifyResult confirm(CompetitionSceneVerifyReq req);

    List<CompetitionSceneOperationLog> selectOperationLogList(CompetitionSceneOperationLog log);
}
