package com.teaching.competition.review.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端只读评分记录视图。
 */
@Data
public class ReviewResultRecordVO {
    private Long recordId;
    private Long assignmentId;
    private Long reviewerId;
    private Long reviewerUserId;
    private String reviewerName;
    private String recordStatus;
    private BigDecimal totalScore;
    private String grade;
    private String recommendation;
    private String commentText;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submittedTime;
}
