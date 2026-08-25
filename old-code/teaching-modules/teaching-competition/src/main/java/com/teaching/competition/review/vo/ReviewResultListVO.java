package com.teaching.competition.review.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端评审结果列表视图。
 */
@Data
public class ReviewResultListVO {
    private Long resultId;
    private Long activityId;
    private String activityName;
    private Long roundId;
    private String roundName;
    private Long objectId;
    private String objectCode;
    private String objectName;
    private String orgName;
    private String submitStatus;
    private Integer reviewerCount;
    private Integer submittedCount;
    private Integer unsubmittedCount;
    private String completionText;
    private String completionStatus;
    private BigDecimal calculatedScore;
    private String calculatedGrade;
    private Integer calculatedRank;
    private String evaluationConclusion;
    private String resultStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date generatedTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishedTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date revokedTime;
}
