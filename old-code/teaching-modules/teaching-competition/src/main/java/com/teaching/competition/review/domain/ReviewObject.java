package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.util.Date;

        /**
         * 评审对象表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewObject extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long activityId;

    private String objectCode;

    private String objectName;

    private String objectType;

    private String summary;

    private String subjectCode1;

    private String subjectCode2;

    private String subjectCode3;

    private String categoryCodes;

    private String keywords;

    private String orgName;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String submitStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    private Long submittedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lockedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date invalidTime;

    private String createdFrom;

    private String sourceModule;

    private String sourceBizType;

    private String sourceBizId;

    private String sourceTeamId;

    private String sourceRegistrationId;

    private String extraData;
        }
