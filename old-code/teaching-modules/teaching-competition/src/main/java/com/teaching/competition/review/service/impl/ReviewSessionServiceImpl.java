package com.teaching.competition.review.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewSession;
import com.teaching.competition.review.domain.ReviewSessionEventLog;
import com.teaching.competition.review.domain.ReviewSessionObject;
import com.teaching.competition.review.dto.ReviewSessionCurrentObjectDTO;
import com.teaching.competition.review.enums.ReviewCheckinStatus;
import com.teaching.competition.review.enums.ReviewEventType;
import com.teaching.competition.review.enums.ReviewObjectStatus;
import com.teaching.competition.review.enums.ReviewSessionObjectStatus;
import com.teaching.competition.review.enums.ReviewSessionStatus;
import com.teaching.competition.review.mapper.ReviewObjectMapper;
import com.teaching.competition.review.mapper.ReviewSessionEventLogMapper;
import com.teaching.competition.review.mapper.ReviewSessionMapper;
import com.teaching.competition.review.mapper.ReviewSessionObjectMapper;
import com.teaching.competition.review.service.IReviewSessionService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import com.teaching.competition.review.vo.ReviewSessionCurrentObjectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 现场评审场次表Service业务层处理。
 */
@Service
public class ReviewSessionServiceImpl extends AbstractReviewCrudService<ReviewSession> implements IReviewSessionService {
    @Autowired
    private ReviewSessionMapper mapper;

    @Autowired
    private ReviewObjectMapper objectMapper;

    @Autowired
    private ReviewSessionObjectMapper sessionObjectMapper;

    @Autowired
    private ReviewSessionEventLogMapper eventLogMapper;

    @Override
    protected ReviewCrudMapper<ReviewSession> mapper() {
        return mapper;
    }

    @Override
    public int insert(ReviewSession entity) {
        if (entity != null && StringUtils.isEmpty(entity.getStatus())) {
            entity.setStatus(ReviewSessionStatus.NOT_STARTED.getCode());
        }
        return super.insert(entity);
    }

    @Override
    public int update(ReviewSession entity) {
        if (entity == null || entity.getId() == null) {
            throw new ServiceException("更新场次ID不能为空");
        }
        ReviewSession existed = mapper.selectById(entity.getId());
        if (existed == null) {
            throw new ServiceException("现场评审场次不存在");
        }
        if (!isSessionCrudEditable(existed.getStatus())) {
            throw new ServiceException("现场评审场次已进行、结束或归档，不能通过基础接口修改");
        }
        if (entity.getCurrentObjectId() != null && !entity.getCurrentObjectId().equals(existed.getCurrentObjectId())) {
            throw new ServiceException("当前评审对象必须通过现场控制台或 current-object 专用接口切换");
        }
        if (StringUtils.isNotEmpty(entity.getStatus()) && !entity.getStatus().equals(existed.getStatus())) {
            throw new ServiceException("场次状态必须通过现场控制台专用流程变更，不能通过基础编辑接口直接修改");
        }
        entity.setActivityId(null);
        entity.setRoundId(null);
        entity.setCurrentObjectId(null);
        entity.setCurrentStartedTime(null);
        entity.setStatus(null);
        return super.update(entity);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("删除场次ID不能为空");
        }
        for (Long id : ids) {
            ReviewSession session = mapper.selectById(id);
            if (session == null) {
                throw new ServiceException("现场评审场次不存在：" + id);
            }
            if (!isSessionCrudEditable(session.getStatus())) {
                throw new ServiceException("现场评审场次已进行、结束或归档，不能通过基础接口删除");
            }
        }
        return super.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewSessionCurrentObjectVO setCurrentObject(Long sessionId, ReviewSessionCurrentObjectDTO dto) {
        if (sessionId == null || dto == null || dto.getObjectId() == null) {
            throw new ServiceException("场次ID和当前评审对象ID不能为空");
        }
        ReviewSession session = mapper.selectById(sessionId);
        if (session == null) {
            throw new ServiceException("现场评审场次不存在");
        }
        ReviewObject object = objectMapper.selectById(dto.getObjectId());
        if (object == null) {
            throw new ServiceException("评审对象不存在");
        }
        if (!session.getActivityId().equals(object.getActivityId())) {
            throw new ServiceException("场次与评审对象不属于同一评审活动");
        }
        if (ReviewObjectStatus.INVALID.getCode().equals(object.getSubmitStatus())) {
            throw new ServiceException("评审对象已作废，不能设置为当前现场对象");
        }

        ReviewSessionObject query = new ReviewSessionObject();
        query.setSessionId(sessionId);
        query.setObjectId(dto.getObjectId());
        List<ReviewSessionObject> existedList = sessionObjectMapper.selectList(query);
        if (existedList == null || existedList.isEmpty()) {
            throw new ServiceException("评审对象不属于当前现场场次");
        }
        ReviewSessionObject sessionObject = existedList.get(0);
        if (sessionObject.getActivityId() != null && !session.getActivityId().equals(sessionObject.getActivityId())) {
            throw new ServiceException("场次对象与当前场次不属于同一评审活动");
        }
        if (session.getRoundId() != null && sessionObject.getRoundId() != null
                && !session.getRoundId().equals(sessionObject.getRoundId())) {
            throw new ServiceException("场次对象与当前场次不属于同一评审轮次");
        }

        Date now = DateUtils.getNowDate();
        session.setCurrentObjectId(dto.getObjectId());
        session.setCurrentStartedTime(now);
        if (StringUtils.isEmpty(session.getStatus()) || ReviewSessionStatus.NOT_STARTED.getCode().equals(session.getStatus())) {
            session.setStatus(ReviewSessionStatus.IN_PROGRESS.getCode());
        }
        fillUpdateBase(session);
        mapper.update(session);

        sessionObject.setReviewStatus(ReviewSessionObjectStatus.REVIEWING.getCode());
        sessionObject.setActualStartTime(now);
        fillUpdateBase(sessionObject);
        sessionObjectMapper.update(sessionObject);

        ReviewSessionEventLog log = new ReviewSessionEventLog();
        log.setActivityId(session.getActivityId());
        log.setRoundId(session.getRoundId());
        log.setSessionId(sessionId);
        log.setObjectId(dto.getObjectId());
        log.setEventType(resolveEventType(dto.getSourceType()));
        log.setEventContent(buildEventContent(dto));
        log.setOperatorUserId(dto.getOperatorUserId());
        log.setEventTime(now);
        fillCreateBase(log);
        eventLogMapper.insert(log);

        // 业务约束：现场秘书扫码只切换当前对象，不改变 review_assignment，也不授予专家额外评分权限。
        return buildCurrentObjectVO(session, object);
    }

    @Override
    public ReviewSessionCurrentObjectVO getCurrentObject(Long sessionId) {
        if (sessionId == null) {
            throw new ServiceException("场次ID不能为空");
        }
        ReviewSession session = mapper.selectById(sessionId);
        if (session == null) {
            throw new ServiceException("现场评审场次不存在");
        }
        ReviewObject object = session.getCurrentObjectId() == null ? null : objectMapper.selectById(session.getCurrentObjectId());
        return buildCurrentObjectVO(session, object);
    }

    @Override
    public int insertSessionObject(ReviewSessionObject sessionObject) {
        if (sessionObject == null || sessionObject.getSessionId() == null || sessionObject.getObjectId() == null) {
            throw new ServiceException("场次ID和评审对象ID不能为空");
        }
        ReviewSession session = mapper.selectById(sessionObject.getSessionId());
        if (session == null) {
            throw new ServiceException("现场评审场次不存在");
        }
        if (!isSessionCrudEditable(session.getStatus())) {
            throw new ServiceException("现场评审场次已开始，不能通过基础接口新增场次对象");
        }
        ReviewObject object = objectMapper.selectById(sessionObject.getObjectId());
        if (object == null) {
            throw new ServiceException("评审对象不存在");
        }
        if (object.getActivityId() == null || !object.getActivityId().equals(session.getActivityId())) {
            throw new ServiceException("评审对象与场次不属于同一评审活动");
        }
        ReviewSessionObject duplicateQuery = new ReviewSessionObject();
        duplicateQuery.setSessionId(sessionObject.getSessionId());
        duplicateQuery.setObjectId(sessionObject.getObjectId());
        List<ReviewSessionObject> duplicates = sessionObjectMapper.selectList(duplicateQuery);
        if (duplicates != null && !duplicates.isEmpty()) {
            throw new ServiceException("该评审对象已加入当前现场场次");
        }
        if (sessionObject.getActivityId() == null) {
            sessionObject.setActivityId(session.getActivityId());
        }
        if (sessionObject.getRoundId() == null) {
            sessionObject.setRoundId(session.getRoundId());
        }
        if (sessionObject.getSequenceNo() == null) {
            sessionObject.setSequenceNo(nextSequenceNo(sessionObject.getSessionId()));
        }
        if (StringUtils.isEmpty(sessionObject.getCheckinStatus())) {
            sessionObject.setCheckinStatus(ReviewCheckinStatus.WAITING.getCode());
        }
        if (StringUtils.isEmpty(sessionObject.getReviewStatus())) {
            sessionObject.setReviewStatus(ReviewSessionObjectStatus.WAITING.getCode());
        }
        fillCreateBase(sessionObject);
        return sessionObjectMapper.insert(sessionObject);
    }

    @Override
    public int updateSessionObject(ReviewSessionObject sessionObject) {
        if (sessionObject == null || sessionObject.getId() == null) {
            throw new ServiceException("场次对象ID不能为空");
        }
        ReviewSessionObject existed = sessionObjectMapper.selectById(sessionObject.getId());
        if (existed == null) {
            throw new ServiceException("现场评审对象不存在");
        }
        ReviewSession session = mapper.selectById(existed.getSessionId());
        if (session == null) {
            throw new ServiceException("现场评审场次不存在");
        }
        if (!isSessionCrudEditable(session.getStatus())) {
            throw new ServiceException("现场评审场次已开始，不能通过管理端维护场次对象");
        }
        ReviewSessionObject update = new ReviewSessionObject();
        update.setId(existed.getId());
        update.setSequenceNo(sessionObject.getSequenceNo());
        update.setSecretaryNote(sessionObject.getSecretaryNote());
        fillUpdateBase(update);
        return sessionObjectMapper.update(update);
    }

    @Override
    public int deleteSessionObjectByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("场次对象ID不能为空");
        }
        for (Long id : ids) {
            ReviewSessionObject existed = sessionObjectMapper.selectById(id);
            if (existed == null) {
                throw new ServiceException("现场评审对象不存在：" + id);
            }
            ReviewSession session = mapper.selectById(existed.getSessionId());
            if (session == null) {
                throw new ServiceException("现场评审场次不存在");
            }
            if (!isSessionCrudEditable(session.getStatus())) {
                throw new ServiceException("现场评审场次已开始，不能通过管理端删除场次对象");
            }
        }
        return sessionObjectMapper.deleteByIds(ids, currentUsername());
    }

    private Integer nextSequenceNo(Long sessionId) {
        ReviewSessionObject query = new ReviewSessionObject();
        query.setSessionId(sessionId);
        List<ReviewSessionObject> list = sessionObjectMapper.selectList(query);
        int max = 0;
        if (list != null) {
            for (ReviewSessionObject item : list) {
                if (item.getSequenceNo() != null && item.getSequenceNo() > max) {
                    max = item.getSequenceNo();
                }
            }
        }
        return max + 1;
    }

    private boolean isSessionCrudEditable(String status) {
        return StringUtils.isEmpty(status)
                || ReviewSessionStatus.NOT_STARTED.getCode().equals(status)
                || ReviewSessionStatus.PAUSED.getCode().equals(status);
    }

    @Override
    public List<ReviewSessionObject> selectSessionObjectList(ReviewSessionObject query) {
        return sessionObjectMapper.selectList(query);
    }

    @Override
    public List<ReviewSessionEventLog> selectEventLogList(ReviewSessionEventLog query) {
        return eventLogMapper.selectList(query);
    }

    private String resolveEventType(String sourceType) {
        if ("SCAN".equalsIgnoreCase(sourceType)) {
            return ReviewEventType.SCAN_CERT.getCode();
        }
        if ("NEXT".equalsIgnoreCase(sourceType)) {
            return ReviewEventType.NEXT_OBJECT.getCode();
        }
        return ReviewEventType.SET_CURRENT.getCode();
    }

    private String buildEventContent(ReviewSessionCurrentObjectDTO dto) {
        StringBuilder builder = new StringBuilder();
        builder.append("sourceType=").append(dto.getSourceType());
        if (StringUtils.isNotEmpty(dto.getCertificateCode())) {
            builder.append(", certificateCode=").append(dto.getCertificateCode());
        }
        return builder.toString();
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
}
