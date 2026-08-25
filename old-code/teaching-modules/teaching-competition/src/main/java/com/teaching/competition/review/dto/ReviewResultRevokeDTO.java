package com.teaching.competition.review.dto;

import lombok.Data;

/**
 * 撤回评审结果发布入参。
 */
@Data
public class ReviewResultRevokeDTO {
    private String revokeReason;
    private Long operatorUserId;
}
