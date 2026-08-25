package com.teaching.competition.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 赛事通知可见赛场关系。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneNoticeSchedule extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long noticeId;
    private Long scheduleId;
}

