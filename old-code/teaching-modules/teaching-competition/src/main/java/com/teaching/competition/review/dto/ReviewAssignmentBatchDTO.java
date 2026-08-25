package com.teaching.competition.review.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理端批量分配专家评审任务入参。
 */
@Data
public class ReviewAssignmentBatchDTO {
    private Long activityId;

    private Long roundId;

    private List<Long> objectIds;

    private List<Long> reviewerUserIds;

    private Long panelId;

    private String assignmentType;

    private Boolean overwriteExisting;

    private String remark;
}
