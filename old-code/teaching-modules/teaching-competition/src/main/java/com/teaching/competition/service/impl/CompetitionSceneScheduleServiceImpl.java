package com.teaching.competition.service.impl;

import com.alibaba.fastjson2.JSON;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.DictConstant;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneMatchResult;
import com.teaching.competition.domain.CompetitionSceneSchedule;
import com.teaching.competition.domain.CompetitionSceneScheduleAutoSequenceDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleManualTargetDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleNameSequenceDTO;
import com.teaching.competition.domain.CompetitionSceneSchedulePersonBindDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleReviewObjectBindDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleTarget;
import com.teaching.competition.domain.CompetitionSceneScheduleSyncReviewSessionDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleTargetSequenceDTO;
import com.teaching.competition.domain.CompetitionSceneScheduleTeamBindDTO;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.mapper.CompetitionSceneCredentialMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleTargetMapper;
import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectCertificateRef;
import com.teaching.competition.review.domain.ReviewObjectMember;
import com.teaching.competition.review.domain.ReviewSession;
import com.teaching.competition.review.domain.ReviewSessionObject;
import com.teaching.competition.review.enums.ReviewCertificateValidStatus;
import com.teaching.competition.review.enums.ReviewCheckinStatus;
import com.teaching.competition.review.enums.ReviewObjectStatus;
import com.teaching.competition.review.enums.ReviewSessionObjectStatus;
import com.teaching.competition.review.mapper.ReviewObjectCertificateRefMapper;
import com.teaching.competition.review.mapper.ReviewObjectMapper;
import com.teaching.competition.review.mapper.ReviewObjectMemberMapper;
import com.teaching.competition.review.mapper.ReviewSessionMapper;
import com.teaching.competition.review.mapper.ReviewSessionObjectMapper;
import com.teaching.competition.service.ICompetitionSceneScheduleService;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * 赛事现场赛场安排Service业务层处理。
 */
@Service
public class CompetitionSceneScheduleServiceImpl implements ICompetitionSceneScheduleService {

    @Autowired
    private CompetitionSceneScheduleMapper scheduleMapper;

    @Autowired
    private CompetitionSceneScheduleTargetMapper targetMapper;

    @Autowired
    private CompetitionSceneCredentialMapper credentialMapper;

    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    private ReviewObjectMapper reviewObjectMapper;

    @Autowired
    private ReviewObjectMemberMapper reviewObjectMemberMapper;

    @Autowired
    private ReviewObjectCertificateRefMapper reviewObjectCertificateRefMapper;

    @Autowired
    private ReviewSessionMapper reviewSessionMapper;

    @Autowired
    private ReviewSessionObjectMapper reviewSessionObjectMapper;

    @Override
    public CompetitionSceneSchedule selectCompetitionSceneScheduleById(Long scheduleId) {
        return scheduleMapper.selectCompetitionSceneScheduleById(scheduleId);
    }

    @Override
    public List<CompetitionSceneSchedule> selectCompetitionSceneScheduleList(CompetitionSceneSchedule schedule) {
        return scheduleMapper.selectCompetitionSceneScheduleList(schedule);
    }

    @Override
    public int insertCompetitionSceneSchedule(CompetitionSceneSchedule schedule) {
        Date now = DateUtils.getNowDate();
        schedule.setCreateTime(now);
        schedule.setUpdateTime(now);
        schedule.setCreateBy(currentUsername());
        schedule.setUpdateBy(currentUsername());
        if (StringUtils.isEmpty(schedule.getCredentialType())) {
            schedule.setCredentialType(CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        } else {
            schedule.setCredentialType(normalizeCredentialType(schedule.getCredentialType(), false));
        }
        if (StringUtils.isEmpty(schedule.getConfigDimension())) {
            schedule.setConfigDimension(CompetitionSceneConstants.DIMENSION_PERSON);
        } else {
            schedule.setConfigDimension(resolveConfigDimension(schedule.getConfigDimension()));
        }
        if (StringUtils.isEmpty(schedule.getStatus())) {
            schedule.setStatus(CompetitionSceneConstants.STATUS_NORMAL);
        }
        if (schedule.getVersion() == null) {
            schedule.setVersion(0L);
        }
        if (StringUtils.isEmpty(schedule.getDelFlag())) {
            schedule.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        }
        return scheduleMapper.insertCompetitionSceneSchedule(schedule);
    }

    @Override
    public int updateCompetitionSceneSchedule(CompetitionSceneSchedule schedule) {
        schedule.setUpdateTime(DateUtils.getNowDate());
        schedule.setUpdateBy(currentUsername());
        return scheduleMapper.updateCompetitionSceneSchedule(schedule);
    }

    @Override
    public int deleteCompetitionSceneScheduleByIds(Long[] scheduleIds) {
        return scheduleMapper.deleteCompetitionSceneScheduleByIds(scheduleIds, currentUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneMatchResult matchScheduleTargets(Long scheduleId) {
        CompetitionSceneSchedule schedule = requireSchedule(scheduleId);
        CompetitionSceneMatchResult result = new CompetitionSceneMatchResult();
        List<CompetitionApplyInfo> applyInfoList = selectMatchedApplyInfos(schedule);
        if (applyInfoList.isEmpty()) {
            result.setMessage("未匹配到审核通过且已支付的报名信息");
            return result;
        }

        List<CompetitionSceneScheduleTarget> existedTargets =
                targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(scheduleId);
        Set<String> existedKeys = existedTargets.stream()
                .map(CompetitionSceneScheduleTarget::getTargetKey)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toSet());

        List<CompetitionSceneScheduleTarget> targets = buildTargets(schedule, applyInfoList);
        int matchedCount = 0;
        int skippedCount = 0;
        for (CompetitionSceneScheduleTarget target : targets) {
            if (existedKeys.contains(target.getTargetKey())) {
                skippedCount++;
                continue;
            }
            if (saveOrRestoreScheduleTarget(target)) {
                existedKeys.add(target.getTargetKey());
                matchedCount++;
            } else {
                if (StringUtils.isNotEmpty(target.getTargetKey())) {
                    existedKeys.add(target.getTargetKey());
                }
                skippedCount++;
            }
        }

        result.setMatchedCount(matchedCount);
        result.setSkippedCount(skippedCount);
        result.setMessage("匹配完成");
        return result;
    }

    @Override
    public List<CompetitionSceneScheduleTarget> selectScheduleTargetList(CompetitionSceneScheduleTarget target) {
        return targetMapper.selectCompetitionSceneScheduleTargetList(target);
    }

    @Override
    public CompetitionSceneMatchResult insertScheduleTarget(CompetitionSceneScheduleTarget target) {
        if (target == null) {
            throw new ServiceException("安排对象不能为空");
        }
        CompetitionSceneSchedule schedule = requireSchedule(target.getScheduleId());
        fillTargetBaseInfo(target, schedule, true);
        fillTargetIdentity(target);
        fillTargetInsertDefaults(target);
        CompetitionSceneMatchResult result = new CompetitionSceneMatchResult();
        if (targetExists(target.getScheduleId(), target.getTargetKey())) {
            result.setSkippedCount(1);
            result.setMessage(buildDuplicateTargetMessage(target));
            return result;
        }
        if (saveOrRestoreScheduleTarget(target)) {
            result.setMatchedCount(1);
            result.setMessage("新增成功");
            return result;
        }
        result.setSkippedCount(1);
        result.setMessage(buildDuplicateTargetMessage(target));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneMatchResult insertScheduleTargets(List<CompetitionSceneScheduleTarget> targets) {
        if (targets == null || targets.isEmpty()) {
            throw new ServiceException("安排对象不能为空");
        }
        Long scheduleId = targets.stream()
                .filter(Objects::nonNull)
                .map(CompetitionSceneScheduleTarget::getScheduleId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new ServiceException("赛场安排ID不能为空"));
        CompetitionSceneSchedule schedule = requireSchedule(scheduleId);

        List<CompetitionSceneScheduleTarget> existedTargets =
                targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(scheduleId);
        Set<String> existedKeys = existedTargets.stream()
                .map(CompetitionSceneScheduleTarget::getTargetKey)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toSet());

        CompetitionSceneMatchResult result = new CompetitionSceneMatchResult();
        int matchedCount = 0;
        int skippedCount = 0;
        for (CompetitionSceneScheduleTarget target : targets) {
            if (target == null) {
                skippedCount++;
                continue;
            }
            if (target.getScheduleId() == null) {
                target.setScheduleId(scheduleId);
            } else if (!Objects.equals(target.getScheduleId(), scheduleId)) {
                throw new ServiceException("批量新增对象必须属于同一个赛场安排");
            }
            fillTargetBaseInfo(target, schedule, true);
            fillTargetIdentity(target);
            fillTargetInsertDefaults(target);
            if (StringUtils.isNotEmpty(target.getTargetKey()) && existedKeys.contains(target.getTargetKey())) {
                skippedCount++;
                continue;
            }
            if (saveOrRestoreScheduleTarget(target)) {
                if (StringUtils.isNotEmpty(target.getTargetKey())) {
                    existedKeys.add(target.getTargetKey());
                }
                matchedCount++;
            } else {
                skippedCount++;
                if (StringUtils.isNotEmpty(target.getTargetKey())) {
                    existedKeys.add(target.getTargetKey());
                }
            }
        }

        result.setMatchedCount(matchedCount);
        result.setSkippedCount(skippedCount);
        result.setMessage("批量新增完成");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneMatchResult bindReviewObjects(Long scheduleId, CompetitionSceneScheduleReviewObjectBindDTO dto) {
        CompetitionSceneSchedule schedule = requireSchedule(scheduleId);
        List<Long> ids = dto == null ? null : dto.getReviewObjectIds();
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException("评审对象ID不能为空");
        }
        CompetitionSceneMatchResult result = new CompetitionSceneMatchResult();
        result.setTotalCount(ids.size());
        for (Long reviewObjectId : ids) {
            if (reviewObjectId == null) {
                incFailed(result, "评审对象ID为空");
                continue;
            }
            ReviewObject object = reviewObjectMapper.selectById(reviewObjectId);
            if (object == null) {
                incFailed(result, "评审对象不存在：" + reviewObjectId);
                continue;
            }
            CompetitionSceneScheduleTarget target = buildTargetFromReviewObject(schedule, object,
                    CompetitionSceneConstants.TARGET_TYPE_REVIEW_OBJECT);
            saveTargetAndCount(target, result);
        }
        result.setMessage("绑定评审对象完成");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneMatchResult bindTeams(Long scheduleId, CompetitionSceneScheduleTeamBindDTO dto) {
        CompetitionSceneSchedule schedule = requireSchedule(scheduleId);
        List<String> teamCodes = dto == null ? null : dto.getTeamCodes();
        if (teamCodes == null || teamCodes.isEmpty()) {
            throw new ServiceException("团队编号不能为空");
        }
        CompetitionSceneMatchResult result = new CompetitionSceneMatchResult();
        result.setTotalCount(teamCodes.size());
        for (String rawTeamCode : teamCodes) {
            String teamCode = trimToNull(rawTeamCode);
            if (teamCode == null) {
                incFailed(result, "团队编号为空");
                continue;
            }
            ReviewObject object = findReviewObjectByTeam(null, teamCode);
            CompetitionSceneScheduleTarget target;
            if (object != null) {
                target = buildTargetFromReviewObject(schedule, object, CompetitionSceneConstants.TARGET_TYPE_TEAM);
                target.setTargetKey("TEAM:" + teamCode);
                target.setConfigDimension(CompetitionSceneConstants.DIMENSION_TEAM);
            } else {
                CompetitionApplyInfo applyInfo = firstApplyByTeamCode(teamCode);
                target = applyInfo == null
                        ? buildManualLikeTarget(schedule, CompetitionSceneConstants.TARGET_TYPE_TEAM, teamCode, teamCode, null, null)
                        : buildTargetFromApply(schedule, applyInfo);
                target.setTargetType(CompetitionSceneConstants.TARGET_TYPE_TEAM);
                target.setTargetName(StringUtils.isNotEmpty(target.getTeamName()) ? target.getTeamName() : teamCode);
                target.setConfigDimension(CompetitionSceneConstants.DIMENSION_TEAM);
                target.setTargetKey("TEAM:" + teamCode);
                target.setSourceModule("competition");
                target.setSourceBizType("TEAM");
                target.setSourceBizId(teamCode);
                result.getWarnings().add("团队 " + teamCode + " 未匹配到评审对象，已仅作为赛场安排对象保存");
            }
            saveTargetAndCount(target, result);
        }
        result.setMessage("绑定团队完成");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneMatchResult bindPersons(Long scheduleId, CompetitionSceneSchedulePersonBindDTO dto) {
        CompetitionSceneSchedule schedule = requireSchedule(scheduleId);
        List<String> memberIds = dto == null ? null : dto.getMemberIds();
        if (memberIds == null || memberIds.isEmpty()) {
            throw new ServiceException("人员ID不能为空");
        }
        CompetitionSceneMatchResult result = new CompetitionSceneMatchResult();
        result.setTotalCount(memberIds.size());
        for (String rawMemberId : memberIds) {
            String memberIdText = trimToNull(rawMemberId);
            if (memberIdText == null) {
                incFailed(result, "人员ID为空");
                continue;
            }
            Long numericMemberId = parseLong(memberIdText);
            ReviewObject object = findReviewObjectByMember(null, memberIdText);
            CompetitionSceneScheduleTarget target;
            if (object != null) {
                target = buildTargetFromReviewObject(schedule, object, CompetitionSceneConstants.TARGET_TYPE_PERSON);
                target.setTargetKey("PERSON:" + memberIdText);
                target.setMemberId(numericMemberId);
                fillTargetFromPrimaryMember(target, object.getActivityId(), object.getId(), memberIdText, null);
            } else {
                CompetitionApplyInfo applyInfo = numericMemberId == null
                        ? null
                        : competitionApplyInfoMapper.selectCompetitionApplyInfoByMemberId(numericMemberId);
                target = applyInfo == null
                        ? buildManualLikeTarget(schedule, CompetitionSceneConstants.TARGET_TYPE_PERSON,
                                memberIdText, "人员" + memberIdText, numericMemberId, null)
                        : buildTargetFromApply(schedule, applyInfo);
                target.setTargetType(CompetitionSceneConstants.TARGET_TYPE_PERSON);
                target.setTargetName(StringUtils.isNotEmpty(target.getUserName()) ? target.getUserName() : "人员" + memberIdText);
                target.setTargetKey("PERSON:" + memberIdText);
                target.setSourceModule("competition");
                target.setSourceBizType("PERSON");
                target.setSourceBizId(memberIdText);
                result.getWarnings().add("人员 " + memberIdText + " 未匹配到评审对象，已仅作为赛场安排对象保存");
            }
            saveTargetAndCount(target, result);
        }
        result.setMessage("绑定人员完成");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneMatchResult insertManualTarget(Long scheduleId, CompetitionSceneScheduleManualTargetDTO dto) {
        CompetitionSceneSchedule schedule = requireSchedule(scheduleId);
        if (dto == null || StringUtils.isEmpty(dto.getTargetName())) {
            throw new ServiceException("手工对象名称不能为空");
        }
        CompetitionSceneScheduleTarget target = buildManualLikeTarget(schedule,
                CompetitionSceneConstants.TARGET_TYPE_MANUAL,
                null,
                dto.getTargetName(),
                null,
                null);
        target.setOrgName(dto.getOrgName());
        target.setPhone(dto.getContactPhone());
        target.setRemark(dto.getRemark());
        target.setTargetKey("MANUAL:" + UUID.randomUUID().toString().replace("-", ""));
        CompetitionSceneMatchResult result = new CompetitionSceneMatchResult();
        result.setTotalCount(1);
        saveTargetAndCount(target, result);
        result.setMessage("手工新增完成");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneMatchResult updateTargetSequences(Long scheduleId, List<CompetitionSceneScheduleTargetSequenceDTO> items) {
        requireSchedule(scheduleId);
        if (items == null || items.isEmpty()) {
            throw new ServiceException("顺序列表不能为空");
        }
        Map<Long, CompetitionSceneScheduleTarget> existedMap = targetMapper
                .selectCompetitionSceneScheduleTargetByScheduleId(scheduleId)
                .stream()
                .collect(Collectors.toMap(CompetitionSceneScheduleTarget::getTargetId, item -> item, (a, b) -> a));
        Map<Long, Integer> finalSequenceMap = new HashMap<>();
        for (CompetitionSceneScheduleTarget target : existedMap.values()) {
            finalSequenceMap.put(target.getTargetId(), target.getSequenceNo());
        }
        for (CompetitionSceneScheduleTargetSequenceDTO item : items) {
            if (item == null || item.getTargetId() == null || !existedMap.containsKey(item.getTargetId())) {
                throw new ServiceException("存在不属于当前赛场安排的绑定对象");
            }
            Integer sequenceNo = item.getSequenceNo();
            if (sequenceNo != null && sequenceNo <= 0) {
                throw new ServiceException("顺序号必须大于0");
            }
            finalSequenceMap.put(item.getTargetId(), sequenceNo);
        }
        Set<Integer> sequenceSet = new HashSet<>();
        for (Integer sequenceNo : finalSequenceMap.values()) {
            if (sequenceNo == null) {
                continue;
            }
            if (!sequenceSet.add(sequenceNo)) {
                throw new ServiceException("同一赛场安排下顺序号不能重复：" + sequenceNo);
            }
        }
        CompetitionSceneMatchResult result = new CompetitionSceneMatchResult();
        result.setTotalCount(items.size());
        for (CompetitionSceneScheduleTargetSequenceDTO item : items) {
            targetMapper.updateCompetitionSceneScheduleTargetSequence(scheduleId, item.getTargetId(),
                    item.getSequenceNo(), currentUsername());
            result.setMatchedCount(result.getMatchedCount() + 1);
        }
        result.setMessage("顺序保存完成");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneMatchResult autoGenerateTargetSequence(Long scheduleId, CompetitionSceneScheduleAutoSequenceDTO dto) {
        requireSchedule(scheduleId);
        boolean overwrite = dto != null && Boolean.TRUE.equals(dto.getOverwriteExisting());
        List<CompetitionSceneScheduleTarget> targets = targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(scheduleId);
        int next = overwrite ? 1 : targets.stream()
                .map(CompetitionSceneScheduleTarget::getSequenceNo)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1;
        CompetitionSceneMatchResult result = new CompetitionSceneMatchResult();
        result.setTotalCount(targets.size());
        for (CompetitionSceneScheduleTarget target : targets) {
            if (!overwrite && target.getSequenceNo() != null) {
                result.setSkippedCount(result.getSkippedCount() + 1);
                continue;
            }
            targetMapper.updateCompetitionSceneScheduleTargetSequence(scheduleId, target.getTargetId(), next++, currentUsername());
            result.setMatchedCount(result.getMatchedCount() + 1);
        }
        result.setMessage("自动生成顺序完成");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneMatchResult generateTargetSequenceByNames(Long scheduleId, CompetitionSceneScheduleNameSequenceDTO dto) {
        requireSchedule(scheduleId);
        List<String> orderedNames = parseSequenceNames(dto);
        if (orderedNames.isEmpty()) {
            throw new ServiceException("姓名名单不能为空");
        }
        List<CompetitionSceneScheduleTarget> targets = targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(scheduleId);
        if (targets.isEmpty()) {
            throw new ServiceException("当前赛场安排暂无绑定对象");
        }

        Map<String, List<CompetitionSceneScheduleTarget>> targetNameMap = new HashMap<>();
        for (CompetitionSceneScheduleTarget target : targets) {
            for (String candidate : buildTargetNameCandidates(target)) {
                String normalized = normalizeSequenceName(candidate);
                if (StringUtils.isEmpty(normalized)) {
                    continue;
                }
                targetNameMap.computeIfAbsent(normalized, key -> new ArrayList<>()).add(target);
            }
        }

        CompetitionSceneMatchResult result = new CompetitionSceneMatchResult();
        result.setTotalCount(orderedNames.size());
        List<CompetitionSceneScheduleTarget> matchedTargets = new ArrayList<>();
        Set<Long> usedTargetIds = new HashSet<>();
        Set<String> usedNames = new HashSet<>();
        for (String rawName : orderedNames) {
            String name = trimToNull(rawName);
            String normalized = normalizeSequenceName(name);
            if (StringUtils.isEmpty(normalized)) {
                continue;
            }
            if (!usedNames.add(normalized)) {
                result.setSkippedCount(result.getSkippedCount() + 1);
                result.getWarnings().add("姓名重复，已忽略后续输入：" + name);
                continue;
            }
            List<CompetitionSceneScheduleTarget> candidates = targetNameMap.get(normalized);
            if (candidates == null || candidates.isEmpty()) {
                result.setFailedCount(result.getFailedCount() + 1);
                result.getWarnings().add("未匹配到姓名：" + name);
                continue;
            }
            CompetitionSceneScheduleTarget selected = null;
            for (CompetitionSceneScheduleTarget candidate : candidates) {
                if (!usedTargetIds.contains(candidate.getTargetId())) {
                    selected = candidate;
                    break;
                }
            }
            if (selected == null) {
                result.setSkippedCount(result.getSkippedCount() + 1);
                result.getWarnings().add("姓名已被前序规则占用：" + name);
                continue;
            }
            if (candidates.size() > 1) {
                result.getWarnings().add("姓名存在多个匹配，已按当前列表顺序选取：" + name);
            }
            matchedTargets.add(selected);
            usedTargetIds.add(selected.getTargetId());
            result.setMatchedCount(result.getMatchedCount() + 1);
        }

        if (matchedTargets.isEmpty()) {
            throw new ServiceException("姓名名单未匹配到任何绑定对象");
        }

        List<CompetitionSceneScheduleTarget> orderedTargets = new ArrayList<>(matchedTargets);
        for (CompetitionSceneScheduleTarget target : targets) {
            if (!usedTargetIds.contains(target.getTargetId())) {
                orderedTargets.add(target);
            }
        }
        int sequence = 1;
        for (CompetitionSceneScheduleTarget target : orderedTargets) {
            targetMapper.updateCompetitionSceneScheduleTargetSequence(scheduleId, target.getTargetId(),
                    sequence++, currentUsername());
        }
        result.setMessage("按姓名排序完成");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneMatchResult syncTargetsToReviewSession(Long scheduleId, CompetitionSceneScheduleSyncReviewSessionDTO dto) {
        requireSchedule(scheduleId);
        if (dto == null || dto.getSessionId() == null) {
            throw new ServiceException("评审现场场次ID不能为空");
        }
        ReviewSession session = reviewSessionMapper.selectById(dto.getSessionId());
        if (session == null) {
            throw new ServiceException("评审现场场次不存在");
        }
        List<CompetitionSceneScheduleTarget> targets = targetMapper.selectCompetitionSceneScheduleTargetByScheduleId(scheduleId);
        CompetitionSceneMatchResult result = new CompetitionSceneMatchResult();
        result.setTotalCount(targets.size());
        int fallbackSequence = currentMaxReviewSessionSequence(session.getId()) + 1;
        for (CompetitionSceneScheduleTarget target : targets) {
            Long objectId = resolveReviewObjectId(target, session.getActivityId());
            if (objectId == null) {
                result.setSkippedCount(result.getSkippedCount() + 1);
                result.getWarnings().add(buildTargetDisplayName(target) + " 未匹配到评审对象，未同步到现场场次");
                continue;
            }
            ReviewObject object = reviewObjectMapper.selectById(objectId);
            if (object == null || !Objects.equals(object.getActivityId(), session.getActivityId())) {
                result.setSkippedCount(result.getSkippedCount() + 1);
                result.getWarnings().add(buildTargetDisplayName(target) + " 匹配到的评审对象不属于当前评审活动，未同步");
                continue;
            }
            ReviewSessionObject existed = findReviewSessionObject(session.getId(), objectId);
            Integer sequenceNo = target.getSequenceNo() == null ? fallbackSequence++ : target.getSequenceNo();
            if (existed != null) {
                existed.setSequenceNo(sequenceNo);
                existed.setUpdateBy(currentUsername());
                existed.setUpdateTime(DateUtils.getNowDate());
                reviewSessionObjectMapper.update(existed);
                result.setSkippedCount(result.getSkippedCount() + 1);
                continue;
            }
            ReviewSessionObject sessionObject = new ReviewSessionObject();
            sessionObject.setActivityId(session.getActivityId());
            sessionObject.setRoundId(session.getRoundId());
            sessionObject.setSessionId(session.getId());
            sessionObject.setObjectId(objectId);
            sessionObject.setSequenceNo(sequenceNo);
            sessionObject.setCheckinStatus(ReviewCheckinStatus.WAITING.getCode());
            sessionObject.setReviewStatus(ReviewSessionObjectStatus.WAITING.getCode());
            sessionObject.setRemark("由赛场安排 " + scheduleId + " 同步");
            sessionObject.setCreateBy(currentUsername());
            sessionObject.setUpdateBy(currentUsername());
            Date now = DateUtils.getNowDate();
            sessionObject.setCreateTime(now);
            sessionObject.setUpdateTime(now);
            sessionObject.setDelFlag("0");
            reviewSessionObjectMapper.insert(sessionObject);
            result.setMatchedCount(result.getMatchedCount() + 1);
        }
        result.setMessage("同步评审现场场次完成");
        return result;
    }

    @Override
    public int updateScheduleTarget(CompetitionSceneScheduleTarget target) {
        if (target.getTargetId() == null) {
            throw new ServiceException("安排对象ID不能为空");
        }
        CompetitionSceneScheduleTarget existed = targetMapper.selectCompetitionSceneScheduleTargetById(target.getTargetId());
        if (existed == null) {
            throw new ServiceException("安排对象不存在");
        }
        Long scheduleId = target.getScheduleId() == null ? existed.getScheduleId() : target.getScheduleId();
        CompetitionSceneSchedule schedule = requireSchedule(scheduleId);
        String originalTargetKey = StringUtils.isNotEmpty(target.getTargetKey())
                ? target.getTargetKey()
                : existed.getTargetKey();
        target.setScheduleId(scheduleId);
        if (StringUtils.isEmpty(target.getConfigDimension())) {
            target.setConfigDimension(existed.getConfigDimension());
        }
        target.setTargetKey(null);
        fillTargetBaseInfo(target, schedule, false);
        if (StringUtils.isNotEmpty(originalTargetKey)
                && originalTargetKey.startsWith("MANUAL:")
                && StringUtils.isNotEmpty(target.getTargetKey())
                && target.getTargetKey().startsWith("MANUAL:")) {
            target.setTargetKey(originalTargetKey);
        }
        fillTargetIdentity(target);
        if (StringUtils.isEmpty(target.getTargetKey())) {
            fillTargetKey(target);
        }
        target.setWaitingGroupCode(existed.getWaitingGroupCode());
        target.setWaitingGroupName(existed.getWaitingGroupName());
        if (targetExistsForOther(target.getScheduleId(), target.getTargetKey(), target.getTargetId())) {
            throw new ServiceException(buildDuplicateTargetMessage(target));
        }
        target.setUpdateBy(currentUsername());
        target.setUpdateTime(DateUtils.getNowDate());
        try {
            return targetMapper.updateCompetitionSceneScheduleTarget(target);
        } catch (DuplicateKeyException e) {
            throw new ServiceException(buildDuplicateTargetMessage(target));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteScheduleTargetByIds(Long[] targetIds) {
        String username = currentUsername();
        credentialMapper.deleteCompetitionSceneCredentialByTargetIds(targetIds, username);
        return targetMapper.deleteCompetitionSceneScheduleTargetByIds(targetIds, username);
    }

    private CompetitionSceneScheduleTarget buildTargetFromReviewObject(CompetitionSceneSchedule schedule,
                                                                       ReviewObject object,
                                                                       String targetType) {
        CompetitionSceneScheduleTarget target = new CompetitionSceneScheduleTarget();
        target.setScheduleId(schedule.getScheduleId());
        target.setCompetitionSeriesId(schedule.getCompetitionSeriesId());
        target.setCredentialType(CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        target.setConfigDimension(StringUtils.isNotEmpty(object.getSourceTeamId())
                ? CompetitionSceneConstants.DIMENSION_TEAM
                : CompetitionSceneConstants.DIMENSION_PERSON);
        target.setTargetSource(CompetitionSceneConstants.TARGET_SOURCE_REVIEW);
        target.setTargetType(targetType);
        target.setReviewObjectId(object.getId());
        target.setTargetName(object.getObjectName());
        target.setTeamCode(object.getSourceTeamId());
        target.setTeamName(object.getObjectName());
        target.setUserName(object.getContactName());
        target.setPhone(object.getContactPhone());
        target.setEmail(object.getContactEmail());
        target.setOrgName(object.getOrgName());
        target.setCompetitionTrackId(schedule.getCompetitionTrackId());
        target.setCompetitionTrackName(schedule.getCompetitionTrackName());
        target.setSecondLevelCode(schedule.getSecondLevelCode());
        target.setSecondLevelName(schedule.getSecondLevelName());
        target.setWaitingGroupCode(schedule.getWaitingGroupCode());
        target.setWaitingGroupName(schedule.getWaitingGroupName());
        target.setCertificateCode(firstCertificateCode(object.getActivityId(), object.getId()));
        target.setSourceModule(StringUtils.isNotEmpty(object.getSourceModule()) ? object.getSourceModule() : "review");
        target.setSourceBizType(StringUtils.isNotEmpty(object.getSourceBizType()) ? object.getSourceBizType() : "REVIEW_OBJECT");
        target.setSourceBizId(StringUtils.isNotEmpty(object.getSourceBizId()) ? object.getSourceBizId() : String.valueOf(object.getId()));
        if (CompetitionSceneConstants.TARGET_TYPE_TEAM.equals(targetType) && StringUtils.isNotEmpty(object.getSourceTeamId())) {
            target.setTargetKey("TEAM:" + object.getSourceTeamId());
        } else {
            target.setTargetKey("REVIEW_OBJECT:" + object.getId());
        }
        fillTargetFromPrimaryMember(target, object.getActivityId(), object.getId(), null, null);
        target.setTargetSnapshotJson(JSON.toJSONString(object));
        fillTargetInsertDefaults(target);
        return target;
    }

    private CompetitionSceneScheduleTarget buildManualLikeTarget(CompetitionSceneSchedule schedule,
                                                                 String targetType,
                                                                 String targetCode,
                                                                 String targetName,
                                                                 Long memberId,
                                                                 Long userId) {
        CompetitionSceneScheduleTarget target = new CompetitionSceneScheduleTarget();
        target.setScheduleId(schedule.getScheduleId());
        target.setCompetitionSeriesId(schedule.getCompetitionSeriesId());
        target.setCredentialType(CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT);
        target.setConfigDimension(CompetitionSceneConstants.TARGET_TYPE_TEAM.equals(targetType)
                ? CompetitionSceneConstants.DIMENSION_TEAM
                : CompetitionSceneConstants.DIMENSION_PERSON);
        target.setTargetSource(CompetitionSceneConstants.TARGET_SOURCE_MANUAL);
        target.setTargetType(targetType);
        target.setTargetName(targetName);
        target.setTeamCode(CompetitionSceneConstants.TARGET_TYPE_TEAM.equals(targetType) ? targetCode : null);
        target.setTeamName(CompetitionSceneConstants.TARGET_TYPE_TEAM.equals(targetType) ? targetName : null);
        target.setMemberId(memberId);
        target.setUserId(userId);
        target.setUserName(CompetitionSceneConstants.TARGET_TYPE_PERSON.equals(targetType) ? targetName : null);
        target.setCompetitionTrackId(schedule.getCompetitionTrackId());
        target.setCompetitionTrackName(schedule.getCompetitionTrackName());
        target.setSecondLevelCode(schedule.getSecondLevelCode());
        target.setSecondLevelName(schedule.getSecondLevelName());
        target.setWaitingGroupCode(schedule.getWaitingGroupCode());
        target.setWaitingGroupName(schedule.getWaitingGroupName());
        target.setSourceModule(CompetitionSceneConstants.TARGET_TYPE_MANUAL.equals(targetType) ? "MANUAL" : "competition");
        target.setSourceBizType(targetType);
        target.setSourceBizId(targetCode);
        if (CompetitionSceneConstants.TARGET_TYPE_TEAM.equals(targetType) && StringUtils.isNotEmpty(targetCode)) {
            target.setTargetKey("TEAM:" + targetCode);
        } else if (CompetitionSceneConstants.TARGET_TYPE_PERSON.equals(targetType) && memberId != null) {
            target.setTargetKey("PERSON:" + memberId);
        } else {
            fillTargetKey(target);
        }
        fillTargetInsertDefaults(target);
        return target;
    }

    private void saveTargetAndCount(CompetitionSceneScheduleTarget target, CompetitionSceneMatchResult result) {
        if (targetExists(target.getScheduleId(), target.getTargetKey())) {
            result.setSkippedCount(result.getSkippedCount() + 1);
            result.getWarnings().add(buildDuplicateTargetMessage(target));
            return;
        }
        if (saveOrRestoreScheduleTarget(target)) {
            result.setMatchedCount(result.getMatchedCount() + 1);
        } else {
            result.setSkippedCount(result.getSkippedCount() + 1);
            result.getWarnings().add(buildDuplicateTargetMessage(target));
        }
    }

    private void incFailed(CompetitionSceneMatchResult result, String message) {
        result.setFailedCount(result.getFailedCount() + 1);
        result.getWarnings().add(message);
    }

    private CompetitionApplyInfo firstApplyByTeamCode(String teamCode) {
        List<CompetitionApplyInfo> applyInfos = competitionApplyInfoMapper.selectCompetitionApplyTeamCode(teamCode);
        return applyInfos == null || applyInfos.isEmpty() ? null : applyInfos.get(0);
    }

    private String firstCertificateCode(Long activityId, Long objectId) {
        if (objectId == null) {
            return null;
        }
        ReviewObjectCertificateRef query = new ReviewObjectCertificateRef();
        query.setActivityId(activityId);
        query.setObjectId(objectId);
        query.setValidStatus(ReviewCertificateValidStatus.VALID.getCode());
        List<ReviewObjectCertificateRef> certificates = reviewObjectCertificateRefMapper.selectList(query);
        return certificates == null || certificates.isEmpty() ? null : certificates.get(0).getCertificateCode();
    }

    private void fillTargetFromPrimaryMember(CompetitionSceneScheduleTarget target,
                                             Long activityId,
                                             Long objectId,
                                             String preferredMemberId,
                                             Long preferredUserId) {
        ReviewObjectMember query = new ReviewObjectMember();
        query.setActivityId(activityId);
        query.setObjectId(objectId);
        if (StringUtils.isNotEmpty(preferredMemberId)) {
            query.setPersonId(preferredMemberId);
        }
        if (preferredUserId != null) {
            query.setUserId(preferredUserId);
        }
        List<ReviewObjectMember> members = reviewObjectMemberMapper.selectList(query);
        if ((members == null || members.isEmpty()) && objectId != null) {
            ReviewObjectMember fallbackQuery = new ReviewObjectMember();
            fallbackQuery.setActivityId(activityId);
            fallbackQuery.setObjectId(objectId);
            members = reviewObjectMemberMapper.selectList(fallbackQuery);
        }
        if (members == null || members.isEmpty()) {
            return;
        }
        ReviewObjectMember member = members.get(0);
        Long memberId = parseLong(member.getPersonId());
        if (memberId != null) {
            target.setMemberId(memberId);
        }
        if (target.getUserId() == null) {
            target.setUserId(member.getUserId());
        }
        if (StringUtils.isEmpty(target.getUserName())) {
            target.setUserName(member.getMemberName());
        }
        if (StringUtils.isEmpty(target.getTargetName()) && StringUtils.isNotEmpty(member.getMemberName())) {
            target.setTargetName(member.getMemberName());
        }
        if (StringUtils.isEmpty(target.getPhone())) {
            target.setPhone(member.getPhone());
        }
        if (StringUtils.isEmpty(target.getEmail())) {
            target.setEmail(member.getEmail());
        }
        if (StringUtils.isEmpty(target.getOrgName())) {
            target.setOrgName(member.getOrgName());
        }
        if (StringUtils.isEmpty(target.getCertificateCode())) {
            target.setCertificateCode(member.getCertificateCode());
        }
    }

    private ReviewObject findReviewObjectByTeam(Long activityId, String teamCode) {
        if (StringUtils.isEmpty(teamCode)) {
            return null;
        }
        ReviewObject query = new ReviewObject();
        query.setActivityId(activityId);
        query.setSourceTeamId(teamCode);
        List<ReviewObject> list = reviewObjectMapper.selectList(query);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    private ReviewObject findReviewObjectByMember(Long activityId, String memberId) {
        if (StringUtils.isEmpty(memberId)) {
            return null;
        }
        ReviewObjectMember query = new ReviewObjectMember();
        query.setActivityId(activityId);
        query.setPersonId(memberId);
        List<ReviewObjectMember> members = reviewObjectMemberMapper.selectList(query);
        if (members == null || members.isEmpty()) {
            return null;
        }
        return reviewObjectMapper.selectById(members.get(0).getObjectId());
    }

    private ReviewObject findReviewObjectByUser(Long activityId, Long userId) {
        if (userId == null) {
            return null;
        }
        ReviewObjectMember query = new ReviewObjectMember();
        query.setActivityId(activityId);
        query.setUserId(userId);
        List<ReviewObjectMember> members = reviewObjectMemberMapper.selectList(query);
        if (members == null || members.isEmpty()) {
            return null;
        }
        return reviewObjectMapper.selectById(members.get(0).getObjectId());
    }

    private ReviewObject findReviewObjectByCertificate(Long activityId, String certificateCode) {
        if (StringUtils.isEmpty(certificateCode)) {
            return null;
        }
        ReviewObjectCertificateRef query = new ReviewObjectCertificateRef();
        query.setActivityId(activityId);
        query.setCertificateCode(certificateCode);
        query.setValidStatus(ReviewCertificateValidStatus.VALID.getCode());
        List<ReviewObjectCertificateRef> refs = reviewObjectCertificateRefMapper.selectList(query);
        if (refs == null || refs.isEmpty()) {
            return null;
        }
        return reviewObjectMapper.selectById(refs.get(0).getObjectId());
    }

    private Long resolveReviewObjectId(CompetitionSceneScheduleTarget target, Long activityId) {
        if (target.getReviewObjectId() != null) {
            return target.getReviewObjectId();
        }
        ReviewObject object = findReviewObjectByTeam(activityId, target.getTeamCode());
        if (object != null) {
            return object.getId();
        }
        object = findReviewObjectByMember(activityId, target.getMemberId() == null ? null : String.valueOf(target.getMemberId()));
        if (object != null) {
            return object.getId();
        }
        object = findReviewObjectByUser(activityId, target.getUserId());
        if (object != null) {
            return object.getId();
        }
        object = findReviewObjectByCertificate(activityId, target.getCertificateCode());
        return object == null ? null : object.getId();
    }

    private ReviewSessionObject findReviewSessionObject(Long sessionId, Long objectId) {
        ReviewSessionObject query = new ReviewSessionObject();
        query.setSessionId(sessionId);
        query.setObjectId(objectId);
        List<ReviewSessionObject> list = reviewSessionObjectMapper.selectList(query);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    private int currentMaxReviewSessionSequence(Long sessionId) {
        ReviewSessionObject query = new ReviewSessionObject();
        query.setSessionId(sessionId);
        List<ReviewSessionObject> list = reviewSessionObjectMapper.selectList(query);
        if (list == null || list.isEmpty()) {
            return 0;
        }
        return list.stream()
                .map(ReviewSessionObject::getSequenceNo)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }

    private String buildTargetDisplayName(CompetitionSceneScheduleTarget target) {
        if (StringUtils.isNotEmpty(target.getTargetName())) {
            return target.getTargetName();
        }
        if (StringUtils.isNotEmpty(target.getTeamName())) {
            return target.getTeamName();
        }
        if (StringUtils.isNotEmpty(target.getUserName())) {
            return target.getUserName();
        }
        return target.getTargetKey() == null ? "绑定对象" + target.getTargetId() : target.getTargetKey();
    }

    private boolean saveOrRestoreScheduleTarget(CompetitionSceneScheduleTarget target) {
        if (restoreDeletedScheduleTarget(target)) {
            return true;
        }
        try {
            return targetMapper.insertCompetitionSceneScheduleTarget(target) > 0;
        } catch (DuplicateKeyException e) {
            return restoreDeletedScheduleTarget(target);
        }
    }

    private boolean restoreDeletedScheduleTarget(CompetitionSceneScheduleTarget target) {
        if (target == null || target.getScheduleId() == null || StringUtils.isEmpty(target.getTargetKey())) {
            return false;
        }
        CompetitionSceneScheduleTarget existed =
                targetMapper.selectCompetitionSceneScheduleTargetByScheduleIdAndTargetKey(
                        target.getScheduleId(), target.getTargetKey());
        if (existed == null || CompetitionSceneConstants.DEL_FLAG_NORMAL.equals(existed.getDelFlag())) {
            return false;
        }
        target.setTargetId(existed.getTargetId());
        return targetMapper.restoreCompetitionSceneScheduleTarget(target) > 0;
    }

    private CompetitionSceneSchedule requireSchedule(Long scheduleId) {
        if (scheduleId == null) {
            throw new ServiceException("赛场安排ID不能为空");
        }
        CompetitionSceneSchedule schedule = scheduleMapper.selectCompetitionSceneScheduleById(scheduleId);
        if (schedule == null) {
            throw new ServiceException("赛场安排不存在");
        }
        return schedule;
    }

    private List<CompetitionApplyInfo> selectMatchedApplyInfos(CompetitionSceneSchedule schedule) {
        CompetitionApplyInfo query = new CompetitionApplyInfo();
        query.setCompetitionSeriesId(schedule.getCompetitionSeriesId());
        query.setCompetitionTrackId(schedule.getCompetitionTrackId());
        query.setSecondLevelCode(schedule.getSecondLevelCode());
        query.setCheckStatus(Constants.CHECK_PASS);
        query.setPayStatus(DictConstant.PAID);

        List<CompetitionApplyInfo> list = competitionApplyInfoMapper.selectCompetitionApplyInfoList(query);
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    private List<CompetitionSceneScheduleTarget> buildTargets(CompetitionSceneSchedule schedule,
                                                              List<CompetitionApplyInfo> applyInfoList) {
        if (CompetitionSceneConstants.DIMENSION_TEAM.equals(schedule.getConfigDimension())) {
            Map<String, CompetitionApplyInfo> teamApplyMap = new LinkedHashMap<>();
            for (CompetitionApplyInfo applyInfo : applyInfoList) {
                if (StringUtils.isEmpty(applyInfo.getTeamCode())) {
                    continue;
                }
                teamApplyMap.putIfAbsent(applyInfo.getTeamCode(), applyInfo);
            }
            return teamApplyMap.values().stream()
                    .map(applyInfo -> buildTargetFromApply(schedule, applyInfo))
                    .collect(Collectors.toList());
        }
        return applyInfoList.stream()
                .map(applyInfo -> buildTargetFromApply(schedule, applyInfo))
                .collect(Collectors.toList());
    }

    private CompetitionSceneScheduleTarget buildTargetFromApply(CompetitionSceneSchedule schedule,
                                                                CompetitionApplyInfo applyInfo) {
        CompetitionSceneScheduleTarget target = new CompetitionSceneScheduleTarget();
        target.setScheduleId(schedule.getScheduleId());
        target.setCompetitionSeriesId(schedule.getCompetitionSeriesId());
        target.setCredentialType(resolveCredentialTypeByRole(applyInfo.getCompetitionRoleName(), schedule.getCredentialType()));
        target.setConfigDimension(resolveConfigDimension(schedule.getConfigDimension()));
        target.setTargetSource(CompetitionSceneConstants.TARGET_SOURCE_APPLY);
        target.setTargetType(CompetitionSceneConstants.DIMENSION_TEAM.equals(target.getConfigDimension())
                ? CompetitionSceneConstants.TARGET_TYPE_TEAM
                : CompetitionSceneConstants.TARGET_TYPE_PERSON);
        target.setTargetName(CompetitionSceneConstants.DIMENSION_TEAM.equals(target.getConfigDimension())
                ? applyInfo.getTeamName()
                : applyInfo.getUserName());
        target.setTeamCode(applyInfo.getTeamCode());
        target.setTeamName(applyInfo.getTeamName());
        target.setMemberId(applyInfo.getMemberId());
        target.setUserId(applyInfo.getUserId());
        target.setUserName(applyInfo.getUserName());
        target.setPhone(applyInfo.getPhone());
        target.setEmail(applyInfo.getEmail());
        target.setIdCardType(applyInfo.getIdCardType());
        target.setIdCardHash(sha256(applyInfo.getIdCard()));
        target.setIdCardSuffix(lastSuffix(applyInfo.getIdCard(), 6));
        target.setSchool(applyInfo.getSchool());
        target.setSchoolName(applyInfo.getSchoolName());
        target.setOrgId(applyInfo.getOrgId());
        target.setOrgName(applyInfo.getOrgName());
        target.setCompetitionRoleName(normalizeTargetRole(applyInfo.getCompetitionRoleName(), true));
        target.setCompetitionTrackId(applyInfo.getCompetitionTrackId());
        target.setCompetitionTrackName(applyInfo.getCompetitionTrackName());
        target.setSecondLevelCode(applyInfo.getSecondLevelCode());
        target.setSecondLevelName(applyInfo.getSecondLevelName());
        target.setLeaderTeacherId(applyInfo.getLeaderTeacherId());
        target.setLeaderTeacher(applyInfo.getLeaderTeacher());
        target.setGuideTeacher(applyInfo.getGuideTeacher());
        target.setWaitingGroupCode(schedule.getWaitingGroupCode());
        target.setWaitingGroupName(schedule.getWaitingGroupName());
        target.setTargetSnapshotJson(JSON.toJSONString(buildApplySnapshot(applyInfo)));
        target.setMatchStatus(CompetitionSceneConstants.MATCH_STATUS_MATCHED);
        target.setSourceModule("competition");
        target.setSourceBizType(CompetitionSceneConstants.DIMENSION_TEAM.equals(target.getConfigDimension()) ? "TEAM" : "PERSON");
        target.setSourceBizId(CompetitionSceneConstants.DIMENSION_TEAM.equals(target.getConfigDimension())
                ? applyInfo.getTeamCode()
                : (applyInfo.getMemberId() == null ? null : String.valueOf(applyInfo.getMemberId())));
        target.setStatus(CompetitionSceneConstants.STATUS_NORMAL);
        target.setVersion(0L);
        target.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        Date now = DateUtils.getNowDate();
        target.setCreateTime(now);
        target.setUpdateTime(now);
        target.setCreateBy(currentUsername());
        target.setUpdateBy(currentUsername());
        fillTargetKey(target);
        return target;
    }

    private void fillTargetBaseInfo(CompetitionSceneScheduleTarget target,
                                    CompetitionSceneSchedule schedule,
                                    boolean inheritWaitingGroup) {
        target.setCompetitionSeriesId(schedule.getCompetitionSeriesId());
        target.setCompetitionRoleName(normalizeTargetRole(target.getCompetitionRoleName(), false));
        target.setCredentialType(resolveCredentialTypeForSave(target.getCredentialType(),
                target.getCompetitionRoleName(), schedule.getCredentialType()));
        target.setConfigDimension(resolveConfigDimension(target.getConfigDimension()));
        if (StringUtils.isEmpty(target.getCompetitionTrackId())) {
            target.setCompetitionTrackId(schedule.getCompetitionTrackId());
        }
        if (StringUtils.isEmpty(target.getCompetitionTrackName())) {
            target.setCompetitionTrackName(schedule.getCompetitionTrackName());
        }
        if (StringUtils.isEmpty(target.getSecondLevelCode())) {
            target.setSecondLevelCode(schedule.getSecondLevelCode());
        }
        if (StringUtils.isEmpty(target.getSecondLevelName())) {
            target.setSecondLevelName(schedule.getSecondLevelName());
        }
        if (inheritWaitingGroup) {
            target.setWaitingGroupCode(schedule.getWaitingGroupCode());
            target.setWaitingGroupName(schedule.getWaitingGroupName());
        } else {
            target.setWaitingGroupCode(null);
            target.setWaitingGroupName(null);
        }
        fillTargetKey(target);
    }

    private void fillTargetInsertDefaults(CompetitionSceneScheduleTarget target) {
        Date now = DateUtils.getNowDate();
        target.setCreateBy(currentUsername());
        target.setUpdateBy(currentUsername());
        target.setCreateTime(now);
        target.setUpdateTime(now);
        if (StringUtils.isEmpty(target.getTargetSource())) {
            target.setTargetSource(CompetitionSceneConstants.TARGET_SOURCE_MANUAL);
        }
        if (StringUtils.isEmpty(target.getTargetType())) {
            target.setTargetType(CompetitionSceneConstants.DIMENSION_TEAM.equals(target.getConfigDimension())
                    ? CompetitionSceneConstants.TARGET_TYPE_TEAM
                    : CompetitionSceneConstants.TARGET_TYPE_PERSON);
        }
        if (StringUtils.isEmpty(target.getTargetName())) {
            target.setTargetName(StringUtils.isNotEmpty(target.getTeamName()) ? target.getTeamName() : target.getUserName());
        }
        if (StringUtils.isEmpty(target.getMatchStatus())) {
            target.setMatchStatus(CompetitionSceneConstants.MATCH_STATUS_MATCHED);
        }
        if (StringUtils.isEmpty(target.getStatus())) {
            target.setStatus(CompetitionSceneConstants.STATUS_NORMAL);
        }
        if (target.getVersion() == null) {
            target.setVersion(0L);
        }
        if (StringUtils.isEmpty(target.getDelFlag())) {
            target.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        }
        if (StringUtils.isEmpty(target.getTargetSnapshotJson())) {
            target.setTargetSnapshotJson(JSON.toJSONString(target));
        }
    }

    private boolean targetExists(Long scheduleId, String targetKey) {
        if (scheduleId == null || StringUtils.isEmpty(targetKey)) {
            return false;
        }
        CompetitionSceneScheduleTarget query = new CompetitionSceneScheduleTarget();
        query.setScheduleId(scheduleId);
        query.setTargetKey(targetKey);
        List<CompetitionSceneScheduleTarget> existedTargets =
                targetMapper.selectCompetitionSceneScheduleTargetList(query);
        return existedTargets != null && !existedTargets.isEmpty();
    }

    private boolean targetExistsForOther(Long scheduleId, String targetKey, Long targetId) {
        if (scheduleId == null || StringUtils.isEmpty(targetKey)) {
            return false;
        }
        CompetitionSceneScheduleTarget query = new CompetitionSceneScheduleTarget();
        query.setScheduleId(scheduleId);
        query.setTargetKey(targetKey);
        List<CompetitionSceneScheduleTarget> existedTargets =
                targetMapper.selectCompetitionSceneScheduleTargetList(query);
        if (existedTargets == null || existedTargets.isEmpty()) {
            return false;
        }
        return existedTargets.stream()
                .anyMatch(item -> !Objects.equals(item.getTargetId(), targetId));
    }

    private String buildDuplicateTargetMessage(CompetitionSceneScheduleTarget target) {
        String targetName = StringUtils.isNotEmpty(target.getUserName())
                ? target.getUserName()
                : (StringUtils.isNotEmpty(target.getTeamName()) ? target.getTeamName() : "该对象");
        return targetName + "已在当前安排中，无需重复添加";
    }

    private void fillTargetKey(CompetitionSceneScheduleTarget target) {
        if (StringUtils.isNotEmpty(target.getTargetKey())) {
            return;
        }
        if (CompetitionSceneConstants.TARGET_TYPE_REVIEW_OBJECT.equals(target.getTargetType())
                && target.getReviewObjectId() != null) {
            target.setTargetKey("REVIEW_OBJECT:" + target.getReviewObjectId());
            return;
        }
        if (CompetitionSceneConstants.TARGET_TYPE_TEAM.equals(target.getTargetType())
                && StringUtils.isNotEmpty(target.getTeamCode())) {
            target.setTargetKey("TEAM:" + target.getTeamCode());
            return;
        }
        if (CompetitionSceneConstants.TARGET_TYPE_PERSON.equals(target.getTargetType())
                && target.getMemberId() != null) {
            target.setTargetKey("PERSON:" + target.getMemberId());
            return;
        }
        if (CompetitionSceneConstants.DIMENSION_TEAM.equals(target.getConfigDimension())
                && StringUtils.isNotEmpty(target.getTeamCode())) {
            target.setTargetKey("TEAM:" + target.getTeamCode());
            return;
        }
        if (target.getMemberId() != null) {
            target.setTargetKey("PERSON:" + target.getMemberId());
            return;
        }
        if (target.getUserId() != null) {
            target.setTargetKey("USER:" + target.getUserId());
            return;
        }
        target.setTargetKey("MANUAL:" + UUID.randomUUID().toString().replace("-", ""));
    }

    private void fillTargetIdentity(CompetitionSceneScheduleTarget target) {
        if (StringUtils.isNotEmpty(target.getIdCard())) {
            target.setIdCardHash(sha256(target.getIdCard()));
            target.setIdCardSuffix(lastSuffix(target.getIdCard(), 6));
        }
    }

    private String resolveConfigDimension(String configDimension) {
        if (CompetitionSceneConstants.DIMENSION_TEAM.equals(configDimension)) {
            return CompetitionSceneConstants.DIMENSION_TEAM;
        }
        return CompetitionSceneConstants.DIMENSION_PERSON;
    }

    private String resolveCredentialTypeForSave(String credentialType, String role, String scheduleCredentialType) {
        if (StringUtils.isNotEmpty(credentialType)) {
            return normalizeCredentialType(credentialType, false);
        }
        String inferred = resolveCredentialTypeByRole(role, null);
        if (StringUtils.isNotEmpty(inferred)) {
            return inferred;
        }
        if (StringUtils.isNotEmpty(scheduleCredentialType)) {
            return normalizeCredentialType(scheduleCredentialType, false);
        }
        throw new ServiceException("请选择证件类型");
    }

    private String resolveCredentialTypeByRole(String role, String fallbackCredentialType) {
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
        return StringUtils.isNotEmpty(fallbackCredentialType)
                ? normalizeCredentialType(fallbackCredentialType, false)
                : CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT;
    }

    private String normalizeCredentialType(String credentialType, boolean allowEmpty) {
        if (StringUtils.isEmpty(credentialType)) {
            if (allowEmpty) {
                return null;
            }
            throw new ServiceException("请选择证件类型");
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_COMPETITOR.equals(credentialType)) {
            return CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT;
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT.equals(credentialType)
                || CompetitionSceneConstants.CREDENTIAL_TYPE_TEACHER.equals(credentialType)
                || CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credentialType)
                || CompetitionSceneConstants.CREDENTIAL_TYPE_STAFF.equals(credentialType)) {
            return credentialType;
        }
        throw new ServiceException("证件类型不合法");
    }

    private String normalizeTargetRole(String role, boolean allowDefault) {
        if (StringUtils.isEmpty(role)) {
            if (allowDefault) {
                return CompetitionSceneConstants.TARGET_ROLE_MEMBER;
            }
            throw new ServiceException("请选择角色");
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
                if (allowDefault) {
                    return CompetitionSceneConstants.TARGET_ROLE_MEMBER;
                }
                throw new ServiceException("角色不合法");
        }
    }

    private Map<String, Object> buildApplySnapshot(CompetitionApplyInfo applyInfo) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("memberId", applyInfo.getMemberId());
        snapshot.put("competitionSeriesId", applyInfo.getCompetitionSeriesId());
        snapshot.put("userId", applyInfo.getUserId());
        snapshot.put("teamCode", applyInfo.getTeamCode());
        snapshot.put("teamName", applyInfo.getTeamName());
        snapshot.put("userName", applyInfo.getUserName());
        snapshot.put("phone", applyInfo.getPhone());
        snapshot.put("email", applyInfo.getEmail());
        snapshot.put("idCardType", applyInfo.getIdCardType());
        snapshot.put("idCardHash", sha256(applyInfo.getIdCard()));
        snapshot.put("idCardSuffix", lastSuffix(applyInfo.getIdCard(), 6));
        snapshot.put("school", applyInfo.getSchool());
        snapshot.put("schoolName", applyInfo.getSchoolName());
        snapshot.put("orgId", applyInfo.getOrgId());
        snapshot.put("orgName", applyInfo.getOrgName());
        snapshot.put("competitionRoleName", applyInfo.getCompetitionRoleName());
        snapshot.put("competitionTrackId", applyInfo.getCompetitionTrackId());
        snapshot.put("competitionTrackName", applyInfo.getCompetitionTrackName());
        snapshot.put("secondLevelCode", applyInfo.getSecondLevelCode());
        snapshot.put("secondLevelName", applyInfo.getSecondLevelName());
        snapshot.put("checkStatus", applyInfo.getCheckStatus());
        snapshot.put("payStatus", applyInfo.getPayStatus());
        snapshot.put("delFlag", applyInfo.getDelFlag());
        return snapshot;
    }

    private String currentUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception e) {
            return "system";
        }
    }

    private List<String> parseSequenceNames(CompetitionSceneScheduleNameSequenceDTO dto) {
        List<String> names = new ArrayList<>();
        if (dto == null) {
            return names;
        }
        if (dto.getTargetNames() != null) {
            for (String name : dto.getTargetNames()) {
                String value = trimToNull(name);
                if (value != null) {
                    names.add(value);
                }
            }
        }
        String namesText = trimToNull(dto.getNamesText());
        if (namesText != null) {
            String[] parts = namesText.split("[\\s,，、;；\\n\\r\\t]+");
            for (String part : parts) {
                String value = trimToNull(part);
                if (value != null) {
                    names.add(value);
                }
            }
        }
        return names;
    }

    private List<String> buildTargetNameCandidates(CompetitionSceneScheduleTarget target) {
        List<String> candidates = new ArrayList<>();
        if (target == null) {
            return candidates;
        }
        candidates.add(target.getUserName());
        candidates.add(target.getTargetName());
        candidates.add(target.getTeamName());
        candidates.add(target.getLeaderTeacher());
        candidates.add(target.getGuideTeacher());
        return candidates;
    }

    private String normalizeSequenceName(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        return trimmed.replaceAll("\\s+", "");
    }

    private String trimToNull(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Long parseLong(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return Long.valueOf(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String sha256(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new ServiceException("证件号Hash生成失败");
        }
    }

    private String lastSuffix(String value, int length) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= length ? trimmed : trimmed.substring(trimmed.length() - length);
    }
}
