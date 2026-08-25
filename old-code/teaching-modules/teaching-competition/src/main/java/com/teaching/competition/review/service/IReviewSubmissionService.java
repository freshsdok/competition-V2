package com.teaching.competition.review.service;

import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectMaterial;
import com.teaching.competition.review.dto.ReviewSubmissionActionDTO;
import com.teaching.competition.review.dto.ReviewSubmissionDraftDTO;
import com.teaching.competition.review.dto.ReviewSubmissionMaterialDTO;
import com.teaching.competition.review.vo.ReviewSubmissionCloseResultVO;
import com.teaching.competition.review.vo.ReviewSubmissionDetailVO;
import com.teaching.competition.review.vo.ReviewSubmissionResultVO;
import com.teaching.competition.review.vo.ReviewSubmissionTaskVO;

import java.util.List;

/**
 * 被评审人填报Service接口。
 */
public interface IReviewSubmissionService {
    List<ReviewSubmissionTaskVO> myList();

    ReviewSubmissionDetailVO detail(Long objectId);

    ReviewObject saveDraft(Long objectId, ReviewSubmissionDraftDTO dto);

    ReviewObjectMaterial addMaterial(Long objectId, ReviewSubmissionMaterialDTO dto);

    List<ReviewObjectMaterial> listMaterials(Long objectId);

    int deleteMaterial(Long materialId);

    ReviewObject submit(Long objectId);

    ReviewObject withdrawRequest(Long objectId, ReviewSubmissionActionDTO dto);

    ReviewObject withdrawApprove(Long objectId, ReviewSubmissionActionDTO dto);

    ReviewObject withdrawReject(Long objectId, ReviewSubmissionActionDTO dto);

    ReviewSubmissionCloseResultVO closeSubmission(Long activityId);

    ReviewSubmissionResultVO publishedResult(Long objectId);
}
