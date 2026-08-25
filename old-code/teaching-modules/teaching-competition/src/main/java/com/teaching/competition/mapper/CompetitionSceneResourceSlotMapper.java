package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionSceneResourceSlot;
import com.teaching.competition.domain.CompetitionSceneResourceSlotQuery;
import com.teaching.competition.domain.CompetitionSceneResourceSlotVO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 大赛现场设备资源预约时段Mapper接口。
 */
public interface CompetitionSceneResourceSlotMapper {

    CompetitionSceneResourceSlotVO selectCompetitionSceneResourceSlotById(Long slotId);

    CompetitionSceneResourceSlot selectCompetitionSceneResourceSlotEntityById(Long slotId);

    List<CompetitionSceneResourceSlot> selectCompetitionSceneResourceSlotEntitiesByIds(@Param("slotIds") Long[] slotIds);

    List<CompetitionSceneResourceSlotVO> selectCompetitionSceneResourceSlotList(CompetitionSceneResourceSlotQuery query);

    int countOverlappingSlots(@Param("scheduleResourceId") Long scheduleResourceId,
                              @Param("startTime") Date startTime,
                              @Param("endTime") Date endTime,
                              @Param("excludeSlotId") Long excludeSlotId);

    int insertCompetitionSceneResourceSlot(CompetitionSceneResourceSlot slot);

    int batchInsertCompetitionSceneResourceSlot(@Param("slots") List<CompetitionSceneResourceSlot> slots);

    int updateCompetitionSceneResourceSlot(CompetitionSceneResourceSlot slot);

    int deleteCompetitionSceneResourceSlotByIds(@Param("slotIds") Long[] slotIds,
                                                @Param("updateBy") String updateBy);

    int updateCompetitionSceneResourceSlotStatus(@Param("slotId") Long slotId,
                                                 @Param("slotStatus") String slotStatus,
                                                 @Param("updateBy") String updateBy);

    int updateCompetitionSceneResourceSlotStatusIfCurrent(@Param("slotId") Long slotId,
                                                          @Param("fromStatus") String fromStatus,
                                                          @Param("slotStatus") String slotStatus,
                                                          @Param("updateBy") String updateBy);

    int reserveSharedCompetitionSceneResourceSlotCapacity(@Param("slotId") Long slotId,
                                                          @Param("reservedWorkstationCount") Integer reservedWorkstationCount,
                                                          @Param("now") Date now,
                                                          @Param("updateBy") String updateBy);

    int reserveExclusiveCompetitionSceneResourceSlotCapacity(@Param("slotId") Long slotId,
                                                             @Param("reservedDeviceCount") Integer reservedDeviceCount,
                                                             @Param("reservedWorkstationCount") Integer reservedWorkstationCount,
                                                             @Param("now") Date now,
                                                             @Param("updateBy") String updateBy);

    int reserveCompetitionSceneResourceSlotCapacity(@Param("slotId") Long slotId,
                                                    @Param("reservedDeviceCount") Integer reservedDeviceCount,
                                                    @Param("reservedWorkstationCount") Integer reservedWorkstationCount,
                                                    @Param("updateBy") String updateBy);

    int releaseSharedCompetitionSceneResourceSlotCapacity(@Param("slotId") Long slotId,
                                                          @Param("reservedWorkstationCount") Integer reservedWorkstationCount,
                                                          @Param("updateBy") String updateBy);

    int releaseExclusiveCompetitionSceneResourceSlotCapacity(@Param("slotId") Long slotId,
                                                             @Param("reservedDeviceCount") Integer reservedDeviceCount,
                                                             @Param("reservedWorkstationCount") Integer reservedWorkstationCount,
                                                             @Param("updateBy") String updateBy);

    int releaseCompetitionSceneResourceSlotCapacity(@Param("slotId") Long slotId,
                                                    @Param("reservedDeviceCount") Integer reservedDeviceCount,
                                                    @Param("reservedWorkstationCount") Integer reservedWorkstationCount,
                                                    @Param("updateBy") String updateBy);
}
