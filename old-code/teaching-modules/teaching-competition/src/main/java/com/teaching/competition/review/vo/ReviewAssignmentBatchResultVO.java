package com.teaching.competition.review.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量分配专家评审任务结果。
 */
@Data
public class ReviewAssignmentBatchResultVO {
    private Integer totalCount = 0;

    private Integer successCount = 0;

    private Integer skipCount = 0;

    private Integer failedCount = 0;

    private List<Long> createdAssignmentIds = new ArrayList<>();

    private List<String> skippedItems = new ArrayList<>();

    private List<String> failedItems = new ArrayList<>();
}
