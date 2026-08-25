package com.teaching.competition.review.service;

import com.teaching.competition.review.domain.ReviewAssignment;
import com.teaching.competition.review.dto.ReviewAssignmentBatchDTO;
import com.teaching.competition.review.vo.ReviewAssignmentBatchResultVO;

/**
 * 评审任务分配表Service接口。
 */
public interface IReviewAssignmentService extends IReviewCrudService<ReviewAssignment> {
    ReviewAssignmentBatchResultVO batchAssign(ReviewAssignmentBatchDTO dto);
}
