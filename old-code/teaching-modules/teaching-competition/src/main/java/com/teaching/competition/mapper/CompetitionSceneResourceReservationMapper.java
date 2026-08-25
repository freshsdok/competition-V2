package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionSceneReservationSubject;
import com.teaching.competition.domain.CompetitionSceneResourceReservation;
import com.teaching.competition.domain.CompetitionSceneResourceReservationQuery;
import com.teaching.competition.domain.CompetitionSceneResourceReservationVO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 大赛现场设备资源预约记录Mapper接口。
 */
public interface CompetitionSceneResourceReservationMapper {

    CompetitionSceneResourceReservationVO selectCompetitionSceneResourceReservationById(Long reservationId);

    CompetitionSceneResourceReservation selectCompetitionSceneResourceReservationEntityById(Long reservationId);

    List<CompetitionSceneResourceReservationVO> selectCompetitionSceneResourceReservationList(
            CompetitionSceneResourceReservationQuery query);

    List<CompetitionSceneResourceReservationVO> selectVisibleCompetitionSceneResourceReservationList(
            @Param("subjects") List<CompetitionSceneReservationSubject> subjects);

    CompetitionSceneResourceReservationVO selectEffectiveReservationBySubject(@Param("scheduleId") Long scheduleId,
                                                                              @Param("subjectType") String subjectType,
                                                                              @Param("subjectCode") String subjectCode,
                                                                              @Param("now") Date now);

    CompetitionSceneResourceReservationVO selectEffectiveReservationByScheduleResourceAndSubject(
            @Param("scheduleResourceId") Long scheduleResourceId,
            @Param("subjectType") String subjectType,
            @Param("subjectCode") String subjectCode,
            @Param("now") Date now);

    CompetitionSceneResourceReservationVO selectEffectiveReservationByActiveKey(
            @Param("activeReservationKey") String activeReservationKey);

    CompetitionSceneResourceReservationVO selectReservationByIdempotencyKey(
            @Param("idempotencyKey") String idempotencyKey);

    int countEffectiveReservationBySlot(@Param("slotId") Long slotId,
                                        @Param("now") Date now);

    int insertCompetitionSceneResourceReservation(CompetitionSceneResourceReservation reservation);

    int cancelCompetitionSceneResourceReservation(@Param("reservationId") Long reservationId,
                                                  @Param("cancelReason") String cancelReason,
                                                  @Param("updateBy") String updateBy);
}
