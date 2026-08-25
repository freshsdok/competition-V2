package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionSceneCredentialScopeGrant;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事现场证件作用域授权Mapper接口。
 */
public interface CompetitionSceneCredentialScopeGrantMapper {

    CompetitionSceneCredentialScopeGrant selectGrantById(Long grantId);

    List<CompetitionSceneCredentialScopeGrant> selectActiveGrantsByCredential(@Param("credentialId") Long credentialId);

    List<CompetitionSceneCredentialScopeGrant> selectActiveScheduleGrants(@Param("credentialId") Long credentialId,
                                                                          @Param("scheduleId") Long scheduleId);

    CompetitionSceneCredentialScopeGrant selectActiveScheduleGrant(@Param("credentialId") Long credentialId,
                                                                   @Param("scheduleId") Long scheduleId,
                                                                   @Param("sourceTargetId") Long sourceTargetId);

    CompetitionSceneCredentialScopeGrant selectActiveScheduleGrantForUpdate(@Param("credentialId") Long credentialId,
                                                                            @Param("scheduleId") Long scheduleId,
                                                                            @Param("sourceTargetId") Long sourceTargetId);

    int insertGrant(CompetitionSceneCredentialScopeGrant grant);

    int updateGrantMutableFields(CompetitionSceneCredentialScopeGrant grant);

    int revokeGrant(@Param("grantId") Long grantId,
                    @Param("updateBy") String updateBy);

    int revokeGrantsByTarget(@Param("sourceScheduleId") Long sourceScheduleId,
                             @Param("sourceTargetId") Long sourceTargetId,
                             @Param("updateBy") String updateBy);
}
