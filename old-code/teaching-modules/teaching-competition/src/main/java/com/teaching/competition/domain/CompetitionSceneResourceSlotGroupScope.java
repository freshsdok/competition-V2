package com.teaching.competition.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 赛事现场资源预约时段允许组别。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneResourceSlotGroupScope extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long slotId;
    private Long scheduleResourceId;
    private String allowedGroupCode;
    private String allowedGroupName;
    private Integer enabled;
    private Integer deleted;
}

