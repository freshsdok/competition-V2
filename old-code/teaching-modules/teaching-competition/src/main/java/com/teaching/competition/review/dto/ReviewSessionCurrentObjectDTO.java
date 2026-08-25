package com.teaching.competition.review.dto;

import lombok.Data;

/**
 * 设置现场场次当前评审对象入参。
 */
@Data
public class ReviewSessionCurrentObjectDTO {
    private Long objectId;
    private Long operatorUserId;
    private String sourceType;
    private String certificateCode;
}
