package com.teaching.competition.review.service;

import com.teaching.competition.review.domain.ReviewSession;
import com.teaching.competition.review.domain.ReviewSessionEventLog;
import com.teaching.competition.review.domain.ReviewSessionObject;
import com.teaching.competition.review.dto.ReviewSessionCurrentObjectDTO;
import com.teaching.competition.review.vo.ReviewSessionCurrentObjectVO;

import java.util.List;

/**
 * 现场评审场次表Service接口。
 */
public interface IReviewSessionService extends IReviewCrudService<ReviewSession> {
    ReviewSessionCurrentObjectVO setCurrentObject(Long sessionId, ReviewSessionCurrentObjectDTO dto);

    ReviewSessionCurrentObjectVO getCurrentObject(Long sessionId);

    int insertSessionObject(ReviewSessionObject sessionObject);

    int updateSessionObject(ReviewSessionObject sessionObject);

    int deleteSessionObjectByIds(Long[] ids);

    List<ReviewSessionObject> selectSessionObjectList(ReviewSessionObject query);

    List<ReviewSessionEventLog> selectEventLogList(ReviewSessionEventLog query);
}
