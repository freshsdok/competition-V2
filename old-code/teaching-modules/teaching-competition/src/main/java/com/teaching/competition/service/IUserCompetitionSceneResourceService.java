package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneResourceBookableQuery;
import com.teaching.competition.domain.CompetitionSceneResourceBookableVO;
import com.teaching.competition.domain.CompetitionSceneResourceReservationCancelReq;
import com.teaching.competition.domain.CompetitionSceneResourceReservationReq;
import com.teaching.competition.domain.CompetitionSceneResourceReservationVO;
import com.teaching.competition.domain.CompetitionSceneResourceSlotVO;

import java.util.List;

/**
 * 用户端大赛现场设备资源预约服务。
 */
public interface IUserCompetitionSceneResourceService {

    List<CompetitionSceneResourceBookableVO> selectBookableResourceList(Long userId,
                                                                        CompetitionSceneResourceBookableQuery query);

    CompetitionSceneResourceBookableVO selectBookableResourceById(Long userId, Long scheduleResourceId);

    List<CompetitionSceneResourceSlotVO> selectBookableSlotList(Long userId, Long scheduleResourceId);

    CompetitionSceneResourceReservationVO submitReservation(Long userId, CompetitionSceneResourceReservationReq req);

    List<CompetitionSceneResourceReservationVO> selectMyReservationList(Long userId);

    CompetitionSceneResourceReservationVO cancelReservation(Long userId, CompetitionSceneResourceReservationCancelReq req);
}
