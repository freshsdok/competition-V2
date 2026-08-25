package com.teaching.competition.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端可预约资源查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneResourceBookableQuery extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long scheduleId;
    private Long scheduleResourceId;
    private Long resourceId;
}
