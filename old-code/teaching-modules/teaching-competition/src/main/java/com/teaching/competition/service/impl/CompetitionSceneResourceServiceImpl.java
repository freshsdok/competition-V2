package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.CompetitionSceneResourceConstants;
import com.teaching.competition.domain.CompetitionSceneResource;
import com.teaching.competition.domain.CompetitionSceneResourceQuery;
import com.teaching.competition.domain.CompetitionSceneResourceStatusReq;
import com.teaching.competition.domain.CompetitionSceneResourceVO;
import com.teaching.competition.mapper.CompetitionSceneResourceMapper;
import com.teaching.competition.service.ICompetitionSceneResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 大赛现场设备资源台账Service业务层处理。
 */
@Service
public class CompetitionSceneResourceServiceImpl implements ICompetitionSceneResourceService {

    private static final Set<String> RESOURCE_TYPES = new HashSet<>(Arrays.asList(
            CompetitionSceneResourceConstants.RESOURCE_TYPE_ROOM,
            CompetitionSceneResourceConstants.RESOURCE_TYPE_LAB,
            CompetitionSceneResourceConstants.RESOURCE_TYPE_DEVICE,
            CompetitionSceneResourceConstants.RESOURCE_TYPE_WORKSTATION,
            CompetitionSceneResourceConstants.RESOURCE_TYPE_SERVER,
            CompetitionSceneResourceConstants.RESOURCE_TYPE_SOFTWARE,
            CompetitionSceneResourceConstants.RESOURCE_TYPE_OTHER
    ));

    private static final Set<String> RESOURCE_STATUSES = new HashSet<>(Arrays.asList(
            CompetitionSceneResourceConstants.RESOURCE_STATUS_ENABLED,
            CompetitionSceneResourceConstants.RESOURCE_STATUS_DISABLED,
            CompetitionSceneResourceConstants.RESOURCE_STATUS_MAINTENANCE
    ));

    @Autowired
    private CompetitionSceneResourceMapper resourceMapper;

    @Override
    public CompetitionSceneResourceVO selectCompetitionSceneResourceById(Long resourceId) {
        return resourceMapper.selectCompetitionSceneResourceById(resourceId);
    }

    @Override
    public List<CompetitionSceneResourceVO> selectCompetitionSceneResourceList(CompetitionSceneResourceQuery query) {
        return resourceMapper.selectCompetitionSceneResourceList(query);
    }

    @Override
    public int insertCompetitionSceneResource(CompetitionSceneResource resource) {
        if (resource == null) {
            throw new ServiceException("资源信息不能为空");
        }
        normalizeResource(resource);
        validateResource(resource, false);
        ensureResourceCodeUnique(resource.getResourceCode(), null);
        Date now = DateUtils.getNowDate();
        resource.setCreateBy(currentUsername());
        resource.setUpdateBy(currentUsername());
        resource.setCreateTime(now);
        resource.setUpdateTime(now);
        if (resource.getSortOrder() == null) {
            resource.setSortOrder(0);
        }
        resource.setDeleted(CompetitionSceneResourceConstants.DELETED_NO);
        return resourceMapper.insertCompetitionSceneResource(resource);
    }

    @Override
    public int updateCompetitionSceneResource(CompetitionSceneResource resource) {
        if (resource == null || resource.getResourceId() == null) {
            throw new ServiceException("资源ID不能为空");
        }
        normalizeResource(resource);
        validateResource(resource, true);
        ensureResourceCodeUnique(resource.getResourceCode(), resource.getResourceId());
        resource.setUpdateBy(currentUsername());
        resource.setUpdateTime(DateUtils.getNowDate());
        return resourceMapper.updateCompetitionSceneResource(resource);
    }

    @Override
    public int deleteCompetitionSceneResourceByIds(Long[] resourceIds) {
        if (resourceIds == null || resourceIds.length == 0) {
            throw new ServiceException("资源ID不能为空");
        }
        if (resourceMapper.countScheduleResourceByResourceIds(resourceIds) > 0) {
            throw new ServiceException("资源已布置到赛场安排，不能删除");
        }
        return resourceMapper.deleteCompetitionSceneResourceByIds(resourceIds, currentUsername());
    }

    @Override
    public int changeCompetitionSceneResourceStatus(CompetitionSceneResourceStatusReq req) {
        if (req == null || req.getResourceId() == null) {
            throw new ServiceException("资源ID不能为空");
        }
        if (!RESOURCE_STATUSES.contains(req.getResourceStatus())) {
            throw new ServiceException("资源状态不合法");
        }
        return resourceMapper.updateCompetitionSceneResourceStatus(req.getResourceId(), req.getResourceStatus(), currentUsername());
    }

    private void normalizeResource(CompetitionSceneResource resource) {
        resource.setResourceCode(trim(resource.getResourceCode()));
        resource.setResourceName(trim(resource.getResourceName()));
        resource.setResourceType(trim(resource.getResourceType()));
        resource.setResourceStatus(trim(resource.getResourceStatus()));
        if (StringUtils.isEmpty(resource.getResourceStatus())) {
            resource.setResourceStatus(CompetitionSceneResourceConstants.RESOURCE_STATUS_ENABLED);
        }
    }

    private void validateResource(CompetitionSceneResource resource, boolean update) {
        if (update && resource.getResourceId() == null) {
            throw new ServiceException("资源ID不能为空");
        }
        if (StringUtils.isEmpty(resource.getResourceCode())) {
            throw new ServiceException("资源编号不能为空");
        }
        if (StringUtils.isEmpty(resource.getResourceName())) {
            throw new ServiceException("资源名称不能为空");
        }
        if (!RESOURCE_TYPES.contains(resource.getResourceType())) {
            throw new ServiceException("资源类型不合法");
        }
        if (!RESOURCE_STATUSES.contains(resource.getResourceStatus())) {
            throw new ServiceException("资源状态不合法");
        }
        if (resource.getDeviceQuantity() == null || resource.getDeviceQuantity() <= 0) {
            throw new ServiceException("设备数量必须大于0");
        }
        if (resource.getWorkstationCount() == null || resource.getWorkstationCount() <= 0) {
            throw new ServiceException("单台设备工位数必须大于0");
        }
        if (resource.getDefaultSlotDurationMinutes() == null || resource.getDefaultSlotDurationMinutes() <= 0) {
            throw new ServiceException("默认单场周期必须大于0分钟");
        }
        if (resource.getDefaultSharedOccupancy() == null) {
            throw new ServiceException("默认共享占用不能为空");
        }
        if (resource.getNeedOpsConfirm() == null) {
            throw new ServiceException("是否需要运维确认不能为空");
        }
    }

    private void ensureResourceCodeUnique(String resourceCode, Long currentResourceId) {
        CompetitionSceneResource existed = resourceMapper.selectCompetitionSceneResourceByCode(resourceCode);
        if (existed == null) {
            return;
        }
        if (currentResourceId == null || !Objects.equals(existed.getResourceId(), currentResourceId)) {
            throw new ServiceException("资源编号已存在");
        }
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
