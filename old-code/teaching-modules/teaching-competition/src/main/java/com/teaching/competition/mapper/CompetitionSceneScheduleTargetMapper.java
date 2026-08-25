package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionSceneScheduleTarget;
import com.teaching.competition.domain.CompetitionSceneResourceSlotGroupScope;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事现场赛场安排对象Mapper接口。
 */
public interface CompetitionSceneScheduleTargetMapper {

    CompetitionSceneScheduleTarget selectCompetitionSceneScheduleTargetById(Long targetId);

    List<CompetitionSceneScheduleTarget> selectCompetitionSceneScheduleTargetList(CompetitionSceneScheduleTarget target);

    List<CompetitionSceneScheduleTarget> selectCompetitionSceneScheduleTargetByScheduleId(Long scheduleId);

    CompetitionSceneScheduleTarget selectCompetitionSceneScheduleTargetByScheduleIdAndTargetKey(@Param("scheduleId") Long scheduleId,
                                                                                                @Param("targetKey") String targetKey);

    List<CompetitionSceneScheduleTarget> selectCompetitionSceneScheduleTargetByIds(@Param("targetIds") List<Long> targetIds);

    List<CompetitionSceneResourceSlotGroupScope> selectDistinctGroupOptions(@Param("scheduleId") Long scheduleId,
                                                                            @Param("competitionSeriesId") Long competitionSeriesId);

    int insertCompetitionSceneScheduleTarget(CompetitionSceneScheduleTarget target);

    int restoreCompetitionSceneScheduleTarget(CompetitionSceneScheduleTarget target);

    int updateCompetitionSceneScheduleTarget(CompetitionSceneScheduleTarget target);

    int updateCompetitionSceneScheduleTargetSequence(@Param("scheduleId") Long scheduleId,
                                                     @Param("targetId") Long targetId,
                                                     @Param("sequenceNo") Integer sequenceNo,
                                                     @Param("updateBy") String updateBy);

    int deleteCompetitionSceneScheduleTargetByIds(@Param("targetIds") Long[] targetIds, @Param("updateBy") String updateBy);
}
