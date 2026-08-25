package com.teaching.competition.review.service;

import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectCertificateRef;
import com.teaching.competition.review.dto.ReviewObjectImportDTO;
import com.teaching.competition.review.vo.ReviewCertificateResolveResultVO;
import com.teaching.competition.review.vo.ReviewObjectImportPreviewVO;
import com.teaching.competition.review.vo.ReviewObjectImportResultVO;

import java.util.List;

/**
 * 评审对象表Service接口。
 */
public interface IReviewObjectService extends IReviewCrudService<ReviewObject> {
    List<ReviewObjectImportPreviewVO> importPreview(ReviewObjectImportDTO dto);

    ReviewObjectImportResultVO importFromBusiness(ReviewObjectImportDTO dto);

    ReviewObjectImportResultVO syncFileTaskMaterials(ReviewObjectImportDTO dto);

    int insertCertificateRef(ReviewObjectCertificateRef ref);

    List<ReviewObjectCertificateRef> selectCertificateRefList(ReviewObjectCertificateRef query);

    ReviewCertificateResolveResultVO resolveCertificate(Long activityId, String certificateCode, Long sessionId);
}
