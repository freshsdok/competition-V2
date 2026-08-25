package com.teaching.competition.review.service;

import com.teaching.competition.review.domain.ReviewRecord;
import com.teaching.competition.review.dto.ReviewMyReviewScoreDTO;
import com.teaching.competition.review.vo.ReviewMyReviewActivityRoundVO;
import com.teaching.competition.review.vo.ReviewMyReviewDetailVO;
import com.teaching.competition.review.vo.ReviewMyReviewTaskVO;
import com.teaching.competition.review.vo.ReviewSessionCurrentObjectVO;

import java.util.List;

/**
 * 专家端我的评审任务Service接口。
 */
public interface IReviewMyReviewService {
    List<ReviewMyReviewActivityRoundVO> myActivityRounds();

    ReviewSessionCurrentObjectVO currentObject(Long sessionId);

    List<ReviewMyReviewTaskVO> myList(Long activityId, Long roundId, String objectName, String objectCode,
                                      String assignmentStatus, String keywords, Long sessionId);

    ReviewMyReviewDetailVO detail(Long assignmentId);

    ReviewMyReviewDetailVO criteria(Long assignmentId);

    ReviewRecord saveDraft(Long assignmentId, ReviewMyReviewScoreDTO dto);

    ReviewRecord submit(Long assignmentId, ReviewMyReviewScoreDTO dto);
}
