package com.teaching.competition.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 大赛现场设备资源台账。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CompetitionSceneResource extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long resourceId;
    private String resourceCode;
    private String resourceName;
    private String resourceType;
    private String resourceStatus;
    private String brandModel;
    private Integer deviceQuantity;

    /** 单台设备工位数。 */
    private Integer workstationCount;

    /** 默认单场周期，单位分钟。 */
    private Integer defaultSlotDurationMinutes;

    private Boolean defaultSharedOccupancy;

    /** 是否需要运维确认，仅作为提示字段，不触发运维流程。 */
    private Boolean needOpsConfirm;

    private String opsContactName;
    private String opsContactPhone;
    private String safetyNotice;
    private String attentionNotes;
    private String parameterJson;
    private String usageInstructions;
    private String imageUrls;
    private String adminRemark;
    private Integer sortOrder;
    private Integer deleted;
}
