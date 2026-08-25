package com.teaching.competition.review.domain;

    import com.fasterxml.jackson.annotation.JsonFormat;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    /**
     * 评审人画像表。
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public class ReviewerProfile extends ReviewBaseEntity {
        private static final long serialVersionUID = 1L;

private Long userId;

private String reviewerName;

private String orgName;

private String phone;

private String email;

private String subjectCode1;

private String subjectCode2;

private String subjectCode3;

private String categoryCodes;

private String keywords;

private Integer maxTaskCount;

private String status;
    }
