package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionPromotedInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 赛事晋级Mapper接口
 *
 * @author teaching
 * @date 2026-05-19
 */
public interface CompetitionPromotedInfoMapper {
    /**
     * 查询赛事晋级
     *
     * @param promotedId 赛事晋级主键
     * @return 赛事晋级
     */
    public CompetitionPromotedInfo selectCompetitionPromotedInfoByPromotedId(Long promotedId);

    public Integer selectCompetitionPromotedInfoByCompetitionSeriesId(Long competitionSeriesId);

    /**
     * 查询赛事晋级列表
     *
     * @param competitionPromotedInfo 赛事晋级
     * @return 赛事晋级集合
     */
    public List<CompetitionPromotedInfo> selectCompetitionPromotedInfoList(CompetitionPromotedInfo competitionPromotedInfo);

    /**
     * 新增赛事晋级
     *
     * @param competitionPromotedInfo 赛事晋级
     * @return 结果
     */
    public int insertCompetitionPromotedInfo(CompetitionPromotedInfo competitionPromotedInfo);

    /**
     * 修改赛事晋级
     *
     * @param competitionPromotedInfo 赛事晋级
     * @return 结果
     */
    public int updateCompetitionPromotedInfo(CompetitionPromotedInfo competitionPromotedInfo);

    /**
     * 删除赛事晋级
     *
     * @param promotedId 赛事晋级主键
     * @return 结果
     */
    public int deleteCompetitionPromotedInfoByPromotedId(Long promotedId);

    /**
     * 批量删除赛事晋级
     *
     * @param promotedIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionPromotedInfoByPromotedIds(Long[] promotedIds);

    /**
     * 判断是否可以修改
     * @param teamCode 队伍编码
     * @param seriesId 赛事系列id
     * @return 是否可以修改
     */
    boolean canModify(@Param("teamCode") String teamCode,
                      @Param("seriesId") Long seriesId,
                      @Param("teacherId") Long teacherId);

    /**
     * 判断是否可以报名 是否在报名时间范围内
     * @param competitionSeriesId
     * @return
     */
    boolean canApplyByCompetitionSeriesId(@Param("seriesId") Long competitionSeriesId);
}
