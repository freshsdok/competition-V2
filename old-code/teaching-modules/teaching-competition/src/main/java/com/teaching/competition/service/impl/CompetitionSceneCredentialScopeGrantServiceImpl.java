package com.teaching.competition.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneCredentialAbility;
import com.teaching.competition.domain.CompetitionSceneCredentialScopeGrant;
import com.teaching.competition.domain.CompetitionSceneSchedule;
import com.teaching.competition.mapper.CompetitionSceneCredentialScopeGrantMapper;
import com.teaching.competition.service.ICompetitionSceneCredentialScopeGrantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 赛事现场证件作用域授权Service业务层处理。
 */
@Service
public class CompetitionSceneCredentialScopeGrantServiceImpl implements ICompetitionSceneCredentialScopeGrantService {

    private static final String GRANT_STATUS_ACTIVE = "ACTIVE";
    private static final String SOURCE_TYPE_SCHEDULE_TARGET = "SCHEDULE_TARGET";
    private static final String SOURCE_TYPE_MANUAL = "MANUAL";
    private static final String SOURCE_TYPE_IMPORT = "IMPORT";
    private static final String SOURCE_TYPE_COMPETITION_DIRECT = "COMPETITION_DIRECT";
    private static final Integer DELETED_NO = 0;

    @Autowired
    private CompetitionSceneCredentialScopeGrantMapper grantMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneCredentialScopeGrant insertGrant(CompetitionSceneCredentialScopeGrant grant) {
        normalizeGrant(grant, CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE.equals(grant == null ? null : grant.getScopeType()));
        grantMapper.insertGrant(grant);
        return grant.getGrantId() == null ? grant : grantMapper.selectGrantById(grant.getGrantId());
    }

    @Override
    public List<CompetitionSceneCredentialScopeGrant> findActiveGrantsByCredential(Long credentialId) {
        if (credentialId == null) {
            return List.of();
        }
        return grantMapper.selectActiveGrantsByCredential(credentialId);
    }

    @Override
    public CompetitionSceneCredentialScopeGrant findActiveScheduleGrant(Long credentialId,
                                                                        Long scheduleId,
                                                                        Long sourceTargetId) {
        if (credentialId == null || scheduleId == null) {
            return null;
        }
        return grantMapper.selectActiveScheduleGrant(credentialId, scheduleId, sourceTargetId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized CompetitionSceneCredentialScopeGrant ensureScheduleGrant(CompetitionSceneCredentialScopeGrant grant) {
        normalizeGrant(grant, true);
        CompetitionSceneCredentialScopeGrant existed = grantMapper.selectActiveScheduleGrantForUpdate(
                grant.getCredentialId(), grant.getScopeRefId(), grant.getSourceTargetId());
        if (existed != null) {
            grant.setGrantId(existed.getGrantId());
            grant.setUpdateBy(currentUsername());
            grant.setUpdateTime(DateUtils.getNowDate());
            grantMapper.updateGrantMutableFields(grant);
            return grantMapper.selectGrantById(existed.getGrantId());
        }
        try {
            grantMapper.insertGrant(grant);
        } catch (DuplicateKeyException e) {
            CompetitionSceneCredentialScopeGrant concurrentExisted = grantMapper.selectActiveScheduleGrantForUpdate(
                    grant.getCredentialId(), grant.getScopeRefId(), grant.getSourceTargetId());
            if (concurrentExisted != null) {
                return concurrentExisted;
            }
            throw e;
        }
        return grant.getGrantId() == null ? grant : grantMapper.selectGrantById(grant.getGrantId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int revokeGrant(Long grantId) {
        if (grantId == null) {
            return 0;
        }
        return grantMapper.revokeGrant(grantId, currentUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int revokeGrantsByTarget(Long sourceScheduleId, Long sourceTargetId) {
        if (sourceScheduleId == null || sourceTargetId == null) {
            return 0;
        }
        return grantMapper.revokeGrantsByTarget(sourceScheduleId, sourceTargetId, currentUsername());
    }

    @Override
    public boolean hasAbility(CompetitionSceneCredentialScopeGrant grant, String abilityCode) {
        if (grant == null || StringUtils.isEmpty(abilityCode)) {
            return false;
        }
        String code = normalizeAbilityCode(abilityCode);
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(grant.getAbilityJson())) {
            return false;
        }
        try {
            JSONObject ability = JSON.parseObject(grant.getAbilityJson());
            Object value = ability == null ? null : ability.get(code);
            return value instanceof Boolean && Boolean.TRUE.equals(value);
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public boolean checkScheduleAbility(Long credentialId, Long scheduleId, String abilityCode) {
        if (credentialId == null || scheduleId == null || StringUtils.isEmpty(abilityCode)) {
            return false;
        }
        List<CompetitionSceneCredentialScopeGrant> grants =
                grantMapper.selectActiveScheduleGrants(credentialId, scheduleId);
        if (grants == null || grants.isEmpty()) {
            return false;
        }
        Date now = DateUtils.getNowDate();
        for (CompetitionSceneCredentialScopeGrant grant : grants) {
            if (isActiveGrant(grant) && isWithinGrantWindow(grant, now) && hasAbility(grant, abilityCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String buildDefaultScheduleGrantAbility(String credentialType) {
        CompetitionSceneCredentialAbility ability = new CompetitionSceneCredentialAbility();
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT.equals(credentialType)
                || CompetitionSceneConstants.CREDENTIAL_TYPE_COMPETITOR.equals(credentialType)) {
            ability.setReport(true);
            ability.setMaterial(true);
            ability.setWaiting(true);
            ability.setResourceReservation(true);
        } else if (CompetitionSceneConstants.CREDENTIAL_TYPE_TEACHER.equals(credentialType)) {
            ability.setReport(true);
        } else if (CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credentialType)) {
            ability.setReport(true);
            ability.setReview(true);
        }
        return JSON.toJSONString(ability);
    }

    @Override
    public String buildDefaultOperationWindowJson(CompetitionSceneSchedule schedule) {
        if (schedule == null) {
            return "{}";
        }
        JSONObject root = new JSONObject();
        root.put("report", buildWindow(schedule.getReportStartTime(), schedule.getReportEndTime(),
                schedule.getReportLocation()));
        root.put("material", buildWindow(schedule.getReportStartTime(),
                firstDate(schedule.getContestEndTime(), schedule.getReportEndTime()),
                schedule.getMaterialLocation()));
        root.put("waiting", buildWindow(schedule.getWaitingStartTime(), schedule.getWaitingEndTime(),
                schedule.getWaitingLocation()));
        root.put("contest", buildWindow(schedule.getContestStartTime(), schedule.getContestEndTime(),
                joinText(schedule.getContestLocation(), schedule.getContestRoom())));
        root.put("resourceReservation", buildWindow(
                earliestDate(schedule.getReportStartTime(), schedule.getWaitingStartTime(), schedule.getContestStartTime()),
                latestDate(schedule.getReportEndTime(), schedule.getWaitingEndTime(), schedule.getContestEndTime()),
                joinText(schedule.getContestLocation(), schedule.getContestRoom())));
        return root.toJSONString();
    }

    private void normalizeGrant(CompetitionSceneCredentialScopeGrant grant, boolean scheduleGrant) {
        if (grant == null) {
            throw new ServiceException("授权信息不能为空");
        }
        if (grant.getCredentialId() == null) {
            throw new ServiceException("授权缺少证件ID");
        }
        if (grant.getCompetitionSeriesId() == null) {
            throw new ServiceException("授权缺少赛事ID");
        }
        if (scheduleGrant) {
            grant.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE);
            if (grant.getScopeRefId() == null && grant.getSourceScheduleId() != null) {
                grant.setScopeRefId(grant.getSourceScheduleId());
            }
            if (grant.getSourceScheduleId() == null && grant.getScopeRefId() != null) {
                grant.setSourceScheduleId(grant.getScopeRefId());
            }
            if (grant.getSourceTargetId() == null) {
                throw new ServiceException("赛场授权缺少来源对象ID");
            }
            grant.setSourceType(SOURCE_TYPE_SCHEDULE_TARGET);
        }
        if (StringUtils.isEmpty(grant.getScopeType())) {
            throw new ServiceException("授权缺少作用域类型");
        }
        if (grant.getScopeRefId() == null) {
            throw new ServiceException("授权缺少作用域引用ID");
        }
        if (StringUtils.isEmpty(grant.getCredentialType())) {
            throw new ServiceException("授权缺少证件类型");
        }
        if (StringUtils.isEmpty(grant.getSubjectType()) || StringUtils.isEmpty(grant.getSubjectCode())) {
            throw new ServiceException("授权缺少主体信息");
        }
        if (StringUtils.isEmpty(grant.getSourceType())) {
            grant.setSourceType(SOURCE_TYPE_MANUAL);
        }
        if (!isAllowedSourceType(grant.getSourceType())) {
            throw new ServiceException("授权来源类型无效");
        }
        if (StringUtils.isEmpty(grant.getGrantStatus())) {
            grant.setGrantStatus(GRANT_STATUS_ACTIVE);
        }
        if (grant.getDeleted() == null) {
            grant.setDeleted(DELETED_NO);
        }
        if (StringUtils.isEmpty(grant.getAbilityJson())) {
            grant.setAbilityJson(buildDefaultScheduleGrantAbility(grant.getCredentialType()));
        }
        grant.setActiveGrantKey(buildActiveGrantKey(grant));
        grant.setGrantSnapshotJson(buildWhitelistGrantSnapshot(grant.getGrantSnapshotJson()));
        Date now = DateUtils.getNowDate();
        if (grant.getCreateTime() == null) {
            grant.setCreateTime(now);
        }
        grant.setUpdateTime(now);
        if (StringUtils.isEmpty(grant.getCreateBy())) {
            grant.setCreateBy(currentUsername());
        }
        grant.setUpdateBy(currentUsername());
    }

    private String normalizeAbilityCode(String abilityCode) {
        String code = abilityCode.trim();
        if ("IDENTITY_VERIFY".equalsIgnoreCase(code)
                || "identity_verify".equalsIgnoreCase(code)
                || "identityVerify".equals(code)) {
            return "identityVerify";
        }
        if (CompetitionSceneConstants.OPERATION_REPORT_SIGN.equals(code)
                || CompetitionSceneConstants.STATE_OPERATION_REPORT.equals(code)
                || "REPORT".equalsIgnoreCase(code)) {
            return "report";
        }
        if (CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE.equals(code)
                || CompetitionSceneConstants.STATE_OPERATION_MATERIAL.equals(code)
                || "MATERIAL".equalsIgnoreCase(code)) {
            return "material";
        }
        if (CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN.equals(code)
                || CompetitionSceneConstants.STATE_OPERATION_WAITING.equals(code)
                || "WAITING".equalsIgnoreCase(code)) {
            return "waiting";
        }
        if ("SCHEDULE_ENTRY".equalsIgnoreCase(code)
                || "schedule_entry".equalsIgnoreCase(code)
                || "scheduleEntry".equals(code)
                || "CONTEST_ENTRY".equalsIgnoreCase(code)) {
            return "scheduleEntry";
        }
        if (CompetitionSceneConstants.OPERATION_EXPERT_REVIEW_ENTRY.equals(code)
                || "REVIEW".equalsIgnoreCase(code)) {
            return "review";
        }
        if ("RESOURCE_RESERVATION".equalsIgnoreCase(code)
                || "RESOURCE_RESERVATION".equals(code)
                || "resource_reservation".equalsIgnoreCase(code)) {
            return "resourceReservation";
        }
        if ("VIP_ACCESS".equalsIgnoreCase(code)
                || "vip_access".equalsIgnoreCase(code)) {
            return "vipAccess";
        }
        return null;
    }

    private boolean isActiveGrant(CompetitionSceneCredentialScopeGrant grant) {
        return grant != null
                && GRANT_STATUS_ACTIVE.equals(grant.getGrantStatus())
                && DELETED_NO.equals(grant.getDeleted());
    }

    private boolean isAllowedSourceType(String sourceType) {
        return SOURCE_TYPE_SCHEDULE_TARGET.equals(sourceType)
                || SOURCE_TYPE_MANUAL.equals(sourceType)
                || SOURCE_TYPE_IMPORT.equals(sourceType)
                || SOURCE_TYPE_COMPETITION_DIRECT.equals(sourceType);
    }

    private String buildActiveGrantKey(CompetitionSceneCredentialScopeGrant grant) {
        if (!isActiveGrant(grant)) {
            return null;
        }
        return grant.getCredentialId()
                + ":" + grant.getScopeType()
                + ":" + grant.getScopeRefId()
                + ":" + (grant.getSourceTargetId() == null ? "NULL" : grant.getSourceTargetId());
    }

    private boolean isWithinGrantWindow(CompetitionSceneCredentialScopeGrant grant, Date now) {
        if (grant == null || now == null) {
            return false;
        }
        if (grant.getValidFrom() != null && grant.getValidFrom().after(now)) {
            return false;
        }
        return grant.getValidTo() == null || !grant.getValidTo().before(now);
    }

    private JSONObject buildWindow(Date start, Date end, String location) {
        JSONObject window = new JSONObject();
        window.put("startTime", formatDate(start));
        window.put("endTime", formatDate(end));
        window.put("location", location);
        return window;
    }

    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private Date firstDate(Date... dates) {
        if (dates == null) {
            return null;
        }
        for (Date date : dates) {
            if (date != null) {
                return date;
            }
        }
        return null;
    }

    private Date earliestDate(Date... dates) {
        Date result = null;
        if (dates == null) {
            return null;
        }
        for (Date date : dates) {
            if (date != null && (result == null || date.before(result))) {
                result = date;
            }
        }
        return result;
    }

    private Date latestDate(Date... dates) {
        Date result = null;
        if (dates == null) {
            return null;
        }
        for (Date date : dates) {
            if (date != null && (result == null || date.after(result))) {
                result = date;
            }
        }
        return result;
    }

    private String joinText(String... values) {
        StringBuilder builder = new StringBuilder();
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.isEmpty(value)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(" / ");
            }
            builder.append(value.trim());
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private String buildWhitelistGrantSnapshot(String raw) {
        if (StringUtils.isEmpty(raw)) {
            return null;
        }
        try {
            Object parsed = JSON.parse(raw);
            JSONObject snapshot = new JSONObject();
            collectAllowedSnapshotFields(parsed, snapshot);
            return snapshot.isEmpty() ? null : snapshot.toJSONString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private void collectAllowedSnapshotFields(Object value, JSONObject snapshot) {
        if (value instanceof JSONObject) {
            JSONObject source = (JSONObject) value;
            for (String key : source.keySet()) {
                Object item = source.get(key);
                String normalizedKey = normalizeSnapshotKey(key);
                if (isAllowedSnapshotKey(normalizedKey) && !snapshot.containsKey(normalizedKey)) {
                    snapshot.put(normalizedKey, item);
                } else {
                    collectAllowedSnapshotFields(item, snapshot);
                }
            }
        } else if (value instanceof JSONArray) {
            JSONArray source = (JSONArray) value;
            for (Object item : source) {
                collectAllowedSnapshotFields(item, snapshot);
            }
        }
    }

    private String normalizeSnapshotKey(String key) {
        if (StringUtils.isEmpty(key)) {
            return "";
        }
        String normalized = key.replace("_", "").replace("-", "");
        if ("scheduleid".equalsIgnoreCase(normalized)) {
            return "scheduleId";
        }
        if ("schedulename".equalsIgnoreCase(normalized)) {
            return "scheduleName";
        }
        if ("targetid".equalsIgnoreCase(normalized)) {
            return "targetId";
        }
        if ("targetname".equalsIgnoreCase(normalized) || "username".equalsIgnoreCase(normalized)
                || "teamname".equalsIgnoreCase(normalized)) {
            return "targetName";
        }
        if ("rolecode".equalsIgnoreCase(normalized) || "competitionrolename".equalsIgnoreCase(normalized)) {
            return "roleCode";
        }
        if ("credentialtype".equalsIgnoreCase(normalized)) {
            return "credentialType";
        }
        if ("teamcode".equalsIgnoreCase(normalized)) {
            return "teamCode";
        }
        if ("subjecttype".equalsIgnoreCase(normalized)) {
            return "subjectType";
        }
        if ("subjectcode".equalsIgnoreCase(normalized)) {
            return "subjectCode";
        }
        if ("groupcode".equalsIgnoreCase(normalized) || "waitinggroupcode".equalsIgnoreCase(normalized)) {
            return "groupCode";
        }
        if ("groupname".equalsIgnoreCase(normalized) || "waitinggroupname".equalsIgnoreCase(normalized)) {
            return "groupName";
        }
        return "";
    }

    private boolean isAllowedSnapshotKey(String key) {
        return "scheduleId".equals(key)
                || "scheduleName".equals(key)
                || "targetId".equals(key)
                || "targetName".equals(key)
                || "roleCode".equals(key)
                || "credentialType".equals(key)
                || "teamCode".equals(key)
                || "subjectType".equals(key)
                || "subjectCode".equals(key)
                || "groupCode".equals(key)
                || "groupName".equals(key);
    }

    private String currentUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception e) {
            return "system";
        }
    }
}
