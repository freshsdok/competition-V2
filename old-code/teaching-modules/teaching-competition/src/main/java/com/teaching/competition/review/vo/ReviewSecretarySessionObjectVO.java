package com.teaching.competition.review.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 秘书端现场场次对象列表返回。
 */
@Data
public class ReviewSecretarySessionObjectVO {
    private Long sessionObjectId;
    private Integer sequenceNo;
    private Long objectId;
    private String objectCode;
    private String objectName;
    private String orgName;
    private String leaderName;
    private String checkinStatus;
    private String reviewStatus;
    private ReviewScoreProgressVO scoreProgress;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualEndTime;
    private String secretaryNote;
}
