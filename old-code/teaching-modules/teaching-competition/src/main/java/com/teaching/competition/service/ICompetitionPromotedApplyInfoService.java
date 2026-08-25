package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionPromotedApplyInfo;
import com.teaching.competition.domain.CompetitionPromotedApplyPcInfo;

import java.util.List;
import java.util.Map;

/**
 * 赛事晋级申请报名信息Service接口
 *
 * @author teaching
 * @date 2026-05-19
 */
public interface ICompetitionPromotedApplyInfoService {
    /**
     * 查询赛事晋级申请报名信息
     *
     * @param applyId 赛事晋级申请报名信息主键
     * @return 赛事晋级申请报名信息
     */
    public CompetitionPromotedApplyInfo selectCompetitionPromotedApplyInfoByApplyId(Long applyId);

    /**
     * 查询赛事晋级申请报名信息列表
     *
     * @param competitionPromotedApplyInfo 赛事晋级申请报名信息
     * @return 赛事晋级申请报名信息集合
     */
    public List<CompetitionPromotedApplyInfo> selectCompetitionPromotedApplyInfoList(CompetitionPromotedApplyInfo competitionPromotedApplyInfo);

    /**
     * C端导出用
     * @param competitionPromotedApplyInfo
     * @return
     */
    public List<CompetitionPromotedApplyPcInfo> selectCompetitionPromotedApplyInfoPcList(CompetitionPromotedApplyInfo competitionPromotedApplyInfo);

    /**
     * 新增赛事晋级申请报名信息
     *
     * @param competitionPromotedApplyInfoList 赛事晋级申请报名信息列表
     * @return 结果
     */
    public int insertCompetitionPromotedApplyInfo(List<CompetitionPromotedApplyInfo> competitionPromotedApplyInfoList);

    /**
     * 修改赛事晋级申请报名信息
     *
     * @param competitionPromotedApplyInfo 赛事晋级申请报名信息
     * @return 结果
     */
    public int updateCompetitionPromotedApplyInfo(CompetitionPromotedApplyInfo competitionPromotedApplyInfo);
    /**
     * 批量删除赛事晋级申请报名信息
     *
     * @param applyIds 需要删除的赛事晋级申请报名信息主键集合
     * @return 结果
     */
    public int deleteCompetitionPromotedApplyInfoByApplyIds(Long[] applyIds);

    /**
     * 删除赛事晋级申请报名信息信息
     *
     * @param applyId 赛事晋级申请报名信息主键
     * @return 结果
     */
    public int deleteCompetitionPromotedApplyInfoByApplyId(Long applyId);

    /**
     * 查询赛事晋级申请报名信息总数
     *
     * @param competitionSeriesId 赛事系列赛id
     * @return 赛事晋级申请报名信息总数
     */
    public Integer countCompetitionPromotedApplyInfoList(Long competitionSeriesId);
    /**
     * 根据赛事系列查询晋级报名信息
     *
     * @param competitionPromotedApplyInfo
     * @return 赛事晋级申请报名信息集合
     */
    public List<CompetitionPromotedApplyInfo> getPromotedPlayerInfoListByCompetitionSeriesId(CompetitionPromotedApplyInfo competitionPromotedApplyInfo);

    /**
     * 根据赛事系列查询晋级报名信息 pc端
     * @param competitionPromotedApplyInfo
     * @return
     */
    public List<CompetitionPromotedApplyInfo> getPromotedPlayerInfoListPcByCompetitionSeriesId(CompetitionPromotedApplyInfo competitionPromotedApplyInfo);
    /**
     * 根据团队编码和赛事系列删除赛事晋级申请报名信息  废弃
     * @param teamCodes
     * @return
     */
    public int logicalDelCompetitionPromotedApplyInfoByTeamCodes(String[] teamCodes);

    /**
     * 根据团队编码和赛事系列删除赛事晋级申请报名信息
     * @param teamCodes
     * @param competitionSeriesId
     * @return
     */
    public int logicalDelCompetitionPromotedApplyInfoByTeamCodeAndCompetitionSeriesId(String teamCodes,Long competitionSeriesId);

    /**
     * 更新赛事晋级申请报名信息
     * @param competitionPromotedApplyInfos
     * @return
     */
    public int updateCompetitionPromotedApplyInfos(List<CompetitionPromotedApplyInfo> competitionPromotedApplyInfos);

    /**
     * pc端修改赛事晋级申请报名信息
     * @param competitionPromotedApplyInfo
     * @return
     */
    public int pcUpdateCompetitionPromotedApplyInfo(CompetitionPromotedApplyInfo competitionPromotedApplyInfo);

    /**
     * pc端晋级团队报名
     * @param promotedId 赛事晋级id
     * @param teamCodes 团队编码s
     * @return
     */
    public Map<String,Object> pcApply(Long promotedId, List<String> teamCodes);
}
