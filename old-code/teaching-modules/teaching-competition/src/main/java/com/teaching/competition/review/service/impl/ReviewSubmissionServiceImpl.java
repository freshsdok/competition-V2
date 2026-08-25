package com.teaching.competition.review.service.impl;

import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.review.constant.ReviewConstants;
import com.teaching.competition.review.domain.ReviewActivity;
import com.teaching.competition.review.domain.ReviewAuditLog;
import com.teaching.competition.review.domain.ReviewObject;
import com.teaching.competition.review.domain.ReviewObjectMaterial;
import com.teaching.competition.review.domain.ReviewObjectMember;
import com.teaching.competition.review.domain.ReviewObjectSubmitLog;
import com.teaching.competition.review.domain.ReviewResult;
import com.teaching.competition.review.domain.ReviewRound;
import com.teaching.competition.review.domain.ReviewSubmissionPermission;
import com.teaching.competition.review.dto.ReviewSubmissionActionDTO;
import com.teaching.competition.review.dto.ReviewSubmissionDraftDTO;
import com.teaching.competition.review.dto.ReviewSubmissionMaterialDTO;
import com.teaching.competition.review.enums.ReviewActivityStatus;
import com.teaching.competition.review.enums.ReviewMaterialType;
import com.teaching.competition.review.enums.ReviewMemberRole;
import com.teaching.competition.review.enums.ReviewObjectStatus;
import com.teaching.competition.review.enums.ReviewPermissionStatus;
import com.teaching.competition.review.enums.ReviewPermissionType;
import com.teaching.competition.review.enums.ReviewResultStatus;
import com.teaching.competition.review.enums.ReviewSubmitActionType;
import com.teaching.competition.review.mapper.ReviewActivityMapper;
import com.teaching.competition.review.mapper.ReviewAuditLogMapper;
import com.teaching.competition.review.mapper.ReviewObjectMapper;
import com.teaching.competition.review.mapper.ReviewObjectMaterialMapper;
import com.teaching.competition.review.mapper.ReviewObjectMemberMapper;
import com.teaching.competition.review.mapper.ReviewObjectSubmitLogMapper;
import com.teaching.competition.review.mapper.ReviewResultMapper;
import com.teaching.competition.review.mapper.ReviewRoundMapper;
import com.teaching.competition.review.mapper.ReviewSubmissionPermissionMapper;
import com.teaching.competition.review.service.IReviewSubmissionService;
import com.teaching.competition.review.support.ReviewCrudMapper;
import com.teaching.competition.review.vo.ReviewSubmissionCloseResultVO;
import com.teaching.competition.review.vo.ReviewSubmissionDetailVO;
import com.teaching.competition.review.vo.ReviewSubmissionResultVO;
import com.teaching.competition.review.vo.ReviewSubmissionTaskVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 被评审人填报Service业务层处理。
 */
@Service
public class ReviewSubmissionServiceImpl extends AbstractReviewCrudService<ReviewObject> implements IReviewSubmissionService {
    private static final String MATERIAL_STATUS_NORMAL = "NORMAL";
    private static final String MATERIAL_STATUS_DELETED = "DELETED";
    private static final String BIZ_TYPE_SUBMISSION = "REVIEW_SUBMISSION";

    @Autowired
    private ReviewObjectMapper objectMapper;

    @Autowired
    private ReviewActivityMapper activityMapper;

    @Autowired
    private ReviewObjectMemberMapper objectMemberMapper;

    @Autowired
    private ReviewObjectMaterialMapper materialMapper;

    @Autowired
    private ReviewSubmissionPermissionMapper permissionMapper;

    @Autowired
    private ReviewObjectSubmitLogMapper submitLogMapper;

    @Autowired
    private ReviewAuditLogMapper auditLogMapper;

    @Autowired
    private ReviewResultMapper resultMapper;

    @Autowired
    private ReviewRoundMapper roundMapper;

    @Override
    protected ReviewCrudMapper<ReviewObject> mapper() {
        return objectMapper;
    }

    @Override
    public List<ReviewSubmissionTaskVO> myList() {
        Long userId = requireCurrentUserId();
        ReviewSubmissionPermission query = new ReviewSubmissionPermission();
        query.setUserId(userId);
        query.setStatus(ReviewPermissionStatus.ACTIVE.getCode());
        List<ReviewSubmissionPermission> permissions = permissionMapper.selectList(query);
        List<ReviewSubmissionTaskVO> list = new ArrayList<>();
        if (permissions == null) {
            return list;
        }
        for (ReviewSubmissionPermission permission : permissions) {
            if (permission.getObjectId() == null) {
                continue;
            }
            ReviewObject object = objectMapper.selectById(permission.getObjectId());
            if (object == null) {
                continue;
            }
            ReviewActivity activity = activityMapper.selectById(object.getActivityId());
            ReviewSubmissionTaskVO vo = new ReviewSubmissionTaskVO();
            vo.setPermissionId(permission.getId());
            vo.setActivityId(object.getActivityId());
            vo.setActivityName(activity == null ? null : activity.getActivityName());
            vo.setObjectId(object.getId());
            vo.setObjectCode(object.getObjectCode());
            vo.setObjectName(object.getObjectName());
            vo.setOrgName(object.getOrgName());
            vo.setSubmitStatus(object.getSubmitStatus());
            vo.setSubmitDeadline(activity == null ? null : activity.getSubmitDeadline());
            vo.setEditable(canEdit(object, activity, permission));
            vo.setWithdrawable(canWithdraw(object, activity, permission));
            vo.setLastUpdateTime(object.getUpdateTime());
            list.add(vo);
        }
        return list;
    }

    @Override
    public ReviewSubmissionDetailVO detail(Long objectId) {
        ReviewObject object = requireObject(objectId);
        ReviewActivity activity = requireActivity(object.getActivityId());
        List<ReviewSubmissionPermission> permissions = currentUserPermissions(objectId);
        if (permissions.isEmpty()) {
            throw new ServiceException("无权查看该评审对象填报信息");
        }
        ReviewSubmissionPermission permission = permissions.get(0);

        ReviewSubmissionDetailVO vo = new ReviewSubmissionDetailVO();
        vo.setObject(object);
        vo.setMembers(selectMembers(objectId));
        vo.setMaterials(selectMaterials(objectId));
        vo.setPermissions(permissions);
        vo.setCurrentStatus(object.getSubmitStatus());
        vo.setEditable(canEdit(object, activity, permission));
        vo.setSubmittable(canSubmit(object, activity, permission));
        vo.setWithdrawable(canWithdraw(object, activity, permission));
        vo.setWarningMessage(buildWarning(object, activity));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewObject saveDraft(Long objectId, ReviewSubmissionDraftDTO dto) {
        ReviewObject object = requireObject(objectId);
        ReviewActivity activity = requireActivity(object.getActivityId());
        requirePermission(objectId, ReviewPermissionType.EDIT.getCode(), ReviewPermissionType.EDIT_SUBMIT.getCode());
        requireEditableStatus(object);
        ensureBeforeDeadline(activity);

        String beforeStatus = object.getSubmitStatus();
        if (dto != null) {
            object.setObjectName(dto.getObjectName());
            object.setSummary(dto.getSummary());
            object.setOrgName(dto.getOrgName());
            object.setContactName(dto.getContactName());
            object.setContactPhone(dto.getContactPhone());
            object.setContactEmail(dto.getContactEmail());
            object.setSubjectCode1(dto.getSubjectCode1());
            object.setSubjectCode2(dto.getSubjectCode2());
            object.setSubjectCode3(dto.getSubjectCode3());
            object.setCategoryCodes(dto.getCategoryCodes());
            object.setKeywords(dto.getKeywords());
            object.setExtraData(dto.getExtraData());
        }
        fillUpdateBase(object);
        objectMapper.update(object);
        writeStatusLog(object, ReviewSubmitActionType.SAVE_DRAFT.getCode(), beforeStatus, object.getSubmitStatus(), "保存草稿");
        writeAudit(object, ReviewSubmitActionType.SAVE_DRAFT.getCode(), "保存评审对象填报草稿");
        return object;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewObjectMaterial addMaterial(Long objectId, ReviewSubmissionMaterialDTO dto) {
        if (dto == null || StringUtils.isEmpty(dto.getFileUrl())) {
            throw new ServiceException("材料文件地址不能为空");
        }
        ReviewObject object = requireObject(objectId);
        ReviewActivity activity = requireActivity(object.getActivityId());
        requirePermission(objectId, ReviewPermissionType.EDIT.getCode(), ReviewPermissionType.EDIT_SUBMIT.getCode());
        requireEditableStatus(object);
        ensureBeforeDeadline(activity);

        ReviewObjectMaterial material = new ReviewObjectMaterial();
        material.setActivityId(object.getActivityId());
        material.setObjectId(objectId);
        material.setMaterialName(firstNotEmpty(dto.getMaterialName(), dto.getFileName(), "填报材料"));
        material.setMaterialType(firstNotEmpty(dto.getMaterialType(), ReviewMaterialType.OTHER.getCode()));
        material.setFileName(firstNotEmpty(dto.getFileName(), dto.getMaterialName()));
        material.setFileUrl(dto.getFileUrl());
        material.setFileSize(dto.getFileSize());
        material.setMimeType(dto.getMimeType());
        material.setFileExt(dto.getFileExt());
        material.setVisibleToReviewer(firstNotEmpty(dto.getVisibleToReviewer(), ReviewConstants.YES));
        material.setSortOrder(dto.getSortOrder());
        material.setUploadBy(currentUserId());
        material.setUploadTime(DateUtils.getNowDate());
        material.setStatus(MATERIAL_STATUS_NORMAL);
        fillCreateBase(material);
        materialMapper.insert(material);
        writeStatusLog(object, ReviewSubmitActionType.MATERIAL_ADD.getCode(), object.getSubmitStatus(), object.getSubmitStatus(),
                "上传材料：" + material.getMaterialName());
        writeAudit(object, ReviewSubmitActionType.MATERIAL_ADD.getCode(), "上传评审材料：" + material.getFileName());
        return material;
    }

    @Override
    public List<ReviewObjectMaterial> listMaterials(Long objectId) {
        requireObject(objectId);
        requireAnyPermission(objectId);
        return selectMaterials(objectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMaterial(Long materialId) {
        ReviewObjectMaterial material = materialMapper.selectById(materialId);
        if (material == null) {
            throw new ServiceException("评审材料不存在");
        }
        ReviewObject object = requireObject(material.getObjectId());
        ReviewActivity activity = requireActivity(object.getActivityId());
        requirePermission(object.getId(), ReviewPermissionType.EDIT.getCode(), ReviewPermissionType.EDIT_SUBMIT.getCode());
        requireEditableStatus(object);
        ensureBeforeDeadline(activity);

        material.setStatus(MATERIAL_STATUS_DELETED);
        fillUpdateBase(material);
        materialMapper.update(material);
        int rows = materialMapper.deleteByIds(new Long[]{materialId}, currentUsername());
        writeStatusLog(object, ReviewSubmitActionType.MATERIAL_DELETE.getCode(), object.getSubmitStatus(), object.getSubmitStatus(),
                "删除材料：" + firstNotEmpty(material.getMaterialName(), material.getFileName()));
        writeAudit(object, ReviewSubmitActionType.MATERIAL_DELETE.getCode(), "删除评审材料：" + material.getId());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewObject submit(Long objectId) {
        ReviewObject object = requireObject(objectId);
        ReviewActivity activity = requireActivity(object.getActivityId());
        requirePermission(objectId, ReviewPermissionType.SUBMIT.getCode(), ReviewPermissionType.EDIT_SUBMIT.getCode());
        requireSubmittableStatus(object);
        ensureBeforeDeadline(activity);
        validateBeforeSubmit(object);

        String beforeStatus = object.getSubmitStatus();
        object.setSubmitStatus(ReviewObjectStatus.SUBMITTED.getCode());
        object.setSubmitTime(DateUtils.getNowDate());
        object.setSubmittedBy(currentUserId());
        fillUpdateBase(object);
        objectMapper.update(object);
        touchPermissionUsedTime(objectId);
        writeStatusLog(object, ReviewSubmitActionType.SUBMIT.getCode(), beforeStatus, object.getSubmitStatus(), "提交评审资料");
        writeAudit(object, ReviewSubmitActionType.SUBMIT.getCode(), "提交评审资料");
        return object;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewObject withdrawRequest(Long objectId, ReviewSubmissionActionDTO dto) {
        ReviewObject object = requireObject(objectId);
        ReviewActivity activity = requireActivity(object.getActivityId());
        requireAnyPermission(objectId);
        if (!ReviewObjectStatus.SUBMITTED.getCode().equals(object.getSubmitStatus())) {
            throw new ServiceException("只有已提交对象可以申请撤回");
        }
        ensureBeforeDeadline(activity);

        String beforeStatus = object.getSubmitStatus();
        object.setSubmitStatus(ReviewObjectStatus.WITHDRAW_REQUESTED.getCode());
        fillUpdateBase(object);
        objectMapper.update(object);
        String reason = actionReason(dto, "申请撤回修改");
        writeStatusLog(object, ReviewSubmitActionType.WITHDRAW_REQUEST.getCode(), beforeStatus, object.getSubmitStatus(), reason);
        writeAudit(object, ReviewSubmitActionType.WITHDRAW_REQUEST.getCode(), "申请撤回评审资料：" + reason);
        return object;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewObject withdrawApprove(Long objectId, ReviewSubmissionActionDTO dto) {
        ReviewObject object = requireObject(objectId);
        ReviewActivity activity = requireActivity(object.getActivityId());
        if (!ReviewObjectStatus.WITHDRAW_REQUESTED.getCode().equals(object.getSubmitStatus())) {
            throw new ServiceException("只有撤回申请中的对象可以审批通过");
        }
        ensureBeforeDeadline(activity);

        String beforeStatus = object.getSubmitStatus();
        object.setSubmitStatus(ReviewObjectStatus.WITHDRAW_APPROVED.getCode());
        fillUpdateBase(object);
        objectMapper.update(object);
        String reason = actionReason(dto, "同意撤回修改");
        writeStatusLog(object, ReviewSubmitActionType.WITHDRAW_APPROVE.getCode(), beforeStatus, object.getSubmitStatus(), reason);
        writeAudit(object, ReviewSubmitActionType.WITHDRAW_APPROVE.getCode(), "管理员审批通过撤回：" + reason);
        return object;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewObject withdrawReject(Long objectId, ReviewSubmissionActionDTO dto) {
        ReviewObject object = requireObject(objectId);
        if (!ReviewObjectStatus.WITHDRAW_REQUESTED.getCode().equals(object.getSubmitStatus())) {
            throw new ServiceException("只有撤回申请中的对象可以驳回");
        }

        String beforeStatus = object.getSubmitStatus();
        object.setSubmitStatus(ReviewObjectStatus.SUBMITTED.getCode());
        fillUpdateBase(object);
        objectMapper.update(object);
        String reason = actionReason(dto, "驳回撤回申请");
        writeStatusLog(object, ReviewSubmitActionType.WITHDRAW_REJECT.getCode(), beforeStatus, object.getSubmitStatus(), reason);
        writeAudit(object, ReviewSubmitActionType.WITHDRAW_REJECT.getCode(), "管理员驳回撤回：" + reason);
        return object;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewSubmissionCloseResultVO closeSubmission(Long activityId) {
        ReviewActivity activity = requireActivity(activityId);
        ReviewObject query = new ReviewObject();
        query.setActivityId(activityId);
        List<ReviewObject> objects = objectMapper.selectList(query);

        ReviewSubmissionCloseResultVO result = new ReviewSubmissionCloseResultVO();
        result.setActivityId(activityId);
        Date now = DateUtils.getNowDate();
        if (objects != null) {
            for (ReviewObject object : objects) {
                String beforeStatus = object.getSubmitStatus();
                if (ReviewObjectStatus.SUBMITTED.getCode().equals(beforeStatus)) {
                    object.setSubmitStatus(ReviewObjectStatus.LOCKED.getCode());
                    object.setLockedTime(now);
                    fillUpdateBase(object);
                    objectMapper.update(object);
                    writeStatusLog(object, ReviewSubmitActionType.LOCK.getCode(), beforeStatus, object.getSubmitStatus(), "填报截止后锁定");
                    writeAudit(object, ReviewSubmitActionType.LOCK.getCode(), "填报截止后锁定评审对象");
                    result.setLockedCount(result.getLockedCount() + 1);
                } else if (shouldInvalidateOnClose(beforeStatus)) {
                    object.setSubmitStatus(ReviewObjectStatus.INVALID.getCode());
                    object.setInvalidTime(now);
                    fillUpdateBase(object);
                    objectMapper.update(object);
                    writeStatusLog(object, ReviewSubmitActionType.INVALID.getCode(), beforeStatus, object.getSubmitStatus(), "填报截止后未完成提交作废");
                    writeAudit(object, ReviewSubmitActionType.INVALID.getCode(), "填报截止后未提交作废");
                    result.setInvalidCount(result.getInvalidCount() + 1);
                } else {
                    result.setIgnoredCount(result.getIgnoredCount() + 1);
                }
            }
        }

        activity.setStatus(ReviewActivityStatus.SUBMIT_CLOSED.getCode());
        fillUpdateBase(activity);
        activityMapper.update(activity);
        result.setMessage("填报关闭完成，锁定 " + result.getLockedCount() + " 个，作废 "
                + result.getInvalidCount() + " 个，忽略 " + result.getIgnoredCount() + " 个");
        return result;
    }

    @Override
    public ReviewSubmissionResultVO publishedResult(Long objectId) {
        ReviewObject object = requireObject(objectId);
        requirePublishedResultPermission(object);

        ReviewResult query = new ReviewResult();
        query.setObjectId(objectId);
        query.setActivityId(object.getActivityId());
        query.setResultStatus(ReviewResultStatus.PUBLISHED.getCode());
        List<ReviewResult> results = resultMapper.selectList(query);
        if (results == null || results.isEmpty()) {
            throw new ServiceException("评审结果暂未发布");
        }
        results.sort(Comparator.comparing(ReviewResult::getPublishedTime, Comparator.nullsLast(Date::compareTo))
                .thenComparing(ReviewResult::getId, Comparator.nullsLast(Long::compareTo)).reversed());
        ReviewResult result = results.get(0);
        ReviewActivity activity = activityMapper.selectById(result.getActivityId());
        ReviewRound round = result.getRoundId() == null ? null : roundMapper.selectById(result.getRoundId());

        ReviewSubmissionResultVO vo = new ReviewSubmissionResultVO();
        vo.setActivityId(result.getActivityId());
        vo.setActivityName(activity == null ? null : activity.getActivityName());
        vo.setRoundId(result.getRoundId());
        vo.setRoundName(round == null ? null : round.getRoundName());
        vo.setObjectId(object.getId());
        vo.setObjectCode(object.getObjectCode());
        vo.setObjectName(object.getObjectName());
        vo.setCalculatedScore(result.getCalculatedScore());
        vo.setCalculatedGrade(result.getCalculatedGrade());
        vo.setCalculatedRank(result.getCalculatedRank());
        vo.setEvaluationConclusion(result.getEvaluationConclusion());
        vo.setResultStatus(result.getResultStatus());
        vo.setPublishedTime(result.getPublishedTime());
        return vo;
    }

    private ReviewObject requireObject(Long objectId) {
        if (objectId == null) {
            throw new ServiceException("评审对象ID不能为空");
        }
        ReviewObject object = objectMapper.selectById(objectId);
        if (object == null) {
            throw new ServiceException("评审对象不存在");
        }
        return object;
    }

    private ReviewActivity requireActivity(Long activityId) {
        if (activityId == null) {
            throw new ServiceException("评审活动ID不能为空");
        }
        ReviewActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new ServiceException("评审活动不存在");
        }
        return activity;
    }

    private Long requireCurrentUserId() {
        Long userId = currentUserId();
        if (userId == null) {
            throw new ServiceException("无法获取当前登录用户");
        }
        return userId;
    }

    private List<ReviewSubmissionPermission> currentUserPermissions(Long objectId) {
        ReviewSubmissionPermission query = new ReviewSubmissionPermission();
        query.setObjectId(objectId);
        query.setUserId(requireCurrentUserId());
        query.setStatus(ReviewPermissionStatus.ACTIVE.getCode());
        List<ReviewSubmissionPermission> permissions = permissionMapper.selectList(query);
        return permissions == null ? new ArrayList<ReviewSubmissionPermission>() : permissions;
    }

    private ReviewSubmissionPermission requireAnyPermission(Long objectId) {
        List<ReviewSubmissionPermission> permissions = currentUserPermissions(objectId);
        if (permissions.isEmpty()) {
            throw new ServiceException("无该评审对象填报权限");
        }
        return permissions.get(0);
    }

    private void requirePublishedResultPermission(ReviewObject object) {
        Long userId = requireCurrentUserId();
        if (!currentUserPermissions(object.getId()).isEmpty()) {
            return;
        }
        for (ReviewObjectMember member : selectMembers(object.getId())) {
            if (Objects.equals(userId, member.getUserId())
                    && (ReviewMemberRole.LEADER.getCode().equals(member.getMemberRole())
                    || ReviewMemberRole.CONTACT.getCode().equals(member.getMemberRole()))) {
                return;
            }
        }
        throw new ServiceException("无权查看该评审对象发布结果");
    }

    private ReviewSubmissionPermission requirePermission(Long objectId, String... permissionTypes) {
        List<ReviewSubmissionPermission> permissions = currentUserPermissions(objectId);
        for (ReviewSubmissionPermission permission : permissions) {
            if (hasPermissionType(permission.getPermissionType(), permissionTypes)) {
                return permission;
            }
        }
        throw new ServiceException("无该评审对象所需填报权限");
    }

    private boolean hasPermissionType(String permissionType, String... requiredTypes) {
        if (StringUtils.isEmpty(permissionType) || requiredTypes == null) {
            return false;
        }
        for (String requiredType : requiredTypes) {
            if (permissionType.equals(requiredType)) {
                return true;
            }
            if (ReviewPermissionType.EDIT_SUBMIT.getCode().equals(permissionType)
                    && (ReviewPermissionType.EDIT.getCode().equals(requiredType)
                    || ReviewPermissionType.SUBMIT.getCode().equals(requiredType))) {
                return true;
            }
        }
        return false;
    }

    private void requireEditableStatus(ReviewObject object) {
        if (!isEditableStatus(object.getSubmitStatus())) {
            throw new ServiceException("当前状态不允许编辑填报资料");
        }
    }

    private void requireSubmittableStatus(ReviewObject object) {
        if (!isSubmittableStatus(object.getSubmitStatus())) {
            throw new ServiceException("当前状态不允许提交评审资料");
        }
    }

    private boolean isEditableStatus(String status) {
        return ReviewObjectStatus.DRAFT.getCode().equals(status)
                || ReviewObjectStatus.WITHDRAW_APPROVED.getCode().equals(status);
    }

    private boolean isSubmittableStatus(String status) {
        return ReviewObjectStatus.DRAFT.getCode().equals(status)
                || ReviewObjectStatus.WITHDRAW_APPROVED.getCode().equals(status);
    }

    private boolean canEdit(ReviewObject object, ReviewActivity activity, ReviewSubmissionPermission permission) {
        return isEditableStatus(object.getSubmitStatus())
                && beforeOrNoDeadline(activity)
                && hasPermissionType(permission.getPermissionType(), ReviewPermissionType.EDIT.getCode(), ReviewPermissionType.EDIT_SUBMIT.getCode());
    }

    private boolean canSubmit(ReviewObject object, ReviewActivity activity, ReviewSubmissionPermission permission) {
        return isSubmittableStatus(object.getSubmitStatus())
                && beforeOrNoDeadline(activity)
                && hasPermissionType(permission.getPermissionType(), ReviewPermissionType.SUBMIT.getCode(), ReviewPermissionType.EDIT_SUBMIT.getCode());
    }

    private boolean canWithdraw(ReviewObject object, ReviewActivity activity, ReviewSubmissionPermission permission) {
        return ReviewObjectStatus.SUBMITTED.getCode().equals(object.getSubmitStatus())
                && beforeOrNoDeadline(activity)
                && permission != null;
    }

    private void ensureBeforeDeadline(ReviewActivity activity) {
        if (!beforeOrNoDeadline(activity)) {
            throw new ServiceException("已超过填报截止时间");
        }
    }

    private boolean beforeOrNoDeadline(ReviewActivity activity) {
        return activity == null || activity.getSubmitDeadline() == null
                || !DateUtils.getNowDate().after(activity.getSubmitDeadline());
    }

    private String buildWarning(ReviewObject object, ReviewActivity activity) {
        if (ReviewObjectStatus.LOCKED.getCode().equals(object.getSubmitStatus())) {
            return "评审对象已锁定，只能查看。";
        }
        if (ReviewObjectStatus.INVALID.getCode().equals(object.getSubmitStatus())) {
            return "评审对象已作废，只能查看。";
        }
        if (!beforeOrNoDeadline(activity)) {
            return "已超过填报截止时间，不能继续修改。";
        }
        if (ReviewObjectStatus.WITHDRAW_REQUESTED.getCode().equals(object.getSubmitStatus())) {
            return "撤回申请审核中。";
        }
        return null;
    }

    private void validateBeforeSubmit(ReviewObject object) {
        if (StringUtils.isEmpty(object.getObjectName())) {
            throw new ServiceException("项目名称不能为空");
        }
        if (StringUtils.isEmpty(object.getSummary())) {
            throw new ServiceException("项目摘要不能为空");
        }
        if (StringUtils.isEmpty(object.getContactPhone())) {
            throw new ServiceException("联系方式不能为空");
        }
        boolean hasLeader = false;
        for (ReviewObjectMember member : selectMembers(object.getId())) {
            if (ReviewMemberRole.LEADER.getCode().equals(member.getMemberRole())
                    || ReviewConstants.YES.equals(member.getIsPrimary())) {
                hasLeader = true;
                break;
            }
        }
        if (!hasLeader) {
            throw new ServiceException("至少需要一名负责人");
        }
    }

    private void touchPermissionUsedTime(Long objectId) {
        List<ReviewSubmissionPermission> permissions = currentUserPermissions(objectId);
        for (ReviewSubmissionPermission permission : permissions) {
            permission.setUsedTime(DateUtils.getNowDate());
            fillUpdateBase(permission);
            permissionMapper.update(permission);
        }
    }

    private List<ReviewObjectMember> selectMembers(Long objectId) {
        ReviewObjectMember query = new ReviewObjectMember();
        query.setObjectId(objectId);
        List<ReviewObjectMember> members = objectMemberMapper.selectList(query);
        return members == null ? new ArrayList<ReviewObjectMember>() : members;
    }

    private List<ReviewObjectMaterial> selectMaterials(Long objectId) {
        ReviewObjectMaterial query = new ReviewObjectMaterial();
        query.setObjectId(objectId);
        List<ReviewObjectMaterial> materials = materialMapper.selectList(query);
        return materials == null ? new ArrayList<ReviewObjectMaterial>() : materials;
    }

    private boolean shouldInvalidateOnClose(String status) {
        return ReviewObjectStatus.DRAFT.getCode().equals(status)
                || ReviewObjectStatus.WITHDRAW_APPROVED.getCode().equals(status)
                || ReviewObjectStatus.WITHDRAW_REQUESTED.getCode().equals(status);
    }

    private void writeStatusLog(ReviewObject object, String actionType, String beforeStatus,
                                String afterStatus, String reason) {
        ReviewObjectSubmitLog log = new ReviewObjectSubmitLog();
        log.setActivityId(object.getActivityId());
        log.setObjectId(object.getId());
        log.setActionType(actionType);
        log.setBeforeStatus(beforeStatus);
        log.setAfterStatus(afterStatus);
        log.setOperatorUserId(currentUserId());
        log.setOperatorName(currentUsername());
        log.setOperateTime(DateUtils.getNowDate());
        log.setActionReason(reason);
        fillCreateBase(log);
        submitLogMapper.insert(log);
    }

    private void writeAudit(ReviewObject object, String actionType, String content) {
        if (auditLogMapper == null) {
            return;
        }
        ReviewAuditLog log = new ReviewAuditLog();
        log.setActivityId(object.getActivityId());
        log.setObjectId(object.getId());
        log.setBizType(BIZ_TYPE_SUBMISSION);
        log.setBizId(String.valueOf(object.getId()));
        log.setActionType(actionType);
        log.setActionContent(content);
        log.setOperatorUserId(currentUserId());
        log.setOperatorName(currentUsername());
        log.setOperateTime(DateUtils.getNowDate());
        fillCreateBase(log);
        auditLogMapper.insert(log);
    }

    private String actionReason(ReviewSubmissionActionDTO dto, String defaultReason) {
        return dto == null || StringUtils.isEmpty(dto.getActionReason()) ? defaultReason : dto.getActionReason();
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
}
