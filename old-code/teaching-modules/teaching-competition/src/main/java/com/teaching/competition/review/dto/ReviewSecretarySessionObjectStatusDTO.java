package com.teaching.competition.review.dto;

import lombok.Data;

/**
 * 秘书端更新现场评审对象状态入参。
 */
@Data
public class ReviewSecretarySessionObjectStatusDTO {
    private String checkinStatus;
    private String reviewStatus;
    private String secretaryNote;
}
