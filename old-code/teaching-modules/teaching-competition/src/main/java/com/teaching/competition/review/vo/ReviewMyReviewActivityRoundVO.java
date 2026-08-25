package com.teaching.competition.review.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 专家端我的评审活动轮次卡片。
 */
@Data
public class ReviewMyReviewActivityRoundVO {
    private Long activityId;

    private String activityName;

    private String activityCode;

    private String activityType;

    private String objectType;

    private String status;

    private Long roundId;

    private String roundName;

    private Integer roundNo;

    private String roundType;

    private String roundStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date roundStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date roundEndTime;

    private Long sessionId;

    private String sessionName;

    private String sessionStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewEndTime;

    private Integer taskCount;

    private Integer pendingTaskCount;

    private Integer submittedTaskCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastAssignedTime;
}
