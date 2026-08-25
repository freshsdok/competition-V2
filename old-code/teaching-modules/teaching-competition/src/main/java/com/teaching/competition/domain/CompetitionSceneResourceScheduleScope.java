package com.teaching.competition.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 赛事现场资源允许预约赛场范围。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneResourceScheduleScope extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long scopeId;
    private Long scheduleResourceId;
    private Long resourceId;
    private Long allowedScheduleId;
    private String sourceType;
    private Integer enabled;
    private Integer deleted;

    /** 允许预约来源赛场名称，仅用于管理端展示。 */
    private String allowedScheduleName;
}
