package com.teaching.competition.service.impl;

import com.alibaba.fastjson2.JSON;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.DictConstant;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneCompetitionDirectIssueReq;
import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionSceneCredentialMember;
import com.teaching.competition.domain.CompetitionSceneCredentialGenerateReq;
import com.teaching.competition.domain.CompetitionSceneSchedule;
import com.teaching.competition.domain.CompetitionSceneScheduleTarget;
import com.teaching.competition.domain.CompetitionTeacherStudentCredentialQuery;
import com.teaching.competition.domain.CompetitionTeacherStudentCredentialVO;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.mapper.CompetitionMainInfoMapper;
import com.teaching.competition.mapper.CompetitionSceneCredentialMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleTargetMapper;
import com.teaching.competition.service.ICompetitionSceneCredentialService;
import com.teaching.competition.service.ICompetitionSceneSubjectOperationStateService;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.CompetitionDetailInfo;
import com.teaching.system.api.domain.CompetitionMainInfoReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 赛事现场证件Service业务层处理。
 */
@Service
public class CompetitionSceneCredentialServiceImpl implements ICompetitionSceneCredentialService {

    private static final int CREDENTIAL_INSERT_MAX_RETRY = 10;

    @Autowired
    private CompetitionSceneCredentialMapper credentialMapper;

    @Autowired
    private CompetitionSceneScheduleMapper scheduleMapper;

    @Autowired
    private CompetitionSceneScheduleTargetMapper targetMapper;

    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    private CompetitionMainInfoMapper competitionMainInfoMapper;

    @Autowired
    private ICompetitionSceneSubjectOperationStateService operationStateService;

    @Override
    public CompetitionSceneCredential selectCompetitionSceneCredentialById(Long credentialId) {
        CompetitionSceneCredential credential = credentialMapper.selectCompetitionSceneCredentialById(credentialId);
        operationStateService.fillCredentialOperationStates(credential == null ? null : Collections.singletonList(credential));
        return credential;
    }

    @Override
    public CompetitionSceneCredential selectCompetitionSceneCredentialByToken(String credentialToken) {
        CompetitionSceneCredential credential = credentialMapper.selectCompetitionSceneCredentialByToken(credentialToken);
        operationStateService.fillCredentialOperationStates(credential == null ? null : Collections.singletonList(credential));
        return credential;
    }

    @Override
    public List<CompetitionSceneCredential> selectCompetitionSceneCredentialList(CompetitionSceneCredential credential) {
        List<CompetitionSceneCredential> list = credentialMapper.selectCompetitionSceneCredentialList(credential);
        operationStateService.fillCredentialOperationStates(list);
        return list;
    }

    @Override
    public List<CompetitionSceneCredential> selectMyCompetitionSceneCredentialList(Long userId) {
        CompetitionSceneCredential query = new CompetitionSceneCredential();
        query.setUserId(userId);
        Map<Long, CompetitionSceneCredential> credentialMap = credentialMapper.selectCompetitionSceneCredentialList(query)
                .stream()
                .collect(Collectors.toMap(CompetitionSceneCredential::getCredentialId, item -> item, (a, b) -> a, LinkedHashMap::new));
        CompetitionSceneCredential subjectCodeQuery = new CompetitionSceneCredential();
        subjectCodeQuery.setSubjectType(CompetitionSceneConstants.SUBJECT_TYPE_USER);
        subjectCodeQuery.setSubjectCode(String.valueOf(userId));
        credentialMapper.selectCompetitionSceneCredentialList(subjectCodeQuery)
                .forEach(item -> credentialMap.put(item.getCredentialId(), item));

        List<CompetitionSceneCredential> list = credentialMap.values().stream().collect(Collectors.toList());
        operationStateService.fillCredentialOperationStates(list);
        fillCredentialTeamMembers(list);
        return list;
    }

    @Override
    public List<CompetitionTeacherStudentCredentialVO> selectTeacherStudentCredentialList(Long teacherUserId,
                                                                                          CompetitionTeacherStudentCredentialQuery query) {
        if (teacherUserId == null) {
            return new ArrayList<>();
        }
        List<CompetitionTeacherStudentCredentialVO> list =
                credentialMapper.selectTeacherStudentCredentialList(teacherUserId, normalizeTeacherStudentQuery(query));
        fillTeacherStudentCredentialStates(list);
        return list;
    }

    @Override
    public CompetitionTeacherStudentCredentialVO selectTeacherStudentCredentialDetail(Long teacherUserId, Long credentialId) {
        if (teacherUserId == null || credentialId == null) {
            throw new ServiceException("无权限查看该学生参赛证");
        }
        CompetitionSceneCredential credential = credentialMapper.selectCompetitionSceneCredentialById(credentialId);
        if (credential == null) {
            throw new ServiceException("证件不存在或无权限查看该学生参赛证");
        }
        CompetitionTeacherStudentCredentialVO detail =
                credentialMapper.selectTeacherStudentCredentialDetail(teacherUserId, credentialId);
        if (detail == null) {
            throw new ServiceException("无权限查看该学生参赛证");
        }
        fillTeacherStudentCredentialStates(Collections.singletonList(detail));
        return detail;
    }

    @Override
    public boolean checkTeacherCanViewCredential(Long teacherUserId, Long credentialId) {
        if (teacherUserId == null || credentialId == null) {
            return false;
        }
        return credentialMapper.selectTeacherStudentCredentialDetail(teacherUserId, credentialId) != null;
    }

    private CompetitionTeacherStudentCredentialQuery normalizeTeacherStudentQuery(CompetitionTeacherStudentCredentialQuery query) {
        CompetitionTeacherStudentCredentialQuery normalized =
                query == null ? new CompetitionTeacherStudentCredentialQuery() : query;
        normalized.setTeamCode(trimToNull(normalized.getTeamCode()));
        normalized.setKeyword(trimToNull(normalized.getKeyword()));
        return normalized;
    }

    private void fillTeacherStudentCredentialStates(List<CompetitionTeacherStudentCredentialVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<CompetitionSceneCredential> credentials = list.stream()
                .filter(item -> item.getCredentialId() != null)
                .map(this::buildCredentialForStateFill)
                .collect(Collectors.toList());
        operationStateService.fillCredentialOperationStates(credentials);
        Map<Long, CompetitionSceneCredential> stateMap = credentials.stream()
                .collect(Collectors.toMap(CompetitionSceneCredential::getCredentialId, item -> item, (a, b) -> a));
        for (CompetitionTeacherStudentCredentialVO item : list) {
            if (item.getCredentialId() == null) {
                item.setCredentialStatus(CompetitionTeacherStudentCredentialVO.STATUS_NOT_GENERATED);
                continue;
            }
            CompetitionSceneCredential state = stateMap.get(item.getCredentialId());
            if (state != null) {
                item.setReportStatus(state.getReportStatus());
                item.setReportTime(state.getReportTime());
                item.setMaterialStatus(state.getMaterialStatus());
                item.setMaterialTime(state.getMaterialTime());
                item.setWaitingStatus(state.getWaitingStatus());
                item.setWaitingTime(state.getWaitingTime());
                item.setDelegateInfo(buildDelegateInfo(state));
            }
            item.setScheduleLocation(joinLocation(item.getContestLocation(), item.getContestRoom()));
        }
    }

    private CompetitionSceneCredential buildCredentialForStateFill(CompetitionTeacherStudentCredentialVO item) {
        CompetitionSceneCredential credential = new CompetitionSceneCredential();
        credential.setCredentialId(item.getCredentialId());
        credential.setScheduleId(item.getScheduleId());
        credential.setCompetitionSeriesId(item.getCompetitionSeriesId());
        credential.setScopeType(item.getScopeType());
        credential.setScopeRefId(item.getScopeRefId());
        credential.setSubjectType(item.getSubjectType());
        credential.setSubjectCode(item.getSubjectCode());
        credential.setConfigDimension(item.getConfigDimension());
        credential.setTeamCode(item.getTeamCode());
        credential.setUserId(item.getUserId());
        credential.setMemberId(item.getMemberId());
        credential.setUserName(item.getStudentName());
        credential.setReportStatus(item.getReportStatus());
        credential.setReportTime(item.getReportTime());
        credential.setMaterialStatus(item.getMaterialStatus());
        credential.setMaterialTime(item.getMaterialTime());
        credential.setWaitingStatus(item.getWaitingStatus());
        credential.setWaitingTime(item.getWaitingTime());
        return credential;
    }

    private String buildDelegateInfo(CompetitionSceneCredential credential) {
        if (credential == null || StringUtils.isEmpty(credential.getMaterialDelegateName())) {
            return null;
        }
        if (StringUtils.isEmpty(credential.getMaterialDelegateRelation())) {
            return credential.getMaterialDelegateName();
        }
        return credential.getMaterialDelegateName() + "（" + credential.getMaterialDelegateRelation() + "）";
    }

    private String joinLocation(String location, String room) {
        if (StringUtils.isEmpty(location)) {
            return room;
        }
        if (StringUtils.isEmpty(room)) {
            return location;
        }
        return location + " / " + room;
    }

    private String trimToNull(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void fillCredentialTeamMembers(List<CompetitionSceneCredential> credentials) {
        if (credentials == null || credentials.isEmpty()) {
            return;
        }
        Map<String, List<CompetitionSceneCredentialMember>> cache = new HashMap<>();
        for (CompetitionSceneCredential credential : credentials) {
            if (credential == null || StringUtils.isEmpty(credential.getTeamCode())) {
                continue;
            }
            String cacheKey = (credential.getCompetitionSeriesId() == null ? "" : credential.getCompetitionSeriesId())
                    + ":" + credential.getTeamCode();
            List<CompetitionSceneCredentialMember> members = cache.computeIfAbsent(cacheKey,
                    key -> selectCredentialTeamMembers(credential.getCompetitionSeriesId(), credential.getTeamCode()));
            if (members.isEmpty()) {
                continue;
            }
            credential.setTeamMembers(members.stream()
                    .filter(member -> !isCredentialOwnerMember(credential, member))
                    .collect(Collectors.toList()));
        }
    }

    private List<CompetitionSceneCredentialMember> selectCredentialTeamMembers(Long competitionSeriesId, String teamCode) {
        if (StringUtils.isEmpty(teamCode)) {
            return new ArrayList<>();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("teamCode", teamCode);
        params.put("competitionSeriesId", competitionSeriesId);
        List<CompetitionApplyInfo> list =
                competitionApplyInfoMapper.selectCertCompetitionApplyInfoListByUserTeamCodeANoTeacher(params);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list.stream()
                .filter(item -> !ApplyConstants.TEAM_GUIDE_TEACHER.equals(item.getCompetitionRoleName()))
                .filter(item -> DictConstant.PAID.equals(item.getPayStatus()))
                .filter(item -> Constants.CHECK_PASS.equals(item.getCheckStatus()) || StringUtils.isEmpty(item.getCheckStatus()))
                .sorted(Comparator.comparing(item -> item.getTeamSort() == null ? Integer.MAX_VALUE : item.getTeamSort()))
                .map(this::buildCredentialMember)
                .filter(member -> StringUtils.isNotEmpty(member.getUserName()))
                .collect(Collectors.toList());
    }

    private CompetitionSceneCredentialMember buildCredentialMember(CompetitionApplyInfo applyInfo) {
        CompetitionSceneCredentialMember member = new CompetitionSceneCredentialMember();
        member.setMemberId(applyInfo.getMemberId());
        member.setUserId(applyInfo.getUserId());
        member.setUserName(applyInfo.getUserName());
        member.setSchoolName(applyInfo.getSchoolName());
        member.setCompetitionRoleName(applyInfo.getCompetitionRoleName());
        return member;
    }

    private boolean isCredentialOwnerMember(CompetitionSceneCredential credential, CompetitionSceneCredentialMember member) {
        if (credential == null || member == null) {
            return false;
        }
        if (credential.getMemberId() != null && Objects.equals(credential.getMemberId(), member.getMemberId())) {
            return true;
        }
        if (credential.getUserId() != null && Objects.equals(credential.getUserId(), member.getUserId())) {
            return true;
        }
        return credential.getMemberId() == null
                && credential.getUserId() == null
                && StringUtils.isNotEmpty(credential.getUserName())
                && credential.getUserName().equals(member.getUserName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateCompetitionSceneCredential(CompetitionSceneCredentialGenerateReq req) {
        if (req == null || req.getScheduleId() == null) {
            throw new ServiceException("赛场安排ID不能为空");
        }
        CompetitionSceneSchedule schedule = scheduleMapper.selectCompetitionSceneScheduleById(req.getScheduleId());
        if (schedule == null) {
            throw new ServiceException("赛场安排不存在");
        }

        List<CompetitionSceneScheduleTarget> targets;
        if (req.getTargetIds() != null && !req.getTargetIds().isEmpty()) {
            targets = targetMapper.selectCompetitionSceneScheduleTargetByIds(req.getTargetIds());
        } else {
            targets = targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(req.getScheduleId());
        }
        if (targets == null || targets.isEmpty()) {
            return 0;
        }

        int count = 0;
        boolean regenerate = Boolean.TRUE.equals(req.getRegenerate());
        CompetitionDetailInfo competition = selectCompetitionDetailOrNull(schedule.getCompetitionSeriesId());
        int nextSequence = selectNextCredentialSequence(schedule);
        for (CompetitionSceneScheduleTarget target : targets) {
            if (!req.getScheduleId().equals(target.getScheduleId())) {
                throw new ServiceException("所选对象不属于当前赛场安排");
            }
            if (!CompetitionSceneConstants.STATUS_NORMAL.equals(target.getStatus())) {
                continue;
            }
            ensureCompetitionCredentialForTarget(schedule, target, competition);
            CompetitionSceneCredential existed = credentialMapper.selectCompetitionSceneCredentialByTargetId(target.getTargetId());
            if (existed != null && !regenerate) {
                continue;
            }
            if (existed != null) {
                credentialMapper.revokeCompetitionSceneCredentialByTargetId(target.getTargetId(), currentUsername());
            }
            CompetitionSceneCredential credential = buildCredential(schedule, target, nextSequence);
            nextSequence = insertCredentialWithRetry(schedule, credential, nextSequence);
            count++;
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneCredential competitionDirectIssue(CompetitionSceneCompetitionDirectIssueReq req) {
        validateCompetitionDirectIssueReq(req);
        String credentialType = normalizeCredentialType(req.getCredentialType());
        String subjectType = normalizeDirectIssueSubjectType(req.getSubjectType());
        String subjectCode = req.getSubjectCode().trim();
        CompetitionSceneCredential existed = selectExistingCompetitionCredential(
                req.getCompetitionSeriesId(), subjectType, subjectCode, resolveDirectIssueUserId(subjectType, subjectCode));
        if (existed != null) {
            throw new ServiceException("该大赛级主体已存在有效现场证件");
        }

        CompetitionDetailInfo competition = selectCompetitionDetail(req.getCompetitionSeriesId());
        CompetitionSceneCredential credential = buildCompetitionDirectCredential(req, competition, credentialType,
                subjectType, subjectCode);
        credentialMapper.insertCompetitionSceneCredential(credential);
        operationStateService.fillCredentialOperationStates(Collections.singletonList(credential));
        return credential;
    }

    @Override
    public int updateCompetitionSceneCredential(CompetitionSceneCredential credential) {
        credential.setUpdateBy(currentUsername());
        credential.setUpdateTime(DateUtils.getNowDate());
        return credentialMapper.updateCompetitionSceneCredential(credential);
    }

    @Override
    public int deleteCompetitionSceneCredentialByIds(Long[] credentialIds) {
        return credentialMapper.deleteCompetitionSceneCredentialByIds(credentialIds, currentUsername());
    }

    private CompetitionSceneCredential ensureCompetitionCredentialForTarget(CompetitionSceneSchedule schedule,
                                                                            CompetitionSceneScheduleTarget target,
                                                                            CompetitionDetailInfo competition) {
        if (schedule == null || target == null || target.getUserId() == null) {
            return null;
        }
        String subjectType = CompetitionSceneConstants.SUBJECT_TYPE_USER;
        String subjectCode = String.valueOf(target.getUserId());
        CompetitionSceneCredential existed = selectExistingCompetitionCredential(
                schedule.getCompetitionSeriesId(), subjectType, subjectCode, target.getUserId());
        if (existed != null) {
            return existed;
        }
        String credentialType = resolveCredentialType(schedule, target);
        CompetitionSceneCredential credential = buildCompetitionCredential(schedule, target, competition,
                credentialType, subjectType, subjectCode);
        credentialMapper.insertCompetitionSceneCredential(credential);
        return credential;
    }

    private CompetitionSceneCredential selectExistingCompetitionCredential(Long competitionSeriesId,
                                                                           String subjectType,
                                                                           String subjectCode,
                                                                           Long userId) {
        if (competitionSeriesId == null) {
            return null;
        }
        if (userId != null) {
            CompetitionSceneCredential existed = credentialMapper.selectEffectiveCompetitionScopeCredentialByUserId(
                    competitionSeriesId, userId);
            if (existed != null) {
                return existed;
            }
        }
        if (StringUtils.isEmpty(subjectType) || StringUtils.isEmpty(subjectCode)) {
            return null;
        }
        return credentialMapper.selectEffectiveCompetitionScopeCredential(competitionSeriesId, subjectType, subjectCode);
    }

    private CompetitionSceneCredential buildCredential(CompetitionSceneSchedule schedule,
                                                       CompetitionSceneScheduleTarget target,
                                                       int sequence) {
        Date now = DateUtils.getNowDate();
        String credentialType = resolveCredentialType(schedule, target);
        String configDimension = resolveConfigDimension(firstNotEmpty(target.getConfigDimension(), schedule.getConfigDimension()));
        CompetitionSceneCredential credential = new CompetitionSceneCredential();
        credential.setScheduleId(schedule.getScheduleId());
        credential.setTargetId(target.getTargetId());
        credential.setCredentialNo(generateCredentialNo(schedule, sequence));
        credential.setCredentialToken(generateToken());
        credential.setQrContent(CompetitionSceneConstants.QR_CONTENT_PREFIX + credential.getCredentialToken());
        credential.setCredentialType(credentialType);
        credential.setIssueChannel(CompetitionSceneConstants.ISSUE_CHANNEL_SCHEDULE_MATCH);
        credential.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE);
        credential.setScopeRefId(schedule.getScheduleId());
        credential.setCredentialName(resolveCredentialName(credentialType));
        credential.setAbilityJson(buildAbilityJson(CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE, credentialType));
        credential.setConfigDimension(configDimension);
        credential.setSubjectType(resolveSubjectType(credentialType, configDimension));
        credential.setCompetitionSeriesId(schedule.getCompetitionSeriesId());
        credential.setCompetitionName(schedule.getCompetitionName());
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
        credential.setCompetitionRoleName(normalizeRoleForSnapshot(target.getCompetitionRoleName()));
        credential.setLeaderTeacherId(target.getLeaderTeacherId());
        credential.setLeaderTeacher(target.getLeaderTeacher());
        credential.setGuideTeacher(target.getGuideTeacher());
        credential.setSubjectCode(resolveCredentialSubjectCode(credential));
        credential.setReportStartTime(schedule.getReportStartTime());
        credential.setReportEndTime(schedule.getReportEndTime());
        credential.setReportLocation(schedule.getReportLocation());
        credential.setContestStartTime(schedule.getContestStartTime());
        credential.setContestEndTime(schedule.getContestEndTime());
        credential.setContestLocation(schedule.getContestLocation());
        credential.setContestRoom(schedule.getContestRoom());
        credential.setSeatNo(target.getSeatNo());
        credential.setWaitingStartTime(schedule.getWaitingStartTime());
        credential.setWaitingEndTime(schedule.getWaitingEndTime());
        credential.setWaitingLocation(schedule.getWaitingLocation());
        credential.setWaitingGroupCode(firstNotEmpty(target.getWaitingGroupCode(), schedule.getWaitingGroupCode()));
        credential.setWaitingGroupName(firstNotEmpty(target.getWaitingGroupName(), schedule.getWaitingGroupName()));
        credential.setMaterialLocation(schedule.getMaterialLocation());
        credential.setNotice(schedule.getNotice());
        credential.setCredentialSnapshotJson(JSON.toJSONString(Map.of("schedule", schedule, "target", target)));
        credential.setValidFrom(schedule.getReportStartTime() == null ? now : schedule.getReportStartTime());
        credential.setValidTo(schedule.getContestEndTime() == null ? schedule.getReportEndTime() : schedule.getContestEndTime());
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
        return credential;
    }

    private CompetitionSceneCredential buildCompetitionCredential(CompetitionSceneSchedule schedule,
                                                                  CompetitionSceneScheduleTarget target,
                                                                  CompetitionDetailInfo competition,
                                                                  String credentialType,
                                                                  String subjectType,
                                                                  String subjectCode) {
        Date now = DateUtils.getNowDate();
        CompetitionSceneCredential credential = new CompetitionSceneCredential();
        credential.setCredentialNo(generateCompetitionDirectCredentialNo(schedule.getCompetitionSeriesId()));
        credential.setCredentialToken(generateToken());
        credential.setQrContent(CompetitionSceneConstants.QR_CONTENT_PREFIX + credential.getCredentialToken());
        credential.setIssueChannel(CompetitionSceneConstants.ISSUE_CHANNEL_SCHEDULE_MATCH);
        credential.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_COMPETITION);
        credential.setScopeRefId(schedule.getCompetitionSeriesId());
        credential.setCredentialType(credentialType);
        credential.setCredentialName(resolveCredentialName(credentialType));
        credential.setAbilityJson(buildAbilityJson(CompetitionSceneConstants.SCOPE_TYPE_COMPETITION, credentialType));
        credential.setConfigDimension(CompetitionSceneConstants.DIMENSION_PERSON);
        credential.setSubjectType(subjectType);
        credential.setSubjectCode(subjectCode);
        credential.setCompetitionSeriesId(schedule.getCompetitionSeriesId());
        credential.setCompetitionName(resolveCompetitionName(schedule, competition));
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
        credential.setCompetitionRoleName(normalizeRoleForSnapshot(target.getCompetitionRoleName()));
        credential.setLeaderTeacherId(target.getLeaderTeacherId());
        credential.setLeaderTeacher(target.getLeaderTeacher());
        credential.setGuideTeacher(target.getGuideTeacher());
        credential.setCredentialSnapshotJson(buildCompetitionAutoSnapshot(schedule, target, competition,
                credentialType, subjectType, subjectCode));
        credential.setValidFrom(resolveCompetitionValidFrom(schedule, competition, now));
        credential.setValidTo(competition == null ? null : competition.getCompetitionEndTime());
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
        return credential;
    }

    private CompetitionSceneCredential buildCompetitionDirectCredential(CompetitionSceneCompetitionDirectIssueReq req,
                                                                        CompetitionDetailInfo competition,
                                                                        String credentialType,
                                                                        String subjectType,
                                                                        String subjectCode) {
        Date now = DateUtils.getNowDate();
        CompetitionSceneCredential credential = new CompetitionSceneCredential();
        credential.setCredentialNo(generateCompetitionDirectCredentialNo(req.getCompetitionSeriesId()));
        credential.setCredentialToken(generateToken());
        credential.setQrContent(CompetitionSceneConstants.QR_CONTENT_PREFIX + credential.getCredentialToken());
        credential.setIssueChannel(CompetitionSceneConstants.ISSUE_CHANNEL_COMPETITION_DIRECT);
        credential.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_COMPETITION);
        credential.setScopeRefId(req.getCompetitionSeriesId());
        credential.setCredentialType(credentialType);
        credential.setCredentialName(StringUtils.isNotEmpty(req.getCredentialName())
                ? req.getCredentialName().trim() : resolveCredentialName(credentialType));
        credential.setAbilityJson(buildAbilityJson(CompetitionSceneConstants.SCOPE_TYPE_COMPETITION, credentialType));
        if (credential.getAbilityJson().contains("\"waiting\":true")) {
            throw new ServiceException("大赛级证件不允许开启候场能力");
        }
        credential.setConfigDimension(CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(subjectType)
                ? CompetitionSceneConstants.DIMENSION_TEAM : CompetitionSceneConstants.DIMENSION_PERSON);
        credential.setSubjectType(subjectType);
        credential.setSubjectCode(subjectCode);
        credential.setCompetitionSeriesId(req.getCompetitionSeriesId());
        credential.setCompetitionName(resolveCompetitionName(competition));
        fillCompetitionDirectSubject(credential, subjectType, subjectCode, req.getSubjectName());
        credential.setCredentialSnapshotJson(buildCompetitionDirectSnapshot(req, competition, subjectType, subjectCode));
        credential.setValidFrom(competition == null || competition.getCompetitionStartTime() == null
                ? now : competition.getCompetitionStartTime());
        credential.setValidTo(competition == null ? null : competition.getCompetitionEndTime());
        credential.setCredentialStatus(CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE);
        credential.setVerifyCount(0);
        credential.setReportStatus(CompetitionSceneConstants.DONE_NO);
        credential.setMaterialStatus(CompetitionSceneConstants.DONE_NO);
        credential.setWaitingStatus(CompetitionSceneConstants.DONE_NO);
        credential.setRemark(req.getRemark());
        credential.setCreateBy(currentUsername());
        credential.setUpdateBy(currentUsername());
        credential.setCreateTime(now);
        credential.setUpdateTime(now);
        credential.setVersion(0L);
        credential.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        return credential;
    }

    private void validateCompetitionDirectIssueReq(CompetitionSceneCompetitionDirectIssueReq req) {
        if (req == null) {
            throw new ServiceException("发证参数不能为空");
        }
        if (req.getCompetitionSeriesId() == null) {
            throw new ServiceException("赛事不能为空");
        }
        if (StringUtils.isEmpty(req.getCredentialType())) {
            throw new ServiceException("证件类型不能为空");
        }
        if (StringUtils.isEmpty(req.getSubjectType())) {
            throw new ServiceException("发证对象类型不能为空");
        }
        if (StringUtils.isEmpty(req.getSubjectCode())) {
            throw new ServiceException("发证对象编码不能为空");
        }
    }

    private CompetitionDetailInfo selectCompetitionDetail(Long competitionSeriesId) {
        CompetitionDetailInfo competition = selectCompetitionDetailOrNull(competitionSeriesId);
        if (competition == null) {
            throw new ServiceException("赛事不存在");
        }
        return competition;
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

    private String normalizeDirectIssueSubjectType(String subjectType) {
        if (StringUtils.isEmpty(subjectType)) {
            throw new ServiceException("发证对象类型不能为空");
        }
        String value = subjectType.trim().toUpperCase();
        if (CompetitionSceneConstants.SUBJECT_TYPE_PERSON.equals(value)) {
            return CompetitionSceneConstants.SUBJECT_TYPE_USER;
        }
        if (CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(value)
                || CompetitionSceneConstants.SUBJECT_TYPE_USER.equals(value)
                || CompetitionSceneConstants.SUBJECT_TYPE_EXPERT.equals(value)
                || CompetitionSceneConstants.SUBJECT_TYPE_STAFF.equals(value)
                || CompetitionSceneConstants.SUBJECT_TYPE_VIP.equals(value)
                || CompetitionSceneConstants.SUBJECT_TYPE_TEMP.equals(value)) {
            return value;
        }
        throw new ServiceException("发证对象类型不合法");
    }

    private Long resolveDirectIssueUserId(String subjectType, String subjectCode) {
        if (CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(subjectType)) {
            return null;
        }
        return parseLongOrNull(subjectCode);
    }

    private String generateCompetitionDirectCredentialNo(Long competitionSeriesId) {
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

    private String resolveCompetitionName(CompetitionDetailInfo competition) {
        if (competition == null) {
            return null;
        }
        return (StringUtils.isNotEmpty(competition.getCompetitionSeriesName())
                ? competition.getCompetitionSeriesName() : "")
                + (StringUtils.isNotEmpty(competition.getCompetitionName())
                ? competition.getCompetitionName() : "");
    }

    private String resolveCompetitionName(CompetitionSceneSchedule schedule, CompetitionDetailInfo competition) {
        String competitionName = resolveCompetitionName(competition);
        if (StringUtils.isNotEmpty(competitionName)) {
            return competitionName;
        }
        return schedule == null ? null : schedule.getCompetitionName();
    }

    private Date resolveCompetitionValidFrom(CompetitionSceneSchedule schedule,
                                             CompetitionDetailInfo competition,
                                             Date defaultTime) {
        if (competition != null && competition.getCompetitionStartTime() != null) {
            return competition.getCompetitionStartTime();
        }
        if (schedule != null && schedule.getReportStartTime() != null) {
            return schedule.getReportStartTime();
        }
        return defaultTime;
    }

    private void fillCompetitionDirectSubject(CompetitionSceneCredential credential,
                                              String subjectType,
                                              String subjectCode,
                                              String subjectName) {
        String name = StringUtils.isNotEmpty(subjectName) ? subjectName.trim() : subjectCode;
        if (CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(subjectType)) {
            credential.setTeamCode(subjectCode);
            credential.setTeamName(name);
        } else {
            credential.setUserId(parseLongOrNull(subjectCode));
            credential.setUserName(name);
        }
        credential.setCompetitionRoleName(resolveDirectIssueRole(credential.getCredentialType(), subjectType));
    }

    private String resolveDirectIssueRole(String credentialType, String subjectType) {
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_TEACHER.equals(credentialType)) {
            return CompetitionSceneConstants.TARGET_ROLE_TEACHER;
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credentialType)
                || CompetitionSceneConstants.SUBJECT_TYPE_EXPERT.equals(subjectType)) {
            return CompetitionSceneConstants.TARGET_ROLE_EXPERT;
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_STAFF.equals(credentialType)
                || CompetitionSceneConstants.SUBJECT_TYPE_STAFF.equals(subjectType)) {
            return CompetitionSceneConstants.TARGET_ROLE_STAFF;
        }
        return CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(subjectType)
                ? CompetitionSceneConstants.TARGET_ROLE_CAPTAIN
                : CompetitionSceneConstants.TARGET_ROLE_MEMBER;
    }

    private String buildCompetitionDirectSnapshot(CompetitionSceneCompetitionDirectIssueReq req,
                                                  CompetitionDetailInfo competition,
                                                  String subjectType,
                                                  String subjectCode) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("issueChannel", CompetitionSceneConstants.ISSUE_CHANNEL_COMPETITION_DIRECT);
        snapshot.put("scopeType", CompetitionSceneConstants.SCOPE_TYPE_COMPETITION);
        snapshot.put("competitionSeriesId", req.getCompetitionSeriesId());
        snapshot.put("competitionName", resolveCompetitionName(competition));
        snapshot.put("credentialType", req.getCredentialType());
        snapshot.put("credentialName", req.getCredentialName());
        snapshot.put("subjectType", subjectType);
        snapshot.put("subjectCode", subjectCode);
        snapshot.put("subjectName", req.getSubjectName());
        snapshot.put("targetSource", CompetitionSceneConstants.ISSUE_CHANNEL_COMPETITION_DIRECT);
        return JSON.toJSONString(snapshot);
    }

    private String buildCompetitionAutoSnapshot(CompetitionSceneSchedule schedule,
                                                CompetitionSceneScheduleTarget target,
                                                CompetitionDetailInfo competition,
                                                String credentialType,
                                                String subjectType,
                                                String subjectCode) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("issueChannel", CompetitionSceneConstants.ISSUE_CHANNEL_SCHEDULE_MATCH);
        snapshot.put("scopeType", CompetitionSceneConstants.SCOPE_TYPE_COMPETITION);
        snapshot.put("competitionSeriesId", schedule.getCompetitionSeriesId());
        snapshot.put("competitionName", resolveCompetitionName(schedule, competition));
        snapshot.put("credentialType", credentialType);
        snapshot.put("credentialName", resolveCredentialName(credentialType));
        snapshot.put("subjectType", subjectType);
        snapshot.put("subjectCode", subjectCode);
        snapshot.put("subjectName", target.getUserName());
        snapshot.put("targetSource", target.getTargetSource());
        snapshot.put("schedule", schedule);
        snapshot.put("target", target);
        return JSON.toJSONString(snapshot);
    }

    private int insertCredentialWithRetry(CompetitionSceneSchedule schedule,
                                          CompetitionSceneCredential credential,
                                          int sequence) {
        int nextSequence = Math.max(sequence, selectNextCredentialSequence(schedule));
        for (int retry = 0; retry < CREDENTIAL_INSERT_MAX_RETRY; retry++) {
            String credentialNo = generateCredentialNo(schedule, nextSequence);
            if (credentialMapper.selectCompetitionSceneCredentialByNo(credentialNo) != null) {
                nextSequence++;
                continue;
            }
            credential.setCredentialNo(credentialNo);
            try {
                credentialMapper.insertCompetitionSceneCredential(credential);
                return nextSequence + 1;
            } catch (DuplicateKeyException e) {
                refreshCredentialToken(credential);
                nextSequence = Math.max(nextSequence + 1, selectNextCredentialSequence(schedule));
            }
        }
        throw new ServiceException("证件编号生成冲突，请稍后重试");
    }

    private int selectNextCredentialSequence(CompetitionSceneSchedule schedule) {
        return credentialMapper.selectMaxCredentialNoSequence(schedule.getScheduleId(), generateCredentialNoPrefix(schedule)) + 1;
    }

    private String generateCredentialNo(CompetitionSceneSchedule schedule, int sequence) {
        return generateCredentialNoPrefix(schedule) + sequence;
    }

    private String generateCredentialNoPrefix(CompetitionSceneSchedule schedule) {
        String date = new SimpleDateFormat("yyyyMMdd").format(resolveCredentialDate(schedule));
        return "CS" + date + "-" + schedule.getScheduleId() + "-";
    }

    private Date resolveCredentialDate(CompetitionSceneSchedule schedule) {
        if (schedule.getContestStartTime() != null) {
            return schedule.getContestStartTime();
        }
        if (schedule.getReportStartTime() != null) {
            return schedule.getReportStartTime();
        }
        return DateUtils.getNowDate();
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

    private String resolveSubjectType(String credentialType, String configDimension) {
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credentialType)) {
            return CompetitionSceneConstants.SUBJECT_TYPE_EXPERT;
        }
        if (CompetitionSceneConstants.DIMENSION_TEAM.equals(configDimension)) {
            return CompetitionSceneConstants.SUBJECT_TYPE_TEAM;
        }
        return CompetitionSceneConstants.SUBJECT_TYPE_USER;
    }

    private String resolveCredentialSubjectCode(CompetitionSceneCredential credential) {
        if (credential == null) {
            return null;
        }
        if (StringUtils.isNotEmpty(credential.getSubjectCode())) {
            return credential.getSubjectCode();
        }
        if (CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(credential.getSubjectType())) {
            return credential.getTeamCode();
        }
        if (credential.getUserId() != null) {
            return String.valueOf(credential.getUserId());
        }
        return credential.getMemberId() == null ? null : "MEMBER:" + credential.getMemberId();
    }

    private Long parseLongOrNull(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
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

    private String buildAbilityJson(String scopeType, String credentialType) {
        Map<String, Object> ability = new LinkedHashMap<>();
        ability.put("report", false);
        ability.put("material", false);
        ability.put("waiting", false);
        ability.put("review", false);
        ability.put("resourceReservation", false);
        ability.put("vipAccess", false);
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT.equals(credentialType)
                || CompetitionSceneConstants.CREDENTIAL_TYPE_COMPETITOR.equals(credentialType)) {
            ability.put("report", true);
            ability.put("material", true);
            if (CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE.equals(scopeType)) {
                ability.put("waiting", true);
                ability.put("resourceReservation", true);
            }
        } else if (CompetitionSceneConstants.CREDENTIAL_TYPE_TEACHER.equals(credentialType)) {
            ability.put("report", true);
        } else if (CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credentialType)) {
            ability.put("report", true);
            ability.put("review", true);
        } else if (CompetitionSceneConstants.CREDENTIAL_TYPE_VIP.equals(credentialType)) {
            ability.put("report", true);
            ability.put("vipAccess", true);
        }
        return JSON.toJSONString(ability);
    }

    private String resolveConfigDimension(String configDimension) {
        if (CompetitionSceneConstants.DIMENSION_TEAM.equals(configDimension)) {
            return CompetitionSceneConstants.DIMENSION_TEAM;
        }
        return CompetitionSceneConstants.DIMENSION_PERSON;
    }

    private String normalizeRoleForSnapshot(String role) {
        String normalized = normalizeTargetRole(role, true);
        return StringUtils.isNotEmpty(normalized) ? normalized : role;
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
            case CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER:
            case "志愿者":
            case "赛场志愿者":
                return CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER;
            default:
                return allowUnknown ? null : CompetitionSceneConstants.TARGET_ROLE_MEMBER;
        }
    }

    private String firstNotEmpty(String first, String second) {
        return StringUtils.isNotEmpty(first) ? first : second;
    }

    private String currentUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception e) {
            return "system";
        }
    }
}
