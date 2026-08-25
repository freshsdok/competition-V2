package com.teaching.competition.review.dto;

import lombok.Data;

import java.util.List;

/**
 * 外部业务导入评审对象入参。
 */
@Data
public class ReviewObjectImportDTO {
    private Long activityId;
    private String sourceModule;
    private String sourceBizType;
    private List<String> sourceBizIds;
    private Long competitionSeriesId;
    private String defenseScheduleText;
    private Long fileTaskId;
    private Boolean submittedOnly;
    private String defaultObjectType;
    private String permissionUserMode;
    private Boolean overwriteExisting;
    private Boolean syncCertificate;
    private Boolean syncMaterial;
    private String initialSubmitStatus;
    private String materialOverwriteMode;
    private List<Long> specifiedUserIds;
}
