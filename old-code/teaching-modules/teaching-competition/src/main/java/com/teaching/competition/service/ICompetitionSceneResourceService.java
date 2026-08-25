package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneResource;
import com.teaching.competition.domain.CompetitionSceneResourceQuery;
import com.teaching.competition.domain.CompetitionSceneResourceStatusReq;
import com.teaching.competition.domain.CompetitionSceneResourceVO;

import java.util.List;

/**
 * 大赛现场设备资源台账Service接口。
 */
public interface ICompetitionSceneResourceService {

    CompetitionSceneResourceVO selectCompetitionSceneResourceById(Long resourceId);

    List<CompetitionSceneResourceVO> selectCompetitionSceneResourceList(CompetitionSceneResourceQuery query);

    int insertCompetitionSceneResource(CompetitionSceneResource resource);

    int updateCompetitionSceneResource(CompetitionSceneResource resource);

    int deleteCompetitionSceneResourceByIds(Long[] resourceIds);

    int changeCompetitionSceneResourceStatus(CompetitionSceneResourceStatusReq req);
}
