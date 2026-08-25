package com.teaching.competition.review.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.review.domain.ReviewActivity;
import com.teaching.competition.review.domain.ReviewAssignment;
import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewRound;
import com.teaching.competition.review.domain.ReviewerProfile;
import com.teaching.competition.review.dto.ReviewAssignmentBatchDTO;
import com.teaching.competition.review.enums.ReviewAssignmentStatus;
import com.teaching.competition.review.enums.ReviewObjectStatus;
import com.teaching.competition.review.mapper.ReviewActivityMapper;
import com.teaching.competition.review.mapper.ReviewAssignmentMapper;
import com.teaching.competition.review.mapper.ReviewObjectMapper;
import com.teaching.competition.review.mapper.ReviewRoundMapper;
import com.teaching.competition.review.mapper.ReviewerProfileMapper;
import com.teaching.competition.review.service.IReviewAssignmentService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import com.teaching.competition.review.vo.ReviewAssignmentBatchResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 评审任务分配表Service业务层处理。
 */
@Service
public class ReviewAssignmentServiceImpl extends AbstractReviewCrudService<ReviewAssignment> implements IReviewAssignmentService {
    @Autowired
    private ReviewAssignmentMapper mapper;

    @Autowired
    private ReviewObjectMapper objectMapper;

    @Autowired
    private ReviewActivityMapper activityMapper;

    @Autowired
    private ReviewRoundMapper roundMapper;

    @Autowired
    private ReviewerProfileMapper reviewerProfileMapper;

    @Override
    protected ReviewCrudMapper<ReviewAssignment> mapper() {
        return mapper;
    }

    @Override
    public int insert(ReviewAssignment entity) {
        if (entity == null) {
            throw new ServiceException("保存评审任务不能为空");
        }
        validateAssignmentObject(entity.getObjectId());
        if (StringUtils.isEmpty(entity.getAssignmentType())) {
            entity.setAssignmentType("NORMAL");
        }
        if (StringUtils.isEmpty(entity.getStatus())) {
            entity.setStatus(ReviewAssignmentStatus.ASSIGNED.getCode());
        }
        if (entity.getAssignedTime() == null) {
            entity.setAssignedTime(DateUtils.getNowDate());
        }
        return super.insert(entity);
    }

    @Override
    public int update(ReviewAssignment entity) {
        if (entity == null || entity.getId() == null) {
            throw new ServiceException("更新评审任务ID不能为空");
        }
        ReviewAssignment existed = mapper.selectById(entity.getId());
        if (existed == null) {
            throw new ServiceException("评审任务不存在");
        }
        if (!isAssignmentCrudEditable(existed.getStatus())) {
            throw new ServiceException("评审任务已提交、锁定或取消，不能通过基础接口修改");
        }
        if (ReviewAssignmentStatus.SUBMITTED.getCode().equals(entity.getStatus())) {
            throw new ServiceException("评审任务提交状态必须由专家评分提交流程写入");
        }
        if (entity.getObjectId() != null && !entity.getObjectId().equals(existed.getObjectId())) {
            validateAssignmentObject(entity.getObjectId());
        }
        entity.setSubmittedTime(null);
        return super.update(entity);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("删除评审任务ID不能为空");
        }
        for (Long id : ids) {
            ReviewAssignment assignment = mapper.selectById(id);
            if (assignment == null) {
                throw new ServiceException("评审任务不存在：" + id);
            }
            if (!isAssignmentCrudEditable(assignment.getStatus())) {
                throw new ServiceException("评审任务已提交、锁定或取消，不能通过基础接口删除");
            }
        }
        return super.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewAssignmentBatchResultVO batchAssign(ReviewAssignmentBatchDTO dto) {
        validateBatchAssignInput(dto);
        ReviewAssignmentBatchResultVO result = new ReviewAssignmentBatchResultVO();
        result.setTotalCount(dto.getObjectIds().size() * dto.getReviewerUserIds().size());
        boolean overwrite = Boolean.TRUE.equals(dto.getOverwriteExisting());
        for (Long objectId : dto.getObjectIds()) {
            ReviewObject object = objectMapper.selectById(objectId);
            String objectLabel = object == null ? String.valueOf(objectId) : object.getObjectName() + "(" + objectId + ")";
            if (object == null) {
                addObjectFailed(result, dto, objectLabel, "评审对象不存在");
                continue;
            }
            if (!Objects.equals(dto.getActivityId(), object.getActivityId())) {
                addObjectFailed(result, dto, objectLabel, "评审对象不属于当前活动");
                continue;
            }
            if (ReviewObjectStatus.INVALID.getCode().equals(object.getSubmitStatus())) {
                addObjectFailed(result, dto, objectLabel, "评审对象已作废");
                continue;
            }
            for (Long reviewerUserId : dto.getReviewerUserIds()) {
                String itemLabel = objectLabel + " -> 用户" + reviewerUserId;
                if (reviewerUserId == null) {
                    addFailed(result, itemLabel, "专家用户ID不能为空");
                    continue;
                }
                try {
                    ReviewAssignment existed = selectExisting(dto.getActivityId(), dto.getRoundId(), objectId, reviewerUserId);
                    if (existed != null) {
                        if (!overwrite) {
                            addSkipped(result, itemLabel, "已存在分配任务");
                            continue;
                        }
                        if (!isAssignmentCrudEditable(existed.getStatus())) {
                            addSkipped(result, itemLabel, "已提交、锁定或取消的任务不能覆盖");
                            continue;
                        }
                        applyBatchAssignment(existed, dto, objectId, reviewerUserId);
                        update(existed);
                        incrementSuccess(result);
                        continue;
                    }
                    ReviewAssignment assignment = new ReviewAssignment();
                    applyBatchAssignment(assignment, dto, objectId, reviewerUserId);
                    insert(assignment);
                    result.getCreatedAssignmentIds().add(assignment.getId());
                    incrementSuccess(result);
                } catch (Exception ex) {
                    addFailed(result, itemLabel, ex.getMessage());
                }
            }
        }
        return result;
    }

    private void validateBatchAssignInput(ReviewAssignmentBatchDTO dto) {
        if (dto == null) {
            throw new ServiceException("批量分配参数不能为空");
        }
        if (dto.getActivityId() == null) {
            throw new ServiceException("评审活动不能为空");
        }
        ReviewActivity activity = activityMapper.selectById(dto.getActivityId());
        if (activity == null) {
            throw new ServiceException("评审活动不存在");
        }
        if (dto.getRoundId() == null) {
            throw new ServiceException("评审轮次不能为空");
        }
        ReviewRound round = roundMapper.selectById(dto.getRoundId());
        if (round == null) {
            throw new ServiceException("评审轮次不存在");
        }
        if (!Objects.equals(dto.getActivityId(), round.getActivityId())) {
            throw new ServiceException("评审轮次不属于当前活动");
        }
        if (dto.getObjectIds() == null || dto.getObjectIds().isEmpty()) {
            throw new ServiceException("请选择评审对象");
        }
        if (dto.getReviewerUserIds() == null || dto.getReviewerUserIds().isEmpty()) {
            throw new ServiceException("请选择专家用户");
        }
    }

    private ReviewAssignment selectExisting(Long activityId, Long roundId, Long objectId, Long reviewerUserId) {
        ReviewAssignment query = new ReviewAssignment();
        query.setActivityId(activityId);
        query.setRoundId(roundId);
        query.setObjectId(objectId);
        query.setReviewerUserId(reviewerUserId);
        List<ReviewAssignment> list = mapper.selectList(query);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    private void applyBatchAssignment(ReviewAssignment assignment, ReviewAssignmentBatchDTO dto,
                                      Long objectId, Long reviewerUserId) {
        assignment.setActivityId(dto.getActivityId());
        assignment.setRoundId(dto.getRoundId());
        assignment.setObjectId(objectId);
        assignment.setReviewerUserId(reviewerUserId);
        assignment.setReviewerId(resolveReviewerId(reviewerUserId));
        assignment.setPanelId(dto.getPanelId());
        assignment.setAssignmentType(StringUtils.isEmpty(dto.getAssignmentType()) ? "NORMAL" : dto.getAssignmentType());
        assignment.setStatus(ReviewAssignmentStatus.ASSIGNED.getCode());
        assignment.setAssignedBy(currentUserId());
        assignment.setAssignedTime(DateUtils.getNowDate());
        assignment.setSubmittedTime(null);
        assignment.setRemark(dto.getRemark());
    }

    private Long resolveReviewerId(Long reviewerUserId) {
        if (reviewerUserId == null) {
            return null;
        }
        ReviewerProfile query = new ReviewerProfile();
        query.setUserId(reviewerUserId);
        List<ReviewerProfile> profiles = reviewerProfileMapper.selectList(query);
        return profiles == null || profiles.isEmpty() ? null : profiles.get(0).getId();
    }

    private void incrementSuccess(ReviewAssignmentBatchResultVO result) {
        result.setSuccessCount(result.getSuccessCount() + 1);
    }

    private void addSkipped(ReviewAssignmentBatchResultVO result, String item, String reason) {
        result.setSkipCount(result.getSkipCount() + 1);
        result.getSkippedItems().add(item + "：" + reason);
    }

    private void addFailed(ReviewAssignmentBatchResultVO result, String item, String reason) {
        result.setFailedCount(result.getFailedCount() + 1);
        result.getFailedItems().add(item + "：" + reason);
    }

    private void addObjectFailed(ReviewAssignmentBatchResultVO result, ReviewAssignmentBatchDTO dto,
                                 String objectLabel, String reason) {
        for (Long reviewerUserId : dto.getReviewerUserIds()) {
            addFailed(result, objectLabel + " -> 用户" + reviewerUserId, reason);
        }
    }

    private void validateAssignmentObject(Long objectId) {
        if (objectId == null) {
            throw new ServiceException("评审对象ID不能为空");
        }
        ReviewObject object = objectMapper.selectById(objectId);
        if (object == null) {
            throw new ServiceException("评审对象不存在");
        }
        if (ReviewObjectStatus.INVALID.getCode().equals(object.getSubmitStatus())) {
            throw new ServiceException("评审对象已作废，不能分配评审任务");
        }
    }

    private boolean isAssignmentCrudEditable(String status) {
        return StringUtils.isEmpty(status)
                || ReviewAssignmentStatus.ASSIGNED.getCode().equals(status)
                || ReviewAssignmentStatus.IN_PROGRESS.getCode().equals(status)
                || ReviewAssignmentStatus.RETURNED.getCode().equals(status);
    }
}
