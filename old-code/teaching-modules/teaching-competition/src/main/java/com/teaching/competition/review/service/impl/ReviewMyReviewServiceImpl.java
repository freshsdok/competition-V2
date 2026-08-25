package com.teaching.competition.review.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.review.constant.ReviewConstants;
import com.teaching.competition.review.domain.ReviewActivity;
import com.teaching.competition.review.domain.ReviewAssignment;
import com.teaching.competition.review.domain.ReviewAuditLog;
import com.teaching.competition.review.domain.ReviewCriteria;
import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectMaterial;
import com.teaching.competition.review.domain.ReviewObjectMember;
import com.teaching.competition.review.domain.ReviewRecord;
import com.teaching.competition.review.domain.ReviewRound;
import com.teaching.competition.review.domain.ReviewRule;
import com.teaching.competition.review.domain.ReviewScoreDetail;
import com.teaching.competition.review.domain.ReviewSession;
import com.teaching.competition.review.dto.ReviewMyReviewScoreDTO;
import com.teaching.competition.review.dto.ReviewScoreDetailDTO;
import com.teaching.competition.review.enums.ReviewAssignmentStatus;
import com.teaching.competition.review.enums.ReviewCriteriaType;
import com.teaching.competition.review.enums.ReviewMaterialType;
import com.teaching.competition.review.enums.ReviewObjectStatus;
import com.teaching.competition.review.enums.ReviewRecordStatus;
import com.teaching.competition.review.enums.ReviewRoundType;
import com.teaching.competition.review.enums.ReviewRuleScoreMode;
import com.teaching.competition.review.enums.ReviewSessionStatus;
import com.teaching.competition.review.mapper.ReviewActivityMapper;
import com.teaching.competition.review.mapper.ReviewAssignmentMapper;
import com.teaching.competition.review.mapper.ReviewAuditLogMapper;
import com.teaching.competition.review.mapper.ReviewCriteriaMapper;
import com.teaching.competition.review.mapper.ReviewObjectMapper;
import com.teaching.competition.review.mapper.ReviewObjectMaterialMapper;
import com.teaching.competition.review.mapper.ReviewObjectMemberMapper;
import com.teaching.competition.review.mapper.ReviewRecordMapper;
import com.teaching.competition.review.mapper.ReviewRoundMapper;
import com.teaching.competition.review.mapper.ReviewRuleMapper;
import com.teaching.competition.review.mapper.ReviewScoreDetailMapper;
import com.teaching.competition.review.mapper.ReviewSessionMapper;
import com.teaching.competition.review.service.IReviewMyReviewService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import com.teaching.competition.review.vo.ReviewMyReviewActivityRoundVO;
import com.teaching.competition.review.vo.ReviewMyReviewDetailVO;
import com.teaching.competition.review.vo.ReviewMyReviewTaskVO;
import com.teaching.competition.review.vo.ReviewSessionCurrentObjectVO;
import com.teaching.system.api.RemoteFileReviewImportService;
import com.teaching.system.api.domain.FileReviewImportMaterial;
import com.teaching.system.api.domain.FileReviewImportSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 专家端我的评审任务Service业务层处理。
 */
@Service
public class ReviewMyReviewServiceImpl extends AbstractReviewCrudService<ReviewRecord> implements IReviewMyReviewService {
    private static final Logger log = LoggerFactory.getLogger(ReviewMyReviewServiceImpl.class);
    private static final String SOURCE_MODULE_SYSTEM = "system";
    private static final String SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER = "FILE_UPLOAD_MANAGER";
    private static final String MATERIAL_STATUS_NORMAL = "NORMAL";
    private static final String BIZ_TYPE_MY_REVIEW = "REVIEW_MY_REVIEW";
    private static final String ACTION_SAVE_DRAFT = "MY_REVIEW_DRAFT";
    private static final String ACTION_SUBMIT = "MY_REVIEW_SUBMIT";
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Autowired
    private ReviewAssignmentMapper assignmentMapper;

    @Autowired
    private ReviewRecordMapper recordMapper;

    @Autowired
    private ReviewScoreDetailMapper scoreDetailMapper;

    @Autowired
    private ReviewObjectMapper objectMapper;

    @Autowired
    private ReviewActivityMapper activityMapper;

    @Autowired
    private ReviewRoundMapper roundMapper;

    @Autowired
    private ReviewRuleMapper ruleMapper;

    @Autowired
    private ReviewCriteriaMapper criteriaMapper;

    @Autowired
    private ReviewObjectMemberMapper objectMemberMapper;

    @Autowired
    private ReviewObjectMaterialMapper materialMapper;

    @Autowired
    private ReviewSessionMapper sessionMapper;

    @Autowired
    private ReviewAuditLogMapper auditLogMapper;

    @Autowired
    private RemoteFileReviewImportService remoteFileReviewImportService;

    @Override
    protected ReviewCrudMapper<ReviewRecord> mapper() {
        return recordMapper;
    }

    @Override
    public List<ReviewMyReviewActivityRoundVO> myActivityRounds() {
        Long userId = requireCurrentUserId();
        ReviewAssignment query = new ReviewAssignment();
        query.setReviewerUserId(userId);
        List<ReviewAssignment> assignments = assignmentMapper.selectList(query);
        if (assignments == null || assignments.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, ReviewMyReviewActivityRoundVO> activityRoundMap = new LinkedHashMap<>();
        Map<String, Set<Long>> activityRoundPanelMap = new HashMap<>();
        Map<String, Boolean> activityRoundUnscopedMap = new HashMap<>();
        Map<Long, ReviewActivity> activityCache = new HashMap<>();
        Map<Long, ReviewRound> roundCache = new HashMap<>();
        for (ReviewAssignment assignment : assignments) {
            Long activityId = assignment.getActivityId();
            Long roundId = assignment.getRoundId();
            if (activityId == null || roundId == null) {
                continue;
            }
            String activityRoundKey = activityId + ":" + roundId;
            ReviewMyReviewActivityRoundVO activityVO = activityRoundMap.get(activityRoundKey);
            if (activityVO == null) {
                ReviewActivity activity = activityCache.computeIfAbsent(activityId, activityMapper::selectById);
                ReviewRound round = roundCache.computeIfAbsent(roundId, roundMapper::selectById);
                if (activity == null || round == null || !activityId.equals(round.getActivityId())) {
                    continue;
                }
                activityVO = buildActivityRoundVO(activity, round);
                activityRoundMap.put(activityRoundKey, activityVO);
            }
            activityVO.setTaskCount(activityVO.getTaskCount() + 1);
            if (ReviewAssignmentStatus.SUBMITTED.getCode().equals(assignment.getStatus())) {
                activityVO.setSubmittedTaskCount(activityVO.getSubmittedTaskCount() + 1);
            } else if (!ReviewAssignmentStatus.CANCELLED.getCode().equals(assignment.getStatus())
                    && !ReviewAssignmentStatus.LOCKED.getCode().equals(assignment.getStatus())) {
                activityVO.setPendingTaskCount(activityVO.getPendingTaskCount() + 1);
            }
            if (assignment.getPanelId() != null) {
                activityRoundPanelMap.computeIfAbsent(activityRoundKey, key -> new HashSet<>())
                        .add(assignment.getPanelId());
            } else {
                activityRoundUnscopedMap.put(activityRoundKey, Boolean.TRUE);
            }
            if (assignment.getAssignedTime() != null
                    && (activityVO.getLastAssignedTime() == null
                    || assignment.getAssignedTime().after(activityVO.getLastAssignedTime()))) {
                activityVO.setLastAssignedTime(assignment.getAssignedTime());
            }
        }

        List<ReviewMyReviewActivityRoundVO> activities = new ArrayList<>(activityRoundMap.values());
        activities.forEach(item -> {
            String key = item.getActivityId() + ":" + item.getRoundId();
            applyOnsiteSession(item, activityRoundPanelMap.get(key),
                    Boolean.TRUE.equals(activityRoundUnscopedMap.get(key)));
        });
        activities.sort(Comparator
                .comparing((ReviewMyReviewActivityRoundVO item) -> !ReviewRoundType.ONSITE_DEFENSE.getCode().equals(item.getRoundType()))
                .thenComparing(item -> !"IN_PROGRESS".equals(item.getRoundStatus()))
                .thenComparing(item -> !"REVIEWING".equals(item.getStatus()))
                .thenComparing(ReviewMyReviewActivityRoundVO::getLastAssignedTime,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ReviewMyReviewActivityRoundVO::getActivityId,
                        Comparator.nullsLast(Comparator.reverseOrder())));
        return activities;
    }

    private ReviewMyReviewActivityRoundVO buildActivityRoundVO(ReviewActivity activity, ReviewRound round) {
        ReviewMyReviewActivityRoundVO vo = new ReviewMyReviewActivityRoundVO();
        vo.setActivityId(activity.getId());
        vo.setActivityName(activity.getActivityName());
        vo.setActivityCode(activity.getActivityCode());
        vo.setActivityType(activity.getActivityType());
        vo.setObjectType(activity.getObjectType());
        vo.setStatus(activity.getStatus());
        vo.setReviewStartTime(activity.getReviewStartTime());
        vo.setReviewEndTime(activity.getReviewEndTime());
        vo.setRoundId(round.getId());
        vo.setRoundName(round.getRoundName());
        vo.setRoundNo(round.getRoundNo());
        vo.setRoundType(round.getRoundType());
        vo.setRoundStatus(round.getStatus());
        vo.setRoundStartTime(round.getStartTime());
        vo.setRoundEndTime(round.getEndTime());
        vo.setTaskCount(0);
        vo.setPendingTaskCount(0);
        vo.setSubmittedTaskCount(0);
        return vo;
    }

    private void applyOnsiteSession(ReviewMyReviewActivityRoundVO vo, Set<Long> reviewerPanelIds,
                                    boolean hasUnscopedAssignment) {
        if (vo == null || !ReviewRoundType.ONSITE_DEFENSE.getCode().equals(vo.getRoundType())) {
            return;
        }
        ReviewSession query = new ReviewSession();
        query.setActivityId(vo.getActivityId());
        query.setRoundId(vo.getRoundId());
        List<ReviewSession> sessions = sessionMapper.selectList(query);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        List<ReviewSession> matchedSessions = sessions;
        if (!hasUnscopedAssignment && reviewerPanelIds != null && !reviewerPanelIds.isEmpty()) {
            List<ReviewSession> panelSessions = sessions.stream()
                    .filter(item -> item.getPanelId() != null && reviewerPanelIds.contains(item.getPanelId()))
                    .collect(Collectors.toList());
            if (panelSessions.isEmpty()) {
                return;
            }
            matchedSessions = panelSessions;
        }
        matchedSessions.sort(Comparator
                .comparing((ReviewSession item) -> !ReviewSessionStatus.IN_PROGRESS.getCode().equals(item.getStatus()))
                .thenComparing(item -> item.getCurrentObjectId() == null)
                .thenComparing(ReviewSession::getId, Comparator.nullsLast(Comparator.reverseOrder())));
        ReviewSession session = matchedSessions.get(0);
        vo.setSessionId(session.getId());
        vo.setSessionName(session.getSessionName());
        vo.setSessionStatus(session.getStatus());
    }

    @Override
    public ReviewSessionCurrentObjectVO currentObject(Long sessionId) {
        if (sessionId == null) {
            throw new ServiceException("现场场次ID不能为空");
        }
        ReviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new ServiceException("现场评审场次不存在");
        }
        ReviewAssignment query = new ReviewAssignment();
        query.setReviewerUserId(requireCurrentUserId());
        query.setActivityId(session.getActivityId());
        query.setRoundId(session.getRoundId());
        List<ReviewAssignment> assignments = assignmentMapper.selectList(query);
        if (assignments == null || assignments.isEmpty()) {
            throw new ServiceException("当前专家未被分配到该现场轮次");
        }
        if (session.getPanelId() != null && assignments.stream()
                .noneMatch(item -> item.getPanelId() == null || session.getPanelId().equals(item.getPanelId()))) {
            throw new ServiceException("当前专家未被分配到该现场场次所属专家组");
        }
        ReviewObject object = session.getCurrentObjectId() == null
                ? null : objectMapper.selectById(session.getCurrentObjectId());
        ReviewSessionCurrentObjectVO vo = new ReviewSessionCurrentObjectVO();
        vo.setSessionId(session.getId());
        vo.setActivityId(session.getActivityId());
        vo.setRoundId(session.getRoundId());
        vo.setObjectId(object == null ? null : object.getId());
        vo.setObjectCode(object == null ? null : object.getObjectCode());
        vo.setObjectName(object == null ? null : object.getObjectName());
        vo.setCurrentStartedTime(session.getCurrentStartedTime());
        vo.setStatus(session.getStatus());
        return vo;
    }

    @Override
    public List<ReviewMyReviewTaskVO> myList(Long activityId, Long roundId, String objectName, String objectCode,
                                             String assignmentStatus, String keywords, Long sessionId) {
        Long userId = requireCurrentUserId();
        ReviewAssignment query = new ReviewAssignment();
        query.setReviewerUserId(userId);
        query.setActivityId(activityId);
        query.setRoundId(roundId);
        query.setStatus(assignmentStatus);
        List<ReviewAssignment> assignments = assignmentMapper.selectList(query);
        if (assignments == null || assignments.isEmpty()) {
            return new ArrayList<>();
        }

        Long currentObjectId = resolveCurrentObjectId(sessionId, activityId, roundId);
        List<ReviewMyReviewTaskVO> list = new ArrayList<>();
        for (ReviewAssignment assignment : assignments) {
            ReviewObject object = objectMapper.selectById(assignment.getObjectId());
            if (!matchesObjectFilter(object, objectName, objectCode, keywords)) {
                continue;
            }
            ReviewMyReviewTaskVO vo = buildTaskVO(assignment, object, currentObjectId);
            list.add(vo);
        }
        list.sort(Comparator.comparing((ReviewMyReviewTaskVO item) -> !Boolean.TRUE.equals(item.getCurrentObject()))
                .thenComparing(ReviewMyReviewTaskVO::getAssignmentId, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewMyReviewDetailVO detail(Long assignmentId) {
        ReviewAssignment assignment = requireOwnAssignment(assignmentId);
        ReviewObject object = requireObject(assignment.getObjectId());
        ReviewRound round = requireRound(assignment.getRoundId());
        ReviewRule rule = resolveRule(assignment.getActivityId(), round);
        List<ReviewCriteria> criteriaList = selectCriteria(rule);
        ReviewRecord record = selectLatestRecord(assignmentId);

        ReviewMyReviewDetailVO vo = new ReviewMyReviewDetailVO();
        vo.setAssignment(assignment);
        vo.setReviewObject(object);
        vo.setRound(round);
        vo.setMembers(selectMembers(object.getId()));
        vo.setMaterials(selectReviewerVisibleMaterials(object));
        vo.setRule(rule);
        vo.setCriteriaList(criteriaList);
        vo.setExistingRecord(record);
        vo.setExistingScoreDetails(record == null ? new ArrayList<>() : selectScoreDetails(record.getId()));
        ReviewAccess access = reviewAccess(assignment, object, round, record, rule);
        vo.setCanReview(access.canReview);
        vo.setCannotReviewReason(access.reason);
        return vo;
    }

    @Override
    public ReviewMyReviewDetailVO criteria(Long assignmentId) {
        return detail(assignmentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewRecord saveDraft(Long assignmentId, ReviewMyReviewScoreDTO dto) {
        return saveInternal(assignmentId, dto, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewRecord submit(Long assignmentId, ReviewMyReviewScoreDTO dto) {
        return saveInternal(assignmentId, dto, true);
    }

    private ReviewRecord saveInternal(Long assignmentId, ReviewMyReviewScoreDTO dto, boolean submit) {
        if (dto == null) {
            throw new ServiceException("评分内容不能为空");
        }
        ReviewAssignment assignment = requireOwnAssignment(assignmentId);
        ReviewObject object = requireObject(assignment.getObjectId());
        ReviewRound round = requireRound(assignment.getRoundId());
        ReviewRule rule = resolveRule(assignment.getActivityId(), round);
        List<ReviewCriteria> criteriaList = selectCriteria(rule);
        ReviewRecord existingRecord = selectLatestRecord(assignmentId);
        requireEditableReview(assignment, object, round, existingRecord, rule);

        Map<Long, ReviewCriteria> criteriaMap = criteriaList.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(ReviewCriteria::getId, item -> item, (a, b) -> a));
        validateRequiredCriteria(criteriaList, dto.getScoreDetails(), submit);
        List<ReviewScoreDetail> details = buildScoreDetails(dto.getScoreDetails(), criteriaMap, assignment, submit);
        BigDecimal totalScore = calculateTotalScore(rule, details);

        ReviewRecord record = existingRecord == null ? new ReviewRecord() : existingRecord;
        applyAssignmentSnapshot(record, assignment);
        record.setTotalScore(totalScore);
        record.setRecommendation(dto.getRecommendation());
        record.setCommentText(dto.getCommentText());
        if (submit) {
            record.setRecordStatus(ReviewRecordStatus.SUBMITTED.getCode());
            record.setSubmittedTime(DateUtils.getNowDate());
        } else {
            record.setRecordStatus(ReviewRecordStatus.DRAFT.getCode());
        }

        if (record.getId() == null) {
            fillCreate(record);
            recordMapper.insert(record);
        } else {
            fillUpdate(record);
            recordMapper.update(record);
            scoreDetailMapper.deleteByRecordId(record.getId(), currentUsername());
        }

        for (ReviewScoreDetail detail : details) {
            detail.setRecordId(record.getId());
            fillCreateBase(detail);
            scoreDetailMapper.insert(detail);
        }

        if (submit) {
            assignment.setStatus(ReviewAssignmentStatus.SUBMITTED.getCode());
            assignment.setSubmittedTime(DateUtils.getNowDate());
        } else {
            assignment.setStatus(ReviewAssignmentStatus.IN_PROGRESS.getCode());
        }
        fillUpdateBase(assignment);
        assignmentMapper.update(assignment);
        writeAudit(object, assignment, record, submit ? ACTION_SUBMIT : ACTION_SAVE_DRAFT,
                submit ? "专家提交评分" : "专家保存评分草稿");
        return record;
    }

    private ReviewMyReviewTaskVO buildTaskVO(ReviewAssignment assignment, ReviewObject object, Long currentObjectId) {
        ReviewActivity activity = activityMapper.selectById(assignment.getActivityId());
        ReviewRound round = roundMapper.selectById(assignment.getRoundId());
        ReviewRule rule = round == null ? null : resolveRule(assignment.getActivityId(), round);
        ReviewRecord record = selectLatestRecord(assignment.getId());
        ReviewMyReviewTaskVO vo = new ReviewMyReviewTaskVO();
        vo.setAssignmentId(assignment.getId());
        vo.setActivityId(assignment.getActivityId());
        vo.setActivityName(activity == null ? null : activity.getActivityName());
        vo.setRoundId(assignment.getRoundId());
        vo.setRoundName(round == null ? null : round.getRoundName());
        vo.setObjectId(assignment.getObjectId());
        vo.setAssignmentStatus(assignment.getStatus());
        vo.setCurrentObject(currentObjectId != null && currentObjectId.equals(assignment.getObjectId()));
        if (object != null) {
            vo.setObjectCode(object.getObjectCode());
            vo.setObjectName(object.getObjectName());
            vo.setObjectStatus(object.getSubmitStatus());
            vo.setOrgName(object.getOrgName());
            vo.setSummary(object.getSummary());
            vo.setSubjectCode1(object.getSubjectCode1());
            vo.setSubjectCode2(object.getSubjectCode2());
            vo.setSubjectCode3(object.getSubjectCode3());
            vo.setCategoryCodes(object.getCategoryCodes());
            vo.setKeywords(object.getKeywords());
        }
        if (record != null) {
            vo.setRecordId(record.getId());
            vo.setRecordStatus(record.getRecordStatus());
            vo.setTotalScore(record.getTotalScore());
            vo.setSubmittedTime(record.getSubmittedTime());
        }
        ReviewAccess access = reviewAccess(assignment, object, round, record, rule);
        vo.setCanReview(access.canReview);
        vo.setCannotReviewReason(access.reason);
        return vo;
    }

    private boolean matchesObjectFilter(ReviewObject object, String objectName, String objectCode, String keywords) {
        if (object == null) {
            return false;
        }
        if (StringUtils.isNotEmpty(objectName) && !contains(object.getObjectName(), objectName)) {
            return false;
        }
        if (StringUtils.isNotEmpty(objectCode) && !contains(object.getObjectCode(), objectCode)) {
            return false;
        }
        return StringUtils.isEmpty(keywords) || contains(object.getKeywords(), keywords);
    }

    private boolean contains(String value, String keyword) {
        return StringUtils.isNotEmpty(value) && value.contains(keyword);
    }

    private Long resolveCurrentObjectId(Long sessionId, Long activityId, Long roundId) {
        if (sessionId == null) {
            return null;
        }
        ReviewSession session = sessionMapper.selectById(sessionId);
        if (session == null
                || (activityId != null && !activityId.equals(session.getActivityId()))
                || (roundId != null && !roundId.equals(session.getRoundId()))) {
            return null;
        }
        return session.getCurrentObjectId();
    }

    private ReviewAssignment requireOwnAssignment(Long assignmentId) {
        if (assignmentId == null) {
            throw new ServiceException("评审任务ID不能为空");
        }
        ReviewAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new ServiceException("评审任务不存在");
        }
        Long userId = requireCurrentUserId();
        if (!Objects.equals(userId, assignment.getReviewerUserId())) {
            throw new ServiceException("只能访问分配给自己的评审任务");
        }
        return assignment;
    }

    private ReviewObject requireObject(Long objectId) {
        if (objectId == null) {
            throw new ServiceException("评审对象ID不能为空");
        }
        ReviewObject object = objectMapper.selectById(objectId);
        if (object == null) {
            throw new ServiceException("评审对象不存在");
        }
        return object;
    }

    private ReviewRound requireRound(Long roundId) {
        if (roundId == null) {
            throw new ServiceException("评审轮次ID不能为空");
        }
        ReviewRound round = roundMapper.selectById(roundId);
        if (round == null) {
            throw new ServiceException("评审轮次不存在");
        }
        return round;
    }

    private Long requireCurrentUserId() {
        Long userId = currentUserId();
        if (userId == null) {
            throw new ServiceException("无法获取当前登录用户");
        }
        return userId;
    }

    private ReviewRule resolveRule(Long activityId, ReviewRound round) {
        if (round != null && round.getRuleId() != null) {
            ReviewRule rule = ruleMapper.selectById(round.getRuleId());
            if (rule != null && ReviewConstants.YES.equals(rule.getEnabled())) {
                return rule;
            }
        }
        ReviewRule roundRuleQuery = new ReviewRule();
        roundRuleQuery.setActivityId(activityId);
        roundRuleQuery.setRoundId(round == null ? null : round.getId());
        roundRuleQuery.setEnabled(ReviewConstants.YES);
        List<ReviewRule> roundRules = ruleMapper.selectList(roundRuleQuery);
        if (roundRules != null && !roundRules.isEmpty()) {
            return roundRules.get(0);
        }
        ReviewRule activityRuleQuery = new ReviewRule();
        activityRuleQuery.setActivityId(activityId);
        activityRuleQuery.setEnabled(ReviewConstants.YES);
        List<ReviewRule> activityRules = ruleMapper.selectList(activityRuleQuery);
        if (activityRules == null) {
            return null;
        }
        for (ReviewRule rule : activityRules) {
            if (rule.getRoundId() == null) {
                return rule;
            }
        }
        return null;
    }

    private List<ReviewCriteria> selectCriteria(ReviewRule rule) {
        if (rule == null || rule.getId() == null) {
            return new ArrayList<>();
        }
        ReviewCriteria query = new ReviewCriteria();
        query.setRuleId(rule.getId());
        query.setEnabled(ReviewConstants.YES);
        List<ReviewCriteria> criteriaList = criteriaMapper.selectList(query);
        if (criteriaList == null) {
            return new ArrayList<>();
        }
        criteriaList.sort(Comparator.comparing(ReviewCriteria::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ReviewCriteria::getId, Comparator.nullsLast(Long::compareTo)));
        return criteriaList;
    }

    private List<ReviewObjectMember> selectMembers(Long objectId) {
        ReviewObjectMember query = new ReviewObjectMember();
        query.setObjectId(objectId);
        List<ReviewObjectMember> members = objectMemberMapper.selectList(query);
        return members == null ? new ArrayList<>() : members;
    }

    private List<ReviewObjectMaterial> selectReviewerVisibleMaterials(ReviewObject object) {
        if (object == null || object.getId() == null) {
            return new ArrayList<>();
        }
        List<ReviewObjectMaterial> materials = selectMaterials(object.getId());
        if ((materials == null || materials.isEmpty()) && isFileUploadManagerObject(object)) {
            syncMissingFileUploadMaterials(object);
            materials = selectMaterials(object.getId());
        }
        if (materials == null) {
            return new ArrayList<>();
        }
        return materials.stream()
                .filter(this::isReviewerVisibleMaterial)
                .filter(this::isNormalMaterial)
                .sorted(Comparator.comparing(ReviewObjectMaterial::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ReviewObjectMaterial::getId, Comparator.nullsLast(Long::compareTo)))
                .collect(Collectors.toList());
    }

    private List<ReviewObjectMaterial> selectMaterials(Long objectId) {
        ReviewObjectMaterial query = new ReviewObjectMaterial();
        query.setObjectId(objectId);
        List<ReviewObjectMaterial> materials = materialMapper.selectList(query);
        return materials == null ? new ArrayList<>() : materials;
    }

    private boolean isFileUploadManagerObject(ReviewObject object) {
        return SOURCE_MODULE_SYSTEM.equals(object.getSourceModule())
                && SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER.equals(object.getSourceBizType())
                && StringUtils.isNotEmpty(object.getSourceBizId());
    }

    private void syncMissingFileUploadMaterials(ReviewObject object) {
        Long sourceId = parseLong(object.getSourceBizId());
        if (sourceId == null) {
            return;
        }
        try {
            R<List<FileReviewImportSource>> response = remoteFileReviewImportService.listByIds(
                    Collections.singletonList(sourceId), SecurityConstants.INNER);
            if (response == null || R.isError(response) || response.getData() == null || response.getData().isEmpty()) {
                return;
            }
            FileReviewImportSource source = response.getData().get(0);
            if (source.getMaterials() == null || source.getMaterials().isEmpty()) {
                return;
            }
            int sortOrder = 1;
            for (FileReviewImportMaterial sourceMaterial : source.getMaterials()) {
                if (sourceMaterial == null || StringUtils.isEmpty(sourceMaterial.getDownloadLink())) {
                    continue;
                }
                ReviewObjectMaterial material = buildImportedMaterial(object, source, sourceMaterial, sortOrder++);
                fillCreateBase(material);
                materialMapper.insert(material);
            }
        } catch (Exception ex) {
            log.warn("文件任务导入材料兜底同步失败，objectId={}, sourceBizId={}",
                    object.getId(), object.getSourceBizId(), ex);
        }
    }

    private ReviewObjectMaterial buildImportedMaterial(ReviewObject object, FileReviewImportSource source,
                                                       FileReviewImportMaterial sourceMaterial, int sortOrder) {
        String fileName = firstNotEmpty(sourceMaterial.getFileName(), "文件任务材料-" + sortOrder);
        String fileExt = resolveFileExt(fileName);
        ReviewObjectMaterial material = new ReviewObjectMaterial();
        material.setActivityId(object.getActivityId());
        material.setObjectId(object.getId());
        material.setMaterialName(fileName);
        material.setMaterialType(resolveMaterialType(fileExt));
        material.setFileName(fileName);
        material.setFileUrl(sourceMaterial.getDownloadLink());
        material.setFileSize(sourceMaterial.getFileSize());
        material.setMimeType(sourceMaterial.getMimeType());
        material.setFileExt(fileExt);
        material.setVisibleToReviewer(ReviewConstants.YES);
        material.setSortOrder(sortOrder);
        material.setUploadBy(source.getUserId());
        material.setUploadTime(source.getUploadTime() == null ? DateUtils.getNowDate() : source.getUploadTime());
        material.setStatus(MATERIAL_STATUS_NORMAL);
        material.setSourceModule(SOURCE_MODULE_SYSTEM);
        material.setSourceBizType(SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER);
        material.setSourceBizId(object.getSourceBizId());
        material.setSourceMaterialKey(sourceMaterial.getDownloadLink());
        return material;
    }

    private String resolveFileExt(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }
        String cleanName = fileName;
        int queryIndex = cleanName.indexOf('?');
        if (queryIndex >= 0) {
            cleanName = cleanName.substring(0, queryIndex);
        }
        int slashIndex = Math.max(cleanName.lastIndexOf('/'), cleanName.lastIndexOf('\\'));
        if (slashIndex >= 0 && slashIndex + 1 < cleanName.length()) {
            cleanName = cleanName.substring(slashIndex + 1);
        }
        int dotIndex = cleanName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex + 1 >= cleanName.length()) {
            return null;
        }
        return cleanName.substring(dotIndex + 1).toLowerCase();
    }

    private String resolveMaterialType(String fileExt) {
        if (StringUtils.isEmpty(fileExt)) {
            return ReviewMaterialType.OTHER.getCode();
        }
        if ("pdf".equals(fileExt)) {
            return ReviewMaterialType.PDF.getCode();
        }
        if ("doc".equals(fileExt) || "docx".equals(fileExt)) {
            return ReviewMaterialType.DOC.getCode();
        }
        if ("ppt".equals(fileExt) || "pptx".equals(fileExt)) {
            return ReviewMaterialType.PPT.getCode();
        }
        if ("mp4".equals(fileExt) || "mov".equals(fileExt) || "avi".equals(fileExt)) {
            return ReviewMaterialType.VIDEO.getCode();
        }
        if ("jpg".equals(fileExt) || "jpeg".equals(fileExt) || "png".equals(fileExt) || "gif".equals(fileExt)) {
            return ReviewMaterialType.IMAGE.getCode();
        }
        if ("zip".equals(fileExt) || "rar".equals(fileExt) || "7z".equals(fileExt)) {
            return ReviewMaterialType.ZIP.getCode();
        }
        return ReviewMaterialType.OTHER.getCode();
    }

    private Long parseLong(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String firstNotEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.isNotEmpty(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean isReviewerVisibleMaterial(ReviewObjectMaterial material) {
        return material != null && (StringUtils.isEmpty(material.getVisibleToReviewer())
                || ReviewConstants.YES.equals(material.getVisibleToReviewer()));
    }

    private boolean isNormalMaterial(ReviewObjectMaterial material) {
        return material != null && (StringUtils.isEmpty(material.getStatus())
                || MATERIAL_STATUS_NORMAL.equals(material.getStatus()));
    }

    private ReviewRecord selectLatestRecord(Long assignmentId) {
        if (assignmentId == null) {
            return null;
        }
        ReviewRecord query = new ReviewRecord();
        query.setAssignmentId(assignmentId);
        List<ReviewRecord> records = recordMapper.selectList(query);
        if (records == null || records.isEmpty()) {
            return null;
        }
        records.sort(Comparator.comparing(ReviewRecord::getId, Comparator.nullsLast(Long::compareTo)).reversed());
        return records.get(0);
    }

    private List<ReviewScoreDetail> selectScoreDetails(Long recordId) {
        ReviewScoreDetail query = new ReviewScoreDetail();
        query.setRecordId(recordId);
        List<ReviewScoreDetail> details = scoreDetailMapper.selectList(query);
        if (details == null) {
            return new ArrayList<>();
        }
        details.sort(Comparator.comparing(ReviewScoreDetail::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ReviewScoreDetail::getId, Comparator.nullsLast(Long::compareTo)));
        return details;
    }

    private void requireEditableReview(ReviewAssignment assignment, ReviewObject object, ReviewRound round,
                                       ReviewRecord record, ReviewRule rule) {
        ReviewAccess access = reviewAccess(assignment, object, round, record, rule);
        if (!access.canReview) {
            throw new ServiceException(access.reason);
        }
    }

    private ReviewAccess reviewAccess(ReviewAssignment assignment, ReviewObject object, ReviewRound round,
                                      ReviewRecord record, ReviewRule rule) {
        if (assignment == null) {
            return ReviewAccess.deny("评审任务不存在");
        }
        if (object == null) {
            return ReviewAccess.deny("评审对象不存在");
        }
        if (ReviewObjectStatus.INVALID.getCode().equals(object.getSubmitStatus())) {
            return ReviewAccess.deny("评审对象已作废，禁止评分");
        }
        if (!isObjectStatusReviewable(object.getSubmitStatus(), round, assignment)) {
            return ReviewAccess.deny("当前评审对象状态不允许评分");
        }
        if (ReviewAssignmentStatus.SUBMITTED.getCode().equals(assignment.getStatus())) {
            return ReviewAccess.deny("评分已提交，不能再次编辑");
        }
        if (ReviewAssignmentStatus.LOCKED.getCode().equals(assignment.getStatus())
                || ReviewAssignmentStatus.CANCELLED.getCode().equals(assignment.getStatus())) {
            return ReviewAccess.deny("评审任务已锁定或取消，禁止评分");
        }
        if (!ReviewAssignmentStatus.ASSIGNED.getCode().equals(assignment.getStatus())
                && !ReviewAssignmentStatus.IN_PROGRESS.getCode().equals(assignment.getStatus())
                && !ReviewAssignmentStatus.RETURNED.getCode().equals(assignment.getStatus())) {
            return ReviewAccess.deny("当前评审任务状态不允许评分");
        }
        if (record != null && ReviewRecordStatus.SUBMITTED.getCode().equals(record.getRecordStatus())) {
            return ReviewAccess.deny("评分记录已提交，不能再次编辑");
        }
        if (rule == null || rule.getId() == null) {
            return ReviewAccess.deny("评分规则未配置，不能评分");
        }
        return ReviewAccess.allow();
    }

    private boolean isObjectStatusReviewable(String submitStatus, ReviewRound round, ReviewAssignment assignment) {
        if (ReviewObjectStatus.LOCKED.getCode().equals(submitStatus)
                || ReviewObjectStatus.REVIEWING.getCode().equals(submitStatus)) {
            return true;
        }
        if (!isOnsiteReview(round, assignment)) {
            return false;
        }
        // 现场评审允许未完成线上提交的对象先评分；作废、归档、已评审等终态仍不可评分。
        return ReviewObjectStatus.DRAFT.getCode().equals(submitStatus)
                || ReviewObjectStatus.SUBMITTED.getCode().equals(submitStatus);
    }

    private boolean isOnsiteReview(ReviewRound round, ReviewAssignment assignment) {
        String roundType = round == null ? null : round.getRoundType();
        if (ReviewRoundType.ONSITE_DEFENSE.getCode().equals(roundType)
                || ReviewRoundType.GROUP_REVIEW.getCode().equals(roundType)) {
            return true;
        }
        String assignmentType = assignment == null ? null : assignment.getAssignmentType();
        return "ONSITE".equals(assignmentType) || "GROUP".equals(assignmentType);
    }

    private void validateRequiredCriteria(List<ReviewCriteria> criteriaList, List<ReviewScoreDetailDTO> detailDTOList,
                                          boolean submit) {
        if (!submit) {
            return;
        }
        Map<Long, ReviewScoreDetailDTO> detailMap = new HashMap<>();
        if (detailDTOList != null) {
            for (ReviewScoreDetailDTO dto : detailDTOList) {
                if (dto != null && dto.getCriteriaId() != null) {
                    detailMap.put(dto.getCriteriaId(), dto);
                }
            }
        }
        for (ReviewCriteria criteria : criteriaList) {
            if (!ReviewConstants.YES.equals(criteria.getRequired())) {
                continue;
            }
            ReviewScoreDetailDTO dto = detailMap.get(criteria.getId());
            if (dto == null) {
                throw new ServiceException("必填评分项不能为空：" + criteria.getCriteriaName());
            }
            String scoreType = criteria.getScoreType();
            if (ReviewCriteriaType.NUMBER.getCode().equals(scoreType) && dto.getScoreValue() == null) {
                throw new ServiceException("必填评分项不能为空：" + criteria.getCriteriaName());
            }
            if (ReviewCriteriaType.SINGLE_CHOICE.getCode().equals(scoreType) && StringUtils.isEmpty(dto.getOptionValue())) {
                throw new ServiceException("必填评分项不能为空：" + criteria.getCriteriaName());
            }
            if (ReviewCriteriaType.TEXT.getCode().equals(scoreType) && StringUtils.isEmpty(dto.getTextValue())) {
                throw new ServiceException("必填评分项不能为空：" + criteria.getCriteriaName());
            }
        }
    }

    private List<ReviewScoreDetail> buildScoreDetails(List<ReviewScoreDetailDTO> dtoList,
                                                      Map<Long, ReviewCriteria> criteriaMap,
                                                      ReviewAssignment assignment,
                                                      boolean submit) {
        List<ReviewScoreDetail> details = new ArrayList<>();
        if (dtoList == null) {
            return details;
        }
        for (ReviewScoreDetailDTO dto : dtoList) {
            if (dto == null || dto.getCriteriaId() == null) {
                continue;
            }
            ReviewCriteria criteria = criteriaMap.get(dto.getCriteriaId());
            if (criteria == null) {
                throw new ServiceException("评分指标不存在或未启用：" + dto.getCriteriaId());
            }
            ReviewScoreDetail detail = new ReviewScoreDetail();
            detail.setActivityId(assignment.getActivityId());
            detail.setRoundId(assignment.getRoundId());
            detail.setObjectId(assignment.getObjectId());
            detail.setCriteriaId(criteria.getId());
            detail.setCriteriaName(criteria.getCriteriaName());
            detail.setScoreType(criteria.getScoreType());
            detail.setOptionValue(dto.getOptionValue());
            detail.setTextValue(dto.getTextValue());
            detail.setWeight(criteria.getWeight());
            detail.setSortOrder(criteria.getSortOrder());
            detail.setScoreValue(resolveScoreValue(criteria, dto, submit));
            details.add(detail);
        }
        return details;
    }

    private BigDecimal resolveScoreValue(ReviewCriteria criteria, ReviewScoreDetailDTO dto, boolean submit) {
        String scoreType = criteria.getScoreType();
        if (ReviewCriteriaType.NUMBER.getCode().equals(scoreType)) {
            BigDecimal scoreValue = dto.getScoreValue();
            if (scoreValue != null) {
                validateNumberRange(criteria, scoreValue);
            }
            return scoreValue;
        }
        if (ReviewCriteriaType.SINGLE_CHOICE.getCode().equals(scoreType)) {
            if (StringUtils.isEmpty(dto.getOptionValue())) {
                return null;
            }
            OptionScore optionScore = parseOptionScore(criteria.getOptionsJson(), dto.getOptionValue());
            if (submit && optionScore.optionsConfigured && !optionScore.matched) {
                throw new ServiceException("单选项取值无效：" + criteria.getCriteriaName());
            }
            return optionScore.score;
        }
        return null;
    }

    private void validateNumberRange(ReviewCriteria criteria, BigDecimal scoreValue) {
        if (criteria.getMinScore() != null && scoreValue.compareTo(criteria.getMinScore()) < 0) {
            throw new ServiceException(criteria.getCriteriaName() + "不能低于最低分" + criteria.getMinScore());
        }
        if (criteria.getMaxScore() != null && scoreValue.compareTo(criteria.getMaxScore()) > 0) {
            throw new ServiceException(criteria.getCriteriaName() + "不能高于最高分" + criteria.getMaxScore());
        }
    }

    private BigDecimal calculateTotalScore(ReviewRule rule, List<ReviewScoreDetail> details) {
        if (rule == null) {
            throw new ServiceException("评分规则未配置，不能评分");
        }
        if (details == null || details.isEmpty()) {
            return BigDecimal.ZERO;
        }
        String scoreMode = StringUtils.isEmpty(rule.getScoreMode()) ? ReviewRuleScoreMode.SUM.getCode() : rule.getScoreMode();
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (ReviewScoreDetail detail : details) {
            BigDecimal score = detail.getScoreValue();
            if (score == null) {
                continue;
            }
            count++;
            if (ReviewRuleScoreMode.WEIGHTED_SUM.getCode().equals(scoreMode)) {
                BigDecimal weight = detail.getWeight() == null ? BigDecimal.ZERO : detail.getWeight();
                total = total.add(score.multiply(weight).divide(HUNDRED, 4, RoundingMode.HALF_UP));
            } else {
                total = total.add(score);
            }
        }
        if (ReviewRuleScoreMode.AVERAGE.getCode().equals(scoreMode) && count > 0) {
            return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private OptionScore parseOptionScore(String optionsJson, String optionValue) {
        OptionScore result = new OptionScore();
        if (StringUtils.isEmpty(optionsJson)) {
            return result;
        }
        try {
            Object parsed = JSON.parse(optionsJson);
            if (parsed instanceof JSONArray) {
                JSONArray array = (JSONArray) parsed;
                result.optionsConfigured = !array.isEmpty();
                for (Object item : array) {
                    if (item instanceof JSONObject) {
                        JSONObject object = (JSONObject) item;
                        if (matchesOption(object, optionValue)) {
                            result.matched = true;
                            result.score = readScore(object);
                            return result;
                        }
                    } else if (Objects.equals(String.valueOf(item), optionValue)) {
                        result.matched = true;
                        return result;
                    }
                }
            } else if (parsed instanceof JSONObject) {
                JSONObject object = (JSONObject) parsed;
                result.optionsConfigured = !object.isEmpty();
                if (object.containsKey(optionValue)) {
                    result.matched = true;
                    Object value = object.get(optionValue);
                    result.score = scoreFromObject(value);
                    return result;
                }
                for (String key : object.keySet()) {
                    Object value = object.get(key);
                    if (value instanceof JSONObject && matchesOption((JSONObject) value, optionValue)) {
                        result.matched = true;
                        result.score = readScore((JSONObject) value);
                        return result;
                    }
                }
            }
        } catch (Exception ex) {
            result.optionsConfigured = false;
        }
        return result;
    }

    private boolean matchesOption(JSONObject object, String optionValue) {
        return Objects.equals(object.getString("value"), optionValue)
                || Objects.equals(object.getString("label"), optionValue)
                || Objects.equals(object.getString("name"), optionValue);
    }

    private BigDecimal readScore(JSONObject object) {
        Object score = object.get("score");
        if (score == null) {
            score = object.get("scoreValue");
        }
        return scoreFromObject(score);
    }

    private BigDecimal scoreFromObject(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof JSONObject) {
            return readScore((JSONObject) value);
        }
        if (value instanceof Number) {
            return new BigDecimal(String.valueOf(value));
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private void applyAssignmentSnapshot(ReviewRecord record, ReviewAssignment assignment) {
        record.setActivityId(assignment.getActivityId());
        record.setRoundId(assignment.getRoundId());
        record.setObjectId(assignment.getObjectId());
        record.setAssignmentId(assignment.getId());
        record.setReviewerId(assignment.getReviewerId());
        record.setReviewerUserId(assignment.getReviewerUserId());
    }

    private void writeAudit(ReviewObject object, ReviewAssignment assignment, ReviewRecord record,
                            String actionType, String content) {
        if (auditLogMapper == null) {
            return;
        }
        ReviewAuditLog log = new ReviewAuditLog();
        log.setActivityId(assignment.getActivityId());
        log.setRoundId(assignment.getRoundId());
        log.setObjectId(object.getId());
        log.setBizType(BIZ_TYPE_MY_REVIEW);
        log.setBizId(String.valueOf(record.getId()));
        log.setActionType(actionType);
        log.setActionContent(content);
        log.setOperatorUserId(currentUserId());
        log.setOperatorName(currentUsername());
        log.setOperateTime(DateUtils.getNowDate());
        fillCreateBase(log);
        auditLogMapper.insert(log);
    }

    private static class ReviewAccess {
        private final boolean canReview;
        private final String reason;

        private ReviewAccess(boolean canReview, String reason) {
            this.canReview = canReview;
            this.reason = reason;
        }

        private static ReviewAccess allow() {
            return new ReviewAccess(true, null);
        }

        private static ReviewAccess deny(String reason) {
            return new ReviewAccess(false, reason);
        }
    }

    private static class OptionScore {
        private boolean optionsConfigured;
        private boolean matched;
        private BigDecimal score;
    }
}
