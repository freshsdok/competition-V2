package com.teaching.competition.review.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.mapper.CompetitionSceneCredentialMapper;
import com.teaching.competition.mapper.TeamManagerInfoMapper;
import com.teaching.competition.review.constant.ReviewConstants;
import com.teaching.competition.review.domain.ReviewActivity;
import com.teaching.competition.review.domain.ReviewAuditLog;
import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectCertificateRef;
import com.teaching.competition.review.domain.ReviewObjectExternalRef;
import com.teaching.competition.review.domain.ReviewObjectMaterial;
import com.teaching.competition.review.domain.ReviewObjectMember;
import com.teaching.competition.review.domain.ReviewSession;
import com.teaching.competition.review.domain.ReviewSessionObject;
import com.teaching.competition.review.domain.ReviewSubmissionPermission;
import com.teaching.competition.review.dto.ReviewObjectImportDTO;
import com.teaching.competition.review.enums.ReviewActivityStatus;
import com.teaching.competition.review.enums.ReviewCertificateType;
import com.teaching.competition.review.enums.ReviewCertificateValidStatus;
import com.teaching.competition.review.enums.ReviewMaterialType;
import com.teaching.competition.review.enums.ReviewMemberRole;
import com.teaching.competition.review.enums.ReviewObjectCreatedFrom;
import com.teaching.competition.review.enums.ReviewObjectStatus;
import com.teaching.competition.review.enums.ReviewObjectType;
import com.teaching.competition.review.enums.ReviewPermissionStatus;
import com.teaching.competition.review.enums.ReviewPermissionType;
import com.teaching.competition.review.mapper.ReviewActivityMapper;
import com.teaching.competition.review.mapper.ReviewAuditLogMapper;
import com.teaching.competition.review.mapper.ReviewObjectCertificateRefMapper;
import com.teaching.competition.review.mapper.ReviewObjectExternalRefMapper;
import com.teaching.competition.review.mapper.ReviewObjectMaterialMapper;
import com.teaching.competition.review.mapper.ReviewObjectMapper;
import com.teaching.competition.review.mapper.ReviewObjectMemberMapper;
import com.teaching.competition.review.mapper.ReviewSessionMapper;
import com.teaching.competition.review.mapper.ReviewSessionObjectMapper;
import com.teaching.competition.review.mapper.ReviewSubmissionPermissionMapper;
import com.teaching.competition.review.service.IReviewObjectService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import com.teaching.competition.review.vo.ReviewCertificateResolveResultVO;
import com.teaching.competition.review.vo.ReviewCertificateResolveVO;
import com.teaching.competition.review.vo.ReviewObjectImportPreviewVO;
import com.teaching.competition.review.vo.ReviewObjectImportResultVO;
import com.teaching.system.api.RemoteFileReviewImportService;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.FileReviewImportMaterial;
import com.teaching.system.api.domain.FileReviewImportSource;
import com.teaching.system.api.domain.TeamManagerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;

/**
 * 评审对象表Service业务层处理。
 */
@Service
public class ReviewObjectServiceImpl extends AbstractReviewCrudService<ReviewObject> implements IReviewObjectService {
    private static final String SOURCE_MODULE_SYSTEM = "system";
    private static final String SOURCE_BIZ_TYPE_TEAM = "TEAM";
    private static final String SOURCE_BIZ_TYPE_REGISTRATION = "REGISTRATION";
    private static final String SOURCE_BIZ_TYPE_REGISTRATION_TEAM_CODE = "REGISTRATION_TEAM_CODE";
    private static final String SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER = "FILE_UPLOAD_MANAGER";
    private static final String SOURCE_BIZ_TYPE_DEFENSE_SCHEDULE = "DEFENSE_SCHEDULE";
    private static final String RELATION_TYPE_TEAM = "TEAM";
    private static final String RELATION_TYPE_REGISTRATION = "REGISTRATION";
    private static final String RELATION_TYPE_FILE_UPLOAD_MANAGER = "FILE_UPLOAD_MANAGER";
    private static final String RELATION_TYPE_DEFENSE_SCHEDULE = "DEFENSE_SCHEDULE";
    private static final String PERMISSION_MODE_LEADER = "LEADER";
    private static final String PERMISSION_MODE_CONTACT = "CONTACT";
    private static final String PERMISSION_MODE_ALL_MEMBERS = "ALL_MEMBERS";
    private static final String PERMISSION_MODE_SPECIFIED = "SPECIFIED";
    private static final String MATERIAL_STATUS_NORMAL = "NORMAL";
    private static final String MATERIAL_OVERWRITE_REPLACE = "REPLACE_BY_SOURCE";
    private static final String ACTION_IMPORT_OBJECT = "IMPORT_REVIEW_OBJECT";
    private static final String ACTION_IMPORT_SKIP = "IMPORT_REVIEW_OBJECT_SKIP";
    private static final String ACTION_IMPORT_OVERWRITE = "IMPORT_REVIEW_OBJECT_OVERWRITE";
    private static final String ACTION_CERTIFICATE_ADD = "CERTIFICATE_REF_ADD";
    private static final String ACTION_CERTIFICATE_INVALIDATE = "CERTIFICATE_REF_INVALIDATE";
    private static final String ACTION_RESOLVE_CERTIFICATE = "RESOLVE_CERTIFICATE";

    @Autowired
    private ReviewObjectMapper mapper;

    @Autowired
    private ReviewActivityMapper activityMapper;

    @Autowired
    private ReviewObjectExternalRefMapper externalRefMapper;

    @Autowired
    private ReviewObjectCertificateRefMapper certificateRefMapper;

    @Autowired
    private ReviewObjectMemberMapper objectMemberMapper;

    @Autowired
    private ReviewObjectMaterialMapper materialMapper;

    @Autowired
    private ReviewSubmissionPermissionMapper submissionPermissionMapper;

    @Autowired
    private ReviewAuditLogMapper auditLogMapper;

    @Autowired
    private ReviewSessionMapper sessionMapper;

    @Autowired
    private ReviewSessionObjectMapper sessionObjectMapper;

    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    private TeamManagerInfoMapper teamManagerInfoMapper;

    @Autowired
    private CompetitionSceneCredentialMapper competitionSceneCredentialMapper;

    @Autowired
    private RemoteFileReviewImportService remoteFileReviewImportService;

    @Override
    protected ReviewCrudMapper<ReviewObject> mapper() {
        return mapper;
    }

    @Override
    public int insert(ReviewObject entity) {
        fillObjectDefaults(entity);
        return super.insert(entity);
    }

    @Override
    public int update(ReviewObject entity) {
        if (entity == null || entity.getId() == null) {
            throw new ServiceException("更新对象ID不能为空");
        }
        ReviewObject existed = mapper.selectById(entity.getId());
        if (existed == null) {
            throw new ServiceException("评审对象不存在");
        }
        if (!isObjectCrudEditable(existed.getSubmitStatus())) {
            throw new ServiceException("评审对象已提交、锁定或进入评审流程，不能通过基础接口修改");
        }
        if (StringUtils.isNotEmpty(entity.getSubmitStatus())
                && !entity.getSubmitStatus().equals(existed.getSubmitStatus())) {
            throw new ServiceException("评审对象状态必须通过提交、撤回、关闭填报等专用流程变更");
        }
        entity.setActivityId(null);
        entity.setSubmitStatus(null);
        entity.setSubmittedBy(null);
        entity.setSubmitTime(null);
        entity.setLockedTime(null);
        entity.setInvalidTime(null);
        return super.update(entity);
    }

    @Override
    public int deleteByIds(Long[] ids) {
        if (ids == null || ids.length == 0) {
            throw new ServiceException("删除对象ID不能为空");
        }
        for (Long id : ids) {
            ReviewObject object = mapper.selectById(id);
            if (object == null) {
                throw new ServiceException("评审对象不存在：" + id);
            }
            if (!isObjectCrudEditable(object.getSubmitStatus())
                    && !ReviewObjectStatus.INVALID.getCode().equals(object.getSubmitStatus())) {
                throw new ServiceException("评审对象已提交、锁定或进入评审流程，不能通过基础接口删除");
            }
        }
        return super.deleteByIds(ids);
    }

    @Override
    public List<ReviewObjectImportPreviewVO> importPreview(ReviewObjectImportDTO dto) {
        if (dto == null || dto.getActivityId() == null) {
            throw new ServiceException("评审活动ID不能为空");
        }
        ReviewActivity activity = activityMapper.selectById(dto.getActivityId());
        if (activity == null) {
            throw new ServiceException("评审活动不存在");
        }
        String sourceBizType = normalizeSourceBizType(dto.getSourceBizType());
        String sourceModule = resolveSourceModule(dto.getSourceModule(), sourceBizType);
        ResolvedImportSources resolvedSources = resolveImportSources(dto, sourceBizType);
        if (resolvedSources.sourceBizIds.isEmpty()) {
            if (shouldReturnEmptyPreview(dto, sourceBizType)) {
                return Collections.emptyList();
            }
            throw new ServiceException(resolveEmptySourceMessage(sourceBizType));
        }
        List<ReviewObjectImportPreviewVO> previewList = new ArrayList<>();
        for (String sourceBizId : resolvedSources.sourceBizIds) {
            ReviewObjectImportPreviewVO preview = new ReviewObjectImportPreviewVO();
            preview.setSourceBizId(sourceBizId);
            preview.setSourceBizType(sourceBizType);
            preview.setCanImport(false);
            if (StringUtils.isEmpty(sourceBizId)) {
                preview.getWarnings().add("业务ID为空");
                previewList.add(preview);
                continue;
            }

            try {
                BusinessImportSource source = loadResolvedBusinessSource(resolvedSources, sourceModule, sourceBizType, sourceBizId);
                ReviewObject existed = selectExistingImportObject(dto.getActivityId(), sourceModule, sourceBizType, sourceBizId, source);
                fillPreview(preview, dto, source, existed);
            } catch (Exception ex) {
                preview.getWarnings().add(ex.getMessage());
                preview.setCanImport(false);
            }
            previewList.add(preview);
        }
        return previewList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewObjectImportResultVO importFromBusiness(ReviewObjectImportDTO dto) {
        if (dto == null || dto.getActivityId() == null) {
            throw new ServiceException("评审活动ID不能为空");
        }
        ReviewActivity activity = activityMapper.selectById(dto.getActivityId());
        if (activity == null) {
            throw new ServiceException("评审活动不存在");
        }
        ensureActivityImportable(activity);
        String sourceBizType = normalizeSourceBizType(dto.getSourceBizType());
        String sourceModule = resolveSourceModule(dto.getSourceModule(), sourceBizType);
        ResolvedImportSources resolvedSources = resolveImportSources(dto, sourceBizType);
        if (resolvedSources.sourceBizIds.isEmpty()) {
            throw new ServiceException(resolveEmptySourceMessage(sourceBizType));
        }

        ReviewObjectImportResultVO result = new ReviewObjectImportResultVO();
        result.setTotalCount(resolvedSources.sourceBizIds.size());

        String objectType = firstNotEmpty(dto.getDefaultObjectType(), ReviewObjectType.PROJECT.getCode());
        boolean overwriteExisting = Boolean.TRUE.equals(dto.getOverwriteExisting());
        boolean syncCertificate = Boolean.TRUE.equals(dto.getSyncCertificate());
        boolean syncMaterial = Boolean.TRUE.equals(dto.getSyncMaterial());

        for (String sourceBizId : resolvedSources.sourceBizIds) {
            if (StringUtils.isEmpty(sourceBizId)) {
                markSkipped(result, "空业务ID");
                continue;
            }

            try {
                BusinessImportSource source = loadResolvedBusinessSource(resolvedSources, sourceModule, sourceBizType, sourceBizId);
                ReviewObject existed = selectExistingImportObject(dto.getActivityId(), sourceModule, sourceBizType, sourceBizId, source);
                if (existed != null && !overwriteExisting) {
                    markSkipped(result, sourceBizId + "：已导入");
                    writeAudit(dto.getActivityId(), existed.getId(), sourceBizType, sourceBizId,
                            ACTION_IMPORT_SKIP, "重复导入跳过");
                    continue;
                }

                ReviewObject object = existed == null ? new ReviewObject() : existed;
                applyReviewObjectMapping(object, dto.getActivityId(), sourceModule, sourceBizType, sourceBizId, objectType, source);
                applyImportInitialStatus(object, dto);
                if (existed == null) {
                    fillObjectDefaults(object);
                    fillCreate(object);
                    mapper.insert(object);
                } else {
                    int invalidated = certificateRefMapper.invalidateByObjectId(dto.getActivityId(), existed.getId(), currentUsername());
                    objectMemberMapper.deleteByObjectId(dto.getActivityId(), existed.getId(), currentUsername());
                    submissionPermissionMapper.deleteByObjectId(dto.getActivityId(), existed.getId(), currentUsername());
                    fillObjectDefaults(object);
                    fillUpdateBase(object);
                    mapper.update(object);
                    writeAudit(dto.getActivityId(), object.getId(), sourceBizType, sourceBizId,
                            ACTION_IMPORT_OVERWRITE, "覆盖同步评审对象，旧有效参赛证映射失效数量=" + invalidated);
                    if (invalidated > 0) {
                        writeAudit(dto.getActivityId(), object.getId(), sourceBizType, sourceBizId,
                                ACTION_CERTIFICATE_INVALIDATE, "覆盖导入时将旧参赛证映射标记为 INVALID");
                    }
                }

                syncExternalRef(object, sourceModule, sourceBizType, sourceBizId, source);
                List<ReviewObjectMember> members = syncMembers(object, sourceModule, source);
                syncSubmissionPermissions(object, sourceModule, dto, members, source);
                if (syncCertificate) {
                    syncCertificateRefs(object, sourceModule, source, members);
                }
                if (syncMaterial) {
                    syncMaterials(object, dto, source);
                }

                result.setSuccessCount(result.getSuccessCount() + 1);
                result.setImportedCount(result.getImportedCount() + 1);
                result.getCreatedObjectIds().add(object.getId());
                writeAudit(dto.getActivityId(), object.getId(), sourceBizType, sourceBizId,
                        ACTION_IMPORT_OBJECT, "竞赛业务数据导入评审对象完成");
            } catch (Exception ex) {
                markFailed(result, sourceBizId + "：" + ex.getMessage());
                writeAudit(dto.getActivityId(), null, sourceBizType, sourceBizId,
                        ACTION_IMPORT_OBJECT, "竞赛业务数据导入失败：" + ex.getMessage());
            }
        }

        result.setSkippedCount(result.getSkipCount());
        result.setMessage("导入完成，成功 " + result.getSuccessCount()
                + " 条，跳过 " + result.getSkipCount() + " 条，失败 " + result.getFailedCount() + " 条");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewObjectImportResultVO syncFileTaskMaterials(ReviewObjectImportDTO dto) {
        if (dto == null || dto.getActivityId() == null) {
            throw new ServiceException("评审活动ID不能为空");
        }
        if (dto.getFileTaskId() == null) {
            throw new ServiceException("文件任务ID不能为空");
        }
        ReviewActivity activity = activityMapper.selectById(dto.getActivityId());
        if (activity == null) {
            throw new ServiceException("评审活动不存在");
        }

        ReviewObject query = new ReviewObject();
        query.setActivityId(dto.getActivityId());
        List<ReviewObject> objects = mapper.selectList(query);
        Map<String, List<FileReviewImportSource>> fileSourcesByTeamCode = groupFileSourcesByTeamCode(
                queryFileReviewImportSourcesByTask(dto.getFileTaskId(), resolveSubmittedOnly(dto)));

        ReviewObjectImportResultVO result = new ReviewObjectImportResultVO();
        result.setTotalCount(objects == null ? 0 : objects.size());
        if (objects == null || objects.isEmpty()) {
            result.setMessage("同步完成，当前活动下没有评审对象");
            return result;
        }

        for (ReviewObject object : objects) {
            if (object == null || object.getId() == null) {
                continue;
            }
            String teamCode = object.getSourceTeamId();
            if (StringUtils.isEmpty(teamCode)) {
                markSkipped(result, object.getId() + "：评审对象未关联团队编号");
                continue;
            }
            List<FileReviewImportSource> fileSources = fileSourcesByTeamCode.get(teamCode);
            if (fileSources == null || fileSources.isEmpty()) {
                markSkipped(result, object.getObjectName() + "：" + teamCode + " 未找到上传记录");
                continue;
            }
            BusinessImportSource source = new BusinessImportSource();
            source.sourceBizId = firstNotEmpty(object.getSourceBizId(), teamCode);
            source.sourceBizCode = teamCode;
            source.sourceTeamId = teamCode;
            source.sourceRegistrationId = object.getSourceRegistrationId();
            applyDefenseScheduleMaterials(source, fileSources);
            if (source.materials == null || source.materials.isEmpty()) {
                markSkipped(result, object.getObjectName() + "：" + teamCode + " 未解析到可同步材料");
                continue;
            }
            try {
                syncMaterials(object, dto, source);
                result.setSuccessCount(result.getSuccessCount() + 1);
                result.setImportedCount(result.getImportedCount() + 1);
                result.getCreatedObjectIds().add(object.getId());
                writeAudit(dto.getActivityId(), object.getId(), SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER,
                        String.valueOf(dto.getFileTaskId()), "SYNC_FILE_TASK_MATERIALS",
                        "按文件任务同步评审对象材料，teamCode=" + teamCode);
            } catch (Exception ex) {
                markFailed(result, object.getObjectName() + "：" + ex.getMessage());
            }
        }
        result.setSkippedCount(result.getSkipCount());
        result.setMessage("同步完成，成功 " + result.getSuccessCount()
                + " 条，跳过 " + result.getSkipCount() + " 条，失败 " + result.getFailedCount() + " 条");
        return result;
    }

    private void ensureActivityImportable(ReviewActivity activity) {
        if (activity == null || StringUtils.isEmpty(activity.getStatus())) {
            return;
        }
        String status = activity.getStatus();
        if (ReviewActivityStatus.REVIEWING.getCode().equals(status)
                || ReviewActivityStatus.SUMMARYING.getCode().equals(status)
                || ReviewActivityStatus.PUBLISHED.getCode().equals(status)
                || ReviewActivityStatus.ARCHIVED.getCode().equals(status)) {
            throw new ServiceException("评审活动已进入评审、汇总、发布或归档阶段，不能导入评审对象");
        }
    }

    @Override
    public int insertCertificateRef(ReviewObjectCertificateRef ref) {
        if (ref == null || ref.getActivityId() == null || ref.getObjectId() == null
                || StringUtils.isEmpty(ref.getCertificateCode())) {
            throw new ServiceException("评审活动、评审对象和参赛证编号不能为空");
        }
        ReviewObject object = mapper.selectById(ref.getObjectId());
        if (object == null) {
            throw new ServiceException("评审对象不存在");
        }
        if (!isObjectCrudEditable(object.getSubmitStatus())) {
            throw new ServiceException("评审对象已提交、锁定或进入评审流程，不能通过基础接口新增参赛证映射");
        }
        if (!ref.getActivityId().equals(object.getActivityId())) {
            throw new ServiceException("参赛证映射与评审对象不属于同一活动");
        }
        if (StringUtils.isEmpty(ref.getValidStatus())) {
            ref.setValidStatus(ReviewCertificateValidStatus.VALID.getCode());
        }
        fillCreateBase(ref);
        return certificateRefMapper.insert(ref);
    }

    private boolean isObjectCrudEditable(String status) {
        return StringUtils.isEmpty(status)
                || ReviewObjectStatus.DRAFT.getCode().equals(status)
                || ReviewObjectStatus.WITHDRAW_APPROVED.getCode().equals(status)
                || ReviewObjectStatus.WITHDRAW_REJECTED.getCode().equals(status);
    }

    @Override
    public List<ReviewObjectCertificateRef> selectCertificateRefList(ReviewObjectCertificateRef query) {
        return certificateRefMapper.selectList(query);
    }

    @Override
    public ReviewCertificateResolveResultVO resolveCertificate(Long activityId, String certificateCode, Long sessionId) {
        if (activityId == null || StringUtils.isEmpty(certificateCode)) {
            throw new ServiceException("评审活动ID和参赛证编号不能为空");
        }
        ReviewCertificateResolveResultVO result = new ReviewCertificateResolveResultVO();
        result.setActivityId(activityId);
        result.setCertificateCode(certificateCode);

        List<ReviewCertificateResolveVO> candidates = certificateRefMapper.selectResolveList(activityId, certificateCode);
        if (candidates == null) {
            candidates = Collections.emptyList();
        }

        String resultWarning = null;
        if (candidates.isEmpty()) {
            SceneCredentialResolveResult sceneResult = resolveSceneCredentialCandidates(activityId, certificateCode);
            candidates = sceneResult.candidates;
            resultWarning = sceneResult.warningMessage;
        }

        ReviewSession session = null;
        if (sessionId != null) {
            session = sessionMapper.selectById(sessionId);
            if (session == null) {
                throw new ServiceException("现场评审场次不存在");
            }
            if (!activityId.equals(session.getActivityId())) {
                throw new ServiceException("场次与参赛证解析活动不一致");
            }
        }

        for (ReviewCertificateResolveVO candidate : candidates) {
            if (session != null) {
                boolean inSession = isObjectInSession(sessionId, candidate.getObjectId());
                candidate.setInSession(inSession);
                if (!inSession) {
                    candidate.setWarningMessage("该评审对象不属于当前场次。");
                }
            }
            if (!ReviewObjectStatus.LOCKED.getCode().equals(candidate.getSubmitStatus())) {
                String warning = ReviewObjectStatus.INVALID.getCode().equals(candidate.getSubmitStatus())
                        ? "该评审对象已作废，不能进入评审。"
                        : "该评审对象尚未锁定，可能不能进入评审。";
                candidate.setWarningMessage(firstNotEmpty(candidate.getWarningMessage(), warning));
            }
            resultWarning = firstNotEmpty(resultWarning, candidate.getWarningMessage());
        }

        result.setCandidates(candidates);
        result.setMatchedCount(candidates.size());
        result.setWarningMessage(resultWarning);
        writeAudit(activityId, null, "CERTIFICATE", certificateCode,
                ACTION_RESOLVE_CERTIFICATE, "通过参赛证解析评审对象，匹配数量=" + candidates.size());
        return result;
    }

    private SceneCredentialResolveResult resolveSceneCredentialCandidates(Long activityId, String certificateCode) {
        SceneCredentialResolveResult result = new SceneCredentialResolveResult();
        String credentialToken = normalizeSceneCredentialToken(certificateCode);
        if (StringUtils.isEmpty(credentialToken)) {
            return result;
        }

        CompetitionSceneCredential credential = competitionSceneCredentialMapper.selectCompetitionSceneCredentialByToken(credentialToken);
        String rawCode = certificateCode == null ? null : certificateCode.trim();
        if (credential == null && StringUtils.isNotEmpty(rawCode) && !credentialToken.equals(rawCode)) {
            credential = competitionSceneCredentialMapper.selectCompetitionSceneCredentialByToken(rawCode);
        }
        if (credential == null) {
            return result;
        }

        if (StringUtils.isNotEmpty(credential.getCredentialNo())) {
            List<ReviewCertificateResolveVO> certificateCandidates =
                    certificateRefMapper.selectResolveList(activityId, credential.getCredentialNo());
            if (certificateCandidates != null && !certificateCandidates.isEmpty()) {
                result.candidates = certificateCandidates;
                return result;
            }
        }

        Map<Long, ReviewObject> objectIndex = new LinkedHashMap<>();
        String teamCode = resolveCredentialTeamCode(credential);
        String registrationId = credential.getMemberId() == null ? null : String.valueOf(credential.getMemberId());
        addResolveObjectsBySource(activityId, teamCode, registrationId, objectIndex);
        if (objectIndex.isEmpty()) {
            result.warningMessage = "已识别大赛证，但未找到当前评审活动中对应的评审对象。请确认该团队已导入并加入当前活动。";
            return result;
        }

        List<ReviewCertificateResolveVO> candidates = new ArrayList<>();
        String credentialWarning = StringUtils.isEmpty(credential.getCredentialStatus())
                || CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE.equals(credential.getCredentialStatus())
                ? null
                : "该大赛证状态不是有效状态。";
        for (ReviewObject object : objectIndex.values()) {
            candidates.add(buildSceneCredentialCandidate(object, credential, certificateCode, credentialWarning));
        }
        result.candidates = candidates;
        return result;
    }

    private ReviewCertificateResolveVO buildSceneCredentialCandidate(ReviewObject object,
                                                                    CompetitionSceneCredential credential,
                                                                    String certificateCode,
                                                                    String warningMessage) {
        ReviewCertificateResolveVO candidate = new ReviewCertificateResolveVO();
        String registrationId = credential.getMemberId() == null ? null : String.valueOf(credential.getMemberId());
        candidate.setObjectId(object.getId());
        candidate.setObjectCode(object.getObjectCode());
        candidate.setObjectName(object.getObjectName());
        candidate.setActivityId(object.getActivityId());
        candidate.setSubmitStatus(object.getSubmitStatus());
        candidate.setCertificateCode(firstNotEmpty(credential.getCredentialNo(), credential.getQrContent(), certificateCode));
        candidate.setCertificateType(mapReviewCertificateType(credential.getCredentialType()));
        candidate.setMemberName(firstNotEmpty(credential.getUserName(), credential.getTeamName()));
        candidate.setMemberRole(resolveMemberRole(credential.getCompetitionRoleName()));
        candidate.setSourceTeamId(firstNotEmpty(object.getSourceTeamId(), resolveCredentialTeamCode(credential)));
        candidate.setSourceRegistrationId(firstNotEmpty(object.getSourceRegistrationId(), registrationId));
        candidate.setValidStatus(ReviewCertificateValidStatus.VALID.getCode());
        candidate.setWarningMessage(warningMessage);
        return candidate;
    }

    private String resolveCredentialTeamCode(CompetitionSceneCredential credential) {
        if (credential == null) {
            return null;
        }
        return firstNotEmpty(credential.getTeamCode(),
                CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(credential.getSubjectType())
                        ? credential.getSubjectCode()
                        : null);
    }

    private void addResolveObjectsBySource(Long activityId, String teamCode, String registrationId,
                                           Map<Long, ReviewObject> objectIndex) {
        if (StringUtils.isNotEmpty(teamCode)) {
            ReviewObject query = new ReviewObject();
            query.setActivityId(activityId);
            query.setSourceTeamId(teamCode);
            addResolveObjects(mapper.selectList(query), activityId, objectIndex);

            ReviewObjectExternalRef refQuery = new ReviewObjectExternalRef();
            refQuery.setActivityId(activityId);
            refQuery.setSourceTeamId(teamCode);
            addResolveObjectsByRefs(externalRefMapper.selectList(refQuery), activityId, objectIndex);
        }
        if (StringUtils.isNotEmpty(registrationId)) {
            ReviewObject query = new ReviewObject();
            query.setActivityId(activityId);
            query.setSourceRegistrationId(registrationId);
            addResolveObjects(mapper.selectList(query), activityId, objectIndex);

            ReviewObjectExternalRef refQuery = new ReviewObjectExternalRef();
            refQuery.setActivityId(activityId);
            refQuery.setSourceRegistrationId(registrationId);
            addResolveObjectsByRefs(externalRefMapper.selectList(refQuery), activityId, objectIndex);
        }
    }

    private void addResolveObjects(List<ReviewObject> objects, Long activityId, Map<Long, ReviewObject> objectIndex) {
        if (objects == null) {
            return;
        }
        for (ReviewObject object : objects) {
            addResolveObject(object, activityId, objectIndex);
        }
    }

    private void addResolveObjectsByRefs(List<ReviewObjectExternalRef> refs, Long activityId,
                                         Map<Long, ReviewObject> objectIndex) {
        if (refs == null) {
            return;
        }
        for (ReviewObjectExternalRef ref : refs) {
            if (ref == null || ref.getObjectId() == null) {
                continue;
            }
            addResolveObject(mapper.selectById(ref.getObjectId()), activityId, objectIndex);
        }
    }

    private void addResolveObject(ReviewObject object, Long activityId, Map<Long, ReviewObject> objectIndex) {
        if (object == null || object.getId() == null || !Objects.equals(activityId, object.getActivityId())) {
            return;
        }
        objectIndex.putIfAbsent(object.getId(), object);
    }

    private String normalizeSceneCredentialToken(String raw) {
        if (StringUtils.isEmpty(raw)) {
            return null;
        }
        String value = raw.trim();
        if (value.startsWith("{")) {
            try {
                JSONObject jsonObject = JSON.parseObject(value);
                String nested = firstNotEmpty(jsonObject.getString("credentialToken"),
                        jsonObject.getString("token"),
                        jsonObject.getString("qrContent"),
                        jsonObject.getString("certificateCode"),
                        jsonObject.getString("credentialCode"),
                        jsonObject.getString("code"));
                if (StringUtils.isNotEmpty(nested) && !nested.equals(value)) {
                    return normalizeSceneCredentialToken(nested);
                }
            } catch (Exception ignored) {
                // Fall through and normalize the original scan text.
            }
        }

        String paramValue = extractSceneCredentialParam(value);
        if (StringUtils.isNotEmpty(paramValue) && !paramValue.equals(value)) {
            return normalizeSceneCredentialToken(paramValue);
        }

        int prefixIndex = value.indexOf(CompetitionSceneConstants.QR_CONTENT_PREFIX);
        if (prefixIndex >= 0) {
            value = value.substring(prefixIndex + CompetitionSceneConstants.QR_CONTENT_PREFIX.length());
        }
        int endIndex = firstTokenEndIndex(value);
        if (endIndex >= 0) {
            value = value.substring(0, endIndex);
        }
        return value.trim();
    }

    private String extractSceneCredentialParam(String value) {
        String[] names = {"credentialToken", "qrContent", "certificateCode", "credentialCode", "code", "token"};
        for (String name : names) {
            String marker = name + "=";
            int index = value.indexOf(marker);
            if (index < 0 || !isQueryParamStart(value, index)) {
                continue;
            }
            int start = index + marker.length();
            int end = firstQueryParamEndIndex(value, start);
            return decodeUrlValue(value.substring(start, end));
        }
        return null;
    }

    private boolean isQueryParamStart(String value, int index) {
        if (index == 0) {
            return true;
        }
        char previous = value.charAt(index - 1);
        return previous == '?' || previous == '&' || previous == '#' || Character.isWhitespace(previous);
    }

    private int firstQueryParamEndIndex(String value, int start) {
        for (int i = start; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '&' || ch == '#') {
                return i;
            }
        }
        return value.length();
    }

    private int firstTokenEndIndex(String value) {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '&' || ch == '?' || ch == '#' || ch == '"' || ch == '\'' || ch == '}' || ch == ']'
                    || Character.isWhitespace(ch)) {
                return i;
            }
        }
        return -1;
    }

    private String decodeUrlValue(String value) {
        try {
            return java.net.URLDecoder.decode(value, "UTF-8");
        } catch (Exception ex) {
            return value;
        }
    }

    private void fillPreview(ReviewObjectImportPreviewVO preview, ReviewObjectImportDTO dto,
                             BusinessImportSource source, ReviewObject existed) {
        if (source.defenseRow != null) {
            preview.setDefenseOrder(source.defenseRow.defenseOrder);
            preview.setInputOrgName(source.defenseRow.orgName);
            preview.setInputTeamName(source.defenseRow.teamName);
            preview.setInputLeaderName(source.defenseRow.leaderName);
        }
        preview.setTeamCode(source.sourceTeamId);
        preview.setTeamName(resolvePreviewTeamName(source));
        preview.setObjectName(source.objectName);
        preview.setLeaderName(firstNotEmpty(source.contactName, firstLeaderApplyName(source.applyList)));
        int memberCount = source.applyList == null ? 0 : source.applyList.size();
        if (StringUtils.isNotEmpty(source.contactName) && !isNameInApplyList(source.applyList, source.contactName)) {
            memberCount++;
        }
        preview.setMemberCount(memberCount);
        preview.setCertificateCount(source.credentials == null ? 0 : source.credentials.size());
        preview.setMaterialCount(source.materials == null ? 0 : source.materials.size());
        preview.setPermissionUsers(resolvePreviewPermissionUsers(dto, source));
        preview.setCanImport(true);
        if (source.warnings != null && !source.warnings.isEmpty()) {
            preview.getWarnings().addAll(source.warnings);
        }
        if (!source.importable) {
            preview.setCanImport(false);
        }
        if (existed != null && !Boolean.TRUE.equals(dto.getOverwriteExisting())) {
            preview.setCanImport(false);
            preview.getWarnings().add("该业务数据已导入，当前设置下会跳过。");
        }
        if (memberCount == 0) {
            preview.getWarnings().add("未找到可同步的报名成员。");
        }
        if (preview.getCertificateCount() == 0 && Boolean.TRUE.equals(dto.getSyncCertificate())) {
            preview.getWarnings().add("未找到有效参赛证，导入后现场扫码可能无法定位对象。");
        }
        if (preview.getPermissionUsers().isEmpty()) {
            preview.getWarnings().add("未识别到预计填报授权用户，请检查授权模式或指定用户。");
        }
        if (SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER.equals(preview.getSourceBizType())
                && (source.materials == null || source.materials.isEmpty())) {
            preview.getWarnings().add("文件任务上传记录未解析到可导入材料。");
        }
    }

    private String resolvePreviewTeamName(BusinessImportSource source) {
        if (source == null) {
            return null;
        }
        return firstNotEmpty(
                source.team == null ? null : source.team.getTeamName(),
                source.registration == null ? null : source.registration.getTeamName(),
                source.fileUploadSource == null ? null : source.fileUploadSource.getTeamName(),
                firstApplyValue(source.applyList, "teamName"));
    }

    private List<String> resolvePreviewPermissionUsers(ReviewObjectImportDTO dto, BusinessImportSource source) {
        String mode = firstNotEmpty(dto.getPermissionUserMode(), PERMISSION_MODE_LEADER).toUpperCase();
        List<String> users = new ArrayList<>();
        Set<Long> userIds = new HashSet<>();
        if (PERMISSION_MODE_SPECIFIED.equals(mode)) {
            if (dto.getSpecifiedUserIds() != null) {
                for (Long userId : dto.getSpecifiedUserIds()) {
                    if (userId != null && userIds.add(userId)) {
                        users.add(String.valueOf(userId));
                    }
                }
            }
            return users;
        }

        if (source.applyList != null) {
            for (CompetitionApplyInfo apply : source.applyList) {
                String role = resolveMemberRole(apply, source.team);
                boolean grant = PERMISSION_MODE_ALL_MEMBERS.equals(mode)
                        || ReviewMemberRole.LEADER.getCode().equals(role);
                if (grant && apply.getUserId() != null && userIds.add(apply.getUserId())) {
                    users.add(apply.getUserId() + "-" + firstNotEmpty(apply.getUserName(), ""));
                }
            }
        }

        if ((PERMISSION_MODE_LEADER.equals(mode) || PERMISSION_MODE_CONTACT.equals(mode)
                || PERMISSION_MODE_ALL_MEMBERS.equals(mode))
                && source.registration != null && source.registration.getLeaderTeacherId() != null
                && userIds.add(source.registration.getLeaderTeacherId())) {
            users.add(source.registration.getLeaderTeacherId() + "-"
                    + firstNotEmpty(source.registration.getLeaderTeacher(), "联系人"));
        }
        if (source.fileUploadSource != null && source.fileUploadSource.getUserId() != null
                && userIds.add(source.fileUploadSource.getUserId())) {
            users.add(source.fileUploadSource.getUserId() + "-"
                    + firstNotEmpty(source.fileUploadSource.getUserName(), "上传人"));
        }
        return users;
    }

    private boolean isNameInApplyList(List<CompetitionApplyInfo> applyList, String name) {
        if (applyList == null || StringUtils.isEmpty(name)) {
            return false;
        }
        for (CompetitionApplyInfo apply : applyList) {
            if (name.equals(apply.getUserName())) {
                return true;
            }
        }
        return false;
    }

    private ResolvedImportSources resolveImportSources(ReviewObjectImportDTO dto, String sourceBizType) {
        ResolvedImportSources resolvedSources = new ResolvedImportSources();
        List<String> sourceBizIds = normalizeSourceBizIds(dto.getSourceBizIds());
        if (SOURCE_BIZ_TYPE_DEFENSE_SCHEDULE.equals(sourceBizType)) {
            return resolveDefenseScheduleSources(dto, sourceBizIds);
        }
        if (!sourceBizIds.isEmpty()) {
            resolvedSources.sourceBizIds.addAll(sourceBizIds);
            return resolvedSources;
        }
        if (!SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER.equals(sourceBizType) || dto.getFileTaskId() == null) {
            return resolvedSources;
        }

        List<FileReviewImportSource> fileSources = queryFileReviewImportSourcesByTask(dto.getFileTaskId(), resolveSubmittedOnly(dto));
        for (FileReviewImportSource fileSource : fileSources) {
            if (fileSource == null || fileSource.getId() == null) {
                continue;
            }
            String sourceBizId = String.valueOf(fileSource.getId());
            resolvedSources.sourceBizIds.add(sourceBizId);
            resolvedSources.preloadedSources.put(sourceBizId, buildFileUploadManagerSource(sourceBizId, fileSource));
        }
        return resolvedSources;
    }

    private ResolvedImportSources resolveDefenseScheduleSources(ReviewObjectImportDTO dto, List<String> selectedSourceBizIds) {
        if (dto == null || dto.getCompetitionSeriesId() == null) {
            throw new ServiceException("答辩安排导入必须选择赛事系列");
        }
        if (dto.getFileTaskId() == null) {
            throw new ServiceException("答辩安排导入必须选择文件任务");
        }

        List<DefenseScheduleRow> rows = parseDefenseScheduleRows(dto.getDefenseScheduleText());
        if (rows.isEmpty()) {
            return new ResolvedImportSources();
        }

        Set<String> selectedKeys = new HashSet<>(selectedSourceBizIds == null ? Collections.emptyList() : selectedSourceBizIds);
        Map<String, List<FileReviewImportSource>> fileSourcesByTeamCode = groupFileSourcesByTeamCode(
                queryFileReviewImportSourcesByTask(dto.getFileTaskId(), resolveSubmittedOnly(dto)));
        ResolvedImportSources resolvedSources = new ResolvedImportSources();
        Set<Integer> existedOrders = new HashSet<>();
        Set<String> existedTeamCodes = new HashSet<>();

        for (DefenseScheduleRow row : rows) {
            BusinessImportSource source = buildDefenseScheduleSource(dto, row, fileSourcesByTeamCode);
            if (row.defenseOrder != null && !existedOrders.add(row.defenseOrder)) {
                source.warnings.add("答辩顺序重复：" + row.defenseOrder);
                source.importable = false;
            }
            if (StringUtils.isNotEmpty(source.sourceTeamId) && !existedTeamCodes.add(source.sourceTeamId)) {
                source.warnings.add("答辩安排中存在重复团队：" + source.sourceTeamId);
                source.importable = false;
                source.sourceBizId = "ROW-" + row.lineNo;
            }
            if (!selectedKeys.isEmpty() && !selectedKeys.contains(source.sourceBizId)) {
                continue;
            }
            resolvedSources.sourceBizIds.add(source.sourceBizId);
            resolvedSources.preloadedSources.put(source.sourceBizId, source);
        }
        return resolvedSources;
    }

    private List<DefenseScheduleRow> parseDefenseScheduleRows(String scheduleText) {
        if (StringUtils.isEmpty(scheduleText)) {
            return Collections.emptyList();
        }
        List<DefenseScheduleRow> rows = new ArrayList<>();
        String[] lines = scheduleText.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String rawLine = lines[i];
            if (StringUtils.isEmpty(rawLine) || StringUtils.isEmpty(rawLine.trim())) {
                continue;
            }
            DefenseScheduleRow row = parseDefenseScheduleRow(i + 1, rawLine);
            rows.add(row);
        }
        return rows;
    }

    private DefenseScheduleRow parseDefenseScheduleRow(int lineNo, String rawLine) {
        DefenseScheduleRow row = new DefenseScheduleRow();
        row.lineNo = lineNo;
        String line = rawLine == null ? "" : rawLine.trim();
        String[] cells = line.contains("\t") ? line.split("\\t", -1) : line.split("\\s+");
        if (cells.length >= 4) {
            row.defenseOrder = parseInteger(normalizeScheduleCell(cells[0]));
            row.orgName = normalizeScheduleCell(cells[1]);
            row.teamName = normalizeScheduleCell(cells[2]);
            row.leaderName = firstNonEmptyCell(cells, 3);
        } else {
            row.warnings.add("第" + lineNo + "行格式不完整，应包含：答辩顺序、所属单位、队名、负责人姓名");
        }
        if (row.defenseOrder == null) {
            row.warnings.add("答辩顺序不是有效数字");
        }
        if (StringUtils.isEmpty(row.orgName)) {
            row.warnings.add("所属单位为空");
        }
        if (StringUtils.isEmpty(row.teamName)) {
            row.warnings.add("队名为空");
        }
        if (StringUtils.isEmpty(row.leaderName)) {
            row.warnings.add("负责人姓名为空");
        }
        return row;
    }

    private String firstNonEmptyCell(String[] cells, int startIndex) {
        if (cells == null || startIndex >= cells.length) {
            return null;
        }
        String fallback = null;
        for (int i = startIndex; i < cells.length; i++) {
            String value = normalizeScheduleCell(cells[i]);
            if (StringUtils.isNotEmpty(value)) {
                fallback = value;
            }
        }
        return fallback;
    }

    private String normalizeScheduleCell(String value) {
        if (value == null) {
            return null;
        }
        return value.replace('\u00A0', ' ').replace('\u3000', ' ').trim();
    }

    private Integer parseInteger(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BusinessImportSource buildDefenseScheduleSource(ReviewObjectImportDTO dto, DefenseScheduleRow row,
                                                            Map<String, List<FileReviewImportSource>> fileSourcesByTeamCode) {
        if (!row.warnings.isEmpty()) {
            return buildUnmatchedDefenseScheduleSource(row, row.warnings);
        }

        List<CompetitionApplyInfo> candidates = competitionApplyInfoMapper.selectDefenseScheduleApplyCandidates(
                dto.getCompetitionSeriesId(), row.teamName, row.leaderName, row.orgName);
        Map<String, List<CompetitionApplyInfo>> candidatesByTeamCode = groupApplyListByTeamCode(candidates);
        if (candidatesByTeamCode.isEmpty()) {
            return buildUnmatchedDefenseScheduleSource(row, Collections.singletonList("未匹配到报名数据"));
        }
        if (candidatesByTeamCode.size() > 1) {
            return buildUnmatchedDefenseScheduleSource(row, Collections.singletonList("匹配到多个报名团队，请补充更精确数据"));
        }

        String teamCode = candidatesByTeamCode.keySet().iterator().next();
        List<CompetitionApplyInfo> applyList = selectApplyListByTeamCode(teamCode, dto.getCompetitionSeriesId());
        if (applyList.isEmpty()) {
            applyList = candidatesByTeamCode.get(teamCode);
        }
        TeamManagerInfo team = selectFirstTeamByCode(teamCode);
        if (team != null && team.getCompetitionSeriesId() != null
                && !Objects.equals(team.getCompetitionSeriesId(), dto.getCompetitionSeriesId())) {
            team = null;
        }
        CompetitionApplyInfo registration = firstApply(applyList);

        BusinessImportSource source = new BusinessImportSource();
        source.sourceBizId = teamCode;
        source.sourceBizCode = teamCode;
        source.sourceTeamId = teamCode;
        source.sourceRegistrationId = registration == null || registration.getMemberId() == null
                ? null
                : String.valueOf(registration.getMemberId());
        source.team = team;
        source.registration = registration;
        source.applyList = applyList;
        source.defenseRow = row;
        source.objectName = firstNotEmpty(row.teamName, firstApplyValue(applyList, "teamName"),
                team == null ? null : team.getTeamName(), "导入对象-" + teamCode);
        source.orgName = firstNotEmpty(row.orgName, firstApplyValue(applyList, "schoolName"),
                firstApplyValue(applyList, "orgName"), team == null ? null : team.getSchoolName());
        source.contactName = firstNotEmpty(row.leaderName, firstLeaderApplyName(applyList),
                firstApplyValue(applyList, "leaderTeacher"), team == null ? null : team.getLeaderTeacher());
        source.contactPhone = firstLeaderApplyPhone(applyList);
        source.contactEmail = firstLeaderApplyEmail(applyList);
        source.subjectCode1 = firstNotEmpty(firstApplyValue(applyList, "competitionTrackId"),
                team == null ? null : team.getCompetitionTrackId());
        source.subjectCode2 = firstNotEmpty(firstApplyValue(applyList, "secondLevelCode"),
                team == null ? null : team.getSecondLevelCode());
        source.categoryCodes = buildCategoryCodes(
                firstNotEmpty(firstApplyValue(applyList, "competitionTrackName"), team == null ? null : team.getCompetitionTrackName()),
                firstNotEmpty(firstApplyValue(applyList, "secondLevelName"), team == null ? null : team.getSecondLevelName()));
        source.credentials = selectCredentials(teamCode, null);
        source.importOrgId = registration == null ? null : registration.getOrgId();
        applyDefenseScheduleMaterials(source, fileSourcesByTeamCode.get(teamCode));
        source.extraData = buildDefenseScheduleExtraData(row, dto.getCompetitionSeriesId(), source);
        return source;
    }

    private BusinessImportSource buildUnmatchedDefenseScheduleSource(DefenseScheduleRow row, List<String> warnings) {
        BusinessImportSource source = BusinessImportSource.generic("ROW-" + row.lineNo);
        source.defenseRow = row;
        source.objectName = firstNotEmpty(row.teamName, "答辩安排第" + row.lineNo + "行");
        source.orgName = row.orgName;
        source.contactName = row.leaderName;
        source.importable = false;
        if (warnings != null) {
            source.warnings.addAll(warnings);
        }
        source.extraData = buildDefenseScheduleExtraData(row, null, source);
        return source;
    }

    private Map<String, List<CompetitionApplyInfo>> groupApplyListByTeamCode(List<CompetitionApplyInfo> applyList) {
        Map<String, List<CompetitionApplyInfo>> grouped = new LinkedHashMap<>();
        if (applyList == null) {
            return grouped;
        }
        for (CompetitionApplyInfo apply : applyList) {
            if (apply == null || StringUtils.isEmpty(apply.getTeamCode())) {
                continue;
            }
            grouped.computeIfAbsent(apply.getTeamCode(), key -> new ArrayList<>()).add(apply);
        }
        return grouped;
    }

    private Map<String, List<FileReviewImportSource>> groupFileSourcesByTeamCode(List<FileReviewImportSource> fileSources) {
        Map<String, List<FileReviewImportSource>> grouped = new HashMap<>();
        if (fileSources == null) {
            return grouped;
        }
        for (FileReviewImportSource fileSource : fileSources) {
            if (fileSource == null || StringUtils.isEmpty(fileSource.getTeamCode())) {
                continue;
            }
            grouped.computeIfAbsent(fileSource.getTeamCode(), key -> new ArrayList<>()).add(fileSource);
        }
        return grouped;
    }

    private void applyDefenseScheduleMaterials(BusinessImportSource source, List<FileReviewImportSource> fileSources) {
        if (source == null) {
            return;
        }
        if (fileSources == null || fileSources.isEmpty()) {
            source.warnings.add("所选文件任务下未找到该团队上传记录");
            return;
        }
        List<FileReviewImportMaterial> materials = new ArrayList<>();
        Set<String> addedLinks = new HashSet<>();
        for (FileReviewImportSource fileSource : fileSources) {
            if (fileSource == null) {
                continue;
            }
            if (source.fileUploadSource == null) {
                source.fileUploadSource = fileSource;
                if (source.importOrgId == null) {
                    source.importOrgId = fileSource.getOrgId();
                }
            }
            if (fileSource.getMaterials() == null) {
                continue;
            }
            for (FileReviewImportMaterial material : fileSource.getMaterials()) {
                if (material == null || StringUtils.isEmpty(material.getDownloadLink())) {
                    continue;
                }
                if (addedLinks.add(material.getDownloadLink())) {
                    materials.add(material);
                }
            }
        }
        source.materials = materials;
        if (materials.isEmpty()) {
            source.warnings.add("所选文件任务下未解析到该团队可导入材料");
        }
    }

    private List<CompetitionApplyInfo> selectApplyListByTeamCode(String teamCode, Long competitionSeriesId) {
        List<CompetitionApplyInfo> applyList = selectApplyListByTeamCode(teamCode);
        if (competitionSeriesId == null || applyList.isEmpty()) {
            return applyList;
        }
        List<CompetitionApplyInfo> filtered = new ArrayList<>();
        for (CompetitionApplyInfo apply : applyList) {
            if (apply != null && Objects.equals(apply.getCompetitionSeriesId(), competitionSeriesId)) {
                filtered.add(apply);
            }
        }
        return filtered;
    }

    private ReviewObject selectExistingImportObject(Long activityId, String sourceModule, String sourceBizType,
                                                    String sourceBizId, BusinessImportSource source) {
        ReviewObject existed = mapper.selectBySourceRef(activityId, sourceModule, sourceBizType, sourceBizId);
        if (existed != null || !SOURCE_BIZ_TYPE_DEFENSE_SCHEDULE.equals(sourceBizType)
                || source == null || StringUtils.isEmpty(source.sourceTeamId)) {
            return existed;
        }
        ReviewObject query = new ReviewObject();
        query.setActivityId(activityId);
        query.setSourceTeamId(source.sourceTeamId);
        List<ReviewObject> list = mapper.selectList(query);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    private List<String> normalizeSourceBizIds(List<String> sourceBizIds) {
        if (sourceBizIds == null || sourceBizIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>();
        Set<String> existed = new HashSet<>();
        for (String sourceBizId : sourceBizIds) {
            if (StringUtils.isEmpty(sourceBizId)) {
                continue;
            }
            String value = sourceBizId.trim();
            if (StringUtils.isNotEmpty(value) && existed.add(value)) {
                normalized.add(value);
            }
        }
        return normalized;
    }

    private boolean shouldReturnEmptyPreview(ReviewObjectImportDTO dto, String sourceBizType) {
        return SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER.equals(sourceBizType) && dto != null && dto.getFileTaskId() != null;
    }

    private BusinessImportSource loadResolvedBusinessSource(ResolvedImportSources resolvedSources,
                                                            String sourceModule, String sourceBizType, String sourceBizId) {
        BusinessImportSource source = resolvedSources.preloadedSources.get(sourceBizId);
        return source == null ? loadBusinessSource(sourceModule, sourceBizType, sourceBizId) : source;
    }

    private List<FileReviewImportSource> queryFileReviewImportSourcesByTask(Long fileTaskId, Boolean submittedOnly) {
        R<List<FileReviewImportSource>> ret = remoteFileReviewImportService.listByFileTaskId(
                fileTaskId, submittedOnly, SecurityConstants.INNER);
        if (ret == null || R.isError(ret)) {
            throw new ServiceException(ret == null ? "查询文件任务导入源失败" : ret.getMsg());
        }
        return ret.getData() == null ? Collections.emptyList() : ret.getData();
    }

    private Boolean resolveSubmittedOnly(ReviewObjectImportDTO dto) {
        return dto.getSubmittedOnly() == null || Boolean.TRUE.equals(dto.getSubmittedOnly());
    }

    private String resolveEmptySourceMessage(String sourceBizType) {
        if (SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER.equals(sourceBizType)) {
            return "文件任务ID或文件上传记录ID不能为空，或文件任务下没有可导入的已提交上传记录";
        }
        if (SOURCE_BIZ_TYPE_DEFENSE_SCHEDULE.equals(sourceBizType)) {
            return "答辩安排表不能为空，或没有匹配到可导入的报名团队";
        }
        return "外部业务ID不能为空";
    }

    private BusinessImportSource loadBusinessSource(String sourceModule, String sourceBizType, String sourceBizId) {
        if (SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER.equals(sourceBizType)) {
            return loadFileUploadManagerSource(sourceBizId);
        }
        if (!ReviewConstants.SOURCE_MODULE_COMPETITION.equals(sourceModule)) {
            return BusinessImportSource.generic(sourceBizId);
        }
        if (SOURCE_BIZ_TYPE_TEAM.equals(sourceBizType)) {
            return loadTeamSource(sourceBizId);
        }
        if (SOURCE_BIZ_TYPE_REGISTRATION.equals(sourceBizType)) {
            return loadRegistrationSource(sourceBizId);
        }
        if (SOURCE_BIZ_TYPE_REGISTRATION_TEAM_CODE.equals(sourceBizType)) {
            return loadRegistrationTeamCodeSource(sourceBizId);
        }
        if (SOURCE_BIZ_TYPE_DEFENSE_SCHEDULE.equals(sourceBizType)) {
            return loadRegistrationTeamCodeSource(sourceBizId);
        }
        return BusinessImportSource.generic(sourceBizId);
    }

    private BusinessImportSource loadFileUploadManagerSource(String sourceBizId) {
        Long fileUploadManagerId = parseLong(sourceBizId);
        if (fileUploadManagerId == null) {
            throw new ServiceException("文件上传管理ID必须为数字：" + sourceBizId);
        }
        R<List<FileReviewImportSource>> ret = remoteFileReviewImportService.listByIds(
                Collections.singletonList(fileUploadManagerId), SecurityConstants.INNER);
        if (ret == null || R.isError(ret)) {
            throw new ServiceException(ret == null ? "查询文件上传管理导入源失败" : ret.getMsg());
        }
        List<FileReviewImportSource> list = ret.getData();
        if (list == null || list.isEmpty()) {
            throw new ServiceException("未找到文件上传管理记录：" + sourceBizId);
        }
        FileReviewImportSource fileSource = list.get(0);
        return buildFileUploadManagerSource(sourceBizId, fileSource);
    }

    private BusinessImportSource buildFileUploadManagerSource(String sourceBizId, FileReviewImportSource fileSource) {
        String teamCode = fileSource.getTeamCode();
        TeamManagerInfo team = StringUtils.isEmpty(teamCode)
                ? null
                : selectFirstTeamByCode(teamCode);
        List<CompetitionApplyInfo> applyList = StringUtils.isEmpty(teamCode)
                ? new ArrayList<CompetitionApplyInfo>()
                : selectApplyListByTeamCode(teamCode);
        CompetitionApplyInfo registration = firstApply(applyList);

        BusinessImportSource source = new BusinessImportSource();
        source.sourceBizId = sourceBizId;
        source.sourceBizCode = firstNotEmpty(teamCode, sourceBizId);
        source.sourceTeamId = teamCode;
        source.sourceRegistrationId = registration == null || registration.getMemberId() == null
                ? null
                : String.valueOf(registration.getMemberId());
        source.fileUploadSource = fileSource;
        source.team = team;
        source.registration = registration;
        source.applyList = applyList;
        source.materials = fileSource.getMaterials() == null
                ? new ArrayList<FileReviewImportMaterial>()
                : fileSource.getMaterials();
        source.objectName = firstNotEmpty(fileSource.getTeamName(), fileSource.getUserName(),
                fileSource.getFileTaskName(), "文件任务导入-" + sourceBizId);
        source.orgName = firstNotEmpty(team == null ? null : team.getSchoolName(),
                firstApplyValue(applyList, "schoolName"), firstApplyValue(applyList, "orgName"));
        source.contactName = firstNotEmpty(fileSource.getLeaderTeacherName(), fileSource.getUserName(),
                team == null ? null : team.getLeaderTeacher(), firstLeaderApplyName(applyList));
        source.contactPhone = firstNotEmpty(team == null ? null : team.getLeaderTeacherPhone(), firstLeaderApplyPhone(applyList));
        source.contactEmail = firstNotEmpty(team == null ? null : team.getLeaderTeacherEmail(), firstLeaderApplyEmail(applyList));
        source.subjectCode1 = firstNotEmpty(fileSource.getCompetitionTrackCode(),
                team == null ? null : team.getCompetitionTrackId(), firstApplyValue(applyList, "competitionTrackId"));
        source.subjectCode2 = firstNotEmpty(fileSource.getSecondLevelCode(),
                team == null ? null : team.getSecondLevelCode(), firstApplyValue(applyList, "secondLevelCode"));
        source.categoryCodes = buildCategoryCodes(fileSource.getCompetitionTrackName(), fileSource.getSecondLevelName());
        source.extraData = buildFileUploadExtraData(fileSource);
        source.credentials = StringUtils.isEmpty(teamCode) ? new ArrayList<CompetitionSceneCredential>() : selectCredentials(teamCode, null);
        source.importOrgId = fileSource.getOrgId();
        return source;
    }

    private BusinessImportSource loadTeamSource(String sourceBizId) {
        TeamManagerInfo team = selectFirstTeamByCode(sourceBizId);
        Long teamId = parseLong(sourceBizId);
        if (team == null && teamId != null) {
            team = teamManagerInfoMapper.selectTeamManagerInfoByTeamCode(teamId, null);
        }

        String teamCode = team == null ? sourceBizId : firstNotEmpty(team.getTeamCode(), sourceBizId);
        List<CompetitionApplyInfo> applyList = selectApplyListByTeamCode(teamCode);
        if (team == null && applyList.isEmpty()) {
            throw new ServiceException("未找到竞赛团队或报名成员：" + sourceBizId);
        }

        BusinessImportSource source = new BusinessImportSource();
        source.sourceBizId = sourceBizId;
        source.sourceBizCode = teamCode;
        source.sourceTeamId = teamCode;
        source.team = team;
        source.applyList = applyList;
        source.objectName = firstNotEmpty(team == null ? null : team.getTeamName(), firstApplyValue(applyList, "teamName"), "导入对象-" + sourceBizId);
        source.orgName = firstNotEmpty(team == null ? null : team.getSchoolName(), firstApplyValue(applyList, "schoolName"), firstApplyValue(applyList, "orgName"));
        source.contactName = firstNotEmpty(team == null ? null : team.getLeaderTeacher(), firstApplyValue(applyList, "leaderTeacher"), firstLeaderApplyName(applyList));
        source.contactPhone = firstNotEmpty(team == null ? null : team.getLeaderTeacherPhone(), firstApplyValue(applyList, "leaderTeacherPhone"), firstLeaderApplyPhone(applyList));
        source.contactEmail = firstNotEmpty(team == null ? null : team.getLeaderTeacherEmail(), firstApplyValue(applyList, "leaderTeacherEmail"), firstLeaderApplyEmail(applyList));
        source.subjectCode1 = firstNotEmpty(team == null ? null : team.getCompetitionTrackId(), firstApplyValue(applyList, "competitionTrackId"));
        source.subjectCode2 = firstNotEmpty(team == null ? null : team.getSecondLevelCode(), firstApplyValue(applyList, "secondLevelCode"));
        source.categoryCodes = buildCategoryCodes(
                firstNotEmpty(team == null ? null : team.getCompetitionTrackName(), firstApplyValue(applyList, "competitionTrackName")),
                firstNotEmpty(team == null ? null : team.getSecondLevelName(), firstApplyValue(applyList, "secondLevelName")));
        source.extraData = buildExtraData(team, firstApply(applyList));
        source.credentials = selectCredentials(teamCode, null);
        source.registration = firstApply(applyList);
        source.sourceRegistrationId = source.registration == null || source.registration.getMemberId() == null
                ? null
                : String.valueOf(source.registration.getMemberId());
        return source;
    }

    private BusinessImportSource loadRegistrationSource(String sourceBizId) {
        Long memberId = parseLong(sourceBizId);
        if (memberId == null) {
            throw new ServiceException("报名ID必须为数字：" + sourceBizId);
        }
        CompetitionApplyInfo registration = competitionApplyInfoMapper.selectCompetitionApplyInfoByMemberId(memberId);
        if (registration == null) {
            throw new ServiceException("未找到竞赛报名记录：" + sourceBizId);
        }

        String teamCode = registration.getTeamCode();
        TeamManagerInfo team = StringUtils.isEmpty(teamCode)
                ? null
                : selectFirstTeamByCode(teamCode);
        List<CompetitionApplyInfo> applyList = StringUtils.isEmpty(teamCode)
                ? Collections.singletonList(registration)
                : selectApplyListByTeamCode(teamCode);
        if (applyList.isEmpty()) {
            applyList = Collections.singletonList(registration);
        }

        BusinessImportSource source = new BusinessImportSource();
        source.sourceBizId = sourceBizId;
        source.sourceBizCode = firstNotEmpty(teamCode, sourceBizId);
        source.sourceTeamId = teamCode;
        source.sourceRegistrationId = sourceBizId;
        source.team = team;
        source.registration = registration;
        source.applyList = applyList;
        source.objectName = firstNotEmpty(registration.getTeamName(), team == null ? null : team.getTeamName(), "导入对象-" + sourceBizId);
        source.orgName = firstNotEmpty(registration.getSchoolName(), registration.getOrgName(), team == null ? null : team.getSchoolName());
        source.contactName = firstNotEmpty(registration.getLeaderTeacher(), team == null ? null : team.getLeaderTeacher(), firstLeaderApplyName(applyList));
        source.contactPhone = firstNotEmpty(registration.getLeaderTeacherPhone(), team == null ? null : team.getLeaderTeacherPhone(), firstLeaderApplyPhone(applyList));
        source.contactEmail = firstNotEmpty(registration.getLeaderTeacherEmail(), team == null ? null : team.getLeaderTeacherEmail(), firstLeaderApplyEmail(applyList));
        source.subjectCode1 = firstNotEmpty(registration.getCompetitionTrackId(), team == null ? null : team.getCompetitionTrackId());
        source.subjectCode2 = firstNotEmpty(registration.getSecondLevelCode(), team == null ? null : team.getSecondLevelCode());
        source.categoryCodes = buildCategoryCodes(
                firstNotEmpty(registration.getCompetitionTrackName(), team == null ? null : team.getCompetitionTrackName()),
                firstNotEmpty(registration.getSecondLevelName(), team == null ? null : team.getSecondLevelName()));
        source.extraData = buildExtraData(team, registration);
        source.credentials = StringUtils.isEmpty(teamCode)
                ? selectCredentials(null, memberId)
                : selectCredentials(teamCode, null);
        return source;
    }

    private BusinessImportSource loadRegistrationTeamCodeSource(String teamCode) {
        if (StringUtils.isEmpty(teamCode)) {
            throw new ServiceException("报名团队编号不能为空");
        }
        List<CompetitionApplyInfo> applyList = selectApplyListByTeamCode(teamCode);
        if (applyList.isEmpty()) {
            throw new ServiceException("未找到报名团队编号：" + teamCode);
        }

        TeamManagerInfo team = selectFirstTeamByCode(teamCode);
        CompetitionApplyInfo registration = firstApply(applyList);
        BusinessImportSource source = new BusinessImportSource();
        source.sourceBizId = teamCode;
        source.sourceBizCode = teamCode;
        source.sourceTeamId = teamCode;
        source.sourceRegistrationId = registration == null || registration.getMemberId() == null
                ? null
                : String.valueOf(registration.getMemberId());
        source.team = team;
        source.registration = registration;
        source.applyList = applyList;
        source.objectName = firstNotEmpty(firstApplyValue(applyList, "teamName"), team == null ? null : team.getTeamName(), "导入对象-" + teamCode);
        source.orgName = firstNotEmpty(firstApplyValue(applyList, "schoolName"), firstApplyValue(applyList, "orgName"), team == null ? null : team.getSchoolName());
        source.contactName = firstNotEmpty(firstApplyValue(applyList, "leaderTeacher"), team == null ? null : team.getLeaderTeacher(), firstLeaderApplyName(applyList));
        source.contactPhone = firstNotEmpty(firstApplyValue(applyList, "leaderTeacherPhone"), team == null ? null : team.getLeaderTeacherPhone(), firstLeaderApplyPhone(applyList));
        source.contactEmail = firstNotEmpty(firstApplyValue(applyList, "leaderTeacherEmail"), team == null ? null : team.getLeaderTeacherEmail(), firstLeaderApplyEmail(applyList));
        source.subjectCode1 = firstNotEmpty(firstApplyValue(applyList, "competitionTrackId"), team == null ? null : team.getCompetitionTrackId());
        source.subjectCode2 = firstNotEmpty(firstApplyValue(applyList, "secondLevelCode"), team == null ? null : team.getSecondLevelCode());
        source.categoryCodes = buildCategoryCodes(
                firstNotEmpty(firstApplyValue(applyList, "competitionTrackName"), team == null ? null : team.getCompetitionTrackName()),
                firstNotEmpty(firstApplyValue(applyList, "secondLevelName"), team == null ? null : team.getSecondLevelName()));
        source.extraData = buildExtraData(team, registration);
        source.credentials = selectCredentials(teamCode, null);
        return source;
    }

    private List<CompetitionApplyInfo> selectApplyListByTeamCode(String teamCode) {
        if (StringUtils.isEmpty(teamCode)) {
            return new ArrayList<>();
        }
        List<CompetitionApplyInfo> list = competitionApplyInfoMapper.selectCompetitionApplyTeamCode(teamCode);
        return list == null ? new ArrayList<CompetitionApplyInfo>() : list;
    }

    private TeamManagerInfo selectFirstTeamByCode(String teamCode) {
        if (StringUtils.isEmpty(teamCode)) {
            return null;
        }
        TeamManagerInfo query = new TeamManagerInfo();
        query.setTeamCodes(teamCode);
        List<TeamManagerInfo> teams = teamManagerInfoMapper.selectTeamManagerInfoList(query);
        if (teams == null || teams.isEmpty()) {
            return null;
        }
        return teams.get(0);
    }

    private List<CompetitionSceneCredential> selectCredentials(String teamCode, Long memberId) {
        if (StringUtils.isEmpty(teamCode) && memberId == null) {
            return new ArrayList<>();
        }
        CompetitionSceneCredential query = new CompetitionSceneCredential();
        query.setCredentialStatus(CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE);
        if (StringUtils.isNotEmpty(teamCode)) {
            query.setTeamCode(teamCode);
        }
        if (memberId != null) {
            query.setMemberId(memberId);
        }
        List<CompetitionSceneCredential> list = competitionSceneCredentialMapper.selectCompetitionSceneCredentialList(query);
        return list == null ? new ArrayList<CompetitionSceneCredential>() : list;
    }

    private void applyReviewObjectMapping(ReviewObject object, Long activityId, String sourceModule, String sourceBizType,
                                          String sourceBizId, String objectType, BusinessImportSource source) {
        object.setActivityId(activityId);
        String objectCodeSource = isTeamCodeBasedSource(sourceBizType)
                ? firstNotEmpty(source.sourceBizCode, sourceBizId)
                : sourceBizId;
        object.setObjectCode(buildImportedObjectCode(sourceBizType, objectCodeSource));
        object.setObjectName(firstNotEmpty(source.objectName, "导入对象-" + sourceBizId));
        object.setObjectType(objectType);
        if (StringUtils.isEmpty(object.getSubmitStatus())) {
            object.setSubmitStatus(ReviewObjectStatus.DRAFT.getCode());
        }
        object.setCreatedFrom(ReviewObjectCreatedFrom.BUSINESS_IMPORTED.getCode());
        object.setSourceModule(sourceModule);
        object.setSourceBizType(sourceBizType);
        object.setSourceBizId(sourceBizId);
        object.setSourceTeamId(source.sourceTeamId);
        object.setSourceRegistrationId(source.sourceRegistrationId);
        object.setOrgName(source.orgName);
        object.setContactName(source.contactName);
        object.setContactPhone(source.contactPhone);
        object.setContactEmail(source.contactEmail);
        object.setSubjectCode1(source.subjectCode1);
        object.setSubjectCode2(source.subjectCode2);
        object.setCategoryCodes(source.categoryCodes);
        object.setExtraData(source.extraData);
    }

    private void applyImportInitialStatus(ReviewObject object, ReviewObjectImportDTO dto) {
        if (dto == null || StringUtils.isEmpty(dto.getInitialSubmitStatus())) {
            return;
        }
        String status = dto.getInitialSubmitStatus().toUpperCase();
        if (ReviewObjectStatus.DRAFT.getCode().equals(status)) {
            object.setSubmitStatus(ReviewObjectStatus.DRAFT.getCode());
            object.setLockedTime(null);
            object.setInvalidTime(null);
            return;
        }
        if (ReviewObjectStatus.LOCKED.getCode().equals(status)) {
            object.setSubmitStatus(ReviewObjectStatus.LOCKED.getCode());
            if (object.getLockedTime() == null) {
                object.setLockedTime(DateUtils.getNowDate());
            }
            object.setInvalidTime(null);
            return;
        }
        throw new ServiceException("导入初始状态仅支持 DRAFT 或 LOCKED");
    }

    private void syncExternalRef(ReviewObject object, String sourceModule, String sourceBizType,
                                 String sourceBizId, BusinessImportSource source) {
        ReviewObjectExternalRef query = new ReviewObjectExternalRef();
        query.setActivityId(object.getActivityId());
        query.setObjectId(object.getId());
        query.setSourceModule(sourceModule);
        query.setSourceBizType(sourceBizType);
        query.setSourceBizId(sourceBizId);
        List<ReviewObjectExternalRef> existedList = externalRefMapper.selectList(query);

        ReviewObjectExternalRef ref = existedList == null || existedList.isEmpty()
                ? new ReviewObjectExternalRef()
                : existedList.get(0);
        ref.setActivityId(object.getActivityId());
        ref.setObjectId(object.getId());
        ref.setSourceModule(sourceModule);
        ref.setSourceBizType(sourceBizType);
        ref.setSourceBizId(sourceBizId);
        ref.setSourceBizCode(source.sourceBizCode);
        ref.setSourceTeamId(source.sourceTeamId);
        ref.setSourceRegistrationId(source.sourceRegistrationId);
        ref.setRelationType(resolveRelationType(sourceBizType));
        ref.setExtraData(source.extraData);
        if (ref.getId() == null) {
            fillCreateBase(ref);
            externalRefMapper.insert(ref);
        } else {
            fillUpdateBase(ref);
            externalRefMapper.update(ref);
        }
    }

    private String resolveRelationType(String sourceBizType) {
        if (SOURCE_BIZ_TYPE_TEAM.equals(sourceBizType)) {
            return RELATION_TYPE_TEAM;
        }
        if (SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER.equals(sourceBizType)) {
            return RELATION_TYPE_FILE_UPLOAD_MANAGER;
        }
        if (SOURCE_BIZ_TYPE_DEFENSE_SCHEDULE.equals(sourceBizType)) {
            return RELATION_TYPE_DEFENSE_SCHEDULE;
        }
        return RELATION_TYPE_REGISTRATION;
    }

    private List<ReviewObjectMember> syncMembers(ReviewObject object, String sourceModule, BusinessImportSource source) {
        List<ReviewObjectMember> members = new ArrayList<>();
        Set<String> memberKeys = new HashSet<>();
        int sort = 1;
        for (CompetitionApplyInfo apply : source.applyList) {
            ReviewObjectMember member = buildMemberFromApply(object, sourceModule, source, apply, sort++);
            String key = buildMemberKey(member.getPersonId(), member.getUserId(), member.getMemberName());
            if (memberKeys.add(key)) {
                fillCreateBase(member);
                objectMemberMapper.insert(member);
                members.add(member);
            }
        }

        ReviewObjectMember contact = buildContactMember(object, sourceModule, source, sort);
        if (contact != null) {
            String key = buildMemberKey(contact.getPersonId(), contact.getUserId(), contact.getMemberName());
            if (memberKeys.add(key)) {
                fillCreateBase(contact);
                objectMemberMapper.insert(contact);
                members.add(contact);
            }
        }
        return members;
    }

    private ReviewObjectMember buildMemberFromApply(ReviewObject object, String sourceModule,
                                                    BusinessImportSource source, CompetitionApplyInfo apply, int sortOrder) {
        ReviewObjectMember member = new ReviewObjectMember();
        member.setActivityId(object.getActivityId());
        member.setObjectId(object.getId());
        member.setUserId(apply.getUserId());
        member.setPersonId(apply.getMemberId() == null ? null : String.valueOf(apply.getMemberId()));
        member.setMemberName(apply.getUserName());
        member.setMemberRole(resolveMemberRole(apply, source.team));
        member.setIsPrimary(ReviewMemberRole.LEADER.getCode().equals(member.getMemberRole()) ? ReviewConstants.YES : ReviewConstants.NO);
        member.setPhone(apply.getPhone());
        member.setEmail(apply.getEmail());
        member.setOrgName(firstNotEmpty(apply.getSchoolName(), apply.getOrgName()));
        CompetitionSceneCredential credential = findCredentialForApply(source.credentials, apply);
        if (credential != null) {
            member.setCertificateId(credential.getCredentialId() == null ? null : String.valueOf(credential.getCredentialId()));
            member.setCertificateCode(credential.getCredentialNo());
            member.setCertificateType(mapReviewCertificateType(credential.getCredentialType()));
        }
        member.setSourceModule(sourceModule);
        member.setSourceBizId(apply.getMemberId() == null ? source.sourceBizId : String.valueOf(apply.getMemberId()));
        member.setSortOrder(apply.getTeamSort() == null ? sortOrder : apply.getTeamSort());
        return member;
    }

    private ReviewObjectMember buildContactMember(ReviewObject object, String sourceModule,
                                                  BusinessImportSource source, int sortOrder) {
        Long userId = null;
        String memberName = null;
        String phone = null;
        String email = null;
        String orgName = source.orgName;
        String personId = null;
        if (source.fileUploadSource != null) {
            userId = source.fileUploadSource.getUserId();
            memberName = firstNotEmpty(source.fileUploadSource.getUserName(), source.fileUploadSource.getLeaderTeacherName());
            personId = userId == null ? null : String.valueOf(userId);
        }
        if (StringUtils.isEmpty(memberName) && source.registration != null) {
            userId = source.registration.getLeaderTeacherId();
            memberName = source.registration.getLeaderTeacher();
            phone = source.registration.getLeaderTeacherPhone();
            email = source.registration.getLeaderTeacherEmail();
            orgName = firstNotEmpty(source.registration.getSchoolName(), source.registration.getOrgName(), orgName);
            personId = userId == null ? null : String.valueOf(userId);
        }
        if (StringUtils.isEmpty(memberName) && source.team != null) {
            memberName = source.team.getLeaderTeacher();
            phone = source.team.getLeaderTeacherPhone();
            email = source.team.getLeaderTeacherEmail();
            orgName = firstNotEmpty(source.team.getSchoolName(), orgName);
        }
        if (StringUtils.isEmpty(memberName)) {
            return null;
        }

        ReviewObjectMember member = new ReviewObjectMember();
        member.setActivityId(object.getActivityId());
        member.setObjectId(object.getId());
        member.setUserId(userId);
        member.setPersonId(personId);
        member.setMemberName(memberName);
        member.setMemberRole(ReviewMemberRole.CONTACT.getCode());
        member.setIsPrimary(ReviewConstants.NO);
        member.setPhone(phone);
        member.setEmail(email);
        member.setOrgName(orgName);
        member.setSourceModule(sourceModule);
        member.setSourceBizId(firstNotEmpty(source.sourceRegistrationId, source.sourceBizId));
        member.setSortOrder(sortOrder);
        return member;
    }

    private void syncSubmissionPermissions(ReviewObject object, String sourceModule, ReviewObjectImportDTO dto,
                                           List<ReviewObjectMember> members, BusinessImportSource source) {
        String mode = firstNotEmpty(dto.getPermissionUserMode(), PERMISSION_MODE_LEADER).toUpperCase();
        Set<Long> grantedUsers = new HashSet<>();
        if (PERMISSION_MODE_SPECIFIED.equals(mode)) {
            if (dto.getSpecifiedUserIds() != null) {
                for (Long userId : dto.getSpecifiedUserIds()) {
                    grantPermission(object, sourceModule, source.sourceBizId, userId, source.importOrgId,
                            ReviewPermissionType.EDIT_SUBMIT.getCode(), grantedUsers);
                }
            }
            return;
        }

        for (ReviewObjectMember member : members) {
            if (member.getUserId() == null) {
                continue;
            }
            boolean isLeader = ReviewMemberRole.LEADER.getCode().equals(member.getMemberRole());
            boolean isContact = ReviewMemberRole.CONTACT.getCode().equals(member.getMemberRole());
            if (PERMISSION_MODE_ALL_MEMBERS.equals(mode)) {
                grantPermission(object, sourceModule, source.sourceBizId, member.getUserId(), resolvePermissionOrgId(source, member),
                        isLeader || isContact ? ReviewPermissionType.EDIT_SUBMIT.getCode() : ReviewPermissionType.EDIT.getCode(), grantedUsers);
            } else if (PERMISSION_MODE_CONTACT.equals(mode)) {
                if (isContact) {
                    grantPermission(object, sourceModule, source.sourceBizId, member.getUserId(), resolvePermissionOrgId(source, member),
                            ReviewPermissionType.EDIT_SUBMIT.getCode(), grantedUsers);
                }
            } else if (isLeader || isContact) {
                grantPermission(object, sourceModule, source.sourceBizId, member.getUserId(), resolvePermissionOrgId(source, member),
                        ReviewPermissionType.EDIT_SUBMIT.getCode(), grantedUsers);
            }
        }
    }

    private Long resolvePermissionOrgId(BusinessImportSource source, ReviewObjectMember member) {
        Long orgId = findApplyOrgId(source.applyList, member);
        return orgId == null ? source.importOrgId : orgId;
    }

    private void grantPermission(ReviewObject object, String sourceModule, String sourceBizId, Long userId, Long orgId,
                                 String permissionType, Set<Long> grantedUsers) {
        if (userId == null || !grantedUsers.add(userId)) {
            return;
        }
        ReviewSubmissionPermission permission = new ReviewSubmissionPermission();
        permission.setActivityId(object.getActivityId());
        permission.setObjectId(object.getId());
        permission.setUserId(userId);
        permission.setOrgId(orgId);
        permission.setPermissionType(permissionType);
        permission.setStatus(ReviewPermissionStatus.ACTIVE.getCode());
        permission.setSourceModule(sourceModule);
        permission.setSourceBizId(sourceBizId);
        permission.setGrantedBy(currentUserId());
        permission.setGrantedTime(DateUtils.getNowDate());
        fillCreateBase(permission);
        submissionPermissionMapper.insert(permission);
    }

    private void syncCertificateRefs(ReviewObject object, String sourceModule, BusinessImportSource source,
                                     List<ReviewObjectMember> members) {
        Map<String, ReviewObjectMember> memberIndex = buildMemberIndex(members);
        Set<String> addedCodes = new HashSet<>();
        for (CompetitionSceneCredential credential : source.credentials) {
            if (StringUtils.isEmpty(credential.getCredentialNo()) || !addedCodes.add(credential.getCredentialNo())) {
                continue;
            }
            ReviewObjectMember member = findMemberForCredential(memberIndex, credential);
            ReviewObjectCertificateRef ref = new ReviewObjectCertificateRef();
            ref.setActivityId(object.getActivityId());
            ref.setObjectId(object.getId());
            ref.setCertificateId(credential.getCredentialId() == null ? null : String.valueOf(credential.getCredentialId()));
            ref.setCertificateCode(credential.getCredentialNo());
            ref.setCertificateType(mapReviewCertificateType(credential.getCredentialType()));
            ref.setPersonId(credential.getMemberId() == null ? null : String.valueOf(credential.getMemberId()));
            ref.setUserId(credential.getUserId());
            ref.setMemberId(member == null ? null : member.getId());
            ref.setMemberName(firstNotEmpty(member == null ? null : member.getMemberName(), credential.getUserName()));
            ref.setMemberRole(member == null ? resolveMemberRole(credential.getCompetitionRoleName()) : member.getMemberRole());
            ref.setSourceModule(sourceModule);
            ref.setSourceBizId(source.sourceBizId);
            ref.setSourceTeamId(firstNotEmpty(source.sourceTeamId, credential.getTeamCode()));
            ref.setSourceRegistrationId(source.sourceRegistrationId);
            ref.setValidStatus(ReviewCertificateValidStatus.VALID.getCode());
            fillCreateBase(ref);
            certificateRefMapper.insert(ref);
            writeAudit(object.getActivityId(), object.getId(), "CERTIFICATE", ref.getCertificateCode(),
                    ACTION_CERTIFICATE_ADD, "同步新增评审对象参赛证映射");
        }
    }

    private void syncMaterials(ReviewObject object, ReviewObjectImportDTO dto, BusinessImportSource source) {
        if (source.materials == null || source.materials.isEmpty()) {
            return;
        }
        boolean replace = dto != null && MATERIAL_OVERWRITE_REPLACE.equalsIgnoreCase(dto.getMaterialOverwriteMode());
        Map<String, ReviewObjectMaterial> existedByUrl = selectMaterialIndexByUrl(object.getId());
        int sort = 1;
        for (FileReviewImportMaterial sourceMaterial : source.materials) {
            if (sourceMaterial == null || StringUtils.isEmpty(sourceMaterial.getDownloadLink())) {
                continue;
            }
            ReviewObjectMaterial existed = existedByUrl.get(sourceMaterial.getDownloadLink());
            if (existed != null && !replace) {
                ensureImportedMaterialUsable(existed, source, sourceMaterial);
                continue;
            }
            ReviewObjectMaterial material = existed == null ? new ReviewObjectMaterial() : existed;
            applyMaterialMapping(material, object, source, sourceMaterial, sort++);
            if (material.getId() == null) {
                fillCreateBase(material);
                materialMapper.insert(material);
            } else {
                fillUpdateBase(material);
                materialMapper.update(material);
            }
        }
    }

    private void ensureImportedMaterialUsable(ReviewObjectMaterial material, BusinessImportSource source,
                                              FileReviewImportMaterial sourceMaterial) {
        if (material == null) {
            return;
        }
        boolean changed = false;
        if (StringUtils.isEmpty(material.getVisibleToReviewer())) {
            material.setVisibleToReviewer(ReviewConstants.YES);
            changed = true;
        }
        if (StringUtils.isEmpty(material.getStatus())) {
            material.setStatus(MATERIAL_STATUS_NORMAL);
            changed = true;
        }
        if (StringUtils.isEmpty(material.getSourceModule())) {
            material.setSourceModule(SOURCE_MODULE_SYSTEM);
            changed = true;
        }
        if (StringUtils.isEmpty(material.getSourceBizType())) {
            material.setSourceBizType(SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER);
            changed = true;
        }
        if (StringUtils.isEmpty(material.getSourceBizId()) && source != null && StringUtils.isNotEmpty(source.sourceBizId)) {
            material.setSourceBizId(source.sourceBizId);
            changed = true;
        }
        if (StringUtils.isEmpty(material.getSourceMaterialKey())
                && sourceMaterial != null && StringUtils.isNotEmpty(sourceMaterial.getDownloadLink())) {
            material.setSourceMaterialKey(sourceMaterial.getDownloadLink());
            changed = true;
        }
        if (changed) {
            fillUpdateBase(material);
            materialMapper.update(material);
        }
    }

    private Map<String, ReviewObjectMaterial> selectMaterialIndexByUrl(Long objectId) {
        Map<String, ReviewObjectMaterial> index = new HashMap<>();
        ReviewObjectMaterial query = new ReviewObjectMaterial();
        query.setObjectId(objectId);
        List<ReviewObjectMaterial> materials = materialMapper.selectList(query);
        if (materials == null) {
            return index;
        }
        for (ReviewObjectMaterial material : materials) {
            if (StringUtils.isNotEmpty(material.getFileUrl()) && !index.containsKey(material.getFileUrl())) {
                index.put(material.getFileUrl(), material);
            }
        }
        return index;
    }

    private void applyMaterialMapping(ReviewObjectMaterial material, ReviewObject object, BusinessImportSource source,
                                      FileReviewImportMaterial sourceMaterial, int sortOrder) {
        String fileName = firstNotEmpty(sourceMaterial.getFileName(), "文件任务材料-" + sortOrder);
        String fileExt = resolveFileExt(fileName);
        material.setActivityId(object.getActivityId());
        material.setObjectId(object.getId());
        material.setMaterialName(fileName);
        material.setMaterialType(resolveMaterialType(fileExt));
        material.setFileName(fileName);
        material.setFileUrl(sourceMaterial.getDownloadLink());
        material.setFileSize(sourceMaterial.getFileSize());
        material.setMimeType(sourceMaterial.getMimeType());
        material.setFileExt(fileExt);
        material.setVisibleToReviewer(ReviewConstants.YES);
        material.setSortOrder(sortOrder);
        material.setUploadBy(source.fileUploadSource == null ? currentUserId() : source.fileUploadSource.getUserId());
        material.setUploadTime(source.fileUploadSource == null || source.fileUploadSource.getUploadTime() == null
                ? DateUtils.getNowDate()
                : source.fileUploadSource.getUploadTime());
        material.setStatus(MATERIAL_STATUS_NORMAL);
        material.setSourceModule(SOURCE_MODULE_SYSTEM);
        material.setSourceBizType(SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER);
        material.setSourceBizId(source.sourceBizId);
        material.setSourceMaterialKey(sourceMaterial.getDownloadLink());
    }

    private String resolveFileExt(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return null;
        }
        String cleanName = fileName;
        int queryIndex = cleanName.indexOf('?');
        if (queryIndex >= 0) {
            cleanName = cleanName.substring(0, queryIndex);
        }
        int slashIndex = Math.max(cleanName.lastIndexOf('/'), cleanName.lastIndexOf('\\'));
        if (slashIndex >= 0 && slashIndex + 1 < cleanName.length()) {
            cleanName = cleanName.substring(slashIndex + 1);
        }
        int dotIndex = cleanName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex + 1 >= cleanName.length()) {
            return null;
        }
        return cleanName.substring(dotIndex + 1).toLowerCase();
    }

    private String resolveMaterialType(String fileExt) {
        if (StringUtils.isEmpty(fileExt)) {
            return ReviewMaterialType.OTHER.getCode();
        }
        if ("pdf".equals(fileExt)) {
            return ReviewMaterialType.PDF.getCode();
        }
        if ("doc".equals(fileExt) || "docx".equals(fileExt)) {
            return ReviewMaterialType.DOC.getCode();
        }
        if ("ppt".equals(fileExt) || "pptx".equals(fileExt)) {
            return ReviewMaterialType.PPT.getCode();
        }
        if ("mp4".equals(fileExt) || "mov".equals(fileExt) || "avi".equals(fileExt)) {
            return ReviewMaterialType.VIDEO.getCode();
        }
        if ("jpg".equals(fileExt) || "jpeg".equals(fileExt) || "png".equals(fileExt) || "gif".equals(fileExt)) {
            return ReviewMaterialType.IMAGE.getCode();
        }
        if ("zip".equals(fileExt) || "rar".equals(fileExt) || "7z".equals(fileExt)) {
            return ReviewMaterialType.ZIP.getCode();
        }
        return ReviewMaterialType.OTHER.getCode();
    }

    private boolean isObjectInSession(Long sessionId, Long objectId) {
        ReviewSessionObject query = new ReviewSessionObject();
        query.setSessionId(sessionId);
        query.setObjectId(objectId);
        List<ReviewSessionObject> list = sessionObjectMapper.selectList(query);
        return list != null && !list.isEmpty();
    }

    private void writeAudit(Long activityId, Long objectId, String bizType, String bizId,
                            String actionType, String actionContent) {
        if (auditLogMapper == null) {
            return;
        }
        ReviewAuditLog log = new ReviewAuditLog();
        log.setActivityId(activityId);
        log.setObjectId(objectId);
        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setActionType(actionType);
        log.setActionContent(actionContent);
        log.setOperatorUserId(currentUserId());
        log.setOperatorName(currentUsername());
        log.setOperateTime(DateUtils.getNowDate());
        fillCreateBase(log);
        auditLogMapper.insert(log);
    }

    private void fillObjectDefaults(ReviewObject object) {
        if (object == null) {
            throw new ServiceException("评审对象不能为空");
        }
        if (object.getActivityId() == null) {
            throw new ServiceException("评审活动ID不能为空");
        }
        if (StringUtils.isEmpty(object.getObjectType())) {
            object.setObjectType(ReviewObjectType.PROJECT.getCode());
        }
        if (StringUtils.isEmpty(object.getSubmitStatus())) {
            object.setSubmitStatus(ReviewObjectStatus.DRAFT.getCode());
        }
        if (StringUtils.isEmpty(object.getCreatedFrom())) {
            object.setCreatedFrom(ReviewObjectCreatedFrom.ADMIN_CREATED.getCode());
        }
    }

    private String buildImportedObjectCode(String sourceBizType, String sourceBizId) {
        String type = StringUtils.isNotEmpty(sourceBizType) ? sourceBizType : "BIZ";
        return type.toUpperCase() + "-" + sourceBizId;
    }

    private String normalizeSourceBizType(String sourceBizType) {
        return firstNotEmpty(sourceBizType, "OTHER").toUpperCase();
    }

    private String resolveSourceModule(String sourceModule, String sourceBizType) {
        if (StringUtils.isNotEmpty(sourceModule)) {
            return sourceModule;
        }
        if (SOURCE_BIZ_TYPE_FILE_UPLOAD_MANAGER.equals(sourceBizType)) {
            return SOURCE_MODULE_SYSTEM;
        }
        return ReviewConstants.SOURCE_MODULE_COMPETITION;
    }

    private boolean isTeamCodeBasedSource(String sourceBizType) {
        return SOURCE_BIZ_TYPE_TEAM.equals(sourceBizType)
                || SOURCE_BIZ_TYPE_REGISTRATION_TEAM_CODE.equals(sourceBizType)
                || SOURCE_BIZ_TYPE_DEFENSE_SCHEDULE.equals(sourceBizType);
    }

    private void markSkipped(ReviewObjectImportResultVO result, String item) {
        result.setSkipCount(result.getSkipCount() + 1);
        result.getSkippedItems().add(item);
    }

    private void markFailed(ReviewObjectImportResultVO result, String item) {
        result.setFailedCount(result.getFailedCount() + 1);
        result.getFailedItems().add(item);
    }

    private String resolveMemberRole(CompetitionApplyInfo apply, TeamManagerInfo team) {
        String roleName = apply.getCompetitionRoleName();
        if (containsAny(roleName, ApplyConstants.TEAM_GUIDE_TEACHER, "指导老师", "教师", "老师")) {
            return ReviewMemberRole.TEACHER.getCode();
        }
        if (containsAny(roleName, ApplyConstants.TEAM_LEADER_MEMBER, "负责人", "队长")) {
            return ReviewMemberRole.LEADER.getCode();
        }
        if (team != null && apply.getUserId() != null && Objects.equals(apply.getUserId(), team.getTeamLeaderId())) {
            return ReviewMemberRole.LEADER.getCode();
        }
        if (apply.getTeamSort() != null && apply.getTeamSort() == 1) {
            return ReviewMemberRole.LEADER.getCode();
        }
        return ReviewMemberRole.MEMBER.getCode();
    }

    private String resolveMemberRole(String competitionRoleName) {
        if (containsAny(competitionRoleName, ApplyConstants.TEAM_GUIDE_TEACHER, "指导老师", "教师", "老师")) {
            return ReviewMemberRole.TEACHER.getCode();
        }
        if (containsAny(competitionRoleName, ApplyConstants.TEAM_LEADER_MEMBER, "负责人", "队长")) {
            return ReviewMemberRole.LEADER.getCode();
        }
        return ReviewMemberRole.MEMBER.getCode();
    }

    private String mapReviewCertificateType(String credentialType) {
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_TEACHER.equals(credentialType)) {
            return ReviewCertificateType.TEACHER.getCode();
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credentialType)) {
            return ReviewCertificateType.EXPERT.getCode();
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_STAFF.equals(credentialType)) {
            return ReviewCertificateType.STAFF.getCode();
        }
        return ReviewCertificateType.CONTESTANT.getCode();
    }

    private CompetitionSceneCredential findCredentialForApply(List<CompetitionSceneCredential> credentials, CompetitionApplyInfo apply) {
        for (CompetitionSceneCredential credential : credentials) {
            if (apply.getMemberId() != null && Objects.equals(apply.getMemberId(), credential.getMemberId())) {
                return credential;
            }
            if (apply.getUserId() != null && Objects.equals(apply.getUserId(), credential.getUserId())) {
                return credential;
            }
            if (StringUtils.isNotEmpty(apply.getUserName()) && apply.getUserName().equals(credential.getUserName())) {
                return credential;
            }
        }
        return null;
    }

    private Map<String, ReviewObjectMember> buildMemberIndex(List<ReviewObjectMember> members) {
        Map<String, ReviewObjectMember> index = new HashMap<>();
        for (ReviewObjectMember member : members) {
            if (StringUtils.isNotEmpty(member.getPersonId())) {
                index.put("person:" + member.getPersonId(), member);
            }
            if (member.getUserId() != null) {
                index.put("user:" + member.getUserId(), member);
            }
            if (StringUtils.isNotEmpty(member.getMemberName())) {
                index.put("name:" + member.getMemberName(), member);
            }
        }
        return index;
    }

    private ReviewObjectMember findMemberForCredential(Map<String, ReviewObjectMember> index, CompetitionSceneCredential credential) {
        ReviewObjectMember member = null;
        if (credential.getMemberId() != null) {
            member = index.get("person:" + credential.getMemberId());
        }
        if (member == null && credential.getUserId() != null) {
            member = index.get("user:" + credential.getUserId());
        }
        if (member == null && StringUtils.isNotEmpty(credential.getUserName())) {
            member = index.get("name:" + credential.getUserName());
        }
        return member;
    }

    private Long findApplyOrgId(List<CompetitionApplyInfo> applyList, ReviewObjectMember member) {
        for (CompetitionApplyInfo apply : applyList) {
            if (member.getPersonId() != null && apply.getMemberId() != null
                    && member.getPersonId().equals(String.valueOf(apply.getMemberId()))) {
                return apply.getOrgId();
            }
            if (member.getUserId() != null && Objects.equals(member.getUserId(), apply.getUserId())) {
                return apply.getOrgId();
            }
        }
        return null;
    }

    private String buildMemberKey(String personId, Long userId, String memberName) {
        if (StringUtils.isNotEmpty(personId)) {
            return "person:" + personId;
        }
        if (userId != null) {
            return "user:" + userId;
        }
        return "name:" + firstNotEmpty(memberName, "");
    }

    private Long parseLong(String value) {
        try {
            return StringUtils.isEmpty(value) ? null : Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean containsAny(String value, String... keywords) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        for (String keyword : keywords) {
            if (StringUtils.isNotEmpty(keyword) && value.contains(keyword)) {
                return true;
            }
        }
        return false;
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

    private CompetitionApplyInfo firstApply(List<CompetitionApplyInfo> applyList) {
        return applyList == null || applyList.isEmpty() ? null : applyList.get(0);
    }

    private String firstApplyValue(List<CompetitionApplyInfo> applyList, String field) {
        CompetitionApplyInfo apply = firstApply(applyList);
        if (apply == null) {
            return null;
        }
        if ("teamName".equals(field)) {
            return apply.getTeamName();
        }
        if ("schoolName".equals(field)) {
            return apply.getSchoolName();
        }
        if ("orgName".equals(field)) {
            return apply.getOrgName();
        }
        if ("leaderTeacher".equals(field)) {
            return apply.getLeaderTeacher();
        }
        if ("leaderTeacherPhone".equals(field)) {
            return apply.getLeaderTeacherPhone();
        }
        if ("leaderTeacherEmail".equals(field)) {
            return apply.getLeaderTeacherEmail();
        }
        if ("competitionTrackId".equals(field)) {
            return apply.getCompetitionTrackId();
        }
        if ("secondLevelCode".equals(field)) {
            return apply.getSecondLevelCode();
        }
        if ("competitionTrackName".equals(field)) {
            return apply.getCompetitionTrackName();
        }
        if ("secondLevelName".equals(field)) {
            return apply.getSecondLevelName();
        }
        return null;
    }

    private String firstLeaderApplyName(List<CompetitionApplyInfo> applyList) {
        CompetitionApplyInfo apply = firstLeaderApply(applyList);
        return apply == null ? null : apply.getUserName();
    }

    private String firstLeaderApplyPhone(List<CompetitionApplyInfo> applyList) {
        CompetitionApplyInfo apply = firstLeaderApply(applyList);
        return apply == null ? null : apply.getPhone();
    }

    private String firstLeaderApplyEmail(List<CompetitionApplyInfo> applyList) {
        CompetitionApplyInfo apply = firstLeaderApply(applyList);
        return apply == null ? null : apply.getEmail();
    }

    private CompetitionApplyInfo firstLeaderApply(List<CompetitionApplyInfo> applyList) {
        if (applyList == null) {
            return null;
        }
        for (CompetitionApplyInfo apply : applyList) {
            if (containsAny(apply.getCompetitionRoleName(), ApplyConstants.TEAM_LEADER_MEMBER, "负责人", "队长")) {
                return apply;
            }
            if (apply.getTeamSort() != null && apply.getTeamSort() == 1) {
                return apply;
            }
        }
        return firstApply(applyList);
    }

    private String buildCategoryCodes(String trackName, String secondLevelName) {
        if (StringUtils.isEmpty(trackName) && StringUtils.isEmpty(secondLevelName)) {
            return null;
        }
        return "{\"competitionTrackName\":\"" + jsonEscape(trackName)
                + "\",\"secondLevelName\":\"" + jsonEscape(secondLevelName) + "\"}";
    }

    private String buildExtraData(TeamManagerInfo team, CompetitionApplyInfo apply) {
        List<String> fields = new ArrayList<>();
        if (team != null) {
            addJsonField(fields, "teamId", team.getTeamId() == null ? null : String.valueOf(team.getTeamId()));
            addJsonField(fields, "competitionName", team.getCompetitionName());
            addJsonField(fields, "competitionTrackName", team.getCompetitionTrackName());
            addJsonField(fields, "secondLevelName", team.getSecondLevelName());
        }
        if (apply != null) {
            addJsonField(fields, "memberId", apply.getMemberId() == null ? null : String.valueOf(apply.getMemberId()));
            addJsonField(fields, "competitionName", apply.getCompetitionName());
            addJsonField(fields, "competitionTrackName", apply.getCompetitionTrackName());
            addJsonField(fields, "secondLevelName", apply.getSecondLevelName());
        }
        if (fields.isEmpty()) {
            return null;
        }
        return "{" + String.join(",", fields) + "}";
    }

    private String buildFileUploadExtraData(FileReviewImportSource source) {
        if (source == null) {
            return null;
        }
        List<String> fields = new ArrayList<>();
        addJsonField(fields, "fileUploadManagerId", source.getId() == null ? null : String.valueOf(source.getId()));
        addJsonField(fields, "fileTaskId", source.getFileTaskId() == null ? null : String.valueOf(source.getFileTaskId()));
        addJsonField(fields, "fileTaskName", source.getFileTaskName());
        addJsonField(fields, "userId", source.getUserId() == null ? null : String.valueOf(source.getUserId()));
        addJsonField(fields, "userName", source.getUserName());
        addJsonField(fields, "competitionSeriesId", source.getCompetitionSeriesId());
        addJsonField(fields, "competitionName", source.getCompetitionName());
        addJsonField(fields, "competitionStageId", source.getCompetitionStageId());
        addJsonField(fields, "competitionStageName", source.getCompetitionStageName());
        addJsonField(fields, "competitionTrackCode", source.getCompetitionTrackCode());
        addJsonField(fields, "competitionTrackName", source.getCompetitionTrackName());
        addJsonField(fields, "secondLevelCode", source.getSecondLevelCode());
        addJsonField(fields, "secondLevelName", source.getSecondLevelName());
        addJsonField(fields, "teamCode", source.getTeamCode());
        addJsonField(fields, "teamName", source.getTeamName());
        addJsonField(fields, "leaderTeacherId", source.getLeaderTeacherId());
        addJsonField(fields, "leaderTeacherName", source.getLeaderTeacherName());
        addJsonField(fields, "guideTeacher", source.getGuideTeacher());
        addJsonField(fields, "uploadTime", source.getUploadTime() == null
                ? null
                : DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, source.getUploadTime()));
        addJsonField(fields, "totalSize", source.getTotalSize());
        addJsonField(fields, "materialCount", source.getMaterials() == null ? "0" : String.valueOf(source.getMaterials().size()));
        if (fields.isEmpty()) {
            return null;
        }
        return "{" + String.join(",", fields) + "}";
    }

    private String buildDefenseScheduleExtraData(DefenseScheduleRow row, Long competitionSeriesId,
                                                 BusinessImportSource source) {
        List<String> fields = new ArrayList<>();
        if (row != null) {
            addJsonField(fields, "defenseOrder", row.defenseOrder == null ? null : String.valueOf(row.defenseOrder));
            addJsonField(fields, "defenseLineNo", String.valueOf(row.lineNo));
            addJsonField(fields, "inputOrgName", row.orgName);
            addJsonField(fields, "inputTeamName", row.teamName);
            addJsonField(fields, "inputLeaderName", row.leaderName);
        }
        addJsonField(fields, "competitionSeriesId", competitionSeriesId == null ? null : String.valueOf(competitionSeriesId));
        if (source != null) {
            addJsonField(fields, "teamCode", source.sourceTeamId);
            addJsonField(fields, "registrationId", source.sourceRegistrationId);
            addJsonField(fields, "materialCount", source.materials == null ? "0" : String.valueOf(source.materials.size()));
            if (source.fileUploadSource != null) {
                addJsonField(fields, "fileUploadManagerId", source.fileUploadSource.getId() == null
                        ? null
                        : String.valueOf(source.fileUploadSource.getId()));
                addJsonField(fields, "fileTaskId", source.fileUploadSource.getFileTaskId() == null
                        ? null
                        : String.valueOf(source.fileUploadSource.getFileTaskId()));
                addJsonField(fields, "fileTaskName", source.fileUploadSource.getFileTaskName());
            }
        }
        if (fields.isEmpty()) {
            return null;
        }
        return "{" + String.join(",", fields) + "}";
    }

    private void addJsonField(List<String> fields, String key, String value) {
        if (StringUtils.isNotEmpty(value)) {
            fields.add("\"" + key + "\":\"" + jsonEscape(value) + "\"");
        }
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static class ResolvedImportSources {
        private final List<String> sourceBizIds = new ArrayList<>();
        private final Map<String, BusinessImportSource> preloadedSources = new HashMap<>();
    }

    private static class DefenseScheduleRow {
        private int lineNo;
        private Integer defenseOrder;
        private String orgName;
        private String teamName;
        private String leaderName;
        private final List<String> warnings = new ArrayList<>();
    }

    private static class SceneCredentialResolveResult {
        private List<ReviewCertificateResolveVO> candidates = Collections.emptyList();
        private String warningMessage;
    }

    private static class BusinessImportSource {
        private String sourceBizId;
        private String sourceBizCode;
        private String sourceTeamId;
        private String sourceRegistrationId;
        private String objectName;
        private String orgName;
        private String contactName;
        private String contactPhone;
        private String contactEmail;
        private String subjectCode1;
        private String subjectCode2;
        private String categoryCodes;
        private String extraData;
        private Long importOrgId;
        private boolean importable = true;
        private DefenseScheduleRow defenseRow;
        private FileReviewImportSource fileUploadSource;
        private TeamManagerInfo team;
        private CompetitionApplyInfo registration;
        private List<CompetitionApplyInfo> applyList = new ArrayList<>();
        private List<CompetitionSceneCredential> credentials = new ArrayList<>();
        private List<FileReviewImportMaterial> materials = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        private static BusinessImportSource generic(String sourceBizId) {
            BusinessImportSource source = new BusinessImportSource();
            source.sourceBizId = sourceBizId;
            source.sourceBizCode = sourceBizId;
            source.objectName = "导入对象-" + sourceBizId;
            return source;
        }
    }
}
