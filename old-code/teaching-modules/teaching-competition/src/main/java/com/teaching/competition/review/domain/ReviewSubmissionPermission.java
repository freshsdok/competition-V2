package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.util.Date;

        /**
         * 填报权限表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewSubmissionPermission extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long activityId;

    private Long objectId;

    private Long userId;

    private Long orgId;

    private String permissionType;

    private String status;

    private String sourceModule;

    private String sourceBizId;

    private Long grantedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date grantedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date usedTime;
        }
