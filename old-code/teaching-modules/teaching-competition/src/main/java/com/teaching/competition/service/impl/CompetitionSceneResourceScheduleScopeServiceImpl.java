package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.CompetitionSceneResourceConstants;
import com.teaching.competition.domain.CompetitionSceneResourceScheduleScope;
import com.teaching.competition.mapper.CompetitionSceneResourceScheduleScopeMapper;
import com.teaching.competition.service.ICompetitionSceneResourceScheduleScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 赛事现场资源允许预约赛场范围Service业务层处理。
 */
@Service
public class CompetitionSceneResourceScheduleScopeServiceImpl
        implements ICompetitionSceneResourceScheduleScopeService {

    private static final String SOURCE_TYPE_MANUAL_BIND = "MANUAL_BIND";
    private static final Integer ENABLED_YES = 1;

    @Autowired
    private CompetitionSceneResourceScheduleScopeMapper scheduleScopeMapper;

    @Override
    public List<CompetitionSceneResourceScheduleScope> listByScheduleResourceId(Long scheduleResourceId) {
        if (scheduleResourceId == null) {
            return List.of();
        }
        return scheduleScopeMapper.selectByScheduleResourceId(scheduleResourceId);
    }

    @Override
    public List<Long> listAllowedScheduleIds(Long scheduleResourceId) {
        if (scheduleResourceId == null) {
            return List.of();
        }
        return scheduleScopeMapper.selectAllowedScheduleIds(scheduleResourceId);
    }

    @Override
    public boolean existsAllowedSchedule(Long scheduleResourceId, Long allowedScheduleId) {
        if (scheduleResourceId == null || allowedScheduleId == null) {
            return false;
        }
        return scheduleScopeMapper.countEnabledScope(scheduleResourceId, allowedScheduleId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneResourceScheduleScope addManualBindSchedule(Long scheduleResourceId,
                                                                       Long resourceId,
                                                                       Long allowedScheduleId) {
        validateManualBind(scheduleResourceId, resourceId, allowedScheduleId);
        CompetitionSceneResourceScheduleScope existed =
                scheduleScopeMapper.selectEnabledScope(scheduleResourceId, allowedScheduleId);
        if (existed != null) {
            return existed;
        }
        CompetitionSceneResourceScheduleScope scope = buildManualBindScope(scheduleResourceId, resourceId,
                allowedScheduleId);
        scheduleScopeMapper.insertScope(scope);
        return scope;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeManualBindSchedule(Long scheduleResourceId, Long allowedScheduleId) {
        if (scheduleResourceId == null || allowedScheduleId == null) {
            return 0;
        }
        return scheduleScopeMapper.logicalDeleteManualBind(scheduleResourceId, allowedScheduleId, currentUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneResourceScheduleScope ensureManualBindSchedule(Long scheduleResourceId,
                                                                         Long resourceId,
                                                                         Long allowedScheduleId) {
        return addManualBindSchedule(scheduleResourceId, resourceId, allowedScheduleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CompetitionSceneResourceScheduleScope> batchEnsureManualBindSchedules(Long scheduleResourceId,
                                                                                     Long resourceId,
                                                                                     List<Long> allowedScheduleIds) {
        if (allowedScheduleIds == null || allowedScheduleIds.isEmpty()) {
            throw new ServiceException("允许预约赛场不能为空");
        }
        Set<Long> uniqueScheduleIds = new LinkedHashSet<>();
        for (Long allowedScheduleId : allowedScheduleIds) {
            if (allowedScheduleId != null) {
                uniqueScheduleIds.add(allowedScheduleId);
            }
        }
        if (uniqueScheduleIds.isEmpty()) {
            throw new ServiceException("允许预约赛场不能为空");
        }

        List<CompetitionSceneResourceScheduleScope> result = new ArrayList<>();
        for (Long allowedScheduleId : uniqueScheduleIds) {
            result.add(addManualBindSchedule(scheduleResourceId, resourceId, allowedScheduleId));
        }
        return result;
    }

    private CompetitionSceneResourceScheduleScope buildManualBindScope(Long scheduleResourceId,
                                                                       Long resourceId,
                                                                       Long allowedScheduleId) {
        CompetitionSceneResourceScheduleScope scope = new CompetitionSceneResourceScheduleScope();
        scope.setScheduleResourceId(scheduleResourceId);
        scope.setResourceId(resourceId);
        scope.setAllowedScheduleId(allowedScheduleId);
        scope.setSourceType(SOURCE_TYPE_MANUAL_BIND);
        scope.setEnabled(ENABLED_YES);
        scope.setDeleted(CompetitionSceneResourceConstants.DELETED_NO);
        scope.setCreateBy(currentUsername());
        scope.setUpdateBy(currentUsername());
        scope.setCreateTime(DateUtils.getNowDate());
        scope.setUpdateTime(DateUtils.getNowDate());
        return scope;
    }

    private void validateManualBind(Long scheduleResourceId, Long resourceId, Long allowedScheduleId) {
        if (scheduleResourceId == null) {
            throw new ServiceException("赛场资源布置ID不能为空");
        }
        if (resourceId == null) {
            throw new ServiceException("资源ID不能为空");
        }
        if (allowedScheduleId == null) {
            throw new ServiceException("允许预约赛场ID不能为空");
        }
    }

    private String currentUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception e) {
            return "system";
        }
    }
}
