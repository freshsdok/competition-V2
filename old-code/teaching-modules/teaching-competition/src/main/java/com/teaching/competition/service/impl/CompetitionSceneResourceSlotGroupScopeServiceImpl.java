package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.CompetitionSceneResourceConstants;
import com.teaching.competition.domain.CompetitionSceneResourceSlotGroupScope;
import com.teaching.competition.mapper.CompetitionSceneResourceSlotGroupScopeMapper;
import com.teaching.competition.service.ICompetitionSceneResourceSlotGroupScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 赛事现场资源预约时段允许组别Service业务层处理。
 */
@Service
public class CompetitionSceneResourceSlotGroupScopeServiceImpl
        implements ICompetitionSceneResourceSlotGroupScopeService {

    private static final Integer ENABLED_YES = 1;

    @Autowired
    private CompetitionSceneResourceSlotGroupScopeMapper slotGroupScopeMapper;

    @Override
    public List<CompetitionSceneResourceSlotGroupScope> listBySlotId(Long slotId) {
        if (slotId == null) {
            return List.of();
        }
        return slotGroupScopeMapper.selectBySlotId(slotId);
    }

    @Override
    public List<CompetitionSceneResourceSlotGroupScope> listByScheduleResourceId(Long scheduleResourceId) {
        if (scheduleResourceId == null) {
            return List.of();
        }
        return slotGroupScopeMapper.selectByScheduleResourceId(scheduleResourceId);
    }

    @Override
    public boolean existsAllowedGroup(Long slotId, String groupCode) {
        if (slotId == null || StringUtils.isEmpty(trim(groupCode))) {
            return false;
        }
        return slotGroupScopeMapper.countAllowedGroup(slotId, trim(groupCode)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int replaceSlotGroups(Long slotId,
                                 Long scheduleResourceId,
                                 List<CompetitionSceneResourceSlotGroupScope> groups) {
        if (slotId == null) {
            throw new ServiceException("预约时段ID不能为空");
        }
        if (scheduleResourceId == null) {
            throw new ServiceException("赛场资源布置ID不能为空");
        }
        int affected = slotGroupScopeMapper.logicalDeleteBySlotId(slotId, currentUsername());
        List<CompetitionSceneResourceSlotGroupScope> normalized = normalizeGroups(slotId, scheduleResourceId, groups);
        if (!normalized.isEmpty()) {
            affected += slotGroupScopeMapper.batchInsertScopes(normalized);
        }
        return affected;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchReplaceSlotGroups(List<CompetitionSceneResourceSlotGroupScope> groups) {
        if (groups == null || groups.isEmpty()) {
            return 0;
        }
        Map<Long, List<CompetitionSceneResourceSlotGroupScope>> slotGroupMap = new LinkedHashMap<>();
        Map<Long, Long> scheduleResourceMap = new LinkedHashMap<>();
        for (CompetitionSceneResourceSlotGroupScope group : groups) {
            if (group == null || group.getSlotId() == null) {
                continue;
            }
            slotGroupMap.computeIfAbsent(group.getSlotId(), key -> new ArrayList<>()).add(group);
            if (group.getScheduleResourceId() != null) {
                scheduleResourceMap.put(group.getSlotId(), group.getScheduleResourceId());
            }
        }
        int affected = 0;
        for (Map.Entry<Long, List<CompetitionSceneResourceSlotGroupScope>> entry : slotGroupMap.entrySet()) {
            Long slotId = entry.getKey();
            Long scheduleResourceId = scheduleResourceMap.get(slotId);
            affected += replaceSlotGroups(slotId, scheduleResourceId, entry.getValue());
        }
        return affected;
    }

    @Override
    public boolean isSlotGroupAllowed(Long slotId, String groupCode) {
        if (slotId == null) {
            return false;
        }
        if (slotGroupScopeMapper.countEnabledBySlotId(slotId) <= 0) {
            return true;
        }
        return existsAllowedGroup(slotId, groupCode);
    }

    private List<CompetitionSceneResourceSlotGroupScope> normalizeGroups(
            Long slotId,
            Long scheduleResourceId,
            List<CompetitionSceneResourceSlotGroupScope> groups) {
        List<CompetitionSceneResourceSlotGroupScope> normalized = new ArrayList<>();
        if (groups == null || groups.isEmpty()) {
            return normalized;
        }
        Map<String, CompetitionSceneResourceSlotGroupScope> uniqueMap = new LinkedHashMap<>();
        for (CompetitionSceneResourceSlotGroupScope group : groups) {
            if (group == null || StringUtils.isEmpty(trim(group.getAllowedGroupCode()))) {
                continue;
            }
            group.setSlotId(slotId);
            group.setScheduleResourceId(scheduleResourceId);
            group.setAllowedGroupCode(trim(group.getAllowedGroupCode()));
            group.setAllowedGroupName(trim(group.getAllowedGroupName()));
            group.setEnabled(ENABLED_YES);
            group.setDeleted(CompetitionSceneResourceConstants.DELETED_NO);
            group.setCreateBy(currentUsername());
            group.setUpdateBy(currentUsername());
            group.setCreateTime(DateUtils.getNowDate());
            group.setUpdateTime(DateUtils.getNowDate());
            uniqueMap.put(group.getAllowedGroupCode(), group);
        }
        normalized.addAll(uniqueMap.values());
        return normalized;
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

