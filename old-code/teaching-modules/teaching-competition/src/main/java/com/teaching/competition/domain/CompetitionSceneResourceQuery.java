package com.teaching.competition.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 大赛现场设备资源台账查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneResourceQuery extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String resourceCode;
    private String resourceName;
    private String resourceType;
    private String resourceStatus;
}
