package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.util.Date;

        /**
         * 现场评审对象顺序表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewSessionObject extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long activityId;

    private Long roundId;

    private Long sessionId;

    private Long objectId;

    private Integer sequenceNo;

    private String checkinStatus;

    private String reviewStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualEndTime;

    private String secretaryNote;
        }
