package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionSceneCompetitionDirectIssueReq;
import com.teaching.competition.domain.CompetitionSceneCredentialGenerateReq;
import com.teaching.competition.domain.CompetitionTeacherStudentCredentialQuery;
import com.teaching.competition.domain.CompetitionTeacherStudentCredentialVO;

import java.util.List;

/**
 * 赛事现场证件Service接口。
 */
public interface ICompetitionSceneCredentialService {

    CompetitionSceneCredential selectCompetitionSceneCredentialById(Long credentialId);

    CompetitionSceneCredential selectCompetitionSceneCredentialByToken(String credentialToken);

    List<CompetitionSceneCredential> selectCompetitionSceneCredentialList(CompetitionSceneCredential credential);

    List<CompetitionSceneCredential> selectMyCompetitionSceneCredentialList(Long userId);

    List<CompetitionTeacherStudentCredentialVO> selectTeacherStudentCredentialList(Long teacherUserId,
                                                                                   CompetitionTeacherStudentCredentialQuery query);

    CompetitionTeacherStudentCredentialVO selectTeacherStudentCredentialDetail(Long teacherUserId, Long credentialId);

    boolean checkTeacherCanViewCredential(Long teacherUserId, Long credentialId);

    int generateCompetitionSceneCredential(CompetitionSceneCredentialGenerateReq req);

    CompetitionSceneCredential competitionDirectIssue(CompetitionSceneCompetitionDirectIssueReq req);

    int updateCompetitionSceneCredential(CompetitionSceneCredential credential);

    int deleteCompetitionSceneCredentialByIds(Long[] credentialIds);
}
