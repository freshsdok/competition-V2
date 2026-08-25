package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionTeacherStudentCredentialQuery;
import com.teaching.competition.domain.CompetitionTeacherStudentCredentialVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事现场证件Mapper接口。
 */
public interface CompetitionSceneCredentialMapper {

    CompetitionSceneCredential selectCompetitionSceneCredentialById(Long credentialId);

    CompetitionSceneCredential selectCompetitionSceneCredentialByToken(String credentialToken);

    CompetitionSceneCredential selectCompetitionSceneCredentialByNo(@Param("credentialNo") String credentialNo);

    CompetitionSceneCredential selectCompetitionSceneCredentialByTargetId(Long targetId);

    CompetitionSceneCredential selectEffectiveCompetitionScopeCredential(@Param("competitionSeriesId") Long competitionSeriesId,
                                                                         @Param("subjectType") String subjectType,
                                                                         @Param("subjectCode") String subjectCode);

    CompetitionSceneCredential selectEffectiveCompetitionScopeCredentialStrict(@Param("competitionSeriesId") Long competitionSeriesId,
                                                                               @Param("subjectType") String subjectType,
                                                                               @Param("subjectCode") String subjectCode,
                                                                               @Param("credentialType") String credentialType);

    CompetitionSceneCredential selectEffectiveCompetitionScopeCredentialByUserId(@Param("competitionSeriesId") Long competitionSeriesId,
                                                                                 @Param("userId") Long userId);

    int countCompetitionSceneCredentialByScheduleId(@Param("scheduleId") Long scheduleId);

    int selectMaxCredentialNoSequence(@Param("scheduleId") Long scheduleId,
                                      @Param("credentialNoPrefix") String credentialNoPrefix);

    List<CompetitionSceneCredential> selectCompetitionSceneCredentialList(CompetitionSceneCredential credential);

    List<CompetitionTeacherStudentCredentialVO> selectTeacherStudentCredentialList(@Param("teacherUserId") Long teacherUserId,
                                                                                   @Param("query") CompetitionTeacherStudentCredentialQuery query);

    CompetitionTeacherStudentCredentialVO selectTeacherStudentCredentialDetail(@Param("teacherUserId") Long teacherUserId,
                                                                               @Param("credentialId") Long credentialId);

    int insertCompetitionSceneCredential(CompetitionSceneCredential credential);

    int updateCompetitionSceneCredential(CompetitionSceneCredential credential);

    int resetCompetitionSceneCredentialOperationStatus(@Param("credentialId") Long credentialId,
                                                       @Param("operationType") String operationType,
                                                       @Param("updateBy") String updateBy);

    int deleteCompetitionSceneCredentialByIds(@Param("credentialIds") Long[] credentialIds,
                                              @Param("updateBy") String updateBy);

    int deleteCompetitionSceneCredentialByTargetIds(@Param("targetIds") Long[] targetIds,
                                                    @Param("updateBy") String updateBy);

    int revokeCompetitionSceneCredentialByTargetId(@Param("targetId") Long targetId,
                                                   @Param("updateBy") String updateBy);

    int updateCompetitionSceneCredentialVerifyInfo(CompetitionSceneCredential credential);
}
