package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionSceneNotice;
import com.teaching.competition.domain.CompetitionSceneNoticeAccessVo;
import com.teaching.competition.domain.CompetitionSceneNoticeQuery;
import com.teaching.competition.domain.CompetitionSceneNoticeSchedule;
import com.teaching.competition.domain.CompetitionSceneNoticeVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事现场通知Mapper。
 */
public interface CompetitionSceneNoticeMapper {

    CompetitionSceneNoticeVo selectCompetitionSceneNoticeById(Long noticeId);

    List<CompetitionSceneNoticeVo> selectCompetitionSceneNoticeList(CompetitionSceneNoticeQuery query);

    List<CompetitionSceneNoticeAccessVo> selectMySceneNoticeAccess(@Param("userId") Long userId);

    List<CompetitionSceneNoticeVo> selectMyVisibleNoticeList(@Param("userId") Long userId,
                                                              @Param("memberIds") List<Long> memberIds,
                                                              @Param("targetIds") List<Long> targetIds,
                                                              @Param("seriesIds") List<Long> seriesIds,
                                                              @Param("scheduleIds") List<Long> scheduleIds);

    List<Long> selectNoticeScheduleIds(Long noticeId);

    int countSchedulesInSeries(@Param("competitionSeriesId") Long competitionSeriesId,
                               @Param("scheduleIds") List<Long> scheduleIds);

    Long selectCompetitionIdBySeriesId(Long competitionSeriesId);

    int insertCompetitionSceneNotice(CompetitionSceneNotice notice);

    int updateCompetitionSceneNotice(CompetitionSceneNotice notice);

    int updateNoticePublishStatus(@Param("noticeId") Long noticeId,
                                  @Param("publishStatus") String publishStatus,
                                  @Param("publishTime") java.util.Date publishTime,
                                  @Param("updateBy") String updateBy);

    int deleteCompetitionSceneNoticeByIds(@Param("noticeIds") Long[] noticeIds,
                                          @Param("updateBy") String updateBy);

    int deleteNoticeScheduleRelations(Long noticeId);

    int batchInsertNoticeScheduleRelations(@Param("relations") List<CompetitionSceneNoticeSchedule> relations);
}
