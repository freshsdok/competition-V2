package com.teaching.competition.review.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 秘书端现场场次详情返回。
 */
@Data
public class ReviewSecretarySessionVO {
    private Long sessionId;
    private String sessionName;
    private String sessionCode;
    private String location;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    private Long activityId;
    private String activityName;
    private Long roundId;
    private String roundName;
    private String status;
    private Integer objectCount;
    private Long currentObjectId;
    private String currentObjectCode;
    private String currentObjectName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date currentStartedTime;
}
