package com.teaching.competition.review.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 专家端我的评审任务列表项。
 */
@Data
public class ReviewMyReviewTaskVO {
    private Long assignmentId;

    private Long activityId;

    private String activityName;

    private Long roundId;

    private String roundName;

    private Long objectId;

    private String objectCode;

    private String objectName;

    private String objectStatus;

    private String orgName;

    private String summary;

    private String subjectCode1;

    private String subjectCode2;

    private String subjectCode3;

    private String categoryCodes;

    private String keywords;

    private String assignmentStatus;

    private Long recordId;

    private String recordStatus;

    private BigDecimal totalScore;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submittedTime;

    private Boolean canReview;

    private String cannotReviewReason;

    private Boolean currentObject;
}
