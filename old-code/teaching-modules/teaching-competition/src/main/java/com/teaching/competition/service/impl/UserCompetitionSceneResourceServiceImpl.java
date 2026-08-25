package com.teaching.competition.service.impl;

import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.DictConstant;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.contant.CompetitionSceneResourceConstants;
import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionSceneReservationSubject;
import com.teaching.competition.domain.CompetitionSceneResourceBookableQuery;
import com.teaching.competition.domain.CompetitionSceneResourceBookableVO;
import com.teaching.competition.domain.CompetitionSceneResourceReservation;
import com.teaching.competition.domain.CompetitionSceneResourceReservationCancelReq;
import com.teaching.competition.domain.CompetitionSceneResourceReservationReq;
import com.teaching.competition.domain.CompetitionSceneResourceReservationVO;
import com.teaching.competition.domain.CompetitionSceneResourceSlot;
import com.teaching.competition.domain.CompetitionSceneResourceSlotGroupScope;
import com.teaching.competition.domain.CompetitionSceneResourceSlotQuery;
import com.teaching.competition.domain.CompetitionSceneResourceSlotVO;
import com.teaching.competition.domain.CompetitionSceneResourceVO;
import com.teaching.competition.domain.CompetitionSceneScheduleResource;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceQuery;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceVO;
import com.teaching.competition.domain.CompetitionSceneScheduleTarget;
import com.teaching.competition.exception.CompetitionSceneReservationException;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.mapper.CompetitionSceneCredentialMapper;
import com.teaching.competition.mapper.CompetitionSceneResourceMapper;
import com.teaching.competition.mapper.CompetitionSceneResourceReservationMapper;
import com.teaching.competition.mapper.CompetitionSceneResourceScheduleScopeMapper;
import com.teaching.competition.mapper.CompetitionSceneResourceSlotGroupScopeMapper;
import com.teaching.competition.mapper.CompetitionSceneResourceSlotMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleResourceMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleTargetMapper;
import com.teaching.competition.service.IUserCompetitionSceneResourceService;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 用户端大赛现场设备资源预约服务实现。
 */
@Service
public class UserCompetitionSceneResourceServiceImpl implements IUserCompetitionSceneResourceService {

    @Autowired
    private CompetitionSceneScheduleResourceMapper scheduleResourceMapper;

    @Autowired
    private CompetitionSceneResourceMapper resourceMapper;

    @Autowired
    private CompetitionSceneResourceSlotMapper slotMapper;

    @Autowired
    private CompetitionSceneResourceReservationMapper reservationMapper;

    @Autowired
    private CompetitionSceneResourceScheduleScopeMapper scheduleScopeMapper;

    @Autowired
    private CompetitionSceneResourceSlotGroupScopeMapper slotGroupScopeMapper;

    @Autowired
    private CompetitionSceneScheduleTargetMapper targetMapper;

    @Autowired
    private CompetitionSceneCredentialMapper credentialMapper;

    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired(required = false)
    private PlatformTransactionManager transactionManager;

    @Override
    public List<CompetitionSceneResourceBookableVO> selectBookableResourceList(Long userId,
                                                                               CompetitionSceneResourceBookableQuery query) {
        Date now = DateUtils.getNowDate();
        CompetitionSceneScheduleResourceQuery resourceQuery = new CompetitionSceneScheduleResourceQuery();
        if (query != null) {
            resourceQuery.setScheduleResourceId(query.getScheduleResourceId());
            resourceQuery.setResourceId(query.getResourceId());
        }
        resourceQuery.setBookingStatus(CompetitionSceneResourceConstants.BOOKING_STATUS_OPEN);
        List<CompetitionSceneScheduleResourceVO> resources =
                scheduleResourceMapper.selectCompetitionSceneScheduleResourceList(resourceQuery);
        List<CompetitionSceneReservationSubject> subjects = resolveAllSubjects(userId);
        if (query != null && query.getScheduleId() != null) {
            subjects = subjects.stream()
                    .filter(subject -> Objects.equals(subject.getScheduleId(), query.getScheduleId()))
                    .collect(Collectors.toList());
        }
        List<CompetitionSceneResourceBookableVO> result = new ArrayList<>();
        for (CompetitionSceneScheduleResourceVO scheduleResource : resources) {
            if (!isBookingWindowOpen(scheduleResource, now)) {
                continue;
            }
            CompetitionSceneReservationSubject subject =
                    findScopedSubject(scheduleResource.getScheduleResourceId(), subjects);
            if (subject == null) {
                continue;
            }
            CompetitionSceneResourceBookableVO vo = buildBookableVO(scheduleResource, subject, now);
            if (vo.getNextSlotId() != null || vo.getExistingReservation() != null) {
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public CompetitionSceneResourceBookableVO selectBookableResourceById(Long userId, Long scheduleResourceId) {
        CompetitionSceneScheduleResourceVO scheduleResource = loadScheduleResourceVO(scheduleResourceId);
        Date now = DateUtils.getNowDate();
        ensureResourceOpen(scheduleResource, now);
        CompetitionSceneReservationSubject subject = requireScopedSubject(scheduleResourceId, userId);
        return buildBookableVO(scheduleResource, subject, now);
    }

    @Override
    public List<CompetitionSceneResourceSlotVO> selectBookableSlotList(Long userId, Long scheduleResourceId) {
        CompetitionSceneScheduleResourceVO scheduleResource = loadScheduleResourceVO(scheduleResourceId);
        Date now = DateUtils.getNowDate();
        ensureResourceOpen(scheduleResource, now);
        CompetitionSceneReservationSubject subject = requireScopedSubject(scheduleResourceId, userId);
        return selectOpenSlots(scheduleResourceId, now, subject.getGroupCode(), scheduleResource.getSharedOccupancy());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneResourceReservationVO submitReservation(Long userId,
                                                                   CompetitionSceneResourceReservationReq req) {
        if (userId == null) {
            throw business(CompetitionSceneResourceConstants.ERROR_NOT_SCHEDULE_TARGET, "登录用户无效");
        }
        if (req == null || req.getSlotId() == null) {
            throw business(CompetitionSceneResourceConstants.ERROR_SLOT_NOT_OPEN, "预约时段不能为空");
        }
        checkIdempotencyKeyRequired(req.getIdempotencyKey());
        String idempotencyKey = trim(req.getIdempotencyKey());
        Date now = DateUtils.getNowDate();
        CompetitionSceneResourceReservationVO idempotent =
                reservationMapper.selectReservationByIdempotencyKey(idempotencyKey);
        if (idempotent != null) {
            fillExpired(idempotent, now);
            return idempotent;
        }

        CompetitionSceneResourceSlot slot = slotMapper.selectCompetitionSceneResourceSlotEntityById(req.getSlotId());
        if (slot == null || slot.getStartTime() == null || !slot.getStartTime().after(now)
                || !CompetitionSceneResourceConstants.SLOT_STATUS_OPEN.equals(slot.getSlotStatus())) {
            throw business(CompetitionSceneResourceConstants.ERROR_SLOT_NOT_OPEN, "预约时段未开放");
        }
        CompetitionSceneScheduleResource scheduleResource =
                scheduleResourceMapper.selectCompetitionSceneScheduleResourceEntityById(slot.getScheduleResourceId());
        if (scheduleResource == null) {
            throw business(CompetitionSceneResourceConstants.ERROR_RESOURCE_NOT_OPEN, "赛场资源布置不存在");
        }
        ensureResourceOpen(scheduleResource, now);
        if (!Objects.equals(slot.getScheduleResourceId(), scheduleResource.getScheduleResourceId())) {
            throw business(CompetitionSceneResourceConstants.ERROR_SLOT_NOT_OPEN, "预约时段与资源不匹配");
        }
        CompetitionSceneReservationSubject subject = requireScopedSubject(scheduleResource.getScheduleResourceId(), userId);
        if (!checkSlotGroupAllowed(slot.getSlotId(), subject.getGroupCode())) {
            throw business(CompetitionSceneResourceConstants.ERROR_SLOT_GROUP_DENIED, "当前组别不可预约该时段");
        }

        int occupyPeopleCount = calculateOccupyPeopleCount(subject);
        ReservationCapacitySnapshot capacitySnapshot = calculateCapacitySnapshot(occupyPeopleCount,
                resolveWorkstationCount(slot, scheduleResource), scheduleResource.getSharedOccupancy());
        String activeReservationKey = buildActiveReservationKey(subject.getCompetitionSeriesId(),
                subject.getSubjectType(), subject.getSubjectCode());
        CompetitionSceneResourceReservationVO existing =
                reservationMapper.selectEffectiveReservationByActiveKey(activeReservationKey);
        if (existing != null) {
            fillExpired(existing, now);
            throw business(CompetitionSceneResourceConstants.ERROR_ALREADY_RESERVED,
                    "当前参赛主体已有有效资源预约", existing);
        }

        CompetitionSceneResourceReservation reservation = new CompetitionSceneResourceReservation();
        reservation.setSlotId(slot.getSlotId());
        reservation.setScheduleResourceId(scheduleResource.getScheduleResourceId());
        reservation.setScheduleId(scheduleResource.getScheduleId());
        reservation.setResourceId(scheduleResource.getResourceId());
        reservation.setEventId(scheduleResource.getEventId());
        reservation.setCompetitionSeriesId(subject.getCompetitionSeriesId());
        reservation.setReservationSourceScheduleId(subject.getScheduleId());
        reservation.setSubjectType(subject.getSubjectType());
        reservation.setSubjectCode(subject.getSubjectCode());
        reservation.setTeamCode(subject.getTeamCode());
        reservation.setUserId(subject.getUserId());
        reservation.setOperatorUserId(userId);
        reservation.setOperatorName(currentUsername());
        reservation.setGroupCode(subject.getGroupCode());
        reservation.setGroupName(subject.getGroupName());
        reservation.setOccupyPeopleCount(capacitySnapshot.getOccupyPeopleCount());
        reservation.setReservedDeviceCount(capacitySnapshot.getReservedDeviceCount());
        reservation.setReservedWorkstationCount(capacitySnapshot.getReservedWorkstationCount());
        reservation.setCoveredWorkstationCount(capacitySnapshot.getReservedWorkstationCount());
        reservation.setSharedOccupancySnapshot(capacitySnapshot.getSharedOccupancySnapshot());
        reservation.setWorkstationCountSnapshot(capacitySnapshot.getWorkstationCountSnapshot());
        reservation.setActiveReservationKey(activeReservationKey);
        reservation.setReservationStatus(CompetitionSceneResourceConstants.RESERVATION_STATUS_RESERVED);
        reservation.setCheckStatus(CompetitionSceneResourceConstants.CHECK_STATUS_UNCHECKED);
        reservation.setIdempotencyKey(idempotencyKey);
        reservation.setCreateBy(currentUsername());
        reservation.setUpdateBy(currentUsername());
        reservation.setCreateTime(now);
        reservation.setUpdateTime(now);
        reservation.setDeleted(CompetitionSceneResourceConstants.DELETED_NO);
        CompetitionSceneResourceReservationVO duplicateReservation =
                tryInsertReservationRecord(reservation, idempotencyKey, activeReservationKey, now);
        if (duplicateReservation != null) {
            return duplicateReservation;
        }

        int updated = reserveSlotCapacity(slot.getSlotId(), capacitySnapshot, now);
        if (updated <= 0) {
            throw business(CompetitionSceneResourceConstants.ERROR_CAPACITY_NOT_ENOUGH, "剩余容量不足或时段不可预约");
        }
        CompetitionSceneResourceReservationVO vo =
                reservationMapper.selectCompetitionSceneResourceReservationById(reservation.getReservationId());
        fillExpired(vo, now);
        return vo;
    }

    @Override
    public List<CompetitionSceneResourceReservationVO> selectMyReservationList(Long userId) {
        List<CompetitionSceneReservationSubject> subjects = resolveAllSubjects(userId);
        List<CompetitionSceneResourceReservationVO> list =
                reservationMapper.selectVisibleCompetitionSceneResourceReservationList(subjects);
        Date now = DateUtils.getNowDate();
        list.forEach(item -> fillExpired(item, now));
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneResourceReservationVO cancelReservation(Long userId,
                                                                   CompetitionSceneResourceReservationCancelReq req) {
        if (req == null || req.getReservationId() == null) {
            throw business(CompetitionSceneResourceConstants.ERROR_RESERVATION_NOT_CANCELABLE, "预约记录不能为空");
        }
        Date now = DateUtils.getNowDate();
        CompetitionSceneResourceReservation reservation =
                reservationMapper.selectCompetitionSceneResourceReservationEntityById(req.getReservationId());
        if (reservation == null) {
            throw business(CompetitionSceneResourceConstants.ERROR_RESERVATION_NOT_CANCELABLE, "预约记录不存在");
        }
        Long subjectScheduleId = reservation.getReservationSourceScheduleId() != null
                ? reservation.getReservationSourceScheduleId() : reservation.getScheduleId();
        CompetitionSceneReservationSubject subject = requireSubject(subjectScheduleId, userId);
        if (!Objects.equals(subject.getSubjectType(), reservation.getSubjectType())
                || !Objects.equals(subject.getSubjectCode(), reservation.getSubjectCode())) {
            throw business(CompetitionSceneResourceConstants.ERROR_SUBJECT_MEMBER_INVALID, "当前用户不是该预约主体有效成员");
        }
        if (!CompetitionSceneResourceConstants.RESERVATION_STATUS_RESERVED.equals(reservation.getReservationStatus())) {
            CompetitionSceneResourceReservationVO current =
                    reservationMapper.selectCompetitionSceneResourceReservationById(reservation.getReservationId());
            fillExpired(current, now);
            return current;
        }
        CompetitionSceneResourceSlot slot = slotMapper.selectCompetitionSceneResourceSlotEntityById(reservation.getSlotId());
        if (slot == null || slot.getStartTime() == null || !slot.getStartTime().after(now)) {
            throw business(CompetitionSceneResourceConstants.ERROR_RESERVATION_NOT_CANCELABLE, "预约时段已开始，不能取消");
        }
        int cancelled = reservationMapper.cancelCompetitionSceneResourceReservation(reservation.getReservationId(),
                req.getCancelReason(), currentUsername());
        if (cancelled <= 0) {
            CompetitionSceneResourceReservationVO current =
                    reservationMapper.selectCompetitionSceneResourceReservationById(reservation.getReservationId());
            fillExpired(current, now);
            return current;
        }
        releaseSlotCapacity(reservation);
        CompetitionSceneResourceReservationVO vo =
                reservationMapper.selectCompetitionSceneResourceReservationById(reservation.getReservationId());
        fillExpired(vo, now);
        return vo;
    }

    private CompetitionSceneResourceBookableVO buildBookableVO(CompetitionSceneScheduleResourceVO scheduleResource,
                                                               CompetitionSceneReservationSubject subject,
                                                               Date now) {
        CompetitionSceneResourceBookableVO vo = new CompetitionSceneResourceBookableVO();
        vo.setScheduleResourceId(scheduleResource.getScheduleResourceId());
        vo.setScheduleId(scheduleResource.getScheduleId());
        vo.setResourceId(scheduleResource.getResourceId());
        vo.setEventId(scheduleResource.getEventId());
        vo.setCompetitionSeriesId(subject.getCompetitionSeriesId());
        vo.setUserSourceScheduleId(subject.getScheduleId());
        vo.setScheduleName(scheduleResource.getScheduleName());
        vo.setCompetitionName(scheduleResource.getCompetitionName());
        vo.setResourceCode(scheduleResource.getResourceCode());
        vo.setResourceName(scheduleResource.getResourceName());
        vo.setResourceType(scheduleResource.getResourceType());
        vo.setBrandModel(scheduleResource.getBrandModel());
        vo.setDeploymentLocation(scheduleResource.getDeploymentLocation());
        vo.setDeployedDeviceCount(scheduleResource.getDeployedDeviceCount());
        vo.setWorkstationsPerDevice(scheduleResource.getWorkstationsPerDevice());
        vo.setTotalWorkstations(scheduleResource.getTotalWorkstations());
        vo.setSlotDurationMinutes(scheduleResource.getSlotDurationMinutes());
        vo.setSharedOccupancy(scheduleResource.getSharedOccupancy());
        vo.setNeedOpsConfirm(scheduleResource.getNeedOpsConfirm());
        vo.setOpsContactName(scheduleResource.getOpsContactName());
        vo.setOpsContactPhone(scheduleResource.getOpsContactPhone());
        vo.setBookingStatus(scheduleResource.getBookingStatus());
        vo.setBookingOpenTime(scheduleResource.getBookingOpenTime());
        vo.setBookingCloseTime(scheduleResource.getBookingCloseTime());
        vo.setSafetyNotice(scheduleResource.getSafetyNoticeOverride());
        vo.setAttentionNotes(scheduleResource.getAttentionNotesOverride());
        vo.setUsageInstructions(scheduleResource.getUsageInstructionsOverride());
        CompetitionSceneResourceVO resource = resourceMapper.selectCompetitionSceneResourceById(scheduleResource.getResourceId());
        if (resource != null) {
            vo.setParameterJson(resource.getParameterJson());
            vo.setImageUrls(resource.getImageUrls());
            if (StringUtils.isEmpty(vo.getSafetyNotice())) {
                vo.setSafetyNotice(resource.getSafetyNotice());
            }
            if (StringUtils.isEmpty(vo.getAttentionNotes())) {
                vo.setAttentionNotes(resource.getAttentionNotes());
            }
            if (StringUtils.isEmpty(vo.getUsageInstructions())) {
                vo.setUsageInstructions(resource.getUsageInstructions());
            }
        }
        List<CompetitionSceneResourceSlotVO> slots = selectOpenSlots(scheduleResource.getScheduleResourceId(), now,
                subject.getGroupCode(), scheduleResource.getSharedOccupancy());
        CompetitionSceneResourceSlotVO next = slots.stream()
                .min(Comparator.comparing(CompetitionSceneResourceSlotVO::getStartTime))
                .orElse(null);
        if (next != null) {
            vo.setNextSlotId(next.getSlotId());
            vo.setNextStartTime(next.getStartTime());
            vo.setNextEndTime(next.getEndTime());
            vo.setRemainingDeviceCount(next.getRemainingDeviceCount());
            vo.setRemainingWorkstationCount(next.getRemainingWorkstationCount());
        } else {
            vo.setRemainingDeviceCount(0);
            vo.setRemainingWorkstationCount(0);
        }
        vo.setSubjectType(subject.getSubjectType());
        vo.setSubjectCode(subject.getSubjectCode());
        vo.setSubjectName(buildSubjectName(subject));
        vo.setGroupCode(subject.getGroupCode());
        vo.setGroupName(subject.getGroupName());
        vo.setParticipantCount(subject.getParticipantCount());
        ReservationCapacitySnapshot capacitySnapshot = calculateCapacitySnapshot(calculateOccupyPeopleCount(subject),
                scheduleResource.getWorkstationsPerDevice(), scheduleResource.getSharedOccupancy());
        vo.setSuggestedDeviceCount(capacitySnapshot.getReservedDeviceCount());
        vo.setCoveredWorkstationCount(capacitySnapshot.getReservedWorkstationCount());
        CompetitionSceneResourceReservationVO existing = null;
        String activeReservationKey = buildActiveReservationKey(subject.getCompetitionSeriesId(),
                subject.getSubjectType(), subject.getSubjectCode());
        existing = reservationMapper.selectEffectiveReservationByActiveKey(activeReservationKey);
        if (existing != null) {
            fillExpired(existing, now);
            vo.setExistingReservation(existing);
        }
        vo.setHasExistingReservation(existing != null);
        return vo;
    }

    private CompetitionSceneScheduleResourceVO loadScheduleResourceVO(Long scheduleResourceId) {
        if (scheduleResourceId == null) {
            throw business(CompetitionSceneResourceConstants.ERROR_RESOURCE_NOT_OPEN, "赛场资源布置ID不能为空");
        }
        CompetitionSceneScheduleResourceVO scheduleResource =
                scheduleResourceMapper.selectCompetitionSceneScheduleResourceById(scheduleResourceId);
        if (scheduleResource == null) {
            throw business(CompetitionSceneResourceConstants.ERROR_RESOURCE_NOT_OPEN, "赛场资源布置不存在");
        }
        return scheduleResource;
    }

    private void ensureResourceOpen(CompetitionSceneScheduleResourceVO scheduleResource, Date now) {
        if (scheduleResource == null
                || !CompetitionSceneResourceConstants.BOOKING_STATUS_OPEN.equals(scheduleResource.getBookingStatus())
                || !isBookingWindowOpen(scheduleResource, now)) {
            throw business(CompetitionSceneResourceConstants.ERROR_RESOURCE_NOT_OPEN, "资源预约未开放");
        }
    }

    private void ensureResourceOpen(CompetitionSceneScheduleResource scheduleResource, Date now) {
        if (scheduleResource == null
                || !CompetitionSceneResourceConstants.BOOKING_STATUS_OPEN.equals(scheduleResource.getBookingStatus())
                || !isBookingWindowOpen(scheduleResource.getBookingOpenTime(), scheduleResource.getBookingCloseTime(), now)) {
            throw business(CompetitionSceneResourceConstants.ERROR_RESOURCE_NOT_OPEN, "资源预约未开放");
        }
    }

    private boolean isBookingWindowOpen(CompetitionSceneScheduleResourceVO scheduleResource, Date now) {
        return isBookingWindowOpen(scheduleResource.getBookingOpenTime(), scheduleResource.getBookingCloseTime(), now);
    }

    private boolean isBookingWindowOpen(Date openTime, Date closeTime, Date now) {
        return (openTime == null || !openTime.after(now)) && (closeTime == null || closeTime.after(now));
    }

    private List<CompetitionSceneResourceSlotVO> selectOpenSlots(Long scheduleResourceId,
                                                                 Date now,
                                                                 String groupCode,
                                                                 Boolean sharedOccupancy) {
        CompetitionSceneResourceSlotQuery query = new CompetitionSceneResourceSlotQuery();
        query.setScheduleResourceId(scheduleResourceId);
        query.setSlotStatus(CompetitionSceneResourceConstants.SLOT_STATUS_OPEN);
        List<CompetitionSceneResourceSlotVO> slots = slotMapper.selectCompetitionSceneResourceSlotList(query);
        return slots.stream()
                .filter(slot -> slot.getStartTime() != null && slot.getStartTime().after(now))
                .filter(slot -> hasAvailableSlotCapacity(slot, sharedOccupancy))
                .filter(slot -> checkSlotGroupAllowed(slot.getSlotId(), groupCode))
                .peek(this::fillAllowedGroupNames)
                .collect(Collectors.toList());
    }

    private CompetitionSceneReservationSubject requireScopedSubject(Long scheduleResourceId, Long userId) {
        CompetitionSceneReservationSubject subject = findScopedSubject(scheduleResourceId, resolveAllSubjects(userId));
        if (subject == null) {
            throw business(CompetitionSceneResourceConstants.ERROR_RESOURCE_SCOPE_DENIED,
                    "当前用户所属赛场不在该资源允许预约范围内");
        }
        return subject;
    }

    private CompetitionSceneReservationSubject findScopedSubject(Long scheduleResourceId,
                                                                 List<CompetitionSceneReservationSubject> subjects) {
        if (scheduleResourceId == null || subjects == null || subjects.isEmpty()) {
            return null;
        }
        for (CompetitionSceneReservationSubject subject : subjects) {
            if (subject != null && checkScheduleScopeAllowed(scheduleResourceId, subject.getScheduleId())) {
                return subject;
            }
        }
        return null;
    }

    private boolean hasAvailableSlotCapacity(CompetitionSceneResourceSlotVO slot, Boolean sharedOccupancy) {
        if (slot == null) {
            return false;
        }
        if (Boolean.TRUE.equals(sharedOccupancy)) {
            return defaultZero(slot.getRemainingWorkstationCount()) > 0;
        }
        return defaultZero(slot.getRemainingDeviceCount()) > 0
                && defaultZero(slot.getRemainingWorkstationCount()) > 0;
    }

    private void fillAllowedGroupNames(CompetitionSceneResourceSlotVO slot) {
        if (slot == null || slot.getSlotId() == null || slotGroupScopeMapper == null) {
            return;
        }
        List<CompetitionSceneResourceSlotGroupScope> scopes = slotGroupScopeMapper.selectBySlotId(slot.getSlotId());
        if (scopes == null) {
            return;
        }
        List<String> names = scopes.stream()
                .filter(scope -> scope != null
                        && Integer.valueOf(1).equals(scope.getEnabled())
                        && CompetitionSceneResourceConstants.DELETED_NO.equals(scope.getDeleted()))
                .map(scope -> StringUtils.isNotEmpty(trim(scope.getAllowedGroupName()))
                        ? trim(scope.getAllowedGroupName()) : trim(scope.getAllowedGroupCode()))
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        slot.setAllowedGroupNames(names);
    }

    private Integer resolveWorkstationCount(CompetitionSceneResourceSlot slot,
                                            CompetitionSceneScheduleResource scheduleResource) {
        Integer workstationCount = slot == null ? null : slot.getWorkstationCount();
        if (workstationCount == null || workstationCount <= 0) {
            workstationCount = scheduleResource == null ? null : scheduleResource.getWorkstationsPerDevice();
        }
        if (workstationCount == null || workstationCount <= 0) {
            throw business(CompetitionSceneResourceConstants.ERROR_RESOURCE_NOT_OPEN, "每台设备工位数配置不合法");
        }
        return workstationCount;
    }

    private CompetitionSceneResourceReservationVO tryInsertReservationRecord(CompetitionSceneResourceReservation reservation,
                                                                            String idempotencyKey,
                                                                            String activeReservationKey,
                                                                            Date now) {
        try {
            reservationMapper.insertCompetitionSceneResourceReservation(reservation);
            return null;
        } catch (RuntimeException e) {
            return handleReservationInsertException(e, idempotencyKey, activeReservationKey, now);
        }
    }

    private CompetitionSceneResourceReservationVO handleReservationInsertException(RuntimeException e,
                                                                                   String idempotencyKey,
                                                                                   String activeReservationKey,
                                                                                   Date now) {
        ReservationUniqueConflictType conflictType =
                classifyReservationUniqueConflict(e, idempotencyKey, activeReservationKey);
        if (ReservationUniqueConflictType.NONE.equals(conflictType)) {
            throw e;
        }

        if (ReservationUniqueConflictType.IDEMPOTENCY_KEY.equals(conflictType)) {
            return resolveDuplicateIdempotencyKey(idempotencyKey, now);
        }

        return resolveDuplicateActiveReservationKey(activeReservationKey, now);
    }

    private CompetitionSceneResourceReservationVO resolveDuplicateActiveReservationKey(String activeReservationKey,
                                                                                       Date now) {
        CompetitionSceneResourceReservationVO activeConflict =
                findReservationByActiveKeyWithRetry(activeReservationKey, now);
        if (activeConflict != null) {
            throw business(CompetitionSceneResourceConstants.ERROR_ALREADY_RESERVED,
                    "当前参赛主体已有有效资源预约", activeConflict);
        }
        throw business(CompetitionSceneResourceConstants.ERROR_RESERVATION_CONFLICT_RETRY_LATER,
                "预约冲突，请稍后刷新查看");
    }

    private CompetitionSceneResourceReservationVO resolveDuplicateIdempotencyKey(String idempotencyKey, Date now) {
        CompetitionSceneResourceReservationVO idempotencyConflict =
                findReservationByIdempotencyKeyWithRetry(idempotencyKey, now);
        if (idempotencyConflict != null) {
            return idempotencyConflict;
        }
        throw business(CompetitionSceneResourceConstants.ERROR_IDEMPOTENCY_CONFLICT_RETRY_LATER,
                "请求处理中，请稍后刷新查看");
    }

    private CompetitionSceneResourceReservationVO findReservationByActiveKeyWithRetry(String activeReservationKey,
                                                                                      Date now) {
        return findReservationWithRetry(() ->
                executeReservationLookupInNewTransaction(() ->
                        reservationMapper.selectEffectiveReservationByActiveKey(activeReservationKey)), now);
    }

    private CompetitionSceneResourceReservationVO findReservationByIdempotencyKeyWithRetry(String idempotencyKey,
                                                                                           Date now) {
        return findReservationWithRetry(() ->
                executeReservationLookupInNewTransaction(() ->
                        reservationMapper.selectReservationByIdempotencyKey(idempotencyKey)), now);
    }

    private CompetitionSceneResourceReservationVO findReservationWithRetry(
            Supplier<CompetitionSceneResourceReservationVO> supplier, Date now) {
        for (int attempt = 0; attempt < 3; attempt++) {
            CompetitionSceneResourceReservationVO reservation = supplier.get();
            if (reservation != null) {
                fillExpired(reservation, now);
                return reservation;
            }
            if (attempt < 2) {
                sleepBeforeConflictRetry();
            }
        }
        return null;
    }

    private CompetitionSceneResourceReservationVO executeReservationLookupInNewTransaction(
            Supplier<CompetitionSceneResourceReservationVO> supplier) {
        if (transactionManager == null) {
            return supplier.get();
        }
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.setReadOnly(true);
        return template.execute(status -> supplier.get());
    }

    private void sleepBeforeConflictRetry() {
        try {
            Thread.sleep(50L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ReservationUniqueConflictType classifyReservationUniqueConflict(Throwable e,
                                                                           String idempotencyKey,
                                                                           String activeReservationKey) {
        if (!isSupportedReservationUniqueConflictException(e)) {
            return ReservationUniqueConflictType.NONE;
        }
        String text = collectThrowableText(e);
        if (!isDuplicateConstraintText(text)) {
            return ReservationUniqueConflictType.NONE;
        }
        if (containsUniqueKeyName(text, "uk_scene_resource_idempotency_key")
                || containsUniqueKeyName(text, "uk_scene_reservation_idempotency")
                || containsDuplicateEntryValue(text, idempotencyKey)) {
            return ReservationUniqueConflictType.IDEMPOTENCY_KEY;
        }
        if (containsUniqueKeyName(text, "uk_scene_resource_active_reservation_key")
                || containsDuplicateEntryValue(text, activeReservationKey)) {
            return ReservationUniqueConflictType.ACTIVE_RESERVATION_KEY;
        }
        return ReservationUniqueConflictType.NONE;
    }

    private boolean isSupportedReservationUniqueConflictException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof DuplicateKeyException
                    || current instanceof DataIntegrityViolationException
                    || current instanceof MyBatisSystemException
                    || current instanceof PersistenceException
                    || current instanceof SQLIntegrityConstraintViolationException
                    || current instanceof UndeclaredThrowableException
                    || current instanceof org.springframework.transaction.TransactionSystemException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isDuplicateConstraintText(String text) {
        return text.contains("duplicate entry")
                || text.contains("duplicatekey")
                || text.contains("sqlintegrityconstraintviolationexception")
                || text.contains("unique constraint")
                || text.contains("constraint violation")
                || text.contains("uk_scene_resource_active_reservation_key")
                || text.contains("uk_scene_resource_idempotency_key")
                || text.contains("uk_scene_reservation_idempotency");
    }

    private boolean containsUniqueKeyName(String text, String keyName) {
        if (StringUtils.isEmpty(keyName)) {
            return false;
        }
        String normalizedKey = keyName.toLowerCase(Locale.ROOT);
        return text.contains("key '" + normalizedKey + "'")
                || text.contains("key `" + normalizedKey + "`")
                || text.contains("." + normalizedKey)
                || text.contains(normalizedKey)
                || text.contains("constraint [" + normalizedKey + "]")
                || text.contains("constraint \"" + normalizedKey + "\"")
                || text.contains("constraint '" + normalizedKey + "'");
    }

    private boolean containsDuplicateEntryValue(String text, String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        String normalizedValue = value.toLowerCase(Locale.ROOT);
        return text.contains("duplicate entry '" + normalizedValue + "'")
                || text.contains("duplicate entry `" + normalizedValue + "`")
                || text.contains("duplicate entry \"" + normalizedValue + "\"");
    }

    private String collectThrowableText(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            builder.append(current.getClass().getName()).append(':')
                    .append(current.getMessage()).append('\n');
            current = current.getCause();
        }
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    private int reserveSlotCapacity(Long slotId, ReservationCapacitySnapshot capacitySnapshot, Date now) {
        if (capacitySnapshot == null) {
            return 0;
        }
        int updated;
        if (Boolean.TRUE.equals(capacitySnapshot.getSharedOccupancySnapshot())) {
            updated = slotMapper.reserveSharedCompetitionSceneResourceSlotCapacity(slotId,
                    capacitySnapshot.getReservedWorkstationCount(), now, currentUsername());
        } else {
            updated = slotMapper.reserveExclusiveCompetitionSceneResourceSlotCapacity(slotId,
                    capacitySnapshot.getReservedDeviceCount(), capacitySnapshot.getReservedWorkstationCount(),
                    now, currentUsername());
        }
        if (updated > 0) {
            refreshSlotStatusAfterReserve(slotId, capacitySnapshot.getSharedOccupancySnapshot());
        }
        return updated;
    }

    private int releaseSlotCapacity(CompetitionSceneResourceReservation reservation) {
        if (reservation == null) {
            return 0;
        }
        int reservedDeviceCount = Math.max(0, defaultZero(reservation.getReservedDeviceCount()));
        int reservedWorkstationCount = defaultZero(reservation.getReservedWorkstationCount());
        if (reservedWorkstationCount <= 0) {
            reservedWorkstationCount = Math.max(0, defaultZero(reservation.getCoveredWorkstationCount()));
        }
        int updated;
        if (Boolean.TRUE.equals(reservation.getSharedOccupancySnapshot())) {
            updated = slotMapper.releaseSharedCompetitionSceneResourceSlotCapacity(reservation.getSlotId(),
                    reservedWorkstationCount, currentUsername());
        } else {
            updated = slotMapper.releaseExclusiveCompetitionSceneResourceSlotCapacity(reservation.getSlotId(),
                    reservedDeviceCount, reservedWorkstationCount, currentUsername());
        }
        if (updated > 0) {
            refreshSlotStatusAfterRelease(reservation.getSlotId(), reservation.getSharedOccupancySnapshot());
        }
        return updated;
    }

    private void refreshSlotStatusAfterReserve(Long slotId, Boolean sharedOccupancy) {
        CompetitionSceneResourceSlot latest = slotMapper.selectCompetitionSceneResourceSlotEntityById(slotId);
        if (latest == null || !CompetitionSceneResourceConstants.SLOT_STATUS_OPEN.equals(latest.getSlotStatus())) {
            return;
        }
        if (isSlotFullByCapacity(latest, sharedOccupancy)) {
            slotMapper.updateCompetitionSceneResourceSlotStatusIfCurrent(slotId,
                    CompetitionSceneResourceConstants.SLOT_STATUS_OPEN,
                    CompetitionSceneResourceConstants.SLOT_STATUS_FULL,
                    currentUsername());
        }
    }

    private void refreshSlotStatusAfterRelease(Long slotId, Boolean sharedOccupancy) {
        CompetitionSceneResourceSlot latest = slotMapper.selectCompetitionSceneResourceSlotEntityById(slotId);
        if (latest == null || !CompetitionSceneResourceConstants.SLOT_STATUS_FULL.equals(latest.getSlotStatus())) {
            return;
        }
        Date now = DateUtils.getNowDate();
        if (latest.getStartTime() == null || !latest.getStartTime().after(now)) {
            return;
        }
        if (isSlotCapacityAvailable(latest, sharedOccupancy)) {
            slotMapper.updateCompetitionSceneResourceSlotStatusIfCurrent(slotId,
                    CompetitionSceneResourceConstants.SLOT_STATUS_FULL,
                    CompetitionSceneResourceConstants.SLOT_STATUS_OPEN,
                    currentUsername());
        }
    }

    private boolean isSlotFullByCapacity(CompetitionSceneResourceSlot slot, Boolean sharedOccupancy) {
        if (Boolean.TRUE.equals(sharedOccupancy)) {
            return defaultZero(slot.getRemainingWorkstationCount()) <= 0;
        }
        return defaultZero(slot.getRemainingDeviceCount()) <= 0
                || defaultZero(slot.getRemainingWorkstationCount()) <= 0;
    }

    private boolean isSlotCapacityAvailable(CompetitionSceneResourceSlot slot, Boolean sharedOccupancy) {
        if (Boolean.TRUE.equals(sharedOccupancy)) {
            return defaultZero(slot.getRemainingWorkstationCount()) > 0;
        }
        return defaultZero(slot.getRemainingDeviceCount()) > 0
                && defaultZero(slot.getRemainingWorkstationCount()) > 0;
    }

    private CompetitionSceneReservationSubject requireSubject(Long scheduleId, Long userId) {
        CompetitionSceneReservationSubject subject = resolveSubject(scheduleId, userId, true);
        if (subject == null) {
            throw business(CompetitionSceneResourceConstants.ERROR_NOT_SCHEDULE_TARGET, "当前用户不是该赛场安排匹配对象");
        }
        return subject;
    }

    private CompetitionSceneReservationSubject resolveSubject(Long scheduleId, Long userId, boolean strict) {
        if (scheduleId == null || userId == null) {
            if (strict) {
                throw business(CompetitionSceneResourceConstants.ERROR_SUBJECT_NOT_RESOLVED, "无法识别当前参赛主体");
            }
            return null;
        }
        List<CompetitionSceneScheduleTarget> targets = targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(scheduleId);
        for (CompetitionSceneScheduleTarget target : targets) {
            if (!isTargetAvailable(target)) {
                continue;
            }
            if (StringUtils.isNotEmpty(target.getTeamCode())) {
                List<CompetitionApplyInfo> members = selectTeamPlayers(target.getCompetitionSeriesId(), target.getTeamCode());
                boolean memberMatched = members.stream().anyMatch(item -> Objects.equals(item.getUserId(), userId));
                if (!memberMatched && Objects.equals(target.getUserId(), userId)
                        && !ApplyConstants.TEAM_GUIDE_TEACHER.equals(target.getCompetitionRoleName())) {
                    memberMatched = true;
                }
                if (memberMatched) {
                    return buildTeamSubject(target, members);
                }
                continue;
            }
            if (Objects.equals(target.getUserId(), userId)) {
                if (CompetitionSceneConstants.DIMENSION_TEAM.equals(target.getConfigDimension())) {
                    if (strict) {
                        throw business(CompetitionSceneResourceConstants.ERROR_SUBJECT_NOT_RESOLVED,
                                "团队预约主体缺少teamCode，无法预约");
                    }
                    return null;
                }
                return buildUserSubject(target, userId);
            }
        }
        if (strict) {
            throw business(CompetitionSceneResourceConstants.ERROR_NOT_SCHEDULE_TARGET, "当前用户不是该赛场安排匹配对象");
        }
        return null;
    }

    private List<CompetitionSceneReservationSubject> resolveAllSubjects(Long userId) {
        CompetitionSceneScheduleTarget query = new CompetitionSceneScheduleTarget();
        query.setStatus(CompetitionSceneConstants.STATUS_NORMAL);
        query.setMatchStatus(CompetitionSceneConstants.MATCH_STATUS_MATCHED);
        List<CompetitionSceneScheduleTarget> targets = targetMapper.selectCompetitionSceneScheduleTargetList(query);
        Map<String, CompetitionSceneReservationSubject> subjectMap = new LinkedHashMap<>();
        Map<String, List<CompetitionApplyInfo>> teamMemberCache = new HashMap<>();
        for (CompetitionSceneScheduleTarget target : targets) {
            if (!isTargetAvailable(target)) {
                continue;
            }
            CompetitionSceneReservationSubject subject = null;
            if (StringUtils.isNotEmpty(target.getTeamCode())) {
                String cacheKey = target.getCompetitionSeriesId() + ":" + target.getTeamCode();
                List<CompetitionApplyInfo> members = teamMemberCache.computeIfAbsent(cacheKey,
                        key -> selectTeamPlayers(target.getCompetitionSeriesId(), target.getTeamCode()));
                boolean memberMatched = members.stream().anyMatch(item -> Objects.equals(item.getUserId(), userId));
                if (memberMatched) {
                    subject = buildTeamSubject(target, members);
                }
            } else if (!CompetitionSceneConstants.DIMENSION_TEAM.equals(target.getConfigDimension())
                    && Objects.equals(target.getUserId(), userId)) {
                subject = buildUserSubject(target, userId);
            }
            if (subject != null) {
                subjectMap.put(subject.getScheduleId() + ":" + subject.getSubjectType() + ":" + subject.getSubjectCode(), subject);
            }
        }
        return new ArrayList<>(subjectMap.values());
    }

    private boolean isTargetAvailable(CompetitionSceneScheduleTarget target) {
        return target != null
                && CompetitionSceneConstants.DEL_FLAG_NORMAL.equals(target.getDelFlag())
                && CompetitionSceneConstants.STATUS_NORMAL.equals(target.getStatus())
                && CompetitionSceneConstants.MATCH_STATUS_MATCHED.equals(target.getMatchStatus());
    }

    private CompetitionSceneReservationSubject buildTeamSubject(CompetitionSceneScheduleTarget target,
                                                               List<CompetitionApplyInfo> members) {
        if (StringUtils.isEmpty(target.getTeamCode())) {
            throw business(CompetitionSceneResourceConstants.ERROR_SUBJECT_NOT_RESOLVED, "团队预约主体缺少teamCode，无法预约");
        }
        CompetitionSceneReservationSubject subject = new CompetitionSceneReservationSubject();
        subject.setCompetitionSeriesId(target.getCompetitionSeriesId());
        subject.setScheduleId(target.getScheduleId());
        subject.setTargetId(target.getTargetId());
        subject.setSubjectType(CompetitionSceneResourceConstants.SUBJECT_TYPE_TEAM);
        subject.setSubjectCode(target.getTeamCode());
        subject.setTeamCode(target.getTeamCode());
        subject.setTeamName(target.getTeamName());
        subject.setUserId(target.getUserId());
        subject.setUserName(target.getUserName());
        subject.setRoleCode(target.getCompetitionRoleName());
        subject.setGroupCode(target.getSecondLevelCode());
        subject.setGroupName(target.getSecondLevelName());
        subject.setParticipantCount(Math.max(1, members == null ? 0 : members.size()));
        return subject;
    }

    private CompetitionSceneReservationSubject buildUserSubject(CompetitionSceneScheduleTarget target, Long userId) {
        CompetitionSceneReservationSubject subject = new CompetitionSceneReservationSubject();
        subject.setCompetitionSeriesId(target.getCompetitionSeriesId());
        subject.setScheduleId(target.getScheduleId());
        subject.setTargetId(target.getTargetId());
        subject.setSubjectType(CompetitionSceneResourceConstants.SUBJECT_TYPE_USER);
        subject.setSubjectCode(String.valueOf(userId));
        subject.setUserId(userId);
        subject.setUserName(target.getUserName());
        subject.setRoleCode(target.getCompetitionRoleName());
        subject.setGroupCode(target.getSecondLevelCode());
        subject.setGroupName(target.getSecondLevelName());
        subject.setParticipantCount(1);
        return subject;
    }

    private List<CompetitionApplyInfo> selectTeamPlayers(Long competitionSeriesId, String teamCode) {
        if (StringUtils.isEmpty(teamCode)) {
            return new ArrayList<>();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("teamCode", teamCode);
        params.put("competitionSeriesId", competitionSeriesId);
        List<CompetitionApplyInfo> list =
                competitionApplyInfoMapper.selectCertCompetitionApplyInfoListByUserTeamCodeANoTeacher(params);
        if (list == null) {
            return new ArrayList<>();
        }
        return list.stream()
                .filter(item -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(item.getCompetitionRoleName()))
                .filter(item -> DictConstant.PAID.equals(item.getPayStatus()))
                .filter(item -> Constants.CHECK_PASS.equals(item.getCheckStatus()) || StringUtils.isEmpty(item.getCheckStatus()))
                .collect(Collectors.toList());
    }

    private boolean hasValidCredential(CompetitionSceneReservationSubject subject, Date now) {
        if (subject == null) {
            return false;
        }
        CompetitionSceneCredential query = new CompetitionSceneCredential();
        query.setScheduleId(subject.getScheduleId());
        query.setCredentialStatus(CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE);
        if (CompetitionSceneResourceConstants.SUBJECT_TYPE_TEAM.equals(subject.getSubjectType())) {
            query.setTeamCode(subject.getTeamCode());
        } else {
            query.setUserId(subject.getUserId());
        }
        List<CompetitionSceneCredential> credentials = credentialMapper.selectCompetitionSceneCredentialList(query);
        return credentials.stream().anyMatch(credential -> isCredentialValid(credential, now));
    }

    private void requireValidCredential(CompetitionSceneReservationSubject subject, Date now) {
        if (!hasValidCredential(subject, now)) {
            throw business(CompetitionSceneResourceConstants.ERROR_NO_VALID_CREDENTIAL, "无有效现场证件，不能预约");
        }
    }

    private boolean isCredentialValid(CompetitionSceneCredential credential, Date now) {
        if (credential == null
                || !CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE.equals(credential.getCredentialStatus())) {
            return false;
        }
        if (credential.getValidFrom() != null && credential.getValidFrom().after(now)) {
            return false;
        }
        return credential.getValidTo() == null || !credential.getValidTo().before(now);
    }

    private int calculateReservedDeviceCount(CompetitionSceneReservationSubject subject,
                                             CompetitionSceneScheduleResource scheduleResource) {
        if (scheduleResource == null || scheduleResource.getWorkstationsPerDevice() == null
                || scheduleResource.getWorkstationsPerDevice() <= 0) {
            throw business(CompetitionSceneResourceConstants.ERROR_RESOURCE_NOT_OPEN, "每台设备工位数配置不合法");
        }
        if (CompetitionSceneResourceConstants.SUBJECT_TYPE_TEAM.equals(subject.getSubjectType())) {
            int participantCount = Math.max(1, defaultZero(subject.getParticipantCount()));
            return (participantCount + scheduleResource.getWorkstationsPerDevice() - 1)
                    / scheduleResource.getWorkstationsPerDevice();
        }
        return 1;
    }

    private String buildActiveReservationKey(Long competitionSeriesId, String subjectType, String subjectCode) {
        if (competitionSeriesId == null || StringUtils.isEmpty(trim(subjectType)) || StringUtils.isEmpty(trim(subjectCode))) {
            throw business(CompetitionSceneResourceConstants.ERROR_SUBJECT_NOT_RESOLVED, "无法生成有效预约唯一键");
        }
        return "RESV:" + competitionSeriesId + ":" + trim(subjectType) + ":" + trim(subjectCode);
    }

    private int calculateOccupyPeopleCount(CompetitionSceneReservationSubject subject) {
        if (subject == null) {
            return 1;
        }
        if (CompetitionSceneResourceConstants.SUBJECT_TYPE_TEAM.equals(subject.getSubjectType())) {
            return Math.max(1, defaultZero(subject.getParticipantCount()));
        }
        return 1;
    }

    private ReservationCapacitySnapshot calculateCapacitySnapshot(Integer occupyPeopleCount,
                                                                  Integer workstationCount,
                                                                  Boolean sharedOccupancy) {
        int peopleCount = Math.max(1, defaultZero(occupyPeopleCount));
        int perDeviceWorkstations = defaultOne(workstationCount);
        int deviceCount = (peopleCount + perDeviceWorkstations - 1) / perDeviceWorkstations;
        ReservationCapacitySnapshot snapshot = new ReservationCapacitySnapshot();
        snapshot.setOccupyPeopleCount(peopleCount);
        snapshot.setReservedDeviceCount(deviceCount);
        snapshot.setSharedOccupancySnapshot(Boolean.TRUE.equals(sharedOccupancy));
        snapshot.setWorkstationCountSnapshot(perDeviceWorkstations);
        if (Boolean.TRUE.equals(sharedOccupancy)) {
            snapshot.setReservedWorkstationCount(peopleCount);
        } else {
            snapshot.setReservedWorkstationCount(deviceCount * perDeviceWorkstations);
        }
        return snapshot;
    }

    private void checkIdempotencyKeyRequired(String idempotencyKey) {
        if (StringUtils.isEmpty(trim(idempotencyKey))) {
            throw business(CompetitionSceneResourceConstants.ERROR_IDEMPOTENCY_KEY_REQUIRED, "幂等键不能为空");
        }
    }

    private boolean checkSlotGroupAllowed(Long slotId, String groupCode) {
        if (slotId == null || slotGroupScopeMapper == null) {
            return false;
        }
        if (slotGroupScopeMapper.countEnabledBySlotId(slotId) <= 0) {
            return true;
        }
        return StringUtils.isNotEmpty(trim(groupCode))
                && slotGroupScopeMapper.countAllowedGroup(slotId, trim(groupCode)) > 0;
    }

    private boolean checkScheduleScopeAllowed(Long scheduleResourceId, Long userSourceScheduleId) {
        if (scheduleResourceId == null || userSourceScheduleId == null || scheduleScopeMapper == null) {
            return false;
        }
        return scheduleScopeMapper.countEnabledScope(scheduleResourceId, userSourceScheduleId) > 0;
    }

    private String buildSubjectName(CompetitionSceneReservationSubject subject) {
        if (CompetitionSceneResourceConstants.SUBJECT_TYPE_TEAM.equals(subject.getSubjectType())) {
            return StringUtils.isNotEmpty(subject.getTeamName()) ? subject.getTeamName() : subject.getTeamCode();
        }
        return StringUtils.isNotEmpty(subject.getUserName()) ? subject.getUserName() : subject.getSubjectCode();
    }

    private void fillExpired(CompetitionSceneResourceReservationVO vo, Date now) {
        if (vo != null) {
            vo.setExpired(vo.getSlotEndTime() != null && !vo.getSlotEndTime().after(now));
        }
    }

    private CompetitionSceneReservationException business(String errorCode, String message) {
        return new CompetitionSceneReservationException(errorCode, message);
    }

    private CompetitionSceneReservationException business(String errorCode, String message,
                                                         CompetitionSceneResourceReservationVO existing) {
        return new CompetitionSceneReservationException(errorCode, message, existing);
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private int defaultOne(Integer value) {
        return value == null || value <= 0 ? 1 : value;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private String currentUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception e) {
            return "system";
        }
    }

    private static class ReservationCapacitySnapshot {
        private Integer occupyPeopleCount;
        private Integer reservedDeviceCount;
        private Integer reservedWorkstationCount;
        private Boolean sharedOccupancySnapshot;
        private Integer workstationCountSnapshot;

        public Integer getOccupyPeopleCount() {
            return occupyPeopleCount;
        }

        public void setOccupyPeopleCount(Integer occupyPeopleCount) {
            this.occupyPeopleCount = occupyPeopleCount;
        }

        public Integer getReservedDeviceCount() {
            return reservedDeviceCount;
        }

        public void setReservedDeviceCount(Integer reservedDeviceCount) {
            this.reservedDeviceCount = reservedDeviceCount;
        }

        public Integer getReservedWorkstationCount() {
            return reservedWorkstationCount;
        }

        public void setReservedWorkstationCount(Integer reservedWorkstationCount) {
            this.reservedWorkstationCount = reservedWorkstationCount;
        }

        public Boolean getSharedOccupancySnapshot() {
            return sharedOccupancySnapshot;
        }

        public void setSharedOccupancySnapshot(Boolean sharedOccupancySnapshot) {
            this.sharedOccupancySnapshot = sharedOccupancySnapshot;
        }

        public Integer getWorkstationCountSnapshot() {
            return workstationCountSnapshot;
        }

        public void setWorkstationCountSnapshot(Integer workstationCountSnapshot) {
            this.workstationCountSnapshot = workstationCountSnapshot;
        }
    }

    private enum ReservationUniqueConflictType {
        NONE,
        ACTIVE_RESERVATION_KEY,
        IDEMPOTENCY_KEY
    }
}
