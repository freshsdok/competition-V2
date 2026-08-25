package com.teaching.competition.review.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.review.domain.ReviewActivity;
import com.teaching.competition.review.domain.ReviewAssignment;
import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectMember;
import com.teaching.competition.review.domain.ReviewRecord;
import com.teaching.competition.review.domain.ReviewRound;
import com.teaching.competition.review.domain.ReviewSession;
import com.teaching.competition.review.domain.ReviewSessionEventLog;
import com.teaching.competition.review.domain.ReviewSessionObject;
import com.teaching.competition.review.dto.ReviewSecretarySessionObjectStatusDTO;
import com.teaching.competition.review.dto.ReviewSessionCurrentObjectDTO;
import com.teaching.competition.review.enums.ReviewAssignmentStatus;
import com.teaching.competition.review.enums.ReviewCheckinStatus;
import com.teaching.competition.review.enums.ReviewEventType;
import com.teaching.competition.review.enums.ReviewMemberRole;
import com.teaching.competition.review.enums.ReviewObjectStatus;
import com.teaching.competition.review.enums.ReviewRecordStatus;
import com.teaching.competition.review.enums.ReviewSessionObjectStatus;
import com.teaching.competition.review.enums.ReviewSessionStatus;
import com.teaching.competition.review.mapper.ReviewActivityMapper;
import com.teaching.competition.review.mapper.ReviewAssignmentMapper;
import com.teaching.competition.review.mapper.ReviewObjectMapper;
import com.teaching.competition.review.mapper.ReviewObjectMemberMapper;
import com.teaching.competition.review.mapper.ReviewRecordMapper;
import com.teaching.competition.review.mapper.ReviewRoundMapper;
import com.teaching.competition.review.mapper.ReviewSessionEventLogMapper;
import com.teaching.competition.review.mapper.ReviewSessionMapper;
import com.teaching.competition.review.mapper.ReviewSessionObjectMapper;
import com.teaching.competition.review.service.IReviewSecretaryService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import com.teaching.competition.review.vo.ReviewScoreProgressVO;
import com.teaching.competition.review.vo.ReviewSecretarySessionObjectVO;
import com.teaching.competition.review.vo.ReviewSecretarySessionVO;
import com.teaching.competition.review.vo.ReviewSessionCurrentObjectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 评审秘书移动端现场控制台Service业务层处理。
 */
@Service
public class ReviewSecretaryServiceImpl extends AbstractReviewCrudService<ReviewSession> implements IReviewSecretaryService {
    @Autowired
    private ReviewSessionMapper sessionMapper;

    @Autowired
    private ReviewSessionObjectMapper sessionObjectMapper;

    @Autowired
    private ReviewSessionEventLogMapper eventLogMapper;

    @Autowired
    private ReviewObjectMapper objectMapper;

    @Autowired
    private ReviewObjectMemberMapper objectMemberMapper;

    @Autowired
    private ReviewAssignmentMapper assignmentMapper;

    @Autowired
    private ReviewRecordMapper recordMapper;

    @Autowired
    private ReviewActivityMapper activityMapper;

    @Autowired
    private ReviewRoundMapper roundMapper;

    @Override
    protected ReviewCrudMapper<ReviewSession> mapper() {
        return sessionMapper;
    }

    @Override
    public List<ReviewSecretarySessionVO> listMySessions(ReviewSession query) {
        Long userId = currentUserId();
        if (userId == null) {
            throw new ServiceException("未获取到当前登录用户");
        }
        ReviewSession condition = query == null ? new ReviewSession() : query;
        condition.setSecretaryUserId(userId);
        List<ReviewSession> sessions = sessionMapper.selectList(condition);
        List<ReviewSecretarySessionVO> result = new ArrayList<>();
        if (sessions == null) {
            return result;
        }
        for (ReviewSession session : sessions) {
            ReviewObject currentObject = session.getCurrentObjectId() == null ? null : objectMapper.selectById(session.getCurrentObjectId());
            result.add(buildSessionVO(session, currentObject));
        }
        return result;
    }

    @Override
    public ReviewSecretarySessionVO getSessionDetail(Long sessionId) {
        ReviewSession session = getSessionAndCheckPermission(sessionId);
        ReviewObject currentObject = session.getCurrentObjectId() == null ? null : objectMapper.selectById(session.getCurrentObjectId());
        return buildSessionVO(session, currentObject);
    }

    @Override
    public List<ReviewSecretarySessionObjectVO> listSessionObjects(Long sessionId) {
        ReviewSession session = getSessionAndCheckPermission(sessionId);
        List<ReviewSessionObject> sessionObjects = listOrderedSessionObjects(sessionId);
        List<ReviewSecretarySessionObjectVO> result = new ArrayList<>();
        for (ReviewSessionObject sessionObject : sessionObjects) {
            result.add(buildSessionObjectVO(session, sessionObject));
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewSessionCurrentObjectVO setCurrentObject(Long sessionId, ReviewSessionCurrentObjectDTO dto) {
        if (dto == null || dto.getObjectId() == null) {
            throw new ServiceException("当前评审对象ID不能为空");
        }
        ReviewSession session = getSessionAndCheckPermission(sessionId);
        return doSetCurrentObject(session, dto.getObjectId(), dto.getSourceType(), dto.getCertificateCode(), ReviewEventType.SET_CURRENT.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewSessionCurrentObjectVO nextObject(Long sessionId) {
        ReviewSession session = getSessionAndCheckPermission(sessionId);
        List<ReviewSessionObject> sessionObjects = listOrderedSessionObjects(sessionId);
        if (sessionObjects.isEmpty()) {
            throw new ServiceException("当前场次暂无评审对象");
        }

        int currentIndex = -1;
        if (session.getCurrentObjectId() != null) {
            for (int i = 0; i < sessionObjects.size(); i++) {
                if (Objects.equals(sessionObjects.get(i).getObjectId(), session.getCurrentObjectId())) {
                    currentIndex = i;
                    break;
                }
            }
        }

        for (int i = currentIndex + 1; i < sessionObjects.size(); i++) {
            ReviewSessionObject candidate = sessionObjects.get(i);
            if (isAvailableForNext(candidate)) {
                return doSetCurrentObject(session, candidate.getObjectId(), "NEXT", null, ReviewEventType.NEXT_OBJECT.getCode());
            }
        }
        throw new ServiceException("没有可切换的下一位评审对象");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewSecretarySessionObjectVO updateSessionObjectStatus(Long sessionObjectId, ReviewSecretarySessionObjectStatusDTO dto) {
        if (sessionObjectId == null || dto == null) {
            throw new ServiceException("场次对象ID和状态不能为空");
        }
        ReviewSessionObject sessionObject = sessionObjectMapper.selectById(sessionObjectId);
        if (sessionObject == null) {
            throw new ServiceException("场次对象不存在");
        }
        ReviewSession session = getSessionAndCheckPermission(sessionObject.getSessionId());

        String beforeCheckin = sessionObject.getCheckinStatus();
        String beforeReview = sessionObject.getReviewStatus();
        String checkinStatus = normalizeStatus(dto.getCheckinStatus());
        String reviewStatus = normalizeStatus(dto.getReviewStatus());
        if (StringUtils.isEmpty(checkinStatus) && StringUtils.isEmpty(reviewStatus) && dto.getSecretaryNote() == null) {
            throw new ServiceException("请至少提交一个状态或备注");
        }
        if (StringUtils.isNotEmpty(checkinStatus)) {
            assertCheckinStatus(checkinStatus);
            sessionObject.setCheckinStatus(checkinStatus);
            if (ReviewCheckinStatus.ABSENT.getCode().equals(checkinStatus) && StringUtils.isEmpty(reviewStatus)) {
                reviewStatus = ReviewSessionObjectStatus.SKIPPED.getCode();
            }
        }
        if (StringUtils.isNotEmpty(reviewStatus)) {
            assertReviewStatus(reviewStatus);
            sessionObject.setReviewStatus(reviewStatus);
            if (ReviewSessionObjectStatus.COMPLETED.getCode().equals(reviewStatus) && sessionObject.getActualEndTime() == null) {
                sessionObject.setActualEndTime(DateUtils.getNowDate());
            }
        }
        if (dto.getSecretaryNote() != null) {
            sessionObject.setSecretaryNote(dto.getSecretaryNote());
        }
        fillUpdateBase(sessionObject);
        sessionObjectMapper.update(sessionObject);

        writeEventLog(session, sessionObject.getObjectId(), resolveStatusEventType(checkinStatus, reviewStatus),
                buildStatusEventContent(beforeCheckin, sessionObject.getCheckinStatus(), beforeReview, sessionObject.getReviewStatus(), dto.getSecretaryNote()), DateUtils.getNowDate());

        // 业务约束：秘书端到场、缺席、跳过、延后只更新现场顺序状态，不改变专家 assignment 和评分记录。
        return buildSessionObjectVO(session, sessionObject);
    }

    private ReviewSession getSessionAndCheckPermission(Long sessionId) {
        if (sessionId == null) {
            throw new ServiceException("场次ID不能为空");
        }
        ReviewSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new ServiceException("现场评审场次不存在");
        }
        requireSecretaryOrAdmin(session);
        return session;
    }

    protected void requireSecretaryOrAdmin(ReviewSession session) {
        Long userId = currentUserId();
        if (userId == null) {
            throw new ServiceException("未获取到当前登录用户");
        }
        if (SecurityUtils.isAdmin(userId)) {
            return;
        }
        if (!Objects.equals(userId, session.getSecretaryUserId())) {
            throw new ServiceException("无权操作该现场场次");
        }
    }

    private ReviewSessionCurrentObjectVO doSetCurrentObject(ReviewSession session, Long objectId, String sourceType,
                                                            String certificateCode, String defaultEventType) {
        ReviewObject object = validateCurrentObject(session, objectId);
        ReviewSessionObject sessionObject = findSessionObject(session.getId(), objectId);
        validateSessionObjectForCurrent(sessionObject);

        Date now = DateUtils.getNowDate();
        session.setCurrentObjectId(objectId);
        session.setCurrentStartedTime(now);
        if (StringUtils.isEmpty(session.getStatus()) || ReviewSessionStatus.NOT_STARTED.getCode().equals(session.getStatus())) {
            session.setStatus(ReviewSessionStatus.IN_PROGRESS.getCode());
        }
        fillUpdateBase(session);
        sessionMapper.update(session);

        sessionObject.setReviewStatus(ReviewSessionObjectStatus.REVIEWING.getCode());
        if (sessionObject.getActualStartTime() == null) {
            sessionObject.setActualStartTime(now);
        }
        fillUpdateBase(sessionObject);
        sessionObjectMapper.update(sessionObject);

        writeEventLog(session, objectId, resolveCurrentEventType(sourceType, defaultEventType),
                buildCurrentEventContent(sourceType, certificateCode), now);

        // 业务约束：现场秘书扫码/下一位只切换当前对象，不改变 review_assignment，也不授予专家额外评分权限。
        return buildCurrentObjectVO(session, object);
    }

    private ReviewObject validateCurrentObject(ReviewSession session, Long objectId) {
        ReviewObject object = objectMapper.selectById(objectId);
        if (object == null) {
            throw new ServiceException("评审对象不存在");
        }
        if (!Objects.equals(session.getActivityId(), object.getActivityId())) {
            throw new ServiceException("场次与评审对象不属于同一评审活动");
        }
        if (ReviewObjectStatus.INVALID.getCode().equals(object.getSubmitStatus())) {
            throw new ServiceException("评审对象已作废，不能设置为当前现场对象");
        }
        return object;
    }

    private ReviewSessionObject findSessionObject(Long sessionId, Long objectId) {
        ReviewSessionObject query = new ReviewSessionObject();
        query.setSessionId(sessionId);
        query.setObjectId(objectId);
        List<ReviewSessionObject> list = sessionObjectMapper.selectList(query);
        if (list == null || list.isEmpty()) {
            throw new ServiceException("评审对象不属于当前现场场次");
        }
        return list.get(0);
    }

    private void validateSessionObjectForCurrent(ReviewSessionObject sessionObject) {
        if (ReviewCheckinStatus.ABSENT.getCode().equals(sessionObject.getCheckinStatus())) {
            throw new ServiceException("该对象已标记缺席，不能设为当前对象");
        }
        if (ReviewSessionObjectStatus.SKIPPED.getCode().equals(sessionObject.getReviewStatus())) {
            throw new ServiceException("该对象已跳过，不能设为当前对象");
        }
    }

    private List<ReviewSessionObject> listOrderedSessionObjects(Long sessionId) {
        ReviewSessionObject query = new ReviewSessionObject();
        query.setSessionId(sessionId);
        List<ReviewSessionObject> list = sessionObjectMapper.selectList(query);
        if (list == null) {
            return new ArrayList<>();
        }
        list.sort(Comparator.comparing(ReviewSessionObject::getSequenceNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ReviewSessionObject::getId, Comparator.nullsLast(Long::compareTo)));
        return list;
    }

    private boolean isAvailableForNext(ReviewSessionObject sessionObject) {
        if (ReviewCheckinStatus.ABSENT.getCode().equals(sessionObject.getCheckinStatus())) {
            return false;
        }
        return !ReviewSessionObjectStatus.COMPLETED.getCode().equals(sessionObject.getReviewStatus())
                && !ReviewSessionObjectStatus.SKIPPED.getCode().equals(sessionObject.getReviewStatus());
    }

    private ReviewSecretarySessionVO buildSessionVO(ReviewSession session, ReviewObject currentObject) {
        ReviewSecretarySessionVO vo = new ReviewSecretarySessionVO();
        vo.setSessionId(session.getId());
        vo.setSessionName(session.getSessionName());
        vo.setSessionCode(session.getSessionCode());
        vo.setLocation(session.getLocation());
        vo.setStartTime(session.getStartTime());
        vo.setEndTime(session.getEndTime());
        vo.setActivityId(session.getActivityId());
        vo.setRoundId(session.getRoundId());
        vo.setStatus(session.getStatus());
        vo.setObjectCount(countSessionObjects(session.getId()));
        vo.setCurrentObjectId(session.getCurrentObjectId());
        vo.setCurrentStartedTime(session.getCurrentStartedTime());
        if (currentObject != null) {
            vo.setCurrentObjectCode(currentObject.getObjectCode());
            vo.setCurrentObjectName(currentObject.getObjectName());
        }
        ReviewActivity activity = session.getActivityId() == null ? null : activityMapper.selectById(session.getActivityId());
        if (activity != null) {
            vo.setActivityName(activity.getActivityName());
        }
        ReviewRound round = session.getRoundId() == null ? null : roundMapper.selectById(session.getRoundId());
        if (round != null) {
            vo.setRoundName(round.getRoundName());
        }
        return vo;
    }

    private int countSessionObjects(Long sessionId) {
        if (sessionId == null) {
            return 0;
        }
        ReviewSessionObject query = new ReviewSessionObject();
        query.setSessionId(sessionId);
        List<ReviewSessionObject> list = sessionObjectMapper.selectList(query);
        return list == null ? 0 : list.size();
    }

    private ReviewSecretarySessionObjectVO buildSessionObjectVO(ReviewSession session, ReviewSessionObject sessionObject) {
        ReviewObject object = objectMapper.selectById(sessionObject.getObjectId());
        ReviewSecretarySessionObjectVO vo = new ReviewSecretarySessionObjectVO();
        vo.setSessionObjectId(sessionObject.getId());
        vo.setSequenceNo(sessionObject.getSequenceNo());
        vo.setObjectId(sessionObject.getObjectId());
        vo.setCheckinStatus(sessionObject.getCheckinStatus());
        vo.setReviewStatus(sessionObject.getReviewStatus());
        vo.setActualStartTime(sessionObject.getActualStartTime());
        vo.setActualEndTime(sessionObject.getActualEndTime());
        vo.setSecretaryNote(sessionObject.getSecretaryNote());
        if (object != null) {
            vo.setObjectCode(object.getObjectCode());
            vo.setObjectName(object.getObjectName());
            vo.setOrgName(object.getOrgName());
        }
        vo.setLeaderName(resolveLeaderName(session.getActivityId(), sessionObject.getObjectId()));
        vo.setScoreProgress(resolveScoreProgress(session.getActivityId(), session.getRoundId(), sessionObject.getObjectId()));
        return vo;
    }

    private String resolveLeaderName(Long activityId, Long objectId) {
        ReviewObjectMember query = new ReviewObjectMember();
        query.setActivityId(activityId);
        query.setObjectId(objectId);
        List<ReviewObjectMember> members = objectMemberMapper.selectList(query);
        if (members == null || members.isEmpty()) {
            return null;
        }
        for (ReviewObjectMember member : members) {
            if (ReviewMemberRole.LEADER.getCode().equals(member.getMemberRole()) || "Y".equalsIgnoreCase(member.getIsPrimary())) {
                return member.getMemberName();
            }
        }
        return members.get(0).getMemberName();
    }

    private ReviewScoreProgressVO resolveScoreProgress(Long activityId, Long roundId, Long objectId) {
        ReviewAssignment query = new ReviewAssignment();
        query.setActivityId(activityId);
        query.setRoundId(roundId);
        query.setObjectId(objectId);
        List<ReviewAssignment> assignments = assignmentMapper.selectList(query);
        int total = assignments == null ? 0 : assignments.size();
        int submitted = 0;
        if (assignments != null) {
            for (ReviewAssignment assignment : assignments) {
                if (ReviewAssignmentStatus.SUBMITTED.getCode().equals(assignment.getStatus()) || hasSubmittedRecord(assignment.getId())) {
                    submitted++;
                }
            }
        }
        ReviewScoreProgressVO progress = new ReviewScoreProgressVO();
        progress.setTotalAssignedCount(total);
        progress.setSubmittedCount(submitted);
        progress.setUnsubmittedCount(Math.max(total - submitted, 0));
        progress.setDisplayText(submitted + "/" + total);
        return progress;
    }

    private boolean hasSubmittedRecord(Long assignmentId) {
        if (assignmentId == null) {
            return false;
        }
        ReviewRecord query = new ReviewRecord();
        query.setAssignmentId(assignmentId);
        List<ReviewRecord> records = recordMapper.selectList(query);
        if (records == null) {
            return false;
        }
        for (ReviewRecord record : records) {
            if (ReviewRecordStatus.SUBMITTED.getCode().equals(record.getRecordStatus())) {
                return true;
            }
        }
        return false;
    }

    private ReviewSessionCurrentObjectVO buildCurrentObjectVO(ReviewSession session, ReviewObject object) {
        ReviewSessionCurrentObjectVO vo = new ReviewSessionCurrentObjectVO();
        vo.setSessionId(session.getId());
        vo.setActivityId(session.getActivityId());
        vo.setRoundId(session.getRoundId());
        vo.setObjectId(session.getCurrentObjectId());
        vo.setCurrentStartedTime(session.getCurrentStartedTime());
        vo.setStatus(session.getStatus());
        if (object != null) {
            vo.setObjectCode(object.getObjectCode());
            vo.setObjectName(object.getObjectName());
        }
        return vo;
    }

    private String normalizeStatus(String status) {
        return StringUtils.isEmpty(status) ? null : status.trim().toUpperCase();
    }

    private void assertCheckinStatus(String status) {
        for (ReviewCheckinStatus item : ReviewCheckinStatus.values()) {
            if (item.getCode().equals(status)) {
                return;
            }
        }
        throw new ServiceException("未知的到场状态：" + status);
    }

    private void assertReviewStatus(String status) {
        for (ReviewSessionObjectStatus item : ReviewSessionObjectStatus.values()) {
            if (item.getCode().equals(status)) {
                return;
            }
        }
        throw new ServiceException("未知的评审状态：" + status);
    }

    private String resolveStatusEventType(String checkinStatus, String reviewStatus) {
        if (ReviewCheckinStatus.ABSENT.getCode().equals(checkinStatus)) {
            return ReviewEventType.ABSENT.getCode();
        }
        if (ReviewCheckinStatus.PRESENT.getCode().equals(checkinStatus) || ReviewCheckinStatus.LATE.getCode().equals(checkinStatus)) {
            return ReviewEventType.PRESENT.getCode();
        }
        if (ReviewSessionObjectStatus.SKIPPED.getCode().equals(reviewStatus)) {
            return ReviewEventType.SKIP.getCode();
        }
        if (ReviewSessionObjectStatus.DELAYED.getCode().equals(reviewStatus)) {
            return ReviewEventType.DELAY.getCode();
        }
        return ReviewEventType.SET_CURRENT.getCode();
    }

    private String resolveCurrentEventType(String sourceType, String defaultEventType) {
        if ("SCAN".equalsIgnoreCase(sourceType)) {
            return ReviewEventType.SCAN_CERT.getCode();
        }
        if ("NEXT".equalsIgnoreCase(sourceType)) {
            return ReviewEventType.NEXT_OBJECT.getCode();
        }
        if ("MANUAL".equalsIgnoreCase(sourceType)) {
            return ReviewEventType.SET_CURRENT.getCode();
        }
        return defaultEventType;
    }

    private String buildCurrentEventContent(String sourceType, String certificateCode) {
        StringBuilder builder = new StringBuilder();
        builder.append("sourceType=").append(StringUtils.isEmpty(sourceType) ? "MANUAL" : sourceType);
        if (StringUtils.isNotEmpty(certificateCode)) {
            builder.append(", certificateCode=").append(certificateCode);
        }
        return builder.toString();
    }

    private String buildStatusEventContent(String beforeCheckin, String afterCheckin, String beforeReview, String afterReview, String note) {
        Map<String, Object> content = new HashMap<>();
        content.put("beforeCheckinStatus", beforeCheckin);
        content.put("afterCheckinStatus", afterCheckin);
        content.put("beforeReviewStatus", beforeReview);
        content.put("afterReviewStatus", afterReview);
        if (note != null) {
            content.put("secretaryNote", note);
        }
        return content.toString();
    }

    private void writeEventLog(ReviewSession session, Long objectId, String eventType, String eventContent, Date eventTime) {
        ReviewSessionEventLog log = new ReviewSessionEventLog();
        log.setActivityId(session.getActivityId());
        log.setRoundId(session.getRoundId());
        log.setSessionId(session.getId());
        log.setObjectId(objectId);
        log.setEventType(eventType);
        log.setEventContent(eventContent);
        log.setOperatorUserId(currentUserId());
        log.setEventTime(eventTime);
        fillCreateBase(log);
        eventLogMapper.insert(log);
    }
}
