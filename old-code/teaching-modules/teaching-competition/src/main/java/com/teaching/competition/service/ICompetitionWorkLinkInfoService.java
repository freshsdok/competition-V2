package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionWorkLinkInfo;

import java.util.List;

/**
 * 作品打分链接信息Service接口
 * 
 * @author teaching
 * @date 2025-11-19
 */
public interface ICompetitionWorkLinkInfoService 
{
    /**
     * 查询作品打分链接信息
     * 
     * @param linkId 作品打分链接信息主键
     * @return 作品打分链接信息
     */
    public CompetitionWorkLinkInfo selectCompetitionWorkLinkInfoByLinkId(Long linkId);

    /**
     * 查询作品打分链接信息列表
     * 
     * @param competitionWorkLinkInfo 作品打分链接信息
     * @return 作品打分链接信息集合
     */
    public List<CompetitionWorkLinkInfo> selectCompetitionWorkLinkInfoList(CompetitionWorkLinkInfo competitionWorkLinkInfo);

    /**
     * 新增作品打分链接信息
     * 
     * @param competitionWorkLinkInfo 作品打分链接信息
     * @return 结果
     */
    public String insertCompetitionWorkLinkInfo(CompetitionWorkLinkInfo competitionWorkLinkInfo);

    /**
     * 修改作品打分链接信息
     * 
     * @param competitionWorkLinkInfo 作品打分链接信息
     * @return 结果
     */
    public int updateCompetitionWorkLinkInfo(CompetitionWorkLinkInfo competitionWorkLinkInfo);

    /**
     * 批量删除作品打分链接信息
     * 
     * @param linkIds 需要删除的作品打分链接信息主键集合
     * @return 结果
     */
    public int deleteCompetitionWorkLinkInfoByLinkIds(Long[] linkIds);

    /**
     * 删除作品打分链接信息信息
     * 
     * @param linkId 作品打分链接信息主键
     * @return 结果
     */
    public int deleteCompetitionWorkLinkInfoByLinkId(Long linkId);
}
