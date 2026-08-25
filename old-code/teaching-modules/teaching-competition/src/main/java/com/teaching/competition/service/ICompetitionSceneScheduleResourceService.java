package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneScheduleResource;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceQuery;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceStatusReq;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceVO;

import java.util.List;

/**
 * 大赛现场赛场资源布置Service接口。
 */
public interface ICompetitionSceneScheduleResourceService {

    CompetitionSceneScheduleResourceVO selectCompetitionSceneScheduleResourceById(Long scheduleResourceId);

    List<CompetitionSceneScheduleResourceVO> selectCompetitionSceneScheduleResourceList(CompetitionSceneScheduleResourceQuery query);

    int insertCompetitionSceneScheduleResource(CompetitionSceneScheduleResource scheduleResource);

    int updateCompetitionSceneScheduleResource(CompetitionSceneScheduleResource scheduleResource);

    int deleteCompetitionSceneScheduleResourceByIds(Long[] scheduleResourceIds);

    int changeCompetitionSceneScheduleResourceBookingStatus(CompetitionSceneScheduleResourceStatusReq req);
}
