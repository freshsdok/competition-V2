package com.teaching.competition.review.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 现场场次当前对象返回。
 */
@Data
public class ReviewSessionCurrentObjectVO {
    private Long sessionId;
    private Long activityId;
    private Long roundId;
    private Long objectId;
    private String objectCode;
    private String objectName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date currentStartedTime;
    private String status;
}
