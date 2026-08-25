package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneOneCardVerifyReq;
import com.teaching.competition.domain.CompetitionSceneOneCardVerifyResult;

/**
 * 现场一证多权旁路扫码Service接口。
 */
public interface ICompetitionSceneOneCardVerifyService {

    CompetitionSceneOneCardVerifyResult scan(CompetitionSceneOneCardVerifyReq req);

    CompetitionSceneOneCardVerifyResult confirm(CompetitionSceneOneCardVerifyReq req);
}
