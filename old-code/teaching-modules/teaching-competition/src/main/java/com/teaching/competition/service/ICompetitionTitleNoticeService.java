package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionTitleNotice;

import java.util.List;

/**
 * 提示信息Service接口
 *
 * @author teaching
 */
public interface ICompetitionTitleNoticeService {
    /**
     * 查询提示信息
     *
     * @param noticeId 提示信息主键
     * @return 提示信息
     */
    public CompetitionTitleNotice selectCompetitionTitleNoticeById(Long noticeId);

    /**
     * 查询提示信息列表
     *
     * @param competitionTitleNotice 提示信息
     * @return 提示信息集合
     */
    public List<CompetitionTitleNotice> selectCompetitionTitleNoticeList(CompetitionTitleNotice competitionTitleNotice);

    /**
     * 新增提示信息
     *
     * @param competitionTitleNotice 提示信息
     * @return 结果
     */
    public int insertCompetitionTitleNotice(CompetitionTitleNotice competitionTitleNotice);

    /**
     * 修改提示信息
     *
     * @param competitionTitleNotice 提示信息
     * @return 结果
     */
    public int updateCompetitionTitleNotice(CompetitionTitleNotice competitionTitleNotice);

    /**
     * 删除提示信息
     *
     * @param noticeId 提示信息主键
     * @return 结果
     */
    public int deleteCompetitionTitleNoticeById(Long noticeId);

    /**
     * 批量删除提示信息
     *
     * @param noticeIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCompetitionTitleNoticeByIds(Long[] noticeIds);
}
