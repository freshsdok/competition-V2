package com.teaching.competition.review.domain;

        import com.fasterxml.jackson.annotation.JsonFormat;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
import java.util.Date;

        /**
         * 评审材料表。
         */
        @Data
        @EqualsAndHashCode(callSuper = true)
        public class ReviewObjectMaterial extends ReviewBaseEntity {
            private static final long serialVersionUID = 1L;

    private Long activityId;

    private Long objectId;

    private String materialName;

    private String materialType;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private String mimeType;

    private String fileExt;

    private String visibleToReviewer;

    private Integer sortOrder;

    private Long uploadBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadTime;

    private String status;

    private String sourceModule;

    private String sourceBizType;

    private String sourceBizId;

    private String sourceMaterialKey;
        }
