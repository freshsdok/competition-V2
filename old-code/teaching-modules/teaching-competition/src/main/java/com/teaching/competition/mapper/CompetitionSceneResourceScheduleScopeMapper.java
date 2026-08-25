package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionSceneResourceScheduleScope;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事现场资源允许预约赛场范围Mapper接口。
 */
public interface CompetitionSceneResourceScheduleScopeMapper {

    List<CompetitionSceneResourceScheduleScope> selectByScheduleResourceId(
            @Param("scheduleResourceId") Long scheduleResourceId);

    List<Long> selectAllowedScheduleIds(@Param("scheduleResourceId") Long scheduleResourceId);

    CompetitionSceneResourceScheduleScope selectEnabledScope(@Param("scheduleResourceId") Long scheduleResourceId,
                                                             @Param("allowedScheduleId") Long allowedScheduleId);

    int countEnabledScope(@Param("scheduleResourceId") Long scheduleResourceId,
                          @Param("allowedScheduleId") Long allowedScheduleId);

    int insertScope(CompetitionSceneResourceScheduleScope scope);

    int logicalDeleteManualBind(@Param("scheduleResourceId") Long scheduleResourceId,
                                @Param("allowedScheduleId") Long allowedScheduleId,
                                @Param("updateBy") String updateBy);
}

