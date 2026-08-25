package com.teaching.competition.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 当前登录人按赛事聚合的现场通知。
 */
@Data
public class MyCompetitionSceneNoticeVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long competitionSeriesId;
    private Long competitionId;
    private String competitionName;
    private List<CompetitionSceneNoticeVo> personalNotices = new ArrayList<>();
    private List<CompetitionSceneNoticeVo> announcements = new ArrayList<>();
}

