package com.teaching.competition.review.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 被评审人可见的已发布评审结果。
 */
@Data
public class ReviewSubmissionResultVO {
    private Long activityId;
    private String activityName;
    private Long roundId;
    private String roundName;
    private Long objectId;
    private String objectCode;
    private String objectName;
    private BigDecimal calculatedScore;
    private String calculatedGrade;
    private Integer calculatedRank;
    private String evaluationConclusion;
    private String resultStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishedTime;
}
