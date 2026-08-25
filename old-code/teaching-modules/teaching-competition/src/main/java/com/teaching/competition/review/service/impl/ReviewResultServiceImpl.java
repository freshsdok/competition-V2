package com.teaching.competition.review.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.review.domain.ReviewAuditLog;
import com.teaching.competition.review.domain.ReviewResult;
import com.teaching.competition.review.domain.ReviewResultPublishLog;
import com.teaching.competition.review.dto.ReviewResultConclusionDTO;
import com.teaching.competition.review.dto.ReviewResultGenerateDTO;
import com.teaching.competition.review.dto.ReviewResultPublishDTO;
import com.teaching.competition.review.dto.ReviewResultQueryDTO;
import com.teaching.competition.review.dto.ReviewResultRevokeDTO;
import com.teaching.competition.review.enums.ReviewResultStatus;
import com.teaching.competition.review.mapper.ReviewAuditLogMapper;
import com.teaching.competition.review.mapper.ReviewResultMapper;
import com.teaching.competition.review.mapper.ReviewResultPublishLogMapper;
import com.teaching.competition.review.service.IReviewResultService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import com.teaching.competition.review.vo.ReviewResultGenerateItemVO;
import com.teaching.competition.review.vo.ReviewResultGenerateResponseVO;
import com.teaching.competition.review.vo.ReviewResultListVO;
import com.teaching.competition.review.vo.ReviewResultRecordVO;
import com.teaching.competition.review.vo.ReviewScoreDetailReadonlyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 评审结果表Service业务层处理。
 */
@Service
public class ReviewResultServiceImpl extends AbstractReviewCrudService<ReviewResult> implements IReviewResultService {
    private static final String BIZ_TYPE_RESULT = "REVIEW_RESULT";
    private static final String ACTION_GENERATE = "GENERATE_RESULT";
    private static final String ACTION_REGENERATE = "REGENERATE_RESULT";
    private static final String ACTION_CONCLUSION = "UPDATE_RESULT_CONCLUSION";
    private static final String ACTION_PUBLISH = "PUBLISH_RESULT";
    private static final String ACTION_REVOKE = "REVOKE_RESULT";

    @Autowired
    private ReviewResultMapper mapper;

    @Autowired
    private ReviewResultPublishLogMapper publishLogMapper;

    @Autowired
    private ReviewAuditLogMapper auditLogMapper;

    @Override
    protected ReviewCrudMapper<ReviewResult> mapper() {
        return mapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResultGenerateResponseVO generate(ReviewResultGenerateDTO dto) {
        if (dto == null || dto.getActivityId() == null) {
            throw new ServiceException("评审活动ID不能为空");
        }
        List<ReviewResultGenerateItemVO> items = mapper.selectGenerateItems(dto);
        ReviewResultGenerateResponseVO response = new ReviewResultGenerateResponseVO();
        response.setTotalCount(items == null ? 0 : items.size());
        if (items == null || items.isEmpty()) {
            response.addWarning("未找到可汇总的评审对象或评审任务");
            return response;
        }
        Date now = DateUtils.getNowDate();
        List<Long> affectedRoundIds = new ArrayList<>();
        for (ReviewResultGenerateItemVO item : items) {
            Integer assignedCount = safeCount(item.getAssignedCount());
            Integer submittedCount = safeCount(item.getSubmittedCount());
            if (submittedCount == 0 || item.getCalculatedScore() == null) {
                response.setSkippedCount(response.getSkippedCount() + 1);
                response.addWarning("评审对象[" + displayObject(item) + "]暂无已提交评分，未生成结果");
                continue;
            }
            if (assignedCount > submittedCount) {
                response.addWarning("评审对象[" + displayObject(item) + "]评分尚未完成："
                        + submittedCount + "/" + assignedCount + "，当前结果为已提交评分平均分");
            }
            ReviewResult query = new ReviewResult();
            query.setActivityId(item.getActivityId());
            query.setRoundId(item.getRoundId());
            query.setObjectId(item.getObjectId());
            List<ReviewResult> existedList = mapper.selectList(query);
            ReviewResult result = existedList == null || existedList.isEmpty() ? null : existedList.get(0);
            if (result != null && ReviewResultStatus.PUBLISHED.getCode().equals(result.getResultStatus())) {
                response.setSkippedCount(response.getSkippedCount() + 1);
                response.addWarning("评审对象[" + displayObject(item) + "]结果已发布，请先撤回发布后再重新生成");
                continue;
            }
            boolean existed = result != null;
            if (result == null) {
                result = new ReviewResult();
                result.setActivityId(item.getActivityId());
                result.setRoundId(item.getRoundId());
                result.setObjectId(item.getObjectId());
                result.setReviewerCount(assignedCount);
                result.setSubmittedCount(submittedCount);
                result.setCalculatedScore(item.getCalculatedScore());
                result.setResultStatus(ReviewResultStatus.GENERATED.getCode());
                result.setGeneratedBy(resolveOperator(dto.getGeneratedBy()));
                result.setGeneratedTime(now);
                fillCreate(result);
                mapper.insert(result);
            } else {
                result.setReviewerCount(assignedCount);
                result.setSubmittedCount(submittedCount);
                result.setCalculatedScore(item.getCalculatedScore());
                result.setResultStatus(ReviewResultStatus.GENERATED.getCode());
                result.setGeneratedBy(resolveOperator(dto.getGeneratedBy()));
                result.setGeneratedTime(now);
                fillUpdate(result);
                mapper.update(result);
            }
            response.setGeneratedCount(response.getGeneratedCount() + 1);
            if (!affectedRoundIds.contains(result.getRoundId())) {
                affectedRoundIds.add(result.getRoundId());
            }
            writeAudit(result, existed ? ACTION_REGENERATE : ACTION_GENERATE,
                    existed ? "重新生成评审结果" : "生成评审结果", result.getGeneratedBy());
            response.getResults().add(toGeneratedView(result, item));
        }
        if (dto.getRoundId() != null) {
            refreshRanks(dto.getActivityId(), dto.getRoundId());
        } else {
            for (Long roundId : affectedRoundIds) {
                refreshRanks(dto.getActivityId(), roundId);
            }
        }
        // 业务约束：管理员不得直接修改 calculated_score；本字段仅由汇总逻辑根据已提交评审记录计算。
        return response;
    }

    @Override
    public List<ReviewResultListVO> selectResultList(ReviewResultQueryDTO query) {
        return mapper.selectResultList(query == null ? new ReviewResultQueryDTO() : query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResult updateConclusion(Long id, ReviewResultConclusionDTO dto) {
        if (id == null || dto == null) {
            throw new ServiceException("结果ID和评价结论不能为空");
        }
        ReviewResult result = mapper.selectById(id);
        if (result == null) {
            throw new ServiceException("评审结果不存在");
        }
        result.setEvaluationConclusion(dto.getEvaluationConclusion());
        result.setConclusionGeneratedBy(resolveOperator(dto.getOperatorUserId()));
        result.setConclusionGeneratedTime(DateUtils.getNowDate());
        fillUpdate(result);
        mapper.update(result);
        writeAudit(result, ACTION_CONCLUSION, "填写评审结果发布结论", result.getConclusionGeneratedBy());
        // 业务约束：evaluation_conclusion 是发布性结论，不是改分入口。
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResult publish(Long id, ReviewResultPublishDTO dto) {
        if (id == null) {
            throw new ServiceException("结果ID不能为空");
        }
        ReviewResult result = mapper.selectById(id);
        if (result == null) {
            throw new ServiceException("评审结果不存在");
        }
        if (result.getCalculatedScore() == null) {
            throw new ServiceException("评审结果尚无系统计算分，不能发布");
        }
        Date now = DateUtils.getNowDate();
        Long operatorUserId = resolveOperator(dto == null ? null : dto.getOperatorUserId());
        result.setResultStatus(ReviewResultStatus.PUBLISHED.getCode());
        result.setPublishedBy(operatorUserId);
        result.setPublishedTime(now);
        fillUpdate(result);
        mapper.update(result);

        writePublishLog(result,
                dto == null || StringUtils.isEmpty(dto.getPublishScope()) ? "ALL" : dto.getPublishScope(),
                dto == null ? null : dto.getPublishContent(),
                ReviewResultStatus.PUBLISHED.getCode(),
                operatorUserId,
                now);
        writeAudit(result, ACTION_PUBLISH, "发布评审结果", operatorUserId);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResult revoke(Long id, ReviewResultRevokeDTO dto) {
        if (id == null) {
            throw new ServiceException("结果ID不能为空");
        }
        ReviewResult result = mapper.selectById(id);
        if (result == null) {
            throw new ServiceException("评审结果不存在");
        }
        if (!ReviewResultStatus.PUBLISHED.getCode().equals(result.getResultStatus())) {
            throw new ServiceException("只有已发布结果可以撤回");
        }
        Date now = DateUtils.getNowDate();
        Long operatorUserId = resolveOperator(dto == null ? null : dto.getOperatorUserId());
        String reason = dto == null || StringUtils.isEmpty(dto.getRevokeReason()) ? "撤回发布" : dto.getRevokeReason();
        result.setResultStatus(ReviewResultStatus.REVOKED.getCode());
        result.setRevokedBy(operatorUserId);
        result.setRevokedTime(now);
        fillUpdate(result);
        mapper.update(result);

        writePublishLog(result, "ALL", reason, ReviewResultStatus.REVOKED.getCode(), operatorUserId, now);
        writeAudit(result, ACTION_REVOKE, "撤回评审结果发布：" + reason, operatorUserId);
        return result;
    }

    @Override
    public List<ReviewResultRecordVO> selectRecordList(Long activityId, Long roundId, Long objectId) {
        if (activityId == null || roundId == null || objectId == null) {
            throw new ServiceException("活动、轮次和评审对象不能为空");
        }
        return mapper.selectRecordList(activityId, roundId, objectId);
    }

    @Override
    public List<ReviewScoreDetailReadonlyVO> selectScoreDetails(Long recordId) {
        if (recordId == null) {
            throw new ServiceException("评分记录ID不能为空");
        }
        return mapper.selectScoreDetailList(recordId);
    }

    private void refreshRanks(Long activityId, Long roundId) {
        ReviewResult query = new ReviewResult();
        query.setActivityId(activityId);
        query.setRoundId(roundId);
        List<ReviewResult> results = mapper.selectList(query);
        if (results == null || results.isEmpty()) {
            return;
        }
        List<ReviewResult> ranked = new ArrayList<>();
        for (ReviewResult result : results) {
            if (result.getCalculatedScore() != null) {
                ranked.add(result);
            }
        }
        ranked.sort(Comparator.comparing(ReviewResult::getCalculatedScore, Comparator.nullsLast(BigDecimal::compareTo)).reversed()
                .thenComparing(ReviewResult::getObjectId, Comparator.nullsLast(Long::compareTo)));
        int rank = 1;
        for (ReviewResult result : ranked) {
            if (!Objects.equals(result.getCalculatedRank(), rank)) {
                result.setCalculatedRank(rank);
                mapper.updateRank(result.getId(), rank, currentUsername());
            }
            rank++;
        }
    }

    private ReviewResultListVO toGeneratedView(ReviewResult result, ReviewResultGenerateItemVO item) {
        ReviewResultListVO vo = new ReviewResultListVO();
        vo.setResultId(result.getId());
        vo.setActivityId(result.getActivityId());
        vo.setRoundId(result.getRoundId());
        vo.setObjectId(result.getObjectId());
        vo.setObjectCode(item.getObjectCode());
        vo.setObjectName(item.getObjectName());
        vo.setReviewerCount(result.getReviewerCount());
        vo.setSubmittedCount(result.getSubmittedCount());
        int assigned = safeCount(result.getReviewerCount());
        int submitted = safeCount(result.getSubmittedCount());
        vo.setUnsubmittedCount(Math.max(assigned - submitted, 0));
        vo.setCompletionText(submitted + "/" + assigned);
        vo.setCompletionStatus(completionStatus(assigned, submitted));
        vo.setCalculatedScore(result.getCalculatedScore());
        vo.setCalculatedGrade(result.getCalculatedGrade());
        vo.setCalculatedRank(result.getCalculatedRank());
        vo.setEvaluationConclusion(result.getEvaluationConclusion());
        vo.setResultStatus(result.getResultStatus());
        vo.setGeneratedTime(result.getGeneratedTime());
        vo.setPublishedTime(result.getPublishedTime());
        vo.setRevokedTime(result.getRevokedTime());
        return vo;
    }

    private String completionStatus(int assigned, int submitted) {
        if (submitted <= 0) {
            return "NOT_STARTED";
        }
        if (assigned > 0 && submitted >= assigned) {
            return "COMPLETED";
        }
        return "PARTIAL";
    }

    private void writePublishLog(ReviewResult result, String scope, String content,
                                 String status, Long operatorUserId, Date operateTime) {
        ReviewResultPublishLog log = new ReviewResultPublishLog();
        log.setActivityId(result.getActivityId());
        log.setRoundId(result.getRoundId());
        log.setObjectId(result.getObjectId());
        log.setPublishScope(StringUtils.isEmpty(scope) ? "ALL" : scope);
        log.setPublishContent(content);
        log.setPublishedBy(operatorUserId);
        log.setPublishedTime(operateTime);
        log.setStatus(status);
        fillCreateBase(log);
        publishLogMapper.insert(log);
    }

    private void writeAudit(ReviewResult result, String actionType, String content, Long operatorUserId) {
        if (auditLogMapper == null || result == null) {
            return;
        }
        ReviewAuditLog log = new ReviewAuditLog();
        log.setActivityId(result.getActivityId());
        log.setRoundId(result.getRoundId());
        log.setObjectId(result.getObjectId());
        log.setBizType(BIZ_TYPE_RESULT);
        log.setBizId(String.valueOf(result.getId()));
        log.setActionType(actionType);
        log.setActionContent(content);
        log.setOperatorUserId(operatorUserId);
        log.setOperatorName(currentUsername());
        log.setOperateTime(DateUtils.getNowDate());
        fillCreateBase(log);
        auditLogMapper.insert(log);
    }

    private Long resolveOperator(Long operatorUserId) {
        return operatorUserId == null ? currentUserId() : operatorUserId;
    }

    private Integer safeCount(Integer count) {
        return count == null ? 0 : count;
    }

    private String displayObject(ReviewResultGenerateItemVO item) {
        if (StringUtils.isNotEmpty(item.getObjectCode()) || StringUtils.isNotEmpty(item.getObjectName())) {
            return firstNotEmpty(item.getObjectCode(), "-") + " " + firstNotEmpty(item.getObjectName(), "");
        }
        return String.valueOf(item.getObjectId());
    }

    private String firstNotEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.isNotEmpty(value)) {
                return value;
            }
        }
        return "";
    }
}
