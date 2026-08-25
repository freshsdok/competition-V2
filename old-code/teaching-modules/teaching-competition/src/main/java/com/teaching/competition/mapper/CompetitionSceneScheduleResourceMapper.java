package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionSceneScheduleResource;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceQuery;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 大赛现场赛场资源布置Mapper接口。
 */
public interface CompetitionSceneScheduleResourceMapper {

    CompetitionSceneScheduleResourceVO selectCompetitionSceneScheduleResourceById(Long scheduleResourceId);

    CompetitionSceneScheduleResource selectCompetitionSceneScheduleResourceEntityById(Long scheduleResourceId);

    List<CompetitionSceneScheduleResourceVO> selectCompetitionSceneScheduleResourceList(CompetitionSceneScheduleResourceQuery query);

    int insertCompetitionSceneScheduleResource(CompetitionSceneScheduleResource scheduleResource);

    int updateCompetitionSceneScheduleResource(CompetitionSceneScheduleResource scheduleResource);

    int deleteCompetitionSceneScheduleResourceByIds(@Param("scheduleResourceIds") Long[] scheduleResourceIds,
                                                    @Param("updateBy") String updateBy);

    int updateCompetitionSceneScheduleResourceBookingStatus(@Param("scheduleResourceId") Long scheduleResourceId,
                                                            @Param("bookingStatus") String bookingStatus,
                                                            @Param("updateBy") String updateBy);
}
