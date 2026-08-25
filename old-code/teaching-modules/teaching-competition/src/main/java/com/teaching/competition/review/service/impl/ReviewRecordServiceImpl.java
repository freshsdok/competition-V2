package com.teaching.competition.review.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.competition.review.domain.ReviewRecord;
import com.teaching.competition.review.domain.ReviewAssignment;
import com.teaching.competition.review.domain.ReviewScoreDetail;
import com.teaching.competition.review.dto.ReviewRecordDraftDTO;
import com.teaching.competition.review.dto.ReviewRecordSubmitDTO;
import com.teaching.competition.review.dto.ReviewScoreDetailDTO;
import com.teaching.competition.review.enums.ReviewAssignmentStatus;
import com.teaching.competition.review.enums.ReviewRecordStatus;
import com.teaching.competition.review.mapper.ReviewAssignmentMapper;
import com.teaching.competition.review.mapper.ReviewRecordMapper;
import com.teaching.competition.review.mapper.ReviewScoreDetailMapper;
import com.teaching.competition.review.service.IReviewRecordService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 专家评审记录表Service业务层处理。
 */
@Service
public class ReviewRecordServiceImpl extends AbstractReviewCrudService<ReviewRecord> implements IReviewRecordService {
    @Autowired
    private ReviewRecordMapper mapper;

    @Autowired
    private ReviewAssignmentMapper assignmentMapper;

    @Autowired
    private ReviewScoreDetailMapper scoreDetailMapper;

    @Override
    protected ReviewCrudMapper<ReviewRecord> mapper() {
        return mapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewRecord saveDraft(ReviewRecordDraftDTO dto) {
        throw new ServiceException("旧评分记录写入接口已禁用，请使用专家端 /review/my-review/{assignmentId}/draft 接口");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewRecord submit(ReviewRecordSubmitDTO dto) {
        throw new ServiceException("旧评分记录写入接口已禁用，请使用专家端 /review/my-review/{assignmentId}/submit 接口");
    }

    private ReviewAssignment resolveAndValidateAssignment(Long assignmentId, Long objectId, Long reviewerUserId) {
        if (assignmentId == null) {
            return null;
        }
        ReviewAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new ServiceException("评审任务分配不存在");
        }
        if (objectId != null && !objectId.equals(assignment.getObjectId())) {
            throw new ServiceException("评审记录与分配任务的评审对象不一致");
        }
        if (reviewerUserId != null && !reviewerUserId.equals(assignment.getReviewerUserId())) {
            throw new ServiceException("评审记录与分配任务的专家用户不一致");
        }
        return assignment;
    }

    private void applyAssignmentSnapshot(ReviewRecord record, ReviewAssignment assignment) {
        record.setActivityId(assignment.getActivityId());
        record.setRoundId(assignment.getRoundId());
        record.setObjectId(assignment.getObjectId());
        record.setAssignmentId(assignment.getId());
        record.setReviewerId(assignment.getReviewerId());
        record.setReviewerUserId(assignment.getReviewerUserId());
    }

    private void copyDraftFields(ReviewRecordDraftDTO dto, ReviewRecord record) {
        record.setActivityId(dto.getActivityId());
        record.setRoundId(dto.getRoundId());
        record.setObjectId(dto.getObjectId());
        record.setAssignmentId(dto.getAssignmentId());
        record.setReviewerId(dto.getReviewerId());
        record.setReviewerUserId(dto.getReviewerUserId());
        record.setTotalScore(dto.getTotalScore());
        record.setGrade(dto.getGrade());
        record.setRecommendation(dto.getRecommendation());
        record.setCommentText(dto.getCommentText());
    }

    private void copySubmitFields(ReviewRecordSubmitDTO dto, ReviewRecord record) {
        record.setActivityId(dto.getActivityId() == null ? record.getActivityId() : dto.getActivityId());
        record.setRoundId(dto.getRoundId() == null ? record.getRoundId() : dto.getRoundId());
        record.setObjectId(dto.getObjectId() == null ? record.getObjectId() : dto.getObjectId());
        record.setAssignmentId(dto.getAssignmentId() == null ? record.getAssignmentId() : dto.getAssignmentId());
        record.setReviewerId(dto.getReviewerId() == null ? record.getReviewerId() : dto.getReviewerId());
        record.setReviewerUserId(dto.getReviewerUserId() == null ? record.getReviewerUserId() : dto.getReviewerUserId());
        record.setTotalScore(dto.getTotalScore() == null ? record.getTotalScore() : dto.getTotalScore());
        record.setGrade(dto.getGrade() == null ? record.getGrade() : dto.getGrade());
        record.setRecommendation(dto.getRecommendation() == null ? record.getRecommendation() : dto.getRecommendation());
        record.setCommentText(dto.getCommentText() == null ? record.getCommentText() : dto.getCommentText());
    }

    private void replaceDetails(ReviewRecord record, List<ReviewScoreDetailDTO> details) {
        if (details == null || details.isEmpty()) {
            return;
        }
        for (ReviewScoreDetailDTO detailDTO : details) {
            if (detailDTO == null) {
                continue;
            }
            ReviewScoreDetail detail = new ReviewScoreDetail();
            detail.setRecordId(record.getId());
            detail.setActivityId(record.getActivityId());
            detail.setRoundId(record.getRoundId());
            detail.setObjectId(record.getObjectId());
            detail.setCriteriaId(detailDTO.getCriteriaId());
            detail.setCriteriaName(detailDTO.getCriteriaName());
            detail.setScoreType(detailDTO.getScoreType());
            detail.setScoreValue(detailDTO.getScoreValue());
            detail.setOptionValue(detailDTO.getOptionValue());
            detail.setTextValue(detailDTO.getTextValue());
            detail.setWeight(detailDTO.getWeight());
            detail.setSortOrder(detailDTO.getSortOrder());
            fillCreateBase(detail);
            scoreDetailMapper.insert(detail);
        }
    }
}
