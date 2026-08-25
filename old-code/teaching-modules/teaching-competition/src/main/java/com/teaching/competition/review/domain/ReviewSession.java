package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.util.Date;

        /**
         * 现场评审场次表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewSession extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long activityId;

    private Long roundId;

    private String sessionName;

    private String sessionCode;

    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Long secretaryUserId;

    private Long panelId;

    private Long currentObjectId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date currentStartedTime;

    private String status;
        }
