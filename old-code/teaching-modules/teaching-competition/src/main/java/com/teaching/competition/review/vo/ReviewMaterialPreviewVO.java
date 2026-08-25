package com.teaching.competition.review.vo;

import lombok.Data;

/**
 * 评审材料预览信息。
 */
@Data
public class ReviewMaterialPreviewVO {
    private Long fileId;

    private String fileName;

    private String fileType;

    private String previewType;

    private String previewUrl;

    private String downloadUrl;

    private Boolean converted;

    private String message;
}
