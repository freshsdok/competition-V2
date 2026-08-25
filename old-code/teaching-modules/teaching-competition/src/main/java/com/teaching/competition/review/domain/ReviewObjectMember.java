package com.teaching.competition.review.domain;

    import com.fasterxml.jackson.annotation.JsonFormat;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    /**
     * 评审对象成员表。
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public class ReviewObjectMember extends ReviewBaseEntity {
        private static final long serialVersionUID = 1L;

private Long activityId;

private Long objectId;

private Long userId;

private String personId;

private String memberName;

private String memberRole;

private String isPrimary;

private String phone;

private String email;

private String orgName;

private String certificateId;

private String certificateCode;

private String certificateType;

private String sourceModule;

private String sourceBizId;

private Integer sortOrder;
    }
