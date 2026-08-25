package com.teaching.competition.review.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 我的评审填报任务。
 */
@Data
public class ReviewSubmissionTaskVO {
    private Long permissionId;

    private Long activityId;

    private String activityName;

    private Long objectId;

    private String objectCode;

    private String objectName;

    private String orgName;

    private String submitStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitDeadline;

    private Boolean editable;

    private Boolean withdrawable;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateTime;
}
