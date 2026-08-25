package com.teaching.competition.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.CompetitionSceneResourceConstants;
import com.teaching.competition.domain.CompetitionSceneResourceSlot;
import com.teaching.competition.domain.CompetitionSceneResourceSlotBatchReq;
import com.teaching.competition.domain.CompetitionSceneResourceSlotGroupScope;
import com.teaching.competition.domain.CompetitionSceneResourceSlotQuery;
import com.teaching.competition.domain.CompetitionSceneResourceSlotStatusReq;
import com.teaching.competition.domain.CompetitionSceneResourceSlotVO;
import com.teaching.competition.domain.CompetitionSceneScheduleResource;
import com.teaching.competition.mapper.CompetitionSceneResourceSlotMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleResourceMapper;
import com.teaching.competition.service.ICompetitionSceneResourceSlotGroupScopeService;
import com.teaching.competition.service.ICompetitionSceneResourceSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 大赛现场设备资源预约时段Service业务层处理。
 */
@Service
public class CompetitionSceneResourceSlotServiceImpl implements ICompetitionSceneResourceSlotService {

    private static final Set<String> SLOT_STATUSES = new HashSet<>(Arrays.asList(
            CompetitionSceneResourceConstants.SLOT_STATUS_PENDING,
            CompetitionSceneResourceConstants.SLOT_STATUS_OPEN,
            CompetitionSceneResourceConstants.SLOT_STATUS_FULL,
            CompetitionSceneResourceConstants.SLOT_STATUS_CLOSED,
            CompetitionSceneResourceConstants.SLOT_STATUS_EXPIRED
    ));

    @Autowired
    private CompetitionSceneResourceSlotMapper slotMapper;

    @Autowired
    private CompetitionSceneScheduleResourceMapper scheduleResourceMapper;

    @Override
    public CompetitionSceneResourceSlotVO selectCompetitionSceneResourceSlotById(Long slotId) {
        CompetitionSceneResourceSlotVO slot = slotMapper.selectCompetitionSceneResourceSlotById(slotId);
        fillAllowedGroupNames(slot);
        return slot;
    }

    @Override
    public List<CompetitionSceneResourceSlotVO> selectCompetitionSceneResourceSlotList(CompetitionSceneResourceSlotQuery query) {
        List<CompetitionSceneResourceSlotVO> list = slotMapper.selectCompetitionSceneResourceSlotList(query);
        list.forEach(this::fillAllowedGroupNames);
        return list;
    }

    @Autowired
    private ICompetitionSceneResourceSlotGroupScopeService slotGroupScopeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCompetitionSceneResourceSlot(CompetitionSceneResourceSlot slot) {
        if (slot == null) {
            throw new ServiceException("预约时段不能为空");
        }
        CompetitionSceneScheduleResource scheduleResource = loadScheduleResource(slot.getScheduleResourceId());
        applyScheduleResource(slot, scheduleResource);
        normalizeSlot(slot);
        validateSlot(slot, scheduleResource, false, null);
        initCapacity(slot, scheduleResource, 0, 0);
        Date now = DateUtils.getNowDate();
        slot.setCreateBy(currentUsername());
        slot.setUpdateBy(currentUsername());
        slot.setCreateTime(now);
        slot.setUpdateTime(now);
        slot.setVersion(0L);
        slot.setDeleted(CompetitionSceneResourceConstants.DELETED_NO);
        int rows = slotMapper.insertCompetitionSceneResourceSlot(slot);
        replaceGroupsIfPresent(slot.getSlotId(), slot.getScheduleResourceId(), slot.getAllowedGroups());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchGenerateCompetitionSceneResourceSlot(CompetitionSceneResourceSlotBatchReq req) {
        if (req == null) {
            throw new ServiceException("批量生成参数不能为空");
        }
        CompetitionSceneScheduleResource scheduleResource = loadScheduleResource(req.getScheduleResourceId());
        String slotStatus = normalizeSlotStatus(req.getSlotStatus());
        if (!SLOT_STATUSES.contains(slotStatus)) {
            throw new ServiceException("时段状态不合法");
        }
        if (req.getStartTime() == null || req.getEndTime() == null) {
            throw new ServiceException("批量生成开始和结束时间不能为空");
        }
        if (!req.getEndTime().after(req.getStartTime())) {
            throw new ServiceException("批量生成结束时间必须晚于开始时间");
        }
        if (req.getSlotDurationMinutes() == null || req.getSlotDurationMinutes() <= 0) {
            throw new ServiceException("每场时长必须大于0分钟");
        }
        validateDeviceCapacity(req.getDeviceCapacity(), scheduleResource);

        long durationMillis = req.getSlotDurationMinutes() * 60_000L;
        long cursor = req.getStartTime().getTime();
        long end = req.getEndTime().getTime();
        List<CompetitionSceneResourceSlot> slots = new ArrayList<>();
        Date now = DateUtils.getNowDate();
        String username = currentUsername();
        while (cursor + durationMillis <= end) {
            CompetitionSceneResourceSlot slot = new CompetitionSceneResourceSlot();
            slot.setScheduleResourceId(scheduleResource.getScheduleResourceId());
            slot.setStartTime(new Date(cursor));
            slot.setEndTime(new Date(cursor + durationMillis));
            slot.setDeviceCapacity(req.getDeviceCapacity());
            slot.setSlotStatus(slotStatus);
            validateNoOverlap(scheduleResource.getScheduleResourceId(), slot.getStartTime(), slot.getEndTime(), null);
            applyScheduleResource(slot, scheduleResource);
            initCapacity(slot, scheduleResource, 0, 0);
            slot.setCreateBy(username);
            slot.setUpdateBy(username);
            slot.setCreateTime(now);
            slot.setUpdateTime(now);
            slot.setVersion(0L);
            slot.setDeleted(CompetitionSceneResourceConstants.DELETED_NO);
            slots.add(slot);
            cursor += durationMillis;
        }
        if (slots.isEmpty()) {
            throw new ServiceException("批量生成时间范围不足以生成一个完整时段");
        }
        if (req.getAllowedGroups() == null || req.getAllowedGroups().isEmpty()) {
            return slotMapper.batchInsertCompetitionSceneResourceSlot(slots);
        }
        int affected = 0;
        for (CompetitionSceneResourceSlot slot : slots) {
            affected += slotMapper.insertCompetitionSceneResourceSlot(slot);
            replaceGroupsIfPresent(slot.getSlotId(), slot.getScheduleResourceId(), req.getAllowedGroups());
        }
        return affected;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCompetitionSceneResourceSlot(CompetitionSceneResourceSlot slot) {
        if (slot == null || slot.getSlotId() == null) {
            throw new ServiceException("预约时段ID不能为空");
        }
        CompetitionSceneResourceSlot existed = slotMapper.selectCompetitionSceneResourceSlotEntityById(slot.getSlotId());
        if (existed == null) {
            throw new ServiceException("预约时段不存在");
        }
        CompetitionSceneScheduleResource scheduleResource = loadScheduleResource(existed.getScheduleResourceId());
        slot.setScheduleResourceId(existed.getScheduleResourceId());
        applyScheduleResource(slot, scheduleResource);
        normalizeSlot(slot);
        validateSlot(slot, scheduleResource, true, existed);
        initCapacity(slot, scheduleResource,
                defaultZero(existed.getReservedDeviceCount()),
                defaultZero(existed.getReservedWorkstationCount()));
        slot.setUpdateBy(currentUsername());
        slot.setUpdateTime(DateUtils.getNowDate());
        int rows = slotMapper.updateCompetitionSceneResourceSlot(slot);
        replaceGroupsIfPresent(slot.getSlotId(), slot.getScheduleResourceId(), slot.getAllowedGroups());
        return rows;
    }

    @Override
    public int deleteCompetitionSceneResourceSlotByIds(Long[] slotIds) {
        if (slotIds == null || slotIds.length == 0) {
            throw new ServiceException("预约时段ID不能为空");
        }
        List<CompetitionSceneResourceSlot> slots = slotMapper.selectCompetitionSceneResourceSlotEntitiesByIds(slotIds);
        for (CompetitionSceneResourceSlot slot : slots) {
            if (defaultZero(slot.getReservedDeviceCount()) > 0 || defaultZero(slot.getReservedWorkstationCount()) > 0) {
                throw new ServiceException("已存在预约占用的时段不能删除");
            }
        }
        return slotMapper.deleteCompetitionSceneResourceSlotByIds(slotIds, currentUsername());
    }

    @Override
    public int changeCompetitionSceneResourceSlotStatus(CompetitionSceneResourceSlotStatusReq req) {
        if (req == null || req.getSlotId() == null) {
            throw new ServiceException("预约时段ID不能为空");
        }
        String slotStatus = normalizeSlotStatus(req.getSlotStatus());
        if (!SLOT_STATUSES.contains(slotStatus)) {
            throw new ServiceException("时段状态不合法");
        }
        CompetitionSceneResourceSlot existed = slotMapper.selectCompetitionSceneResourceSlotEntityById(req.getSlotId());
        if (existed == null) {
            throw new ServiceException("预约时段不存在");
        }
        if (CompetitionSceneResourceConstants.SLOT_STATUS_OPEN.equals(slotStatus)
                && defaultZero(existed.getRemainingDeviceCount()) <= 0) {
            throw new ServiceException("剩余设备数不足，不能开放时段");
        }
        return slotMapper.updateCompetitionSceneResourceSlotStatus(req.getSlotId(), slotStatus, currentUsername());
    }

    private CompetitionSceneScheduleResource loadScheduleResource(Long scheduleResourceId) {
        if (scheduleResourceId == null) {
            throw new ServiceException("赛场资源布置ID不能为空");
        }
        CompetitionSceneScheduleResource scheduleResource =
                scheduleResourceMapper.selectCompetitionSceneScheduleResourceEntityById(scheduleResourceId);
        if (scheduleResource == null) {
            throw new ServiceException("赛场资源布置不存在");
        }
        return scheduleResource;
    }

    private void applyScheduleResource(CompetitionSceneResourceSlot slot,
                                       CompetitionSceneScheduleResource scheduleResource) {
        slot.setScheduleResourceId(scheduleResource.getScheduleResourceId());
        slot.setScheduleId(scheduleResource.getScheduleId());
        slot.setResourceId(scheduleResource.getResourceId());
        slot.setEventId(scheduleResource.getEventId());
    }

    private void normalizeSlot(CompetitionSceneResourceSlot slot) {
        slot.setSlotStatus(normalizeSlotStatus(slot.getSlotStatus()));
    }

    private String normalizeSlotStatus(String slotStatus) {
        String value = trim(slotStatus);
        return StringUtils.isEmpty(value) ? CompetitionSceneResourceConstants.SLOT_STATUS_PENDING : value;
    }

    private void validateSlot(CompetitionSceneResourceSlot slot,
                              CompetitionSceneScheduleResource scheduleResource,
                              boolean update,
                              CompetitionSceneResourceSlot existed) {
        if (update && slot.getSlotId() == null) {
            throw new ServiceException("预约时段ID不能为空");
        }
        if (slot.getStartTime() == null || slot.getEndTime() == null) {
            throw new ServiceException("时段开始和结束时间不能为空");
        }
        if (!slot.getEndTime().after(slot.getStartTime())) {
            throw new ServiceException("时段结束时间必须晚于开始时间");
        }
        validateDeviceCapacity(slot.getDeviceCapacity(), scheduleResource);
        if (!SLOT_STATUSES.contains(slot.getSlotStatus())) {
            throw new ServiceException("时段状态不合法");
        }
        validateNoOverlap(scheduleResource.getScheduleResourceId(), slot.getStartTime(), slot.getEndTime(),
                update ? slot.getSlotId() : null);
        if (existed != null) {
            Integer workstationCapacity = slot.getDeviceCapacity() * scheduleResource.getWorkstationsPerDevice();
            if (slot.getDeviceCapacity() < defaultZero(existed.getReservedDeviceCount())) {
                throw new ServiceException("设备容量不能小于已预约设备数");
            }
            if (workstationCapacity < defaultZero(existed.getReservedWorkstationCount())) {
                throw new ServiceException("工位容量不能小于已预约工位数");
            }
        }
    }

    private void validateNoOverlap(Long scheduleResourceId, Date startTime, Date endTime, Long excludeSlotId) {
        int count = slotMapper.countOverlappingSlots(scheduleResourceId, startTime, endTime, excludeSlotId);
        if (count > 0) {
            throw new ServiceException("预约时段与已有时段重叠");
        }
    }

    private void validateDeviceCapacity(Integer deviceCapacity, CompetitionSceneScheduleResource scheduleResource) {
        if (deviceCapacity == null || deviceCapacity <= 0) {
            throw new ServiceException("设备容量必须大于0");
        }
        if (deviceCapacity > scheduleResource.getDeployedDeviceCount()) {
            throw new ServiceException("设备容量不能大于部署设备数");
        }
    }

    private void initCapacity(CompetitionSceneResourceSlot slot,
                              CompetitionSceneScheduleResource scheduleResource,
                              Integer reservedDeviceCount,
                              Integer reservedWorkstationCount) {
        int reservedDevices = defaultZero(reservedDeviceCount);
        int reservedWorkstations = defaultZero(reservedWorkstationCount);
        int workstationCapacity = slot.getDeviceCapacity() * scheduleResource.getWorkstationsPerDevice();
        slot.setWorkstationCount(scheduleResource.getWorkstationsPerDevice());
        slot.setTotalDeviceCount(slot.getDeviceCapacity());
        slot.setTotalWorkstationCount(workstationCapacity);
        slot.setReservedDeviceCount(reservedDevices);
        slot.setReservedWorkstationCount(reservedWorkstations);
        slot.setWorkstationCapacity(workstationCapacity);
        slot.setRemainingDeviceCount(slot.getDeviceCapacity() - reservedDevices);
        slot.setRemainingWorkstationCount(workstationCapacity - reservedWorkstations);
    }

    private void replaceGroupsIfPresent(Long slotId,
                                        Long scheduleResourceId,
                                        List<CompetitionSceneResourceSlotGroupScope> allowedGroups) {
        if (allowedGroups == null) {
            return;
        }
        slotGroupScopeService.replaceSlotGroups(slotId, scheduleResourceId, allowedGroups);
    }

    private void fillAllowedGroupNames(CompetitionSceneResourceSlotVO slot) {
        if (slot == null || slot.getSlotId() == null) {
            return;
        }
        List<CompetitionSceneResourceSlotGroupScope> scopes = slotGroupScopeService.listBySlotId(slot.getSlotId());
        List<String> names = scopes.stream()
                .filter(scope -> scope != null && Integer.valueOf(1).equals(scope.getEnabled())
                        && CompetitionSceneResourceConstants.DELETED_NO.equals(scope.getDeleted()))
                .map(scope -> StringUtils.isNotEmpty(trim(scope.getAllowedGroupName()))
                        ? trim(scope.getAllowedGroupName()) : trim(scope.getAllowedGroupCode()))
                .filter(StringUtils::isNotEmpty)
                .distinct()
                .collect(Collectors.toList());
        slot.setAllowedGroupNames(names);
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
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
