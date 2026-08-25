package com.teaching.competition.review.service;

import com.teaching.competition.review.vo.ReviewMaterialPreviewVO;
import com.teaching.competition.review.vo.ReviewPreviewResource;

/**
 * 评审材料预览服务。
 */
public interface IReviewFilePreviewService {
    ReviewMaterialPreviewVO preview(Long fileId);

    ReviewPreviewResource previewStream(Long fileId);

    ReviewPreviewResource download(Long fileId);
}
