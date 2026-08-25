package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionSceneResource;
import com.teaching.competition.domain.CompetitionSceneResourceQuery;
import com.teaching.competition.domain.CompetitionSceneResourceVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 大赛现场设备资源台账Mapper接口。
 */
public interface CompetitionSceneResourceMapper {

    CompetitionSceneResourceVO selectCompetitionSceneResourceById(Long resourceId);

    CompetitionSceneResource selectCompetitionSceneResourceByCode(String resourceCode);

    List<CompetitionSceneResourceVO> selectCompetitionSceneResourceList(CompetitionSceneResourceQuery query);

    int insertCompetitionSceneResource(CompetitionSceneResource resource);

    int updateCompetitionSceneResource(CompetitionSceneResource resource);

    int deleteCompetitionSceneResourceByIds(@Param("resourceIds") Long[] resourceIds,
                                            @Param("updateBy") String updateBy);

    int updateCompetitionSceneResourceStatus(@Param("resourceId") Long resourceId,
                                             @Param("resourceStatus") String resourceStatus,
                                             @Param("updateBy") String updateBy);

    int countScheduleResourceByResourceIds(@Param("resourceIds") Long[] resourceIds);
}
