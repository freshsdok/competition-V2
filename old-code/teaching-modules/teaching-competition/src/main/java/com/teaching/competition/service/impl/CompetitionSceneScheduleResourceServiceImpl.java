package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.CompetitionSceneResourceConstants;
import com.teaching.competition.domain.CompetitionSceneResourceVO;
import com.teaching.competition.domain.CompetitionSceneSchedule;
import com.teaching.competition.domain.CompetitionSceneScheduleResource;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceQuery;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceStatusReq;
import com.teaching.competition.domain.CompetitionSceneScheduleResourceVO;
import com.teaching.competition.mapper.CompetitionSceneResourceMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleResourceMapper;
import com.teaching.competition.service.ICompetitionSceneScheduleResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 大赛现场赛场资源布置Service业务层处理。
 */
@Service
public class CompetitionSceneScheduleResourceServiceImpl implements ICompetitionSceneScheduleResourceService {

    private static final Set<String> BOOKING_STATUSES = new HashSet<>(Arrays.asList(
            CompetitionSceneResourceConstants.BOOKING_STATUS_DRAFT,
            CompetitionSceneResourceConstants.BOOKING_STATUS_READY,
            CompetitionSceneResourceConstants.BOOKING_STATUS_OPEN,
            CompetitionSceneResourceConstants.BOOKING_STATUS_PAUSED,
            CompetitionSceneResourceConstants.BOOKING_STATUS_CLOSED
    ));

    @Autowired
    private CompetitionSceneScheduleResourceMapper scheduleResourceMapper;

    @Autowired
    private CompetitionSceneResourceMapper resourceMapper;

    @Autowired
    private CompetitionSceneScheduleMapper scheduleMapper;

    @Override
    public CompetitionSceneScheduleResourceVO selectCompetitionSceneScheduleResourceById(Long scheduleResourceId) {
        return scheduleResourceMapper.selectCompetitionSceneScheduleResourceById(scheduleResourceId);
    }

    @Override
    public List<CompetitionSceneScheduleResourceVO> selectCompetitionSceneScheduleResourceList(CompetitionSceneScheduleResourceQuery query) {
        return scheduleResourceMapper.selectCompetitionSceneScheduleResourceList(query);
    }

    @Override
    public int insertCompetitionSceneScheduleResource(CompetitionSceneScheduleResource scheduleResource) {
        if (scheduleResource == null) {
            throw new ServiceException("赛场资源布置信息不能为空");
        }
        CompetitionSceneSchedule schedule = loadSchedule(scheduleResource.getScheduleId());
        CompetitionSceneResourceVO resource = loadResource(scheduleResource.getResourceId());
        if (!CompetitionSceneResourceConstants.RESOURCE_STATUS_ENABLED.equals(resource.getResourceStatus())) {
            throw new ServiceException("只能布置启用状态的资源");
        }
        applyDefaults(scheduleResource, schedule, resource);
        normalize(scheduleResource);
        validate(scheduleResource, false);
        Date now = DateUtils.getNowDate();
        scheduleResource.setCreateBy(currentUsername());
        scheduleResource.setUpdateBy(currentUsername());
        scheduleResource.setCreateTime(now);
        scheduleResource.setUpdateTime(now);
        scheduleResource.setDeleted(CompetitionSceneResourceConstants.DELETED_NO);
        return scheduleResourceMapper.insertCompetitionSceneScheduleResource(scheduleResource);
    }

    @Override
    public int updateCompetitionSceneScheduleResource(CompetitionSceneScheduleResource scheduleResource) {
        if (scheduleResource == null || scheduleResource.getScheduleResourceId() == null) {
            throw new ServiceException("赛场资源布置ID不能为空");
        }
        CompetitionSceneScheduleResource existed = scheduleResourceMapper
                .selectCompetitionSceneScheduleResourceEntityById(scheduleResource.getScheduleResourceId());
        if (existed == null) {
            throw new ServiceException("赛场资源布置不存在");
        }
        CompetitionSceneSchedule schedule = loadSchedule(scheduleResource.getScheduleId());
        CompetitionSceneResourceVO resource = loadResource(scheduleResource.getResourceId());
        if (!existed.getResourceId().equals(scheduleResource.getResourceId())
                && !CompetitionSceneResourceConstants.RESOURCE_STATUS_ENABLED.equals(resource.getResourceStatus())) {
            throw new ServiceException("只能选择启用状态的资源");
        }
        if (scheduleResource.getEventId() == null) {
            scheduleResource.setEventId(schedule.getCompetitionSeriesId());
        }
        normalize(scheduleResource);
        validate(scheduleResource, true);
        scheduleResource.setUpdateBy(currentUsername());
        scheduleResource.setUpdateTime(DateUtils.getNowDate());
        return scheduleResourceMapper.updateCompetitionSceneScheduleResource(scheduleResource);
    }

    @Override
    public int deleteCompetitionSceneScheduleResourceByIds(Long[] scheduleResourceIds) {
        if (scheduleResourceIds == null || scheduleResourceIds.length == 0) {
            throw new ServiceException("赛场资源布置ID不能为空");
        }
        return scheduleResourceMapper.deleteCompetitionSceneScheduleResourceByIds(scheduleResourceIds, currentUsername());
    }

    @Override
    public int changeCompetitionSceneScheduleResourceBookingStatus(CompetitionSceneScheduleResourceStatusReq req) {
        if (req == null || req.getScheduleResourceId() == null) {
            throw new ServiceException("赛场资源布置ID不能为空");
        }
        if (!BOOKING_STATUSES.contains(req.getBookingStatus())) {
            throw new ServiceException("预约发布状态不合法");
        }
        CompetitionSceneScheduleResource existed = scheduleResourceMapper
                .selectCompetitionSceneScheduleResourceEntityById(req.getScheduleResourceId());
        if (existed == null) {
            throw new ServiceException("赛场资源布置不存在");
        }
        return scheduleResourceMapper.updateCompetitionSceneScheduleResourceBookingStatus(
                req.getScheduleResourceId(), req.getBookingStatus(), currentUsername());
    }

    private CompetitionSceneSchedule loadSchedule(Long scheduleId) {
        if (scheduleId == null) {
            throw new ServiceException("赛场安排ID不能为空");
        }
        CompetitionSceneSchedule schedule = scheduleMapper.selectCompetitionSceneScheduleById(scheduleId);
        if (schedule == null) {
            throw new ServiceException("赛场安排不存在");
        }
        return schedule;
    }

    private CompetitionSceneResourceVO loadResource(Long resourceId) {
        if (resourceId == null) {
            throw new ServiceException("资源ID不能为空");
        }
        CompetitionSceneResourceVO resource = resourceMapper.selectCompetitionSceneResourceById(resourceId);
        if (resource == null) {
            throw new ServiceException("资源不存在");
        }
        return resource;
    }

    private void applyDefaults(CompetitionSceneScheduleResource scheduleResource,
                               CompetitionSceneSchedule schedule,
                               CompetitionSceneResourceVO resource) {
        if (scheduleResource.getEventId() == null) {
            scheduleResource.setEventId(schedule.getCompetitionSeriesId());
        }
        if (scheduleResource.getWorkstationsPerDevice() == null) {
            scheduleResource.setWorkstationsPerDevice(resource.getWorkstationCount());
        }
        if (scheduleResource.getSlotDurationMinutes() == null) {
            scheduleResource.setSlotDurationMinutes(resource.getDefaultSlotDurationMinutes());
        }
        if (scheduleResource.getSharedOccupancy() == null) {
            scheduleResource.setSharedOccupancy(resource.getDefaultSharedOccupancy());
        }
        if (scheduleResource.getNeedOpsConfirm() == null) {
            scheduleResource.setNeedOpsConfirm(resource.getNeedOpsConfirm());
        }
        if (StringUtils.isEmpty(scheduleResource.getOpsContactName())) {
            scheduleResource.setOpsContactName(resource.getOpsContactName());
        }
        if (StringUtils.isEmpty(scheduleResource.getOpsContactPhone())) {
            scheduleResource.setOpsContactPhone(resource.getOpsContactPhone());
        }
        if (StringUtils.isEmpty(scheduleResource.getSafetyNoticeOverride())) {
            scheduleResource.setSafetyNoticeOverride(resource.getSafetyNotice());
        }
        if (StringUtils.isEmpty(scheduleResource.getAttentionNotesOverride())) {
            scheduleResource.setAttentionNotesOverride(resource.getAttentionNotes());
        }
        if (StringUtils.isEmpty(scheduleResource.getUsageInstructionsOverride())) {
            scheduleResource.setUsageInstructionsOverride(resource.getUsageInstructions());
        }
        if (StringUtils.isEmpty(scheduleResource.getBookingStatus())) {
            scheduleResource.setBookingStatus(CompetitionSceneResourceConstants.BOOKING_STATUS_DRAFT);
        }
    }

    private void normalize(CompetitionSceneScheduleResource scheduleResource) {
        scheduleResource.setDeploymentLocation(trim(scheduleResource.getDeploymentLocation()));
        scheduleResource.setBookingStatus(trim(scheduleResource.getBookingStatus()));
        scheduleResource.setOpsContactName(trim(scheduleResource.getOpsContactName()));
        scheduleResource.setOpsContactPhone(trim(scheduleResource.getOpsContactPhone()));
        scheduleResource.setSafetyNoticeOverride(trim(scheduleResource.getSafetyNoticeOverride()));
        scheduleResource.setAttentionNotesOverride(trim(scheduleResource.getAttentionNotesOverride()));
        scheduleResource.setUsageInstructionsOverride(trim(scheduleResource.getUsageInstructionsOverride()));
        if (scheduleResource.getBookingStatus() == null) {
            scheduleResource.setBookingStatus(CompetitionSceneResourceConstants.BOOKING_STATUS_DRAFT);
        }
        if (scheduleResource.getDeployedDeviceCount() != null && scheduleResource.getWorkstationsPerDevice() != null) {
            scheduleResource.setTotalWorkstations(
                    scheduleResource.getDeployedDeviceCount() * scheduleResource.getWorkstationsPerDevice());
        }
    }

    private void validate(CompetitionSceneScheduleResource scheduleResource, boolean update) {
        if (update && scheduleResource.getScheduleResourceId() == null) {
            throw new ServiceException("赛场资源布置ID不能为空");
        }
        if (scheduleResource.getScheduleId() == null) {
            throw new ServiceException("赛场安排ID不能为空");
        }
        if (scheduleResource.getResourceId() == null) {
            throw new ServiceException("资源ID不能为空");
        }
        if (scheduleResource.getDeployedDeviceCount() == null || scheduleResource.getDeployedDeviceCount() <= 0) {
            throw new ServiceException("部署设备数必须大于0");
        }
        if (scheduleResource.getWorkstationsPerDevice() == null || scheduleResource.getWorkstationsPerDevice() <= 0) {
            throw new ServiceException("每台设备工位数必须大于0");
        }
        if (scheduleResource.getSlotDurationMinutes() == null || scheduleResource.getSlotDurationMinutes() <= 0) {
            throw new ServiceException("单场占用周期必须大于0分钟");
        }
        if (scheduleResource.getSharedOccupancy() == null) {
            throw new ServiceException("是否共享占用不能为空");
        }
        if (scheduleResource.getNeedOpsConfirm() == null) {
            throw new ServiceException("是否需要运维确认不能为空");
        }
        if (!BOOKING_STATUSES.contains(scheduleResource.getBookingStatus())) {
            throw new ServiceException("预约发布状态不合法");
        }
        scheduleResource.setTotalWorkstations(
                scheduleResource.getDeployedDeviceCount() * scheduleResource.getWorkstationsPerDevice());
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
}
