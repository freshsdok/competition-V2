package com.teaching.competition.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 现场通知展示对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneNoticeVo extends CompetitionSceneNotice {
    private static final long serialVersionUID = 1L;

    private String competitionName;
    private List<Long> scheduleIds;
    private String scheduleNames;
}

