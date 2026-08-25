package com.teaching.competition.review.service;

import com.teaching.competition.review.domain.ReviewResult;
import com.teaching.competition.review.dto.ReviewResultConclusionDTO;
import com.teaching.competition.review.dto.ReviewResultGenerateDTO;
import com.teaching.competition.review.dto.ReviewResultPublishDTO;
import com.teaching.competition.review.dto.ReviewResultQueryDTO;
import com.teaching.competition.review.dto.ReviewResultRevokeDTO;
import com.teaching.competition.review.vo.ReviewResultGenerateResponseVO;
import com.teaching.competition.review.vo.ReviewResultListVO;
import com.teaching.competition.review.vo.ReviewResultRecordVO;
import com.teaching.competition.review.vo.ReviewScoreDetailReadonlyVO;

import java.util.List;

/**
 * 评审结果表Service接口。
 */
public interface IReviewResultService extends IReviewCrudService<ReviewResult> {
    ReviewResultGenerateResponseVO generate(ReviewResultGenerateDTO dto);

    List<ReviewResultListVO> selectResultList(ReviewResultQueryDTO query);

    ReviewResult updateConclusion(Long id, ReviewResultConclusionDTO dto);

    ReviewResult publish(Long id, ReviewResultPublishDTO dto);

    ReviewResult revoke(Long id, ReviewResultRevokeDTO dto);

    List<ReviewResultRecordVO> selectRecordList(Long activityId, Long roundId, Long objectId);

    List<ReviewScoreDetailReadonlyVO> selectScoreDetails(Long recordId);
}
