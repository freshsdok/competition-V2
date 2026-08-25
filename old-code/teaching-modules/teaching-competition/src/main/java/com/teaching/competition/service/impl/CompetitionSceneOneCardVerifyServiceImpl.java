package com.teaching.competition.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionSceneCredentialScopeGrant;
import com.teaching.competition.domain.CompetitionSceneOneCardAction;
import com.teaching.competition.domain.CompetitionSceneOneCardCredentialSummary;
import com.teaching.competition.domain.CompetitionSceneOneCardScheduleActionGroup;
import com.teaching.competition.domain.CompetitionSceneOneCardVerifyReq;
import com.teaching.competition.domain.CompetitionSceneOneCardVerifyResult;
import com.teaching.competition.domain.CompetitionSceneOperationLog;
import com.teaching.competition.domain.CompetitionSceneSchedule;
import com.teaching.competition.domain.CompetitionSceneSubjectOperationState;
import com.teaching.competition.domain.CompetitionSceneSubjectOperationStateQuery;
import com.teaching.competition.mapper.CompetitionSceneCredentialMapper;
import com.teaching.competition.mapper.CompetitionSceneOperationLogMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleMapper;
import com.teaching.competition.service.ICompetitionSceneCredentialScopeGrantService;
import com.teaching.competition.service.ICompetitionSceneOneCardVerifyService;
import com.teaching.competition.service.ICompetitionSceneSubjectOperationStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 现场一证多权旁路扫码业务。
 */
@Service
public class CompetitionSceneOneCardVerifyServiceImpl implements ICompetitionSceneOneCardVerifyService {

    private static final String ACTION_REPORT = "REPORT";
    private static final String ACTION_MATERIAL_SELF = "MATERIAL_SELF";
    private static final String ACTION_MATERIAL_DELEGATE = "MATERIAL_DELEGATE";
    private static final String ACTION_WAITING = "WAITING";
    private static final String GRANT_STATUS_ACTIVE = "ACTIVE";
    private static final Integer DELETED_NO = 0;

    @Autowired
    private CompetitionSceneCredentialMapper credentialMapper;

    @Autowired
    private ICompetitionSceneCredentialScopeGrantService grantService;

    @Autowired
    private ICompetitionSceneSubjectOperationStateService operationStateService;

    @Autowired
    private CompetitionSceneOperationLogMapper operationLogMapper;

    @Autowired
    private CompetitionSceneScheduleMapper scheduleMapper;

    @Override
    public CompetitionSceneOneCardVerifyResult scan(CompetitionSceneOneCardVerifyReq req) {
        CompetitionSceneOneCardVerifyResult result = baseResult();
        CompetitionSceneCredential credential = null;
        try {
            String token = normalizeToken(req);
            if (StringUtils.isEmpty(token)) {
                fillFail(result, "二维码内容不能为空");
                return result;
            }
            credential = credentialMapper.selectCompetitionSceneCredentialByToken(token);
            String credentialCheck = validateCoreCredential(credential);
            if (credentialCheck != null) {
                fillFail(result, credentialCheck);
                return result;
            }
            List<CompetitionSceneCredentialScopeGrant> grants = grantService.findActiveGrantsByCredential(credential.getCredentialId());
            fillPreview(req, credential, grants, result);
            result.setOperationResult(CompetitionSceneConstants.OPERATION_RESULT_PASS);
            result.setResultMessage("旁路扫码预览通过");
            return result;
        } catch (Exception e) {
            result.setOperationResult(CompetitionSceneConstants.OPERATION_RESULT_EXCEPTION);
            result.setResultMessage("旁路扫码异常：" + e.getMessage());
            return result;
        } finally {
            safeWritePilotLog(req, credential, result, CompetitionSceneConstants.OPERATION_STAGE_SCAN, null, null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneOneCardVerifyResult confirm(CompetitionSceneOneCardVerifyReq req) {
        CompetitionSceneOneCardVerifyResult result = baseResult();
        CompetitionSceneCredential credential = null;
        CompetitionSceneCredentialScopeGrant grant = null;
        OperationContext context = null;
        try {
            if (req == null || req.getCredentialId() == null) {
                fillFail(result, "证件ID不能为空");
                return result;
            }
            if (StringUtils.isEmpty(req.getActionType())) {
                fillFail(result, "操作类型不能为空");
                return result;
            }
            credential = credentialMapper.selectCompetitionSceneCredentialById(req.getCredentialId());
            String credentialCheck = validateCoreCredential(credential);
            if (credentialCheck != null) {
                fillFail(result, credentialCheck);
                return result;
            }
            String actionType = normalizeActionType(req.getActionType());
            if (StringUtils.isEmpty(actionType)) {
                fillFail(result, "操作类型不支持");
                return result;
            }
            List<CompetitionSceneCredentialScopeGrant> grants = grantService.findActiveGrantsByCredential(credential.getCredentialId());
            context = buildConfirmContext(req, credential, grants, actionType);
            if (!context.allowed) {
                fillFail(result, context.message);
                safeFillPreview(req, credential, grants, result);
                return result;
            }
            grant = context.grant;

            CompetitionSceneSubjectOperationState existed =
                    operationStateService.selectDoneOperationState(context.query);
            if (existed != null) {
                safeFillPreview(req, credential, grants, result);
                result.setDelegateCredential(toSummary(context.delegateCredential));
                setStateResult(result, existed);
                result.setDuplicate(true);
                result.setAlreadyDone(true);
                result.setOperationResult(CompetitionSceneConstants.OPERATION_RESULT_DUPLICATE);
                result.setResultMessage("alreadyDone：重复操作，状态未变更");
                return result;
            }

            context.state.setRemark(buildStateInsertMarker(req, actionType));
            CompetitionSceneSubjectOperationState saved =
                    operationStateService.insertDoneOperationStateIfAbsent(context.state);
            if (saved == null) {
                throw new IllegalStateException("状态写入结果为空");
            }
            boolean insertedByThisConfirm = isStateInsertedByThisConfirm(context.state, saved);
            safeFillPreview(req, credential, grants, result);
            result.setDelegateCredential(toSummary(context.delegateCredential));
            setStateResult(result, saved);
            if (!insertedByThisConfirm) {
                result.setDuplicate(true);
                result.setAlreadyDone(true);
                result.setOperationResult(CompetitionSceneConstants.OPERATION_RESULT_DUPLICATE);
                result.setResultMessage("alreadyDone：并发或重复操作，状态未变更");
                return result;
            }
            result.setOperationResult(CompetitionSceneConstants.OPERATION_RESULT_PASS);
            result.setResultMessage("确认成功");
            LogWriteResult logResult = safeWritePilotLog(req, credential, result,
                    CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, context, grant);
            if (!logResult.success) {
                appendResultMessage(result, "日志写入失败但业务已完成");
            } else {
                SafeOperationResult updateResult = safeUpdateLastLogId(saved.getStateId(), logResult.logId);
                if (!updateResult.success) {
                    appendResultMessage(result, "状态日志关联更新失败但业务已完成");
                }
            }
            return result;
        } catch (Exception e) {
            markCurrentTransactionRollbackOnly();
            result.setOperationResult(CompetitionSceneConstants.OPERATION_RESULT_EXCEPTION);
            result.setResultMessage("旁路确认异常：" + e.getMessage());
            return result;
        } finally {
            if (!CompetitionSceneConstants.OPERATION_RESULT_PASS.equals(result.getOperationResult())) {
                safeWritePilotLog(req, credential, result, CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, context, grant);
            }
        }
    }

    private void fillPreview(CompetitionSceneOneCardVerifyReq req,
                             CompetitionSceneCredential credential,
                             List<CompetitionSceneCredentialScopeGrant> grants,
                             CompetitionSceneOneCardVerifyResult result) {
        result.setCredential(toSummary(credential));
        String operatorRole = normalizeTargetRole(req == null ? null : req.getOperatorRole(), true);
        String targetRole = resolveCredentialRole(credential, false);
        result.setOperatorRole(StringUtils.isEmpty(operatorRole) ? CompetitionSceneConstants.TARGET_ROLE_UNKNOWN : operatorRole);
        result.setOperatorRoleLabel(roleLabel(result.getOperatorRole()));
        result.setTargetRole(targetRole);
        result.setTargetRoleLabel(roleLabel(targetRole));

        CompetitionSceneSubjectOperationState reportState = selectState(credential,
                CompetitionSceneConstants.SCOPE_TYPE_COMPETITION,
                credential.getCompetitionSeriesId(),
                resolveSubjectType(credential, false),
                resolveSubjectCode(credential, false),
                CompetitionSceneConstants.STATE_OPERATION_REPORT);
        CompetitionSceneSubjectOperationState materialState = null;
        if (CompetitionSceneConstants.SUBJECT_TYPE_USER.equals(credential.getSubjectType())) {
            materialState = selectState(credential,
                    CompetitionSceneConstants.SCOPE_TYPE_COMPETITION,
                    credential.getCompetitionSeriesId(),
                    CompetitionSceneConstants.SUBJECT_TYPE_USER,
                    resolveSubjectCode(credential, true),
                    CompetitionSceneConstants.STATE_OPERATION_MATERIAL);
        }
        result.setReportState(reportState);
        result.setMaterialState(materialState);

        List<CompetitionSceneOneCardAction> allowedActions = new ArrayList<>();
        List<CompetitionSceneOneCardAction> competitionActions = new ArrayList<>();
        if (hasCredentialAbility(credential, "report") && hasCheckinRole(operatorRole)) {
            CompetitionSceneOneCardAction action = buildAction(ACTION_REPORT, "大赛报到",
                    isStateDone(reportState), "已完成大赛报到", "确认该人员完成大赛报到");
            competitionActions.add(action);
            allowedActions.add(action);
        }
        if (hasCredentialAbility(credential, "material")
                && hasMaterialRole(operatorRole)
                && CompetitionSceneConstants.SUBJECT_TYPE_USER.equals(credential.getSubjectType())) {
            CompetitionSceneOneCardAction self = buildAction(ACTION_MATERIAL_SELF, "本人资料领取",
                    isStateDone(materialState), "已完成资料领取", "确认本人完成资料领取");
            CompetitionSceneOneCardAction delegate = buildAction(ACTION_MATERIAL_DELEGATE, "同队代领资料",
                    isStateDone(materialState), "已完成资料领取", "确认同队成员代领资料");
            competitionActions.add(self);
            competitionActions.add(delegate);
            allowedActions.add(self);
            allowedActions.add(delegate);
        }

        List<CompetitionSceneOneCardScheduleActionGroup> scheduleGroups = buildScheduleGroups(credential, grants,
                req == null ? null : req.getCurrentScheduleId());
        result.setScheduleActionGroups(scheduleGroups);

        if (req != null && req.getCurrentScheduleId() != null) {
            ScheduleGrantResolveResult grantResult = resolveScheduleGrant(grants, req.getCurrentScheduleId());
            if (!grantResult.available()) {
                result.setMatrixMessage(grantResult.message);
            } else if (!hasWaitingTargetRole(credential)) {
                result.setMatrixMessage("被扫对象角色不允许候场");
            } else if (grantService.hasAbility(grantResult.grant, "waiting") && hasCheckinRole(operatorRole)) {
                CompetitionSceneSubjectOperationState waitingState = selectState(credential,
                        CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE,
                        req.getCurrentScheduleId(),
                        resolveSubjectType(credential, false),
                        resolveSubjectCode(credential, false),
                        CompetitionSceneConstants.STATE_OPERATION_WAITING);
                result.setWaitingState(waitingState);
                CompetitionSceneOneCardAction waiting = buildAction(ACTION_WAITING, "候场确认",
                        isStateDone(waitingState), "已完成候场确认", "确认该人员进入候场");
                waiting.setScheduleId(req.getCurrentScheduleId());
                waiting.setGrantId(grantResult.grant.getGrantId());
                fillScheduleActionText(waiting, grantResult.grant);
                allowedActions.add(waiting);
            }
        } else if (!scheduleGroups.isEmpty()) {
            result.setMatrixMessage("请选择具体赛场后再确认候场");
        }

        result.setCompetitionActions(competitionActions);
        result.setAllowedActions(allowedActions);
        if (StringUtils.isEmpty(result.getMatrixMessage())) {
            if (allowedActions.isEmpty()) {
                result.setMatrixMessage("当前扫码角色对该证件暂无可执行动作");
            } else {
                result.setMatrixMessage("请选择可执行的现场操作");
            }
        }
    }

    private OperationContext buildConfirmContext(CompetitionSceneOneCardVerifyReq req,
                                                 CompetitionSceneCredential credential,
                                                 List<CompetitionSceneCredentialScopeGrant> grants,
                                                 String actionType) {
        OperationContext context = new OperationContext();
        context.actionType = actionType;
        String operatorRole = normalizeTargetRole(req.getOperatorRole(), true);
        if (ACTION_REPORT.equals(actionType)) {
            if (!hasCredentialAbility(credential, "report")) {
                return context.fail("证件不具备报道能力");
            }
            if (!hasCheckinRole(operatorRole)) {
                return context.fail("当前操作员无报道权限");
            }
            return context.allow(buildStateContext(credential,
                    CompetitionSceneConstants.SCOPE_TYPE_COMPETITION,
                    credential.getCompetitionSeriesId(),
                    resolveSubjectType(credential, false),
                    resolveSubjectCode(credential, false),
                    CompetitionSceneConstants.STATE_OPERATION_REPORT,
                    null,
                    null,
                    null,
                    null));
        }
        if (ACTION_MATERIAL_SELF.equals(actionType) || ACTION_MATERIAL_DELEGATE.equals(actionType)) {
            if (!hasCredentialAbility(credential, "material")) {
                return context.fail("证件不具备资料领取能力");
            }
            if (!hasMaterialRole(operatorRole)) {
                return context.fail("当前操作员无资料领取权限");
            }
            if (!CompetitionSceneConstants.SUBJECT_TYPE_USER.equals(credential.getSubjectType())) {
                return context.fail("资料领取仅支持用户主体");
            }
            DelegateContext delegate = ACTION_MATERIAL_SELF.equals(actionType)
                    ? buildSelfDelegate(credential) : buildTeamDelegate(req, credential);
            if (!delegate.allowed) {
                return context.fail(delegate.message);
            }
            context.delegateCredential = delegate.delegateCredential;
            return context.allow(buildStateContext(credential,
                    CompetitionSceneConstants.SCOPE_TYPE_COMPETITION,
                    credential.getCompetitionSeriesId(),
                    CompetitionSceneConstants.SUBJECT_TYPE_USER,
                    resolveSubjectCode(credential, true),
                    CompetitionSceneConstants.STATE_OPERATION_MATERIAL,
                    delegate.delegateUserId,
                    delegate.delegateName,
                    delegate.delegateCredentialId,
                    delegate.delegateRelation));
        }
        if (ACTION_WAITING.equals(actionType)) {
            if (req.getCurrentScheduleId() == null) {
                return context.fail("候场确认必须指定赛场安排ID");
            }
            if (!hasWaitingTargetRole(credential)) {
                return context.fail("被扫对象角色不允许候场");
            }
            ScheduleGrantResolveResult grantResult = resolveScheduleGrant(grants, req.getCurrentScheduleId());
            if (!grantResult.available()) {
                return context.fail(grantResult.message);
            }
            CompetitionSceneCredentialScopeGrant grant = grantResult.grant;
            if (!grantService.hasAbility(grant, "waiting")) {
                return context.fail("当前赛场授权不具备候场能力");
            }
            if (!hasCheckinRole(operatorRole)) {
                return context.fail("当前操作员无候场确认权限");
            }
            context.grant = grant;
            return context.allow(buildStateContext(credential,
                    CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE,
                    req.getCurrentScheduleId(),
                    resolveSubjectType(credential, false),
                    resolveSubjectCode(credential, false),
                    CompetitionSceneConstants.STATE_OPERATION_WAITING,
                    null,
                    null,
                    null,
                    null));
        }
        return context.fail("操作类型不支持");
    }

    private OperationStateContext buildStateContext(CompetitionSceneCredential credential,
                                                    String scopeType,
                                                    Long scopeRefId,
                                                    String subjectType,
                                                    String subjectCode,
                                                    String operationType,
                                                    Long delegateUserId,
                                                    String delegateName,
                                                    Long delegateCredentialId,
                                                    String delegateRelation) {
        OperationStateContext stateContext = new OperationStateContext();
        stateContext.query = new CompetitionSceneSubjectOperationStateQuery();
        stateContext.query.setCompetitionSeriesId(credential.getCompetitionSeriesId());
        stateContext.query.setScopeType(scopeType);
        stateContext.query.setScopeRefId(scopeRefId);
        stateContext.query.setSubjectType(subjectType);
        stateContext.query.setSubjectCode(subjectCode);
        stateContext.query.setOperationType(operationType);

        Date now = DateUtils.getNowDate();
        stateContext.state = new CompetitionSceneSubjectOperationState();
        stateContext.state.setCompetitionSeriesId(credential.getCompetitionSeriesId());
        stateContext.state.setScopeType(scopeType);
        stateContext.state.setScopeRefId(scopeRefId);
        stateContext.state.setSubjectType(subjectType);
        stateContext.state.setSubjectCode(subjectCode);
        stateContext.state.setOperationType(operationType);
        stateContext.state.setOperationStatus(CompetitionSceneConstants.STATE_STATUS_DONE);
        stateContext.state.setOperationTime(now);
        stateContext.state.setCredentialId(credential.getCredentialId());
        stateContext.state.setOperatorUserId(currentUserId());
        stateContext.state.setOperatorName(currentUsername());
        stateContext.state.setDelegateUserId(delegateUserId);
        stateContext.state.setDelegateName(delegateName);
        stateContext.state.setDelegateCredentialId(delegateCredentialId);
        stateContext.state.setDelegateRelation(delegateRelation);
        stateContext.state.setCreateBy(currentUsername());
        stateContext.state.setUpdateBy(currentUsername());
        stateContext.state.setCreateTime(now);
        stateContext.state.setUpdateTime(now);
        stateContext.state.setDeleted(CompetitionSceneConstants.STATE_DELETED_NO);
        return stateContext;
    }

    private DelegateContext buildSelfDelegate(CompetitionSceneCredential credential) {
        DelegateContext context = new DelegateContext();
        context.allowed = true;
        context.delegateCredential = credential;
        context.delegateUserId = credential.getUserId();
        context.delegateName = credential.getUserName();
        context.delegateCredentialId = credential.getCredentialId();
        context.delegateRelation = CompetitionSceneConstants.DELEGATE_RELATION_SELF;
        return context;
    }

    private DelegateContext buildTeamDelegate(CompetitionSceneOneCardVerifyReq req,
                                              CompetitionSceneCredential credential) {
        DelegateContext context = new DelegateContext();
        if (req.getDelegateCredentialId() == null) {
            return context.fail("代领人证件ID不能为空");
        }
        if (Objects.equals(req.getDelegateCredentialId(), credential.getCredentialId())) {
            return context.fail("本人领取必须使用本人资料领取");
        }
        if (credential.getUserId() == null) {
            return context.fail("被领取人用户ID不能为空");
        }
        CompetitionSceneCredential delegate = credentialMapper.selectCompetitionSceneCredentialById(req.getDelegateCredentialId());
        String delegateCheck = validateCoreCredential(delegate);
        if (delegateCheck != null) {
            return context.fail("代领人" + delegateCheck);
        }
        if (!CompetitionSceneConstants.SUBJECT_TYPE_USER.equals(delegate.getSubjectType())) {
            return context.fail("代领人必须是用户主体");
        }
        if (delegate.getUserId() == null) {
            return context.fail("代领人用户ID不能为空");
        }
        if (Objects.equals(credential.getUserId(), delegate.getUserId())) {
            return context.fail("本人领取必须使用本人资料领取");
        }
        if (StringUtils.isEmpty(credential.getTeamCode()) || StringUtils.isEmpty(delegate.getTeamCode())) {
            return context.fail("资料代领必须双方都有团队信息");
        }
        if (!Objects.equals(credential.getTeamCode(), delegate.getTeamCode())) {
            return context.fail("代领人与被领取人不属于同一团队");
        }
        context.allowed = true;
        context.delegateCredential = delegate;
        context.delegateUserId = delegate.getUserId();
        context.delegateName = delegate.getUserName();
        context.delegateCredentialId = delegate.getCredentialId();
        context.delegateRelation = CompetitionSceneConstants.DELEGATE_RELATION_TEAM_MEMBER;
        return context;
    }

    private List<CompetitionSceneOneCardScheduleActionGroup> buildScheduleGroups(CompetitionSceneCredential credential,
                                                                                 List<CompetitionSceneCredentialScopeGrant> grants,
                                                                                 Long currentScheduleId) {
        List<CompetitionSceneOneCardScheduleActionGroup> groups = new ArrayList<>();
        if (grants == null) {
            return groups;
        }
        for (CompetitionSceneCredentialScopeGrant grant : grants) {
            if (!isActiveScheduleGrant(grant)) {
                continue;
            }
            CompetitionSceneOneCardScheduleActionGroup group = new CompetitionSceneOneCardScheduleActionGroup();
            group.setScheduleId(grant.getScopeRefId());
            group.setGrantId(grant.getGrantId());
            group.setScheduleName(snapshotValue(grant, "scheduleName", scheduleName(grant.getScopeRefId())));
            group.setScheduleTime(scheduleTime(grant));
            group.setScheduleLocation(scheduleLocation(grant));
            CompetitionSceneSubjectOperationState waitingState = selectState(credential,
                    CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE,
                    grant.getScopeRefId(),
                    resolveSubjectType(credential, false),
                    resolveSubjectCode(credential, false),
                    CompetitionSceneConstants.STATE_OPERATION_WAITING);
            group.setWaitingStatus(isStateDone(waitingState) ? CompetitionSceneConstants.DONE_YES : CompetitionSceneConstants.DONE_NO);
            group.setWaitingTime(formatStateTime(waitingState));
            if (currentScheduleId == null || !Objects.equals(currentScheduleId, grant.getScopeRefId())) {
                group.setActions(List.of());
            } else {
                group.setActions(new ArrayList<>());
            }
            groups.add(group);
        }
        return groups;
    }

    private ScheduleGrantResolveResult resolveScheduleGrant(List<CompetitionSceneCredentialScopeGrant> grants,
                                                            Long scheduleId) {
        if (scheduleId == null) {
            return ScheduleGrantResolveResult.fail("候场确认必须指定赛场安排ID");
        }
        List<CompetitionSceneCredentialScopeGrant> matched = new ArrayList<>();
        if (grants != null) {
            for (CompetitionSceneCredentialScopeGrant grant : grants) {
                if (isActiveScheduleGrant(grant) && Objects.equals(grant.getScopeRefId(), scheduleId)) {
                    matched.add(grant);
                }
            }
        }
        if (matched.isEmpty()) {
            return ScheduleGrantResolveResult.fail("证件无当前赛场候场权限");
        }
        if (matched.size() > 1) {
            return ScheduleGrantResolveResult.fail("当前赛场授权存在多条，请联系管理员处理");
        }
        return ScheduleGrantResolveResult.ok(matched.get(0));
    }

    private boolean isActiveScheduleGrant(CompetitionSceneCredentialScopeGrant grant) {
        return grant != null
                && CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE.equals(grant.getScopeType())
                && GRANT_STATUS_ACTIVE.equals(grant.getGrantStatus())
                && DELETED_NO.equals(grant.getDeleted())
                && isWithinGrantWindow(grant, DateUtils.getNowDate());
    }

    private boolean isWithinGrantWindow(CompetitionSceneCredentialScopeGrant grant, Date now) {
        if (grant.getValidFrom() != null && grant.getValidFrom().after(now)) {
            return false;
        }
        return grant.getValidTo() == null || !grant.getValidTo().before(now);
    }

    private CompetitionSceneSubjectOperationState selectState(CompetitionSceneCredential credential,
                                                              String scopeType,
                                                              Long scopeRefId,
                                                              String subjectType,
                                                              String subjectCode,
                                                              String operationType) {
        if (credential == null || credential.getCompetitionSeriesId() == null
                || scopeRefId == null || StringUtils.isEmpty(subjectType) || StringUtils.isEmpty(subjectCode)) {
            return null;
        }
        CompetitionSceneSubjectOperationStateQuery query = new CompetitionSceneSubjectOperationStateQuery();
        query.setCompetitionSeriesId(credential.getCompetitionSeriesId());
        query.setScopeType(scopeType);
        query.setScopeRefId(scopeRefId);
        query.setSubjectType(subjectType);
        query.setSubjectCode(subjectCode);
        query.setOperationType(operationType);
        return operationStateService.selectDoneOperationState(query);
    }

    private CompetitionSceneOneCardAction buildAction(String actionType,
                                                      String label,
                                                      boolean done,
                                                      String doneMessage,
                                                      String pendingMessage) {
        CompetitionSceneOneCardAction action = new CompetitionSceneOneCardAction();
        action.setActionType(actionType);
        action.setActionLabel(label);
        action.setActionKind(CompetitionSceneConstants.ACTION_KIND_CONFIRM);
        action.setEnabled(!done);
        action.setAlreadyDone(done);
        action.setStatus(done ? CompetitionSceneConstants.ACTION_STATUS_DONE : CompetitionSceneConstants.ACTION_STATUS_PENDING);
        action.setMessage(done ? doneMessage : pendingMessage);
        return action;
    }

    private void fillScheduleActionText(CompetitionSceneOneCardAction action,
                                        CompetitionSceneCredentialScopeGrant grant) {
        action.setScheduleName(snapshotValue(grant, "scheduleName", scheduleName(grant.getScopeRefId())));
        action.setScheduleTime(scheduleTime(grant));
        action.setScheduleLocation(scheduleLocation(grant));
    }

    private boolean hasCredentialAbility(CompetitionSceneCredential credential, String abilityCode) {
        if (credential == null || StringUtils.isEmpty(abilityCode) || StringUtils.isEmpty(credential.getAbilityJson())) {
            return false;
        }
        try {
            JSONObject ability = JSON.parseObject(credential.getAbilityJson());
            Object value = ability == null ? null : ability.get(abilityCode);
            return value instanceof Boolean && Boolean.TRUE.equals(value);
        } catch (Exception ignored) {
            return false;
        }
    }

    private String validateCoreCredential(CompetitionSceneCredential credential) {
        if (credential == null) {
            return "证件信息不存在或二维码无效，请联系现场工作人员";
        }
        if (credential.getCredentialId() == null) {
            return "证件ID不能为空";
        }
        if (!CompetitionSceneConstants.DEL_FLAG_NORMAL.equals(credential.getDelFlag())) {
            return "证件已删除或不可用，请联系现场工作人员";
        }
        if (credential.getCompetitionSeriesId() == null) {
            return "证件所属大赛不能为空";
        }
        if (StringUtils.isEmpty(credential.getSubjectType())) {
            return "证件人员类型不能为空";
        }
        if (StringUtils.isEmpty(credential.getSubjectCode())) {
            return "证件主体编码不能为空";
        }
        if (!CompetitionSceneConstants.SCOPE_TYPE_COMPETITION.equals(credential.getScopeType())) {
            return "不是一证多权核心证件，请联系现场工作人员";
        }
        if (!CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE.equals(credential.getCredentialStatus())) {
            return "证件不是有效状态，请联系现场工作人员";
        }
        Date now = DateUtils.getNowDate();
        if (credential.getValidFrom() != null && credential.getValidFrom().after(now)) {
            return "证件尚未生效，请联系现场工作人员";
        }
        if (credential.getValidTo() != null && credential.getValidTo().before(now)) {
            return "证件信息有误，请联系现场工作人员";
        }
        return null;
    }

    private String normalizeToken(CompetitionSceneOneCardVerifyReq req) {
        String raw = null;
        if (req != null) {
            raw = StringUtils.isNotEmpty(req.getCredentialToken()) ? req.getCredentialToken() : req.getQrContent();
        }
        if (StringUtils.isEmpty(raw)) {
            return null;
        }
        raw = raw.trim();
        if (raw.startsWith("{")) {
            try {
                JSONObject jsonObject = JSON.parseObject(raw);
                String token = jsonObject.getString("credentialToken");
                if (StringUtils.isEmpty(token)) {
                    token = jsonObject.getString("token");
                }
                if (StringUtils.isEmpty(token)) {
                    return normalizeContent(jsonObject.getString("qrContent"));
                }
                return normalizeContent(token);
            } catch (Exception ignored) {
                return normalizeContent(raw);
            }
        }
        return normalizeContent(raw);
    }

    private String normalizeContent(String raw) {
        if (StringUtils.isEmpty(raw)) {
            return null;
        }
        String value = raw.trim();
        int queryIndex = value.indexOf("credentialToken=");
        if (queryIndex >= 0) {
            value = value.substring(queryIndex + "credentialToken=".length());
        }
        int prefixIndex = value.indexOf(CompetitionSceneConstants.QR_CONTENT_PREFIX);
        if (prefixIndex >= 0) {
            value = value.substring(prefixIndex + CompetitionSceneConstants.QR_CONTENT_PREFIX.length());
        }
        int endIndex = value.indexOf('&');
        if (endIndex >= 0) {
            value = value.substring(0, endIndex);
        }
        return value;
    }

    private String normalizeActionType(String actionType) {
        if (StringUtils.isEmpty(actionType)) {
            return null;
        }
        String value = actionType.trim().toUpperCase();
        if (ACTION_REPORT.equals(value)) {
            return ACTION_REPORT;
        }
        if (ACTION_MATERIAL_SELF.equals(value)) {
            return ACTION_MATERIAL_SELF;
        }
        if (ACTION_MATERIAL_DELEGATE.equals(value)) {
            return ACTION_MATERIAL_DELEGATE;
        }
        if (ACTION_WAITING.equals(value)) {
            return ACTION_WAITING;
        }
        return null;
    }

    private String resolveSubjectType(CompetitionSceneCredential credential, boolean material) {
        if (material) {
            return CompetitionSceneConstants.SUBJECT_TYPE_USER;
        }
        if (StringUtils.isNotEmpty(credential.getSubjectType())) {
            return credential.getSubjectType();
        }
        return CompetitionSceneConstants.SUBJECT_TYPE_USER;
    }

    private String resolveSubjectCode(CompetitionSceneCredential credential, boolean material) {
        if (material && credential.getUserId() != null) {
            return String.valueOf(credential.getUserId());
        }
        if (StringUtils.isNotEmpty(credential.getSubjectCode())) {
            return credential.getSubjectCode();
        }
        if (credential.getUserId() != null) {
            return String.valueOf(credential.getUserId());
        }
        return credential.getMemberId() == null ? null : "MEMBER:" + credential.getMemberId();
    }

    private String resolveCredentialRole(CompetitionSceneCredential credential, boolean allowUnknown) {
        if (credential == null) {
            return allowUnknown ? CompetitionSceneConstants.TARGET_ROLE_UNKNOWN : CompetitionSceneConstants.TARGET_ROLE_MEMBER;
        }
        String role = normalizeTargetRole(credential.getCompetitionRoleName(), true);
        if (StringUtils.isNotEmpty(role)) {
            return role;
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_TEACHER.equals(credential.getCredentialType())) {
            return CompetitionSceneConstants.TARGET_ROLE_TEACHER;
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credential.getCredentialType())) {
            return CompetitionSceneConstants.TARGET_ROLE_EXPERT;
        }
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_STAFF.equals(credential.getCredentialType())) {
            return CompetitionSceneConstants.TARGET_ROLE_STAFF;
        }
        return allowUnknown ? CompetitionSceneConstants.TARGET_ROLE_UNKNOWN : CompetitionSceneConstants.TARGET_ROLE_MEMBER;
    }

    private String normalizeTargetRole(String role, boolean allowUnknown) {
        if (StringUtils.isEmpty(role)) {
            return allowUnknown ? null : CompetitionSceneConstants.TARGET_ROLE_MEMBER;
        }
        String value = role.trim();
        String upper = value.toUpperCase();
        switch (upper) {
            case CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF:
                return CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF;
            case CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF:
                return CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF;
            case CompetitionSceneConstants.TARGET_ROLE_STAFF:
                return CompetitionSceneConstants.TARGET_ROLE_STAFF;
            case CompetitionSceneConstants.TARGET_ROLE_TEACHER:
                return CompetitionSceneConstants.TARGET_ROLE_TEACHER;
            case CompetitionSceneConstants.TARGET_ROLE_EXPERT:
                return CompetitionSceneConstants.TARGET_ROLE_EXPERT;
            case CompetitionSceneConstants.TARGET_ROLE_CAPTAIN:
                return CompetitionSceneConstants.TARGET_ROLE_CAPTAIN;
            case CompetitionSceneConstants.TARGET_ROLE_MEMBER:
                return CompetitionSceneConstants.TARGET_ROLE_MEMBER;
            default:
                break;
        }
        switch (value) {
            case "签到工作人员":
            case "签到员":
                return CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF;
            case "发资料工作人员":
            case "资料工作人员":
            case "资料员":
                return CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF;
            case "工作人员":
            case "现场工作人员":
                return CompetitionSceneConstants.TARGET_ROLE_STAFF;
            case "教师":
            case "指导教师":
                return CompetitionSceneConstants.TARGET_ROLE_TEACHER;
            case "专家":
            case "评委":
                return CompetitionSceneConstants.TARGET_ROLE_EXPERT;
            case "队长":
                return CompetitionSceneConstants.TARGET_ROLE_CAPTAIN;
            case "队员":
            case "成员":
                return CompetitionSceneConstants.TARGET_ROLE_MEMBER;
            default:
                return allowUnknown ? null : CompetitionSceneConstants.TARGET_ROLE_MEMBER;
        }
    }

    private boolean hasCheckinRole(String role) {
        return CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF.equals(role)
                || CompetitionSceneConstants.TARGET_ROLE_STAFF.equals(role);
    }

    private boolean hasMaterialRole(String role) {
        return CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF.equals(role)
                || CompetitionSceneConstants.TARGET_ROLE_STAFF.equals(role);
    }

    private boolean hasWaitingTargetRole(CompetitionSceneCredential credential) {
        String targetRole = resolveCredentialRole(credential, false);
        return CompetitionSceneConstants.TARGET_ROLE_MEMBER.equals(targetRole)
                || CompetitionSceneConstants.TARGET_ROLE_CAPTAIN.equals(targetRole);
    }

    private String roleLabel(String role) {
        if (CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF.equals(role)) {
            return "签到工作人员";
        }
        if (CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF.equals(role)) {
            return "资料工作人员";
        }
        if (CompetitionSceneConstants.TARGET_ROLE_STAFF.equals(role)) {
            return "现场工作人员";
        }
        if (CompetitionSceneConstants.TARGET_ROLE_TEACHER.equals(role)) {
            return "教师";
        }
        if (CompetitionSceneConstants.TARGET_ROLE_EXPERT.equals(role)) {
            return "专家";
        }
        if (CompetitionSceneConstants.TARGET_ROLE_CAPTAIN.equals(role)) {
            return "队长";
        }
        if (CompetitionSceneConstants.TARGET_ROLE_MEMBER.equals(role)) {
            return "队员";
        }
        return "未配置";
    }

    private CompetitionSceneOneCardCredentialSummary toSummary(CompetitionSceneCredential credential) {
        if (credential == null) {
            return null;
        }
        CompetitionSceneOneCardCredentialSummary summary = new CompetitionSceneOneCardCredentialSummary();
        summary.setCredentialId(credential.getCredentialId());
        summary.setCredentialNo(credential.getCredentialNo());
        summary.setCredentialType(credential.getCredentialType());
        summary.setCredentialName(credential.getCredentialName());
        summary.setIssueChannel(credential.getIssueChannel());
        summary.setScopeType(credential.getScopeType());
        summary.setScopeRefId(credential.getScopeRefId());
        summary.setCompetitionSeriesId(credential.getCompetitionSeriesId());
        summary.setCompetitionName(credential.getCompetitionName());
        summary.setSubjectType(credential.getSubjectType());
        summary.setSubjectCode(credential.getSubjectCode());
        summary.setUserId(credential.getUserId());
        summary.setUserName(credential.getUserName());
        summary.setTeamCode(credential.getTeamCode());
        summary.setTeamName(credential.getTeamName());
        summary.setCompetitionRoleName(credential.getCompetitionRoleName());
        return summary;
    }

    private void setStateResult(CompetitionSceneOneCardVerifyResult result,
                                CompetitionSceneSubjectOperationState state) {
        if (state == null) {
            return;
        }
        if (CompetitionSceneConstants.STATE_OPERATION_REPORT.equals(state.getOperationType())) {
            result.setReportState(state);
        } else if (CompetitionSceneConstants.STATE_OPERATION_MATERIAL.equals(state.getOperationType())) {
            result.setMaterialState(state);
        } else if (CompetitionSceneConstants.STATE_OPERATION_WAITING.equals(state.getOperationType())) {
            result.setWaitingState(state);
        }
    }

    private boolean isStateDone(CompetitionSceneSubjectOperationState state) {
        return state != null && CompetitionSceneConstants.STATE_STATUS_DONE.equals(state.getOperationStatus());
    }

    private String formatStateTime(CompetitionSceneSubjectOperationState state) {
        if (state == null || state.getOperationTime() == null) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(state.getOperationTime());
    }

    private String snapshotValue(CompetitionSceneCredentialScopeGrant grant, String key, String fallback) {
        if (grant == null || StringUtils.isEmpty(grant.getGrantSnapshotJson())) {
            return fallback;
        }
        try {
            JSONObject snapshot = JSON.parseObject(grant.getGrantSnapshotJson());
            String value = snapshot == null ? null : snapshot.getString(key);
            return StringUtils.isNotEmpty(value) ? value : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String scheduleName(Long scheduleId) {
        if (scheduleId == null) {
            return null;
        }
        CompetitionSceneSchedule schedule = scheduleMapper.selectCompetitionSceneScheduleById(scheduleId);
        return schedule == null ? null : schedule.getScheduleName();
    }

    private String scheduleTime(CompetitionSceneCredentialScopeGrant grant) {
        if (grant == null || StringUtils.isEmpty(grant.getOperationWindowJson())) {
            return null;
        }
        try {
            JSONObject root = JSON.parseObject(grant.getOperationWindowJson());
            JSONObject waiting = root == null ? null : root.getJSONObject("waiting");
            if (waiting == null) {
                return null;
            }
            return joinText(waiting.getString("startTime"), waiting.getString("endTime"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String scheduleLocation(CompetitionSceneCredentialScopeGrant grant) {
        if (grant == null || StringUtils.isEmpty(grant.getOperationWindowJson())) {
            return null;
        }
        try {
            JSONObject root = JSON.parseObject(grant.getOperationWindowJson());
            JSONObject waiting = root == null ? null : root.getJSONObject("waiting");
            return waiting == null ? null : waiting.getString("location");
        } catch (Exception ignored) {
            return null;
        }
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
                builder.append(" - ");
            }
            builder.append(value);
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private boolean safeFillPreview(CompetitionSceneOneCardVerifyReq req,
                                    CompetitionSceneCredential credential,
                                    List<CompetitionSceneCredentialScopeGrant> grants,
                                    CompetitionSceneOneCardVerifyResult result) {
        try {
            fillPreview(req, credential, grants, result);
            return true;
        } catch (Exception e) {
            result.setCredential(toSummary(credential));
            result.setMatrixMessage("动作矩阵刷新失败");
            return false;
        }
    }

    private LogWriteResult safeWritePilotLog(CompetitionSceneOneCardVerifyReq req,
                                             CompetitionSceneCredential credential,
                                             CompetitionSceneOneCardVerifyResult result,
                                             String stage,
                                             OperationContext context,
                                             CompetitionSceneCredentialScopeGrant grant) {
        try {
            Long logId = writePilotLog(req, credential, result, stage, context, grant);
            return LogWriteResult.success(logId);
        } catch (Exception e) {
            return LogWriteResult.fail(e.getMessage());
        }
    }

    private SafeOperationResult safeUpdateLastLogId(Long stateId, Long logId) {
        if (stateId == null || logId == null) {
            return SafeOperationResult.success();
        }
        try {
            operationStateService.updateLastLogId(stateId, logId);
            return SafeOperationResult.success();
        } catch (Exception e) {
            return SafeOperationResult.fail(e.getMessage());
        }
    }

    private void appendResultMessage(CompetitionSceneOneCardVerifyResult result, String message) {
        if (result == null || StringUtils.isEmpty(message)) {
            return;
        }
        if (StringUtils.isEmpty(result.getResultMessage())) {
            result.setResultMessage(message);
        } else if (!result.getResultMessage().contains(message)) {
            result.setResultMessage(result.getResultMessage() + "；" + message);
        }
    }

    private String buildStateInsertMarker(CompetitionSceneOneCardVerifyReq req, String actionType) {
        String key = req == null ? null : req.getIdempotencyKey();
        return "ONE_CARD_CONFIRM:" + actionType + ":" + DateUtils.dateTimeNow()
                + ":" + UUID.randomUUID() + (StringUtils.isEmpty(key) ? "" : ":IDEMPOTENT");
    }

    private boolean isStateInsertedByThisConfirm(CompetitionSceneSubjectOperationState requested,
                                                 CompetitionSceneSubjectOperationState saved) {
        return requested != null
                && saved != null
                && StringUtils.isNotEmpty(requested.getRemark())
                && Objects.equals(requested.getRemark(), saved.getRemark());
    }

    private void markCurrentTransactionRollbackOnly() {
        try {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } catch (Exception ignored) {
            // Unit tests instantiate this service directly, outside a Spring transaction proxy.
        }
    }

    private Long writePilotLog(CompetitionSceneOneCardVerifyReq req,
                               CompetitionSceneCredential credential,
                               CompetitionSceneOneCardVerifyResult result,
                               String stage,
                               OperationContext context,
                               CompetitionSceneCredentialScopeGrant grant) {
        CompetitionSceneOperationLog log = new CompetitionSceneOperationLog();
        if (credential != null) {
            log.setCredentialId(credential.getCredentialId());
            log.setScheduleId(req == null ? null : req.getCurrentScheduleId());
            log.setCompetitionSeriesId(credential.getCompetitionSeriesId());
            log.setCredentialNo(credential.getCredentialNo());
            log.setTeamCode(credential.getTeamCode());
            log.setTeamName(credential.getTeamName());
            log.setMemberId(credential.getMemberId());
            log.setUserId(credential.getUserId());
            log.setUserName(credential.getUserName());
            log.setCompetitionTrackId(credential.getCompetitionTrackId());
            log.setCompetitionTrackName(credential.getCompetitionTrackName());
            log.setSecondLevelCode(credential.getSecondLevelCode());
            log.setSecondLevelName(credential.getSecondLevelName());
        }
        log.setOperationType(resolveLogOperationType(req));
        log.setOperationStage(stage);
        log.setOperationResult(result == null ? CompetitionSceneConstants.OPERATION_RESULT_EXCEPTION : result.getOperationResult());
        log.setResultMessage(result == null ? null : result.getResultMessage());
        log.setScheduleCheckResult(CompetitionSceneConstants.CHECK_RESULT_SKIP);
        log.setApplyCheckResult(CompetitionSceneConstants.CHECK_RESULT_SKIP);
        log.setIdentityCheckResult(CompetitionSceneConstants.CHECK_RESULT_SKIP);
        log.setScanIp(req == null ? null : req.getScanIp());
        log.setDeviceInfo(req == null ? null : req.getDeviceInfo());
        log.setOperatorUserId(currentUserId());
        log.setOperatorName(currentUsername());
        log.setOperationTime(DateUtils.getNowDate());
        log.setRequestPayload(JSON.toJSONString(buildSafeRequestPayload(req)));
        log.setResponsePayload(JSON.toJSONString(buildSafeResponsePayload(result)));
        log.setRemark(JSON.toJSONString(buildLogContext(req, context, grant)));
        log.setCreateBy(currentUsername());
        log.setCreateTime(DateUtils.getNowDate());
        log.setUpdateBy(currentUsername());
        log.setUpdateTime(DateUtils.getNowDate());
        log.setVersion(0L);
        log.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        operationLogMapper.insertCompetitionSceneOperationLog(log);
        return log.getLogId();
    }

    private String resolveLogOperationType(CompetitionSceneOneCardVerifyReq req) {
        String actionType = req == null ? null : normalizeActionType(req.getActionType());
        if (ACTION_REPORT.equals(actionType)) {
            return CompetitionSceneConstants.OPERATION_REPORT_SIGN;
        }
        if (ACTION_MATERIAL_SELF.equals(actionType) || ACTION_MATERIAL_DELEGATE.equals(actionType)) {
            return CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE;
        }
        if (ACTION_WAITING.equals(actionType)) {
            return CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN;
        }
        return CompetitionSceneConstants.OPERATION_VERIFY;
    }

    private Map<String, Object> buildSafeRequestPayload(CompetitionSceneOneCardVerifyReq req) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (req == null) {
            return payload;
        }
        payload.put("credentialId", req.getCredentialId());
        payload.put("currentScheduleId", req.getCurrentScheduleId());
        payload.put("actionType", req.getActionType());
        payload.put("delegateCredentialId", req.getDelegateCredentialId());
        payload.put("operatorRole", req.getOperatorRole());
        payload.put("actionScene", req.getActionScene());
        payload.put("deviceId", req.getDeviceId());
        payload.put("idempotencyKey", req.getIdempotencyKey());
        return payload;
    }

    private Map<String, Object> buildSafeResponsePayload(CompetitionSceneOneCardVerifyResult result) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (result == null) {
            return payload;
        }
        payload.put("operationResult", result.getOperationResult());
        payload.put("resultMessage", result.getResultMessage());
        payload.put("duplicate", result.getDuplicate());
        payload.put("credentialId", result.getCredential() == null ? null : result.getCredential().getCredentialId());
        payload.put("actionTypes", result.getAllowedActions() == null ? List.of()
                : result.getAllowedActions().stream().map(CompetitionSceneOneCardAction::getActionType).collect(Collectors.toList()));
        return payload;
    }

    private Map<String, Object> buildLogContext(CompetitionSceneOneCardVerifyReq req,
                                                OperationContext context,
                                                CompetitionSceneCredentialScopeGrant grant) {
        Map<String, Object> payload = new LinkedHashMap<>();
        CompetitionSceneCredentialScopeGrant sourceGrant = grant == null && context != null ? context.grant : grant;
        payload.put("grantId", sourceGrant == null ? null : sourceGrant.getGrantId());
        payload.put("scopeType", sourceGrant == null ? null : sourceGrant.getScopeType());
        payload.put("scopeRefId", sourceGrant == null ? null : sourceGrant.getScopeRefId());
        payload.put("currentScheduleId", req == null ? null : req.getCurrentScheduleId());
        payload.put("actionType", req == null ? null : req.getActionType());
        payload.put("idempotencyKey", req == null ? null : req.getIdempotencyKey());
        return payload;
    }

    private CompetitionSceneOneCardVerifyResult baseResult() {
        CompetitionSceneOneCardVerifyResult result = new CompetitionSceneOneCardVerifyResult();
        result.setDuplicate(false);
        result.setAlreadyDone(false);
        result.setAllowedActions(new ArrayList<>());
        result.setCompetitionActions(new ArrayList<>());
        result.setScheduleActionGroups(new ArrayList<>());
        return result;
    }

    private void fillFail(CompetitionSceneOneCardVerifyResult result, String message) {
        result.setOperationResult(CompetitionSceneConstants.OPERATION_RESULT_FAIL);
        result.setResultMessage(message);
        result.setDuplicate(false);
        result.setAlreadyDone(false);
    }

    private Long currentUserId() {
        try {
            return SecurityUtils.getLoginUser().getSysUser().getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    private String currentUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception e) {
            return "system";
        }
    }

    private static class ScheduleGrantResolveResult {
        private CompetitionSceneCredentialScopeGrant grant;
        private String message;

        private boolean available() {
            return grant != null;
        }

        private static ScheduleGrantResolveResult ok(CompetitionSceneCredentialScopeGrant grant) {
            ScheduleGrantResolveResult result = new ScheduleGrantResolveResult();
            result.grant = grant;
            return result;
        }

        private static ScheduleGrantResolveResult fail(String message) {
            ScheduleGrantResolveResult result = new ScheduleGrantResolveResult();
            result.message = message;
            return result;
        }
    }

    private static class LogWriteResult {
        private boolean success;
        private Long logId;
        private String message;

        private static LogWriteResult success(Long logId) {
            LogWriteResult result = new LogWriteResult();
            result.success = true;
            result.logId = logId;
            return result;
        }

        private static LogWriteResult fail(String message) {
            LogWriteResult result = new LogWriteResult();
            result.success = false;
            result.message = message;
            return result;
        }
    }

    private static class SafeOperationResult {
        private boolean success;
        private String message;

        private static SafeOperationResult success() {
            SafeOperationResult result = new SafeOperationResult();
            result.success = true;
            return result;
        }

        private static SafeOperationResult fail(String message) {
            SafeOperationResult result = new SafeOperationResult();
            result.success = false;
            result.message = message;
            return result;
        }
    }

    private static class OperationContext {
        private boolean allowed;
        private String message;
        private String actionType;
        private CompetitionSceneSubjectOperationStateQuery query;
        private CompetitionSceneSubjectOperationState state;
        private CompetitionSceneCredentialScopeGrant grant;
        private CompetitionSceneCredential delegateCredential;

        private OperationContext allow(OperationStateContext stateContext) {
            this.allowed = true;
            this.query = stateContext.query;
            this.state = stateContext.state;
            return this;
        }

        private OperationContext fail(String message) {
            this.allowed = false;
            this.message = message;
            return this;
        }
    }

    private static class OperationStateContext {
        private CompetitionSceneSubjectOperationStateQuery query;
        private CompetitionSceneSubjectOperationState state;
    }

    private static class DelegateContext {
        private boolean allowed;
        private String message;
        private CompetitionSceneCredential delegateCredential;
        private Long delegateUserId;
        private String delegateName;
        private Long delegateCredentialId;
        private String delegateRelation;

        private DelegateContext fail(String message) {
            this.allowed = false;
            this.message = message;
            return this;
        }
    }
}
