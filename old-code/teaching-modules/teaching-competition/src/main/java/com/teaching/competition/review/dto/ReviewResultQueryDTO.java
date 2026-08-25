package com.teaching.competition.review.dto;

import lombok.Data;

/**
 * 评审结果管理查询入参。
 */
@Data
public class ReviewResultQueryDTO {
    private Long activityId;
    private Long roundId;
    private String objectCode;
    private String objectName;
    private String orgName;
    private String resultStatus;
    private String completionStatus;
}
