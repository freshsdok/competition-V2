package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneCredentialScopeGrant;
import com.teaching.competition.domain.CompetitionSceneSchedule;

import java.util.List;

/**
 * 赛事现场证件作用域授权Service接口。
 */
public interface ICompetitionSceneCredentialScopeGrantService {

    CompetitionSceneCredentialScopeGrant insertGrant(CompetitionSceneCredentialScopeGrant grant);

    List<CompetitionSceneCredentialScopeGrant> findActiveGrantsByCredential(Long credentialId);

    CompetitionSceneCredentialScopeGrant findActiveScheduleGrant(Long credentialId,
                                                                 Long scheduleId,
                                                                 Long sourceTargetId);

    CompetitionSceneCredentialScopeGrant ensureScheduleGrant(CompetitionSceneCredentialScopeGrant grant);

    int revokeGrant(Long grantId);

    int revokeGrantsByTarget(Long sourceScheduleId, Long sourceTargetId);

    boolean hasAbility(CompetitionSceneCredentialScopeGrant grant, String abilityCode);

    boolean checkScheduleAbility(Long credentialId, Long scheduleId, String abilityCode);

    String buildDefaultScheduleGrantAbility(String credentialType);

    String buildDefaultOperationWindowJson(CompetitionSceneSchedule schedule);
}
