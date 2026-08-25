package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 大赛现场设备资源台账展示对象。
 */
@Data
public class CompetitionSceneResourceVO {
    private Long resourceId;
    private String resourceCode;
    private String resourceName;
    private String resourceType;
    private String resourceStatus;
    private String brandModel;
    private Integer deviceQuantity;
    private Integer workstationCount;
    private Integer defaultSlotDurationMinutes;
    private Boolean defaultSharedOccupancy;
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
    private String createBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private String updateBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
