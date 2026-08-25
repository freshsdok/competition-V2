package com.teaching.competition.review.dto;

import lombok.Data;

/**
 * 填报提交、撤回、审批操作入参。
 */
@Data
public class ReviewSubmissionActionDTO {
    private String actionReason;
}
