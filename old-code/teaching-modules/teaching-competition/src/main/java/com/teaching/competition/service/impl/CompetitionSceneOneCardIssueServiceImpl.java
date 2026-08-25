package com.teaching.competition.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionSceneCredentialScopeGrant;
import com.teaching.competition.domain.CompetitionSceneOneCardIssueResult;
import com.teaching.competition.domain.CompetitionSceneSchedule;
import com.teaching.competition.domain.CompetitionSceneScheduleTarget;
import com.teaching.competition.mapper.CompetitionMainInfoMapper;
import com.teaching.competition.mapper.CompetitionSceneCredentialMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleTargetMapper;
import com.teaching.competition.service.ICompetitionSceneCredentialScopeGrantService;
import com.teaching.competition.service.ICompetitionSceneOneCardIssueService;
import com.teaching.system.api.domain.CompetitionDetailInfo;
import com.teaching.system.api.domain.CompetitionMainInfoReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 现场一证多权旁路发证业务。
 *
 * <p>该服务只服务阶段 2 旁路试运行，不接入旧发证、旧扫码和前端主流程。</p>
 */
@Service
public class CompetitionSceneOneCardIssueServiceImpl implements ICompetitionSceneOneCardIssueService {

    private static final int CREDENTIAL_INSERT_MAX_RETRY = 10;
    private static final String GRANT_STATUS_ACTIVE = "ACTIVE";
    private static final String SOURCE_TYPE_SCHEDULE_TARGET = "SCHEDULE_TARGET";

    @Autowired
    private CompetitionSceneScheduleTargetMapper targetMapper;

    @Autowired
    private CompetitionSceneScheduleMapper scheduleMapper;

    @Autowired
    private CompetitionSceneCredentialMapper credentialMapper;

    @Autowired
    private ICompetitionSceneCredentialScopeGrantService grantService;

    @Autowired
    private CompetitionMainInfoMapper competitionMainInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneOneCardIssueResult issueOneCardByTarget(Long targetId) {
        return issueOneCardByScheduleTarget(null, targetId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized CompetitionSceneOneCardIssueResult issueOneCardByScheduleTarget(Long scheduleId, Long targetId) {
        if (targetId == null) {
            throw new ServiceException("赛场对象ID不能为空");
        }
        CompetitionSceneScheduleTarget target = targetMapper.selectCompetitionSceneScheduleTargetById(targetId);
        validateTarget(target);
        if (scheduleId != null && !scheduleId.equals(target.getScheduleId())) {
            throw new ServiceException("赛场对象不属于指定赛场安排");
        }

        CompetitionSceneSchedule schedule = scheduleMapper.selectCompetitionSceneScheduleById(target.getScheduleId());
        validateSchedule(schedule);
        Long competitionSeriesId = resolveCompetitionSeriesId(schedule, target);
        CompetitionDetailInfo competition = selectCompetitionDetailOrNull(competitionSeriesId);
        String credentialType = resolveCredentialType(schedule, target);
        String roleCode = resolveRoleCode(target, credentialType);
        SubjectIdentity subject = resolveSubjectIdentity(schedule, target, credentialType);

        CompetitionSceneCredential credential = selectExistingCoreCredential(
                competitionSeriesId, subject.subjectType, subject.subjectCode, credentialType);
        boolean reusedCredential = credential != null;
        if (credential == null) {
            credential = buildCoreCredential(schedule, target, competition, competitionSeriesId,
                    credentialType, roleCode, subject);
            CoreCredentialIssueOutcome outcome = insertCoreCredentialWithRetry(competitionSeriesId,
                    subject.subjectType, subject.subjectCode, credentialType, credential);
            credential = outcome.credential;
            reusedCredential = outcome.reusedCredential;
        }

        CompetitionSceneCredentialScopeGrant existedGrant = grantService.findActiveScheduleGrant(
                credential.getCredentialId(), schedule.getScheduleId(), target.getTargetId());
        CompetitionSceneCredentialScopeGrant grant = buildScheduleGrant(credential, schedule, target,
                competitionSeriesId, credentialType, roleCode, subject);
        CompetitionSceneCredentialScopeGrant savedGrant = grantService.ensureScheduleGrant(grant);

        CompetitionSceneOneCardIssueResult result = new CompetitionSceneOneCardIssueResult();
        result.setCredentialId(credential.getCredentialId());
        result.setGrantId(savedGrant == null ? null : savedGrant.getGrantId());
        result.setReusedCredential(reusedCredential);
        result.setReusedGrant(existedGrant != null);
        result.setAlreadyGranted(existedGrant != null);
        result.setCompetitionSeriesId(competitionSeriesId);
        result.setSubjectType(subject.subjectType);
        result.setSubjectCode(subject.subjectCode);
        result.setCredentialType(credentialType);
        result.setRoleCode(roleCode);
        result.setScheduleId(schedule.getScheduleId());
        result.setTargetId(target.getTargetId());
        return result;
    }

    private void validateTarget(CompetitionSceneScheduleTarget target) {
        if (target == null || CompetitionSceneConstants.DEL_FLAG_DELETED.equals(target.getDelFlag())) {
            throw new ServiceException("赛场对象不存在");
        }
        if (CompetitionSceneConstants.STATUS_DISABLED.equals(target.getStatus())) {
            throw new ServiceException("赛场对象已停用");
        }
        if (CompetitionSceneConstants.MATCH_STATUS_INVALID.equals(target.getMatchStatus())) {
            throw new ServiceException("赛场对象匹配状态无效");
        }
        if (target.getScheduleId() == null) {
            throw new ServiceException("赛场对象缺少赛场安排ID");
        }
    }

    private void validateSchedule(CompetitionSceneSchedule schedule) {
        if (schedule == null || CompetitionSceneConstants.DEL_FLAG_DELETED.equals(schedule.getDelFlag())) {
            throw new ServiceException("赛场安排不存在");
        }
        if (CompetitionSceneConstants.STATUS_DISABLED.equals(schedule.getStatus())) {
            throw new ServiceException("赛场安排已停用");
        }
        if (schedule.getScheduleId() == null) {
            throw new ServiceException("赛场安排ID不能为空");
        }
    }

    private Long resolveCompetitionSeriesId(CompetitionSceneSchedule schedule, CompetitionSceneScheduleTarget target) {
        Long scheduleCompetitionSeriesId = schedule == null ? null : schedule.getCompetitionSeriesId();
        Long targetCompetitionSeriesId = target == null ? null : target.getCompetitionSeriesId();
        if (scheduleCompetitionSeriesId == null && targetCompetitionSeriesId == null) {
            throw new ServiceException("缺少赛事ID，无法发证");
        }
        if (scheduleCompetitionSeriesId != null && targetCompetitionSeriesId != null
                && !scheduleCompetitionSeriesId.equals(targetCompetitionSeriesId)) {
            throw new ServiceException("赛场安排与对象赛事ID不一致");
        }
        return scheduleCompetitionSeriesId == null ? targetCompetitionSeriesId : scheduleCompetitionSeriesId;
    }

    private CompetitionSceneCredential buildCoreCredential(CompetitionSceneSchedule schedule,
                                                           CompetitionSceneScheduleTarget target,
                                                           CompetitionDetailInfo competition,
                                                           Long competitionSeriesId,
                                                           String credentialType,
                                                           String roleCode,
                                                           SubjectIdentity subject) {
        Date now = DateUtils.getNowDate();
        CompetitionSceneCredential credential = new CompetitionSceneCredential();
        credential.setCredentialNo(generateCompetitionCredentialNo(competitionSeriesId));
        credential.setCredentialToken(generateToken());
        credential.setQrContent(CompetitionSceneConstants.QR_CONTENT_PREFIX + credential.getCredentialToken());
        credential.setIssueChannel(CompetitionSceneConstants.ISSUE_CHANNEL_SCHEDULE_MATCH);
        credential.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_COMPETITION);
        credential.setScopeRefId(competitionSeriesId);
        credential.setCredentialType(credentialType);
        credential.setCredentialName(resolveCredentialName(credentialType));
        credential.setAbilityJson(buildCoreAbilityJson());
        credential.setConfigDimension(resolveConfigDimensionForSubject(subject.subjectType));
        credential.setSubjectType(subject.subjectType);
        credential.setSubjectCode(subject.subjectCode);
        credential.setCompetitionSeriesId(competitionSeriesId);
        credential.setCompetitionName(resolveCompetitionName(schedule, competition));
        credential.setCompetitionStageId(schedule.getCompetitionStageId());
        credential.setCompetitionStageName(schedule.getCompetitionStageName());
        credential.setCompetitionTrackId(firstNotEmpty(target.getCompetitionTrackId(), schedule.getCompetitionTrackId()));
        credential.setCompetitionTrackName(firstNotEmpty(target.getCompetitionTrackName(), schedule.getCompetitionTrackName()));
        credential.setSecondLevelCode(firstNotEmpty(target.getSecondLevelCode(), schedule.getSecondLevelCode()));
        credential.setSecondLevelName(firstNotEmpty(target.getSecondLevelName(), schedule.getSecondLevelName()));
        credential.setTeamCode(target.getTeamCode());
        credential.setTeamName(target.getTeamName());
        credential.setMemberId(target.getMemberId());
        credential.setUserId(target.getUserId());
        credential.setUserName(target.getUserName());
        credential.setPhone(target.getPhone());
        credential.setEmail(target.getEmail());
        credential.setIdCardType(target.getIdCardType());
        credential.setIdCardHash(target.getIdCardHash());
        credential.setIdCardSuffix(target.getIdCardSuffix());
        credential.setSchool(target.getSchool());
        credential.setSchoolName(target.getSchoolName());
        credential.setOrgId(target.getOrgId());
        credential.setOrgName(target.getOrgName());
        credential.setCompetitionRoleName(roleCode);
        credential.setLeaderTeacherId(target.getLeaderTeacherId());
        credential.setLeaderTeacher(target.getLeaderTeacher());
        credential.setGuideTeacher(target.getGuideTeacher());
        credential.setCredentialSnapshotJson(buildCoreCredentialSnapshot(schedule, target, competition,
                competitionSeriesId, credentialType, roleCode, subject));
        credential.setValidFrom(resolveCoreValidFrom(schedule, competition, now));
        credential.setValidTo(resolveCoreValidTo(schedule, competition));
        credential.setCredentialStatus(CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE);
        credential.setVerifyCount(0);
        credential.setReportStatus(CompetitionSceneConstants.DONE_NO);
        credential.setMaterialStatus(CompetitionSceneConstants.DONE_NO);
        credential.setWaitingStatus(CompetitionSceneConstants.DONE_NO);
        credential.setCreateBy(currentUsername());
        credential.setUpdateBy(currentUsername());
        credential.setCreateTime(now);
        credential.setUpdateTime(now);
        credential.setVersion(0L);
        credential.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        credential.setActiveCoreCredentialKey(buildActiveCoreCredentialKey(credential));
        return credential;
    }

    private CompetitionSceneCredentialScopeGrant buildScheduleGrant(CompetitionSceneCredential credential,
                                                                    CompetitionSceneSchedule schedule,
                                                                    CompetitionSceneScheduleTarget target,
                                                                    Long competitionSeriesId,
                                                                    String credentialType,
                                                                    String roleCode,
                                                                    SubjectIdentity subject) {
        CompetitionSceneCredentialScopeGrant grant = new CompetitionSceneCredentialScopeGrant();
        grant.setCredentialId(credential.getCredentialId());
        grant.setCompetitionSeriesId(competitionSeriesId);
        grant.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE);
        grant.setScopeRefId(schedule.getScheduleId());
        grant.setSourceType(SOURCE_TYPE_SCHEDULE_TARGET);
        grant.setSourceScheduleId(schedule.getScheduleId());
        grant.setSourceTargetId(target.getTargetId());
        grant.setCredentialType(credentialType);
        grant.setRoleCode(roleCode);
        grant.setSubjectType(subject.subjectType);
        grant.setSubjectCode(subject.subjectCode);
        grant.setAbilityJson(buildScheduleGrantAbilityJson(credentialType));
        grant.setValidFrom(earliestDate(schedule.getReportStartTime(), schedule.getWaitingStartTime(),
                schedule.getContestStartTime()));
        grant.setValidTo(latestDate(schedule.getReportEndTime(), schedule.getWaitingEndTime(),
                schedule.getContestEndTime()));
        grant.setOperationWindowJson(grantService.buildDefaultOperationWindowJson(schedule));
        grant.setGrantStatus(GRANT_STATUS_ACTIVE);
        grant.setGrantSnapshotJson(buildScheduleGrantSnapshot(schedule, target, credentialType, roleCode, subject));
        grant.setDeleted(0);
        return grant;
    }

    private CoreCredentialIssueOutcome insertCoreCredentialWithRetry(Long competitionSeriesId,
                                                                     String subjectType,
                                                                     String subjectCode,
                                                                     String credentialType,
                                                                     CompetitionSceneCredential credential) {
        for (int retry = 0; retry < CREDENTIAL_INSERT_MAX_RETRY; retry++) {
            if (StringUtils.isEmpty(credential.getCredentialNo())
                    || credentialMapper.selectCompetitionSceneCredentialByNo(credential.getCredentialNo()) != null) {
                credential.setCredentialNo(generateCompetitionCredentialNo(competitionSeriesId));
            }
            if (StringUtils.isEmpty(credential.getCredentialToken())
                    || credentialMapper.selectCompetitionSceneCredentialByToken(credential.getCredentialToken()) != null) {
                refreshCredentialToken(credential);
            }
            try {
                credentialMapper.insertCompetitionSceneCredential(credential);
                return new CoreCredentialIssueOutcome(credential, false);
            } catch (DuplicateKeyException e) {
                CompetitionSceneCredential existed = selectExistingCoreCredential(
                        competitionSeriesId, subjectType, subjectCode, credentialType);
                if (existed != null) {
                    return new CoreCredentialIssueOutcome(existed, true);
                }
                credential.setCredentialNo(generateCompetitionCredentialNo(competitionSeriesId));
                refreshCredentialToken(credential);
            }
        }
        throw new ServiceException("核心证件编号或二维码令牌生成冲突，请稍后重试");
    }

    private CompetitionSceneCredential selectExistingCoreCredential(Long competitionSeriesId,
                                                                    String subjectType,
                                                                    String subjectCode,
                                                                    String credentialType) {
        return credentialMapper.selectEffectiveCompetitionScopeCredentialStrict(
                competitionSeriesId, subjectType, subjectCode, credentialType);
    }

    private String buildActiveCoreCredentialKey(CompetitionSceneCredential credential) {
        if (credential == null
                || !CompetitionSceneConstants.SCOPE_TYPE_COMPETITION.equals(credential.getScopeType())
                || !CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE.equals(credential.getCredentialStatus())
                || !CompetitionSceneConstants.DEL_FLAG_NORMAL.equals(credential.getDelFlag())
                || credential.getCompetitionSeriesId() == null
                || StringUtils.isEmpty(credential.getSubjectType())
                || StringUtils.isEmpty(credential.getSubjectCode())
                || StringUtils.isEmpty(credential.getCredentialType())) {
            return null;
        }
        return credential.getCompetitionSeriesId()
                + ":" + credential.getSubjectType()
                + ":" + credential.getSubjectCode()
                + ":" + credential.getCredentialType();
    }

    private String generateCompetitionCredentialNo(Long competitionSeriesId) {
        String date = new SimpleDateFormat("yyyyMMdd").format(DateUtils.getNowDate());
        String prefix = "CC" + date + "-" + competitionSeriesId + "-";
        for (int retry = 0; retry < CREDENTIAL_INSERT_MAX_RETRY; retry++) {
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            String credentialNo = prefix + suffix;
            if (credentialMapper.selectCompetitionSceneCredentialByNo(credentialNo) == null) {
                return credentialNo;
            }
        }
        throw new ServiceException("证件编号生成冲突，请稍后重试");
    }

    private String generateToken() {
        String token = UUID.randomUUID().toString().replace("-", "");
        while (credentialMapper.selectCompetitionSceneCredentialByToken(token) != null) {
            token = UUID.randomUUID().toString().replace("-", "");
        }
        return token;
    }

    private void refreshCredentialToken(CompetitionSceneCredential credential) {
        credential.setCredentialToken(generateToken());
        credential.setQrContent(CompetitionSceneConstants.QR_CONTENT_PREFIX + credential.getCredentialToken());
    }

    private String buildCoreAbilityJson() {
        Map<String, Object> ability = new LinkedHashMap<>();
        ability.put("identityVerify", true);
        ability.put("report", true);
        ability.put("material", true);
        ability.put("waiting", false);
        ability.put("scheduleEntry", false);
        ability.put("resourceReservation", false);
        ability.put("review", false);
        ability.put("vipAccess", false);
        return JSON.toJSONString(ability);
    }

    private String buildScheduleGrantAbilityJson(String credentialType) {
        Map<String, Object> ability = new LinkedHashMap<>();
        ability.put("identityVerify", false);
        ability.put("report", false);
        ability.put("material", false);
        ability.put("waiting", false);
        ability.put("scheduleEntry", false);
        ability.put("resourceReservation", false);
        ability.put("review", false);
        ability.put("vipAccess", false);
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT.equals(credentialType)
                || CompetitionSceneConstants.CREDENTIAL_TYPE_COMPETITOR.equals(credentialType)) {
            ability.put("waiting", true);
            ability.put("scheduleEntry", true);
            ability.put("resourceReservation", true);
        } else if (CompetitionSceneConstants.CREDENTIAL_TYPE_TEACHER.equals(credentialType)) {
            ability.put("scheduleEntry", true);
        } else if (CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credentialType)) {
            ability.put("scheduleEntry", true);
            ability.put("review", true);
        } else if (CompetitionSceneConstants.CREDENTIAL_TYPE_STAFF.equals(credentialType)) {
            ability.put("scheduleEntry", true);
        } else if (CompetitionSceneConstants.CREDENTIAL_TYPE_VIP.equals(credentialType)) {
            ability.put("scheduleEntry", true);
            ability.put("vipAccess", true);
        } else if (CompetitionSceneConstants.CREDENTIAL_TYPE_TEMP.equals(credentialType)) {
            ability.put("scheduleEntry", true);
        }
        return JSON.toJSONString(ability);
    }

    private String buildCoreCredentialSnapshot(CompetitionSceneSchedule schedule,
                                               CompetitionSceneScheduleTarget target,
                                               CompetitionDetailInfo competition,
                                               Long competitionSeriesId,
                                               String credentialType,
                                               String roleCode,
                                               SubjectIdentity subject) {
        JSONObject snapshot = new JSONObject();
        snapshot.put("issueChannel", CompetitionSceneConstants.ISSUE_CHANNEL_SCHEDULE_MATCH);
        snapshot.put("scopeType", CompetitionSceneConstants.SCOPE_TYPE_COMPETITION);
        snapshot.put("competitionSeriesId", competitionSeriesId);
        snapshot.put("competitionName", resolveCompetitionName(schedule, competition));
        snapshot.put("credentialType", credentialType);
        snapshot.put("credentialName", resolveCredentialName(credentialType));
        snapshot.put("subjectType", subject.subjectType);
        snapshot.put("subjectCode", subject.subjectCode);
        snapshot.put("subjectName", resolveTargetName(target, subject.subjectType));
        snapshot.put("roleCode", roleCode);
        snapshot.put("sourceScheduleId", schedule.getScheduleId());
        snapshot.put("sourceTargetId", target.getTargetId());
        snapshot.put("targetSource", target.getTargetSource());
        snapshot.put("teamCode", target.getTeamCode());
        snapshot.put("groupCode", target.getWaitingGroupCode());
        snapshot.put("groupName", target.getWaitingGroupName());
        return snapshot.toJSONString();
    }

    private String buildScheduleGrantSnapshot(CompetitionSceneSchedule schedule,
                                              CompetitionSceneScheduleTarget target,
                                              String credentialType,
                                              String roleCode,
                                              SubjectIdentity subject) {
        JSONObject snapshot = new JSONObject();
        snapshot.put("scheduleId", schedule.getScheduleId());
        snapshot.put("scheduleName", schedule.getScheduleName());
        snapshot.put("targetId", target.getTargetId());
        snapshot.put("targetName", resolveTargetName(target, subject.subjectType));
        snapshot.put("roleCode", roleCode);
        snapshot.put("credentialType", credentialType);
        snapshot.put("teamCode", target.getTeamCode());
        snapshot.put("subjectType", subject.subjectType);
        snapshot.put("subjectCode", subject.subjectCode);
        snapshot.put("groupCode", firstNotEmpty(target.getWaitingGroupCode(), schedule.getWaitingGroupCode()));
        snapshot.put("groupName", firstNotEmpty(target.getWaitingGroupName(), schedule.getWaitingGroupName()));
        return snapshot.toJSONString();
    }

    private SubjectIdentity resolveSubjectIdentity(CompetitionSceneSchedule schedule,
                                                   CompetitionSceneScheduleTarget target,
                                                   String credentialType) {
        String configDimension = resolveConfigDimension(firstNotEmpty(target.getConfigDimension(), schedule.getConfigDimension()));
        String subjectType;
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credentialType)) {
            subjectType = CompetitionSceneConstants.SUBJECT_TYPE_EXPERT;
        } else if (CompetitionSceneConstants.DIMENSION_TEAM.equals(configDimension)) {
            subjectType = CompetitionSceneConstants.SUBJECT_TYPE_TEAM;
        } else {
            subjectType = CompetitionSceneConstants.SUBJECT_TYPE_USER;
        }
        String subjectCode;
        if (CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(subjectType)) {
            subjectCode = target.getTeamCode();
        } else if (target.getUserId() != null) {
            subjectCode = String.valueOf(target.getUserId());
        } else if (target.getMemberId() != null) {
            subjectCode = "MEMBER:" + target.getMemberId();
        } else {
            subjectCode = target.getTargetKey();
        }
        if (StringUtils.isEmpty(subjectCode)) {
            throw new ServiceException("赛场对象缺少主体编码，无法发证");
        }
        return new SubjectIdentity(subjectType, subjectCode);
    }

    private String resolveCredentialType(CompetitionSceneSchedule schedule, CompetitionSceneScheduleTarget target) {
        if (StringUtils.isNotEmpty(target.getCredentialType())) {
            return normalizeCredentialType(target.getCredentialType());
        }
        String inferred = inferCredentialTypeByRole(target.getCompetitionRoleName());
        if (StringUtils.isNotEmpty(inferred)) {
            return inferred;
        }
        if (StringUtils.isNotEmpty(schedule.getCredentialType())) {
            return normalizeCredentialType(schedule.getCredentialType());
        }
        throw new ServiceException("对象缺少证件类型，无法生成现场证件");
    }

    private String inferCredentialTypeByRole(String role) {
        String normalizedRole = normalizeTargetRole(role, true);
        if (CompetitionSceneConstants.TARGET_ROLE_TEACHER.equals(normalizedRole)) {
            return CompetitionSceneConstants.CREDENTIAL_TYPE_TEACHER;
        }
        if (CompetitionSceneConstants.TARGET_ROLE_EXPERT.equals(normalizedRole)) {
            return CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT;
        }
        if (CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF.equals(normalizedRole)
                || CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF.equals(normalizedRole)
                || CompetitionSceneConstants.TARGET_ROLE_STAFF.equals(normalizedRole)
                || CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER.equals(normalizedRole)) {
            return CompetitionSceneConstants.CREDENTIAL_TYPE_STAFF;
        }
        if (CompetitionSceneConstants.TARGET_ROLE_CAPTAIN.equals(normalizedRole)
                || CompetitionSceneConstants.TARGET_ROLE_MEMBER.equals(normalizedRole)) {
            return CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT;
        }
        return null;
    }

    private String normalizeCredentialType(String credentialType) {
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_COMPETITOR.equals(credentialType)) {
            return CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT;
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT.equals(credentialType)
                || CompetitionSceneConstants.CREDENTIAL_TYPE_TEACHER.equals(credentialType)
                || CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credentialType)
                || CompetitionSceneConstants.CREDENTIAL_TYPE_STAFF.equals(credentialType)
                || CompetitionSceneConstants.CREDENTIAL_TYPE_VIP.equals(credentialType)
                || CompetitionSceneConstants.CREDENTIAL_TYPE_TEMP.equals(credentialType)) {
            return credentialType;
        }
        throw new ServiceException("证件类型不合法");
    }

    private String resolveRoleCode(CompetitionSceneScheduleTarget target, String credentialType) {
        String normalized = normalizeTargetRole(target == null ? null : target.getCompetitionRoleName(), true);
        if (StringUtils.isNotEmpty(normalized)) {
            return normalized;
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_TEACHER.equals(credentialType)) {
            return CompetitionSceneConstants.TARGET_ROLE_TEACHER;
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credentialType)) {
            return CompetitionSceneConstants.TARGET_ROLE_EXPERT;
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_STAFF.equals(credentialType)) {
            return CompetitionSceneConstants.TARGET_ROLE_STAFF;
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT.equals(credentialType)
                && target != null
                && StringUtils.isNotEmpty(target.getTeamCode())) {
            return CompetitionSceneConstants.TARGET_ROLE_CAPTAIN;
        }
        return CompetitionSceneConstants.TARGET_ROLE_MEMBER;
    }

    private String normalizeTargetRole(String role, boolean allowUnknown) {
        if (StringUtils.isEmpty(role)) {
            return allowUnknown ? null : CompetitionSceneConstants.TARGET_ROLE_MEMBER;
        }
        switch (role) {
            case CompetitionSceneConstants.TARGET_ROLE_TEACHER:
            case "教师":
            case "指导教师":
                return CompetitionSceneConstants.TARGET_ROLE_TEACHER;
            case CompetitionSceneConstants.TARGET_ROLE_MEMBER:
            case "队员":
                return CompetitionSceneConstants.TARGET_ROLE_MEMBER;
            case CompetitionSceneConstants.TARGET_ROLE_EXPERT:
            case "专家":
                return CompetitionSceneConstants.TARGET_ROLE_EXPERT;
            case CompetitionSceneConstants.TARGET_ROLE_CAPTAIN:
            case "队长":
                return CompetitionSceneConstants.TARGET_ROLE_CAPTAIN;
            case CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF:
            case "发资料工作人员":
            case "资料工作人员":
                return CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF;
            case CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF:
            case "签到工作人员":
                return CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF;
            case CompetitionSceneConstants.TARGET_ROLE_STAFF:
            case "工作人员":
                return CompetitionSceneConstants.TARGET_ROLE_STAFF;
            case CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER:
            case "志愿者":
            case "赛场志愿者":
                return CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER;
            default:
                return allowUnknown ? null : CompetitionSceneConstants.TARGET_ROLE_MEMBER;
        }
    }

    private String resolveCredentialName(String credentialType) {
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT.equals(credentialType)
                || CompetitionSceneConstants.CREDENTIAL_TYPE_COMPETITOR.equals(credentialType)) {
            return "参赛证";
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_TEACHER.equals(credentialType)) {
            return "教师证";
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credentialType)) {
            return "专家证";
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_STAFF.equals(credentialType)) {
            return "工作证";
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_VIP.equals(credentialType)) {
            return "贵宾证";
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_TEMP.equals(credentialType)) {
            return "临时证";
        }
        return "现场证件";
    }

    private String resolveConfigDimension(String configDimension) {
        if (CompetitionSceneConstants.DIMENSION_TEAM.equals(configDimension)) {
            return CompetitionSceneConstants.DIMENSION_TEAM;
        }
        return CompetitionSceneConstants.DIMENSION_PERSON;
    }

    private String resolveConfigDimensionForSubject(String subjectType) {
        return CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(subjectType)
                ? CompetitionSceneConstants.DIMENSION_TEAM : CompetitionSceneConstants.DIMENSION_PERSON;
    }

    private CompetitionDetailInfo selectCompetitionDetailOrNull(Long competitionSeriesId) {
        if (competitionSeriesId == null) {
            return null;
        }
        CompetitionMainInfoReq query = new CompetitionMainInfoReq();
        query.setCompetitionSeriesId(competitionSeriesId);
        List<CompetitionDetailInfo> list = competitionMainInfoMapper.selectCompetitionDetailInfoByCompetitionId(query);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private String resolveCompetitionName(CompetitionSceneSchedule schedule, CompetitionDetailInfo competition) {
        String competitionName = resolveCompetitionName(competition);
        if (StringUtils.isNotEmpty(competitionName)) {
            return competitionName;
        }
        return schedule == null ? null : schedule.getCompetitionName();
    }

    private String resolveCompetitionName(CompetitionDetailInfo competition) {
        if (competition == null) {
            return null;
        }
        return (StringUtils.isNotEmpty(competition.getCompetitionSeriesName())
                ? competition.getCompetitionSeriesName() : "")
                + (StringUtils.isNotEmpty(competition.getCompetitionName())
                ? competition.getCompetitionName() : "");
    }

    private Date resolveCoreValidFrom(CompetitionSceneSchedule schedule,
                                      CompetitionDetailInfo competition,
                                      Date defaultTime) {
        if (competition != null && competition.getCompetitionStartTime() != null) {
            return competition.getCompetitionStartTime();
        }
        Date scheduleStart = earliestDate(schedule.getReportStartTime(), schedule.getWaitingStartTime(),
                schedule.getContestStartTime());
        return scheduleStart == null ? defaultTime : scheduleStart;
    }

    private Date resolveCoreValidTo(CompetitionSceneSchedule schedule, CompetitionDetailInfo competition) {
        if (competition != null && competition.getCompetitionEndTime() != null) {
            return competition.getCompetitionEndTime();
        }
        return latestDate(schedule.getReportEndTime(), schedule.getWaitingEndTime(), schedule.getContestEndTime());
    }

    private String resolveTargetName(CompetitionSceneScheduleTarget target, String subjectType) {
        if (target == null) {
            return null;
        }
        if (CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(subjectType)
                && StringUtils.isNotEmpty(target.getTeamName())) {
            return target.getTeamName();
        }
        return firstNotEmpty(target.getUserName(), target.getTeamName(), target.getTargetKey());
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

    private String currentUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception e) {
            return "system";
        }
    }

    private static class SubjectIdentity {
        private final String subjectType;
        private final String subjectCode;

        private SubjectIdentity(String subjectType, String subjectCode) {
            this.subjectType = subjectType;
            this.subjectCode = subjectCode;
        }
    }

    private static class CoreCredentialIssueOutcome {
        private final CompetitionSceneCredential credential;
        private final boolean reusedCredential;

        private CoreCredentialIssueOutcome(CompetitionSceneCredential credential, boolean reusedCredential) {
            this.credential = credential;
            this.reusedCredential = reusedCredential;
        }
    }
}
