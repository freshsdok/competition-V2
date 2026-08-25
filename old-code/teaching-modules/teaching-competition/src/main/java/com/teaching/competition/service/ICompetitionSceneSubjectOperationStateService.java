package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionSceneSubjectOperationState;
import com.teaching.competition.domain.CompetitionSceneSubjectOperationStateQuery;

import java.util.List;

/**
 * 赛事现场主体操作状态Service接口。
 */
public interface ICompetitionSceneSubjectOperationStateService {

    CompetitionSceneSubjectOperationState selectDoneOperationState(CompetitionSceneSubjectOperationStateQuery query);

    CompetitionSceneSubjectOperationState insertDoneOperationStateIfAbsent(CompetitionSceneSubjectOperationState state);

    int cancelDoneOperationState(CompetitionSceneSubjectOperationStateQuery query);

    void updateLastLogId(Long stateId, Long lastLogId);

    void fillCredentialOperationStates(List<CompetitionSceneCredential> credentials);
}
