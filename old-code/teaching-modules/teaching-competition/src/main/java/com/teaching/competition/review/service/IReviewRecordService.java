package com.teaching.competition.review.service;

import com.teaching.competition.review.domain.ReviewRecord;
import com.teaching.competition.review.dto.ReviewRecordDraftDTO;
import com.teaching.competition.review.dto.ReviewRecordSubmitDTO;

/**
 * 专家评审记录表Service接口。
 */
public interface IReviewRecordService extends IReviewCrudService<ReviewRecord> {
    ReviewRecord saveDraft(ReviewRecordDraftDTO dto);

    ReviewRecord submit(ReviewRecordSubmitDTO dto);
}
