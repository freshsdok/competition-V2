package com.teaching.competition.service.impl;

import com.teaching.competition.domain.CompetitionTitleNotice;
import com.teaching.competition.mapper.CompetitionTitleNoticeMapper;
import com.teaching.competition.service.ICompetitionTitleNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提示信息Service业务层处理
 *
 * @author teaching
 */
@Service
public class CompetitionTitleNoticeServiceImpl implements ICompetitionTitleNoticeService {
    @Autowired
    private CompetitionTitleNoticeMapper competitionTitleNoticeMapper;

    /**
     * 查询提示信息
     *
     * @param noticeId 提示信息主键
     * @return 提示信息
     */
    @Override
    public CompetitionTitleNotice selectCompetitionTitleNoticeById(Long noticeId) {
        return competitionTitleNoticeMapper.selectCompetitionTitleNoticeById(noticeId);
    }

    /**
     * 查询提示信息列表
     *
     * @param competitionTitleNotice 提示信息
     * @return 提示信息集合
     */
    @Override
    public List<CompetitionTitleNotice> selectCompetitionTitleNoticeList(CompetitionTitleNotice competitionTitleNotice) {
        return competitionTitleNoticeMapper.selectCompetitionTitleNoticeList(competitionTitleNotice);
    }

    /**
     * 新增提示信息
     *
     * @param competitionTitleNotice 提示信息
     * @return 结果
     */
    @Override
    public int insertCompetitionTitleNotice(CompetitionTitleNotice competitionTitleNotice) {
        return competitionTitleNoticeMapper.insertCompetitionTitleNotice(competitionTitleNotice);
    }

    /**
     * 修改提示信息
     *
     * @param competitionTitleNotice 提示信息
     * @return 结果
     */
    @Override
    public int updateCompetitionTitleNotice(CompetitionTitleNotice competitionTitleNotice) {
        return competitionTitleNoticeMapper.updateCompetitionTitleNotice(competitionTitleNotice);
    }

    /**
     * 删除提示信息
     *
     * @param noticeId 提示信息主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionTitleNoticeById(Long noticeId) {
        return competitionTitleNoticeMapper.deleteCompetitionTitleNoticeById(noticeId);
    }

    /**
     * 批量删除提示信息
     *
     * @param noticeIds 需要删除的数据主键集合
     * @return 结果
     */
    @Override
    public int deleteCompetitionTitleNoticeByIds(Long[] noticeIds) {
        return competitionTitleNoticeMapper.deleteCompetitionTitleNoticeByIds(noticeIds);
    }
}
