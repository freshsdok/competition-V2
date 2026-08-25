package com.teaching.competition.review.dto;

import lombok.Data;

/**
 * 发布评审结果入参。
 */
@Data
public class ReviewResultPublishDTO {
    private String publishScope;
    private String publishContent;
    private Long operatorUserId;
}
