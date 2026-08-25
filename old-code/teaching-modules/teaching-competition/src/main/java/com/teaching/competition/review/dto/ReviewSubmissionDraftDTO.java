package com.teaching.competition.review.dto;

import lombok.Data;

/**
 * 评审对象填报草稿保存入参。
 */
@Data
public class ReviewSubmissionDraftDTO {
    private String objectName;

    private String summary;

    private String orgName;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String subjectCode1;

    private String subjectCode2;

    private String subjectCode3;

    private String categoryCodes;

    private String keywords;

    private String extraData;
}
