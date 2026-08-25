package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionSceneResourceSlotGroupScope;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事现场资源预约时段允许组别Mapper接口。
 */
public interface CompetitionSceneResourceSlotGroupScopeMapper {

    List<CompetitionSceneResourceSlotGroupScope> selectBySlotId(@Param("slotId") Long slotId);

    List<CompetitionSceneResourceSlotGroupScope> selectByScheduleResourceId(
            @Param("scheduleResourceId") Long scheduleResourceId);

    int countEnabledBySlotId(@Param("slotId") Long slotId);

    int countAllowedGroup(@Param("slotId") Long slotId,
                          @Param("groupCode") String groupCode);

    int logicalDeleteBySlotId(@Param("slotId") Long slotId,
                              @Param("updateBy") String updateBy);

    int insertScope(CompetitionSceneResourceSlotGroupScope scope);

    int batchInsertScopes(@Param("scopes") List<CompetitionSceneResourceSlotGroupScope> scopes);
}

