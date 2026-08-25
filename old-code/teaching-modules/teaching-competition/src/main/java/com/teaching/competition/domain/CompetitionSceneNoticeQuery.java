package com.teaching.competition.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 现场通知管理端查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneNoticeQuery extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String noticeType;
    private String scopeType;
    private Long competitionSeriesId;
    private Long scheduleId;
    private Long targetId;
    private String title;
    private String noticeLevel;
    private String publishStatus;
}

