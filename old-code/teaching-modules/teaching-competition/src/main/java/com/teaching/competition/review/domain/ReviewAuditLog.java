package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.util.Date;

        /**
         * 审计日志表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewAuditLog extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long activityId;

    private Long roundId;

    private Long objectId;

    private String bizType;

    private String bizId;

    private String actionType;

    private String actionContent;

    private Long operatorUserId;

    private String operatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;

    private String ipAddr;
        }
