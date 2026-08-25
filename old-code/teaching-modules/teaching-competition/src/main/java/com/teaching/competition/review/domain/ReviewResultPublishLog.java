package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.util.Date;

        /**
         * 结果发布日志表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewResultPublishLog extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long activityId;

    private Long roundId;

    private Long objectId;

    private String publishScope;

    private String publishContent;

    private Long publishedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishedTime;

    private String status;
        }
