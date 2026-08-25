package com.teaching.competition.review.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 评审对象提交状态日志表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewObjectSubmitLog extends ReviewBaseEntity {
    private static final long serialVersionUID = 1L;

    private Long activityId;

    private Long objectId;

    private String actionType;

    private String beforeStatus;

    private String afterStatus;

    private Long operatorUserId;

    private String operatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;

    private String actionReason;
}
