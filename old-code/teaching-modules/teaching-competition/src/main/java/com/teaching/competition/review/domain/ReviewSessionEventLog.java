package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.util.Date;

        /**
         * 现场事件日志表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewSessionEventLog extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long activityId;

    private Long roundId;

    private Long sessionId;

    private Long objectId;

    private String eventType;

    private String eventContent;

    private Long operatorUserId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date eventTime;
        }
