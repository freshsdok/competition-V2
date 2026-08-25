package com.teaching.competition.review.service;

import com.teaching.competition.review.dto.ReviewSecretarySessionObjectStatusDTO;
import com.teaching.competition.review.dto.ReviewSessionCurrentObjectDTO;
import com.teaching.competition.review.domain.ReviewSession;
import com.teaching.competition.review.vo.ReviewSecretarySessionObjectVO;
import com.teaching.competition.review.vo.ReviewSecretarySessionVO;
import com.teaching.competition.review.vo.ReviewSessionCurrentObjectVO;

import java.util.List;

/**
 * 评审秘书现场控制台Service接口。
 */
public interface IReviewSecretaryService {
    List<ReviewSecretarySessionVO> listMySessions(ReviewSession query);

    ReviewSecretarySessionVO getSessionDetail(Long sessionId);

    List<ReviewSecretarySessionObjectVO> listSessionObjects(Long sessionId);

    ReviewSessionCurrentObjectVO setCurrentObject(Long sessionId, ReviewSessionCurrentObjectDTO dto);

    ReviewSessionCurrentObjectVO nextObject(Long sessionId);

    ReviewSecretarySessionObjectVO updateSessionObjectStatus(Long sessionObjectId, ReviewSecretarySessionObjectStatusDTO dto);
}
