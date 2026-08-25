package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionSceneSchedule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事现场赛场安排Mapper接口。
 */
public interface CompetitionSceneScheduleMapper {

    CompetitionSceneSchedule selectCompetitionSceneScheduleById(Long scheduleId);

    List<CompetitionSceneSchedule> selectCompetitionSceneScheduleList(CompetitionSceneSchedule schedule);

    int insertCompetitionSceneSchedule(CompetitionSceneSchedule schedule);

    int updateCompetitionSceneSchedule(CompetitionSceneSchedule schedule);

    int deleteCompetitionSceneScheduleByIds(@Param("scheduleIds") Long[] scheduleIds, @Param("updateBy") String updateBy);
}
