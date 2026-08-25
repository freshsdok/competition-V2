package com.teaching.competition.mapper;

import com.teaching.competition.domain.CompetitionPromotedApplyInfo;
import com.teaching.competition.domain.CompetitionPromotedApplyPcInfo;
import com.teaching.competition.domain.PromotedPlayerInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 赛事晋级申请报名信息Mapper接口
 *
 * @author teaching
 * @date 2026-05-19
 */
public interface CompetitionPromotedApplyInfoMapper {
    /**
     * 查询赛事晋级申请报名信息
     *
     * @param applyId 赛事晋级申请报名信息主键
     * @return 赛事晋级申请报名信息
     */
    public CompetitionPromotedApplyInfo selectCompetitionPromotedApplyInfoByApplyId(Long applyId);

    // 获取赛事晋级团队数量
    public Integer getCompetitionPromotedTeamNum(@Param("competitionSeriesId") Long competitionSeriesId,@Param("leaderTeacherId") Long leaderTeacherId);

    // 获取赛事晋级已报名团队数量
    public Integer getCompetitionPromotedIsApplyInfoTeamNum(@Param("competitionSeriesId") Long competitionSeriesId,@Param("leaderTeacherId") Long leaderTeacherId);

    /**
     * 查询赛事晋级申请报名信息列表
     *
     * @param competitionPromotedApplyInfo 赛事晋级申请报名信息
     * @return 赛事晋级申请报名信息集合
     */
    public List<CompetitionPromotedApplyInfo> selectCompetitionPromotedApplyInfoList(CompetitionPromotedApplyInfo competitionPromotedApplyInfo);

    /**
     * 根据团队编码和系列id查询晋级申请报名信息
     * @param
     * @return
     */
    public List<CompetitionPromotedApplyInfo> selectCompetitionPromotedApplyInfoByTeamCodesAndSeriesId(@Param("teamCodes") List<String> teamCodes, @Param("competitionSeriesId") Long competitionSeriesId);

    /**
     * 根据applyId查询整个团队的信息
     * @param applyId
     * @return
     */
    public List<CompetitionPromotedApplyInfo> selectCompetitionPromotedInfosByApplyId(Long applyId);

    /**
     * 新增赛事晋级申请报名信息
     *
     * @param competitionPromotedApplyInfo 赛事晋级申请报名信息
     * @return 结果
     */
    public int insertCompetitionPromotedApplyInfo(CompetitionPromotedApplyInfo competitionPromotedApplyInfo);

    /**
     * 批量新增赛事晋级申请报名信息
     *
     * @param competitionPromotedApplyInfoList 赛事晋级申请报名信息列表
     * @return 结果
     */
    public int batchInsertCompetitionPromotedApplyInfo(@Param("list") List<CompetitionPromotedApplyInfo> competitionPromotedApplyInfoList);

    /**
     * 查询已存在的团队编码列表
     *
     * @return 已存在的团队编码集合
     */
    public Set<String> selectExistTeamCodes(Long competitionSeriesId);

    /**
     * 修改赛事晋级申请报名信息
     *
     * @param competitionPromotedApplyInfo 赛事晋级申请报名信息
     * @return 结果
     */
    public int updateCompetitionPromotedApplyInfo(CompetitionPromotedApplyInfo competitionPromotedApplyInfo);

    /**
     * 删除赛事晋级申请报名信息
     *
     * @param applyId 赛事晋级申请报名信息主键
     * @return 结果
     */
    public int deleteCompetitionPromotedApplyInfoByApplyId(Long applyId);

    /**
     * 批量删除赛事晋级申请报名信息
     *
     * @param applyIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionPromotedApplyInfoByApplyIds(Long[] applyIds);

    /**
     * 查询赛事晋级申请报名信息总数
     *
     * @param competitionSeriesId
     * @return
     */
    public Integer countCompetitionPromotedApplyInfoList(Long competitionSeriesId);

    /**
     * 根据赛事系列查询晋级报名信息
     *
     * @param competitionPromotedApplyInfo
     * @return
     */
    public List<CompetitionPromotedApplyInfo> selectPromotedPlayerInfoListByCompetitionSeries(CompetitionPromotedApplyInfo competitionPromotedApplyInfo);
    public List<CompetitionPromotedApplyPcInfo> selectPromotedPlayerInfoListByCompetitionSeriesPc(CompetitionPromotedApplyInfo competitionPromotedApplyInfo);

    /**
     * 根据赛事系列和团队编码s查询赛事晋级申请报名信息
     * @param competitionSeriesId
     * @param teamCodes
     * @return
     */
    public List<CompetitionPromotedApplyInfo> selectPromotedPlayerAllInfoListByCompetitionSeries(@Param("competitionSeriesId")Long competitionSeriesId,@Param("teamCodes")List<String> teamCodes);

    /**
     * 根据团队编码和赛事系列删除赛事晋级申请报名信息
     *
     * @param teamCodes
     */
    public int logicalDelCompetitionPromotedApplyInfoByTeamCodes(String[] teamCodes);

    /**
     * 根据赛事系列和团队编码查询赛事晋级未报名信息
     *
     * @param competitionSeriesId
     * @param teamCode
     * @return
     */
    public String selectApplyInfoByCompetitionSeriesIdAndTeamCode(@Param("competitionSeriesId") Long competitionSeriesId,
                                                                  @Param("teamCode") String teamCode);

    /**
     * 根据teamCode和赛事系列删除赛事晋级申请报名信息
     *
     * @param teamCode
     * @param competitionSeriesId
     * @return
     */
    public int logicalDelCompetitionPromotedApplyInfoByTeamCodeAndCompetitionSeriesId(@Param("teamCode") String teamCode,
                                                                                      @Param("competitionSeriesId") Long competitionSeriesId,
                                                                                      @Param("updateBy") String updateBy);

    public int logicalDelCompetitionPromotedApplyInfoByCompetitionSeriesId(Long competitionSeriesId);
    /**
     * 根据apply_id修改team_sort 批量
     *
     * @param playerInfos
     */
    public int updateCompetitionPromotedApplyInfos(List<PromotedPlayerInfo> playerInfos);

    /**
     * 根据applyIds逻辑删除赛事晋级申请报名信息
     *
     * @param applyIds
     * @return
     */
    public int logicalDeleteCompetitionPromotedApplyInfoByApplyIds(@Param("applyIds") Long[] applyIds, @Param("updateBy") String updateBy);

    /**
     * 更新指导老师名称
     *
     * @param guideTeacherInfoList
     * @return
     */
    public int updateCompetitionPromotedApplyInfoGuideTeacher(List<PromotedPlayerInfo> guideTeacherInfoList);

    /**
     * 批量新增赛事晋级申请报名信息
     *
     * @param competitionPromotedApplyInfos
     * @return
     */
    public int insertCompetitionPromotedApplyInfos(List<CompetitionPromotedApplyInfo> competitionPromotedApplyInfos);

    /**
     * 同一赛事批量修改多个teamCode报名状态
     * @param competitionSeriesId
     * @param teamCodes
     * @param applyStatus
     * @param updateBy
     * @return
     */
    public int updateApplyStatusByCompetitionAndTeamCodeBatch(@Param("competitionSeriesId") Long competitionSeriesId,
                                                              @Param("teamCodes") List<String> teamCodes,
                                                              @Param("applyStatus") String applyStatus,
                                                              @Param("updateBy") String updateBy);

    public int updateApplyStatusByCompetitionAndTeamCode(@Param("competitionSeriesId") Long competitionSeriesId,
                                                         @Param("oldTeamCode") String oldTeamCode,
                                                         @Param("newTeamCode") String newTeamCode,
                                                         @Param("applyStatus") String applyStatus,
                                                         @Param("updateBy") String updateBy);
}
