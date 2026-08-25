package com.teaching.competition.review.dto;

import lombok.Data;

/**
 * 评审对象材料绑定入参。
 */
@Data
public class ReviewSubmissionMaterialDTO {
    private String materialName;

    private String materialType;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private String mimeType;

    private String fileExt;

    private String visibleToReviewer;

    private Integer sortOrder;
}
