package com.teaching.competition.service;

import com.teaching.competition.domain.CompetitionSceneNoticeForm;
import com.teaching.competition.domain.CompetitionSceneNoticeQuery;
import com.teaching.competition.domain.CompetitionSceneNoticeVo;
import com.teaching.competition.domain.MyCompetitionSceneNoticeVo;

import java.util.List;

/**
 * 赛事现场通知Service。
 */
public interface ICompetitionSceneNoticeService {

    CompetitionSceneNoticeVo selectCompetitionSceneNoticeById(Long noticeId);

    List<CompetitionSceneNoticeVo> selectCompetitionSceneNoticeList(CompetitionSceneNoticeQuery query);

    List<MyCompetitionSceneNoticeVo> selectMyCompetitionSceneNoticeList(Long userId);

    int insertCompetitionSceneNotice(CompetitionSceneNoticeForm form);

    int updateCompetitionSceneNotice(CompetitionSceneNoticeForm form);

    int deleteCompetitionSceneNoticeByIds(Long[] noticeIds);

    int changePublishStatus(CompetitionSceneNoticeForm form);

    int publishCompetitionSceneNotice(Long noticeId);
}

