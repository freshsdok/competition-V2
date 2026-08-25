package com.teaching.competition.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.teaching.common.core.constant.Constants;
import com.teaching.common.core.constant.DictConstant;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionSceneCredentialAbility;
import com.teaching.competition.domain.CompetitionSceneOperationLog;
import com.teaching.competition.domain.CompetitionSceneScanAction;
import com.teaching.competition.domain.CompetitionSceneScheduleActionGroup;
import com.teaching.competition.domain.CompetitionSceneSchedule;
import com.teaching.competition.domain.CompetitionSceneSubjectOperationState;
import com.teaching.competition.domain.CompetitionSceneSubjectOperationStateQuery;
import com.teaching.competition.domain.CompetitionSceneVerifyReq;
import com.teaching.competition.domain.CompetitionSceneVerifyResult;
import com.teaching.competition.mapper.CompetitionApplyInfoMapper;
import com.teaching.competition.mapper.CompetitionSceneCredentialMapper;
import com.teaching.competition.mapper.CompetitionSceneOperationLogMapper;
import com.teaching.competition.mapper.CompetitionSceneScheduleMapper;
import com.teaching.competition.service.ICompetitionSceneSubjectOperationStateService;
import com.teaching.competition.service.ICompetitionSceneVerifyService;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 赛事现场扫码核验Service业务层处理。
 */
@Service
public class CompetitionSceneVerifyServiceImpl implements ICompetitionSceneVerifyService {

    @Autowired
    private CompetitionSceneCredentialMapper credentialMapper;

    @Autowired
    private CompetitionSceneScheduleMapper scheduleMapper;

    @Autowired
    private CompetitionSceneOperationLogMapper operationLogMapper;

    @Autowired
    private CompetitionApplyInfoMapper competitionApplyInfoMapper;

    @Autowired
    private ICompetitionSceneSubjectOperationStateService operationStateService;

    @Override
    public CompetitionSceneVerifyResult scan(CompetitionSceneVerifyReq req) {
        CompetitionSceneVerifyResult result = new CompetitionSceneVerifyResult();
        result.setDuplicate(false);
        result.setApplyCheckResult(CompetitionSceneConstants.CHECK_RESULT_SKIP);
        result.setScheduleCheckResult(CompetitionSceneConstants.CHECK_RESULT_SKIP);
        result.setIdentityCheckResult(CompetitionSceneConstants.CHECK_RESULT_SKIP);

        CompetitionSceneCredential credential = null;
        String applySnapshotJson = null;
        try {
            String token = normalizeToken(req);
            if (StringUtils.isEmpty(token)) {
                fillFail(result, "二维码内容不能为空");
                return result;
            }
            if (req == null) {
                req = new CompetitionSceneVerifyReq();
            }
            req.setCredentialToken(token);
            credential = credentialMapper.selectCompetitionSceneCredentialByToken(token);
            if (credential == null) {
                fillFail(result, "证件不存在或二维码无效");
                return result;
            }
            result.setCredential(credential);

            String scheduleCheck = checkSchedule(credential, result);
            result.setScheduleCheckResult(scheduleCheck);
            if (!CompetitionSceneConstants.CHECK_RESULT_PASS.equals(scheduleCheck)) {
                return result;
            }

            ApplyCheckResult applyCheckResult = checkApply(credential);
            applySnapshotJson = applyCheckResult.applySnapshotJson;
            result.setApplyCheckResult(applyCheckResult.checkResult);
            if (!CompetitionSceneConstants.CHECK_RESULT_PASS.equals(applyCheckResult.checkResult)
                    && !CompetitionSceneConstants.CHECK_RESULT_SKIP.equals(applyCheckResult.checkResult)) {
                fillFail(result, applyCheckResult.message);
                return result;
            }

            String identityCheck = checkIdentity(req, credential);
            result.setIdentityCheckResult(identityCheck);
            if (!CompetitionSceneConstants.CHECK_RESULT_PASS.equals(identityCheck)
                    && !CompetitionSceneConstants.CHECK_RESULT_SKIP.equals(identityCheck)) {
                fillFail(result, "身份信息不一致");
                return result;
            }

            fillOperationMatrix(req, credential, result);
            result.setOperationResult(CompetitionSceneConstants.OPERATION_RESULT_PASS);
            result.setResultMessage("扫码核验通过");
            return result;
        } catch (Exception e) {
            result.setOperationResult(CompetitionSceneConstants.OPERATION_RESULT_EXCEPTION);
            result.setResultMessage("扫码核验异常：" + e.getMessage());
            return result;
        } finally {
            writeLog(req, credential, result, CompetitionSceneConstants.OPERATION_STAGE_SCAN, applySnapshotJson);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneVerifyResult confirm(CompetitionSceneVerifyReq req) {
        CompetitionSceneVerifyResult result = scan(req);
        if (!CompetitionSceneConstants.OPERATION_RESULT_PASS.equals(result.getOperationResult())) {
            return result;
        }

        CompetitionSceneCredential scannedCredential = result.getCredential();
        String operationType = resolveOperationType(req);
        CompetitionSceneCredential credential = resolveConfirmCredential(req, scannedCredential, operationType);
        if (credential == null) {
            fillFail(result, "未找到可执行该操作的赛场证件");
            writeLog(req, scannedCredential, result, CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, null);
            return result;
        }
        if (isWaitingOperation(operationType)
                && CompetitionSceneConstants.SCOPE_TYPE_COMPETITION.equals(resolveScopeType(credential))) {
            fillFail(result, CompetitionSceneConstants.ERROR_CREDENTIAL_SCOPE_NOT_SUPPORT_WAITING);
            writeLog(req, scannedCredential, result, CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, null);
            return result;
        }
        CompetitionSceneScanAction action = findAction(result, req, operationType);
        if (action == null) {
            fillFail(result, "当前扫码角色不可执行该操作");
            writeLog(req, scannedCredential, result, CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, null);
            return result;
        }
        if (CompetitionSceneConstants.ACTION_KIND_PROMPT.equals(action.getActionKind())) {
            fillFail(result, StringUtils.isNotEmpty(action.getMessage()) ? action.getMessage() : "该动作为入口提示，无需确认操作");
            writeLog(req, scannedCredential, result, CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, null);
            return result;
        }
        if (CompetitionSceneConstants.ACTION_STATUS_DONE.equals(action.getStatus())) {
            result.setDuplicate(true);
            result.setOperationResult(CompetitionSceneConstants.OPERATION_RESULT_DUPLICATE);
            result.setResultMessage("alreadyDone：重复操作，证件状态未变更");
            writeLog(req, credential, result, CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, null);
            return result;
        }
        if (!CompetitionSceneConstants.ACTION_KIND_CONFIRM.equals(action.getActionKind())
                || !Boolean.TRUE.equals(action.getEnabled())) {
            fillFail(result, StringUtils.isNotEmpty(action.getMessage()) ? action.getMessage() : "该操作当前不可执行");
            writeLog(req, credential, result, CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, null);
            return result;
        }

        Date now = DateUtils.getNowDate();
        OperationSubjectContext subjectContext;
        try {
            subjectContext = resolveOperationSubject(req, credential, operationType);
        } catch (IllegalArgumentException e) {
            fillFail(result, e.getMessage());
            writeLog(req, credential, result, CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, null);
            return result;
        }
        result.setDelegateCredential(subjectContext == null ? null : subjectContext.delegateCredential);
        if (subjectContext == null) {
            fillFail(result, "无法识别操作主体");
            writeLog(req, credential, result, CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, null);
            return result;
        }
        CompetitionSceneSubjectOperationState existed =
                operationStateService.selectDoneOperationState(subjectContext.query);
        if (isCancelOperation(operationType)) {
            boolean legacyDone = isLegacyOperationDone(credential, baseOperationType(operationType));
            if (existed == null && !legacyDone) {
                fillFail(result, "当前操作尚未完成，无法取消");
                writeLog(req, credential, result, CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, null);
                return result;
            }
            if (existed != null) {
                int cancelled = operationStateService.cancelDoneOperationState(subjectContext.query);
                if (cancelled <= 0) {
                    fillFail(result, "取消失败，状态已变化，请重新扫码确认");
                    writeLog(req, credential, result, CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, null);
                    return result;
                }
            }
            credentialMapper.resetCompetitionSceneCredentialOperationStatus(
                    credential.getCredentialId(), subjectContext.operationType, currentUsername());
            CompetitionSceneCredential latest = credentialMapper.selectCompetitionSceneCredentialById(credential.getCredentialId());
            CompetitionSceneCredential displayCredential = latest == null ? credential : latest;
            if (scannedCredential != null && !Objects.equals(scannedCredential.getCredentialId(), credential.getCredentialId())) {
                displayCredential = credentialMapper.selectCompetitionSceneCredentialById(scannedCredential.getCredentialId());
            }
            if (displayCredential == null) {
                displayCredential = latest == null ? credential : latest;
            }
            result.setCredential(displayCredential);
            fillOperationMatrix(req, result.getCredential(), result);
            result.setDuplicate(false);
            result.setOperationResult(CompetitionSceneConstants.OPERATION_RESULT_PASS);
            result.setResultMessage(cancelSuccessMessage(operationType));
            Long logId = writeLog(req, latest == null ? credential : latest, result, CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, null);
            if (existed != null) {
                operationStateService.updateLastLogId(existed.getStateId(), logId);
            }
            return result;
        }
        if (existed != null) {
            fillDuplicate(result, credential, req, existed);
            return result;
        }
        CompetitionSceneSubjectOperationState state = buildOperationState(credential, operationType, subjectContext, now);
        CompetitionSceneSubjectOperationState savedState =
                operationStateService.insertDoneOperationStateIfAbsent(state);

        CompetitionSceneCredential update = new CompetitionSceneCredential();
        update.setCredentialId(credential.getCredentialId());
        update.setVerifyCount(credential.getVerifyCount() == null ? 1 : credential.getVerifyCount() + 1);
        update.setLastVerifyTime(now);
        update.setUpdateBy(currentUsername());
        update.setUpdateTime(now);

        Long operatorUserId = currentUserId();
        String operatorName = currentUsername();
        if (CompetitionSceneConstants.OPERATION_REPORT_SIGN.equals(operationType)) {
            update.setReportStatus(CompetitionSceneConstants.DONE_YES);
            update.setReportTime(now);
            update.setReportOperatorId(operatorUserId);
            update.setReportOperatorName(operatorName);
        } else if (CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE.equals(operationType)) {
            update.setMaterialStatus(CompetitionSceneConstants.DONE_YES);
            update.setMaterialTime(now);
            update.setMaterialReceiverName(subjectContext.delegateName);
            update.setMaterialReceiverPhone(subjectContext.delegatePhone);
            update.setMaterialReceiverIdSuffix(subjectContext.delegateIdSuffix);
            update.setMaterialOperatorId(operatorUserId);
            update.setMaterialOperatorName(operatorName);
        } else if (CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN.equals(operationType)) {
            update.setWaitingStatus(CompetitionSceneConstants.DONE_YES);
            update.setWaitingTime(now);
            update.setWaitingOperatorId(operatorUserId);
            update.setWaitingOperatorName(operatorName);
        }

        credentialMapper.updateCompetitionSceneCredential(update);
        CompetitionSceneCredential latest = credentialMapper.selectCompetitionSceneCredentialById(credential.getCredentialId());
        CompetitionSceneCredential displayCredential = latest;
        if (scannedCredential != null && !Objects.equals(scannedCredential.getCredentialId(), credential.getCredentialId())) {
            displayCredential = credentialMapper.selectCompetitionSceneCredentialById(scannedCredential.getCredentialId());
        }
        if (displayCredential == null) {
            displayCredential = latest == null ? credential : latest;
        }
        result.setCredential(displayCredential);
        fillOperationMatrix(req, displayCredential, result);
        result.setOperationResult(CompetitionSceneConstants.OPERATION_RESULT_PASS);
        result.setResultMessage("确认成功");
        Long logId = writeLog(req, latest, result, CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, null);
        if (savedState != null) {
            operationStateService.updateLastLogId(savedState.getStateId(), logId);
        }
        return result;
    }

    @Override
    public List<CompetitionSceneOperationLog> selectOperationLogList(CompetitionSceneOperationLog log) {
        return operationLogMapper.selectCompetitionSceneOperationLogList(log);
    }

    private void fillOperationMatrix(CompetitionSceneVerifyReq req,
                                     CompetitionSceneCredential credential,
                                     CompetitionSceneVerifyResult result) {
        if (credential == null || result == null) {
            return;
        }
        String targetRole = resolveCredentialRole(credential, false);
        OperatorContext operatorContext = resolveOperatorContext(req, credential);
        String operatorRole = operatorContext.primaryRole;
        if (StringUtils.isEmpty(operatorRole)) {
            operatorRole = CompetitionSceneConstants.TARGET_ROLE_UNKNOWN;
        }

        CompetitionSceneSubjectOperationState reportState =
                selectStateForCredential(credential, CompetitionSceneConstants.OPERATION_REPORT_SIGN);
        CompetitionSceneSubjectOperationState materialState =
                selectStateForCredential(credential, CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE);
        CompetitionSceneSubjectOperationState waitingState =
                selectStateForCredential(credential, CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN);
        result.setReportState(reportState);
        result.setMaterialState(materialState);
        result.setWaitingState(waitingState);

        ActionMatrix actionMatrix = buildScanActionMatrix(operatorContext, targetRole, credential,
                reportState, materialState, waitingState);
        List<CompetitionSceneScanAction> actions = actionMatrix.actions;
        boolean hasEnabledAction = false;
        String reviewEntryMessage = null;
        for (CompetitionSceneScanAction action : actions) {
            if (Boolean.TRUE.equals(action.getEnabled())) {
                hasEnabledAction = true;
            }
            if (CompetitionSceneConstants.OPERATION_EXPERT_REVIEW_ENTRY.equals(action.getActionType())) {
                reviewEntryMessage = action.getMessage();
            }
        }

        result.setOperatorRole(operatorRole);
        result.setOperatorRoleLabel(roleLabel(operatorRole));
        result.setTargetRole(targetRole);
        result.setTargetRoleLabel(roleLabel(targetRole));
        result.setAvailableActions(actions);
        result.setCompetitionActions(actionMatrix.competitionActions);
        result.setScheduleActionGroups(actionMatrix.scheduleActionGroups);
        result.setReviewEntryAvailable(StringUtils.isNotEmpty(reviewEntryMessage));
        result.setReviewEntryMessage(reviewEntryMessage);
        if (operatorContext.credential == null) {
            result.setMatrixMessage("当前账号未配置本赛事现场操作角色");
        } else if (actions.isEmpty()) {
            result.setMatrixMessage("当前扫码角色对该证件暂无可执行动作");
        } else if (!hasEnabledAction) {
            result.setMatrixMessage("该证件可执行动作已完成");
        } else {
            result.setMatrixMessage("请选择可执行的现场操作");
        }
    }

    private OperatorContext resolveOperatorContext(CompetitionSceneVerifyReq req,
                                                   CompetitionSceneCredential targetCredential) {
        OperatorContext context = new OperatorContext();
        List<CompetitionSceneCredential> candidates = new ArrayList<>();
        Long userId = currentUserId();
        if (userId != null) {
            addOperatorCandidates(candidates, buildOperatorQuery(targetCredential, userId, null, true));
            addOperatorCandidates(candidates, buildOperatorQuery(targetCredential, userId, null, false));
        }
        String operatorPhone = req == null ? null : req.getOperatorPhone();
        if (StringUtils.isNotEmpty(operatorPhone)) {
            addOperatorCandidates(candidates, buildOperatorQuery(targetCredential, null, operatorPhone, true));
            addOperatorCandidates(candidates, buildOperatorQuery(targetCredential, null, operatorPhone, false));
        }

        context.credential = chooseBestOperatorCredential(candidates, targetCredential);
        for (CompetitionSceneCredential candidate : candidates) {
            String role = resolveCredentialRole(candidate, true);
            if (StringUtils.isNotEmpty(role) && !context.roles.contains(role)) {
                context.roles.add(role);
            }
            if (CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER.equals(role)
                    && Objects.equals(candidate.getCompetitionSeriesId(), targetCredential.getCompetitionSeriesId())
                    && candidate.getScheduleId() != null
                    && !context.volunteerScheduleIds.contains(candidate.getScheduleId())) {
                context.volunteerScheduleIds.add(candidate.getScheduleId());
            }
        }
        if (context.credential != null) {
            context.primaryRole = resolveCredentialRole(context.credential, true);
            if (StringUtils.isNotEmpty(context.primaryRole) && !context.roles.contains(context.primaryRole)) {
                context.roles.add(context.primaryRole);
            }
        }
        return context;
    }

    private CompetitionSceneCredential buildOperatorQuery(CompetitionSceneCredential targetCredential,
                                                          Long userId,
                                                          String phone,
                                                          boolean sameSchedule) {
        CompetitionSceneCredential query = new CompetitionSceneCredential();
        query.setCredentialStatus(CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE);
        query.setUserId(userId);
        query.setPhone(phone);
        if (sameSchedule && targetCredential.getScheduleId() != null) {
            query.setScheduleId(targetCredential.getScheduleId());
        } else {
            query.setCompetitionSeriesId(targetCredential.getCompetitionSeriesId());
        }
        return query;
    }

    private void addOperatorCandidates(List<CompetitionSceneCredential> candidates,
                                       CompetitionSceneCredential query) {
        List<CompetitionSceneCredential> list = credentialMapper.selectCompetitionSceneCredentialList(query);
        if (list == null) {
            return;
        }
        for (CompetitionSceneCredential item : list) {
            if (!containsCredential(candidates, item.getCredentialId())) {
                candidates.add(item);
            }
        }
    }

    private boolean containsCredential(List<CompetitionSceneCredential> candidates, Long credentialId) {
        if (credentialId == null) {
            return false;
        }
        for (CompetitionSceneCredential candidate : candidates) {
            if (Objects.equals(candidate.getCredentialId(), credentialId)) {
                return true;
            }
        }
        return false;
    }

    private CompetitionSceneCredential chooseBestOperatorCredential(List<CompetitionSceneCredential> candidates,
                                                                    CompetitionSceneCredential targetCredential) {
        CompetitionSceneCredential best = null;
        int bestScore = -1;
        for (CompetitionSceneCredential candidate : candidates) {
            int score = operatorCredentialScore(candidate, targetCredential);
            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return best;
    }

    private int operatorCredentialScore(CompetitionSceneCredential candidate,
                                        CompetitionSceneCredential targetCredential) {
        int score = rolePriority(resolveCredentialRole(candidate, true));
        if (targetCredential != null && Objects.equals(candidate.getScheduleId(), targetCredential.getScheduleId())) {
            score += 100;
        }
        if (targetCredential != null
                && Objects.equals(candidate.getCompetitionSeriesId(), targetCredential.getCompetitionSeriesId())) {
            score += 10;
        }
        if (targetCredential != null && Objects.equals(candidate.getCredentialId(), targetCredential.getCredentialId())) {
            score -= 1;
        }
        return score;
    }

    private int rolePriority(String role) {
        if (CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF.equals(role)
                || CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF.equals(role)) {
            return 80;
        }
        if (CompetitionSceneConstants.TARGET_ROLE_STAFF.equals(role)) {
            return 70;
        }
        if (CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER.equals(role)) {
            return 65;
        }
        if (CompetitionSceneConstants.TARGET_ROLE_EXPERT.equals(role)) {
            return 60;
        }
        if (CompetitionSceneConstants.TARGET_ROLE_TEACHER.equals(role)
                || CompetitionSceneConstants.TARGET_ROLE_CAPTAIN.equals(role)
                || CompetitionSceneConstants.TARGET_ROLE_MEMBER.equals(role)) {
            return 10;
        }
        return 0;
    }

    private ActionMatrix buildScanActionMatrix(OperatorContext operatorContext,
                                               String targetRole,
                                               CompetitionSceneCredential credential,
                                               CompetitionSceneSubjectOperationState reportState,
                                               CompetitionSceneSubjectOperationState materialState,
                                               CompetitionSceneSubjectOperationState waitingState) {
        ActionMatrix matrix = new ActionMatrix();
        if (!isParticipantSideTarget(targetRole)) {
            return matrix;
        }
        List<String> operatorRoles = operatorContext == null ? null : operatorContext.roles;
        boolean canOperate = hasGeneralStaffRole(operatorRoles);
        boolean canOperateSchedule = canOperate || canVolunteerOperateSchedule(operatorContext, credential);
        boolean competitionScope = CompetitionSceneConstants.SCOPE_TYPE_COMPETITION.equals(resolveScopeType(credential));
        if (canOperate && competitionScope) {
            if (hasAbility(credential, "report")) {
                matrix.addCompetitionAction(buildConfirmAction(CompetitionSceneConstants.OPERATION_REPORT_SIGN,
                        "大赛报到",
                        isStateDone(reportState) || CompetitionSceneConstants.DONE_YES.equals(credential.getReportStatus()),
                        "已完成大赛报到",
                        "确认该人员完成大赛报到"));
            }
            if (hasAbility(credential, "material")) {
                matrix.addCompetitionAction(buildConfirmAction(CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE,
                        "大赛领取资料",
                        isStateDone(materialState) || CompetitionSceneConstants.DONE_YES.equals(credential.getMaterialStatus()),
                        "已完成大赛资料领取",
                        "确认该人员完成大赛资料领取"));
            }
        }

        if (canOperate || hasVolunteerRole(operatorRoles)) {
            if (competitionScope) {
                for (CompetitionSceneCredential scheduleCredential : selectRelatedScheduleCredentials(credential)) {
                    boolean includeActions = canOperate || canVolunteerOperateSchedule(operatorContext, scheduleCredential);
                    if (!includeActions) {
                        continue;
                    }
                    CompetitionSceneScheduleActionGroup group = buildScheduleActionGroup(scheduleCredential, includeActions);
                    if (group != null) {
                        matrix.addScheduleGroup(group);
                    }
                }
            } else if (canOperateSchedule) {
                CompetitionSceneScheduleActionGroup group = buildScheduleActionGroup(credential, true);
                if (group != null) {
                    matrix.addScheduleGroup(group);
                } else if (hasAbility(credential, "waiting")) {
                    matrix.addAction(buildConfirmAction(CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN,
                            "候场确认",
                            isStateDone(waitingState) || CompetitionSceneConstants.DONE_YES.equals(credential.getWaitingStatus()),
                            "已完成候场确认",
                            "确认该人员进入候场"));
                }
            }
        }
        if (hasRole(operatorRoles, CompetitionSceneConstants.TARGET_ROLE_EXPERT)
                && isExpertReviewTarget(targetRole)
                && hasAbility(credential, "review")) {
            CompetitionSceneScanAction action = new CompetitionSceneScanAction();
            action.setActionType(CompetitionSceneConstants.OPERATION_EXPERT_REVIEW_ENTRY);
            action.setActionLabel("专家评审入口");
            action.setActionKind(CompetitionSceneConstants.ACTION_KIND_PROMPT);
            action.setEnabled(true);
            action.setStatus(CompetitionSceneConstants.ACTION_STATUS_PENDING);
            action.setMessage("当前对象可进入专家评审入口，正式评审页面待开放");
            matrix.addAction(action);
        }
        return matrix;
    }

    private CompetitionSceneScanAction buildConfirmAction(String actionType,
                                                          String actionLabel,
                                                          boolean done,
                                                          String doneMessage,
                                                          String pendingMessage) {
        CompetitionSceneScanAction action = new CompetitionSceneScanAction();
        action.setActionType(done ? cancelOperationType(actionType) : actionType);
        action.setActionLabel(done ? cancelActionLabel(actionType, actionLabel) : actionLabel);
        action.setActionKind(CompetitionSceneConstants.ACTION_KIND_CONFIRM);
        action.setEnabled(true);
        action.setStatus(CompetitionSceneConstants.ACTION_STATUS_PENDING);
        action.setMessage(done ? cancelActionMessage(actionType, actionLabel, doneMessage) : pendingMessage);
        return action;
    }

    private String cancelOperationType(String operationType) {
        if (CompetitionSceneConstants.OPERATION_REPORT_SIGN.equals(operationType)) {
            return CompetitionSceneConstants.OPERATION_CANCEL_REPORT_SIGN;
        }
        if (CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE.equals(operationType)) {
            return CompetitionSceneConstants.OPERATION_CANCEL_MATERIAL_RECEIVE;
        }
        if (CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN.equals(operationType)) {
            return CompetitionSceneConstants.OPERATION_CANCEL_WAITING_CHECK_IN;
        }
        return operationType;
    }

    private String cancelActionLabel(String operationType, String actionLabel) {
        return "取消" + actionLabel;
    }

    private String cancelActionMessage(String operationType, String actionLabel, String doneMessage) {
        if (CompetitionSceneConstants.OPERATION_REPORT_SIGN.equals(operationType)) {
            return doneMessage + "，如现场误操作可取消报到";
        }
        if (CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE.equals(operationType)) {
            return doneMessage + "，如现场误操作可取消领取";
        }
        if (CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN.equals(operationType)) {
            return doneMessage + "，如现场误操作可取消候场";
        }
        return "取消" + actionLabel;
    }

    private CompetitionSceneScheduleActionGroup buildScheduleActionGroup(CompetitionSceneCredential scheduleCredential,
                                                                         boolean includeActions) {
        if (!isScheduleCredential(scheduleCredential)) {
            return null;
        }
        String scheduleName = scheduleTitle(scheduleCredential);
        CompetitionSceneSubjectOperationState reportState =
                selectStateForCredential(scheduleCredential, CompetitionSceneConstants.OPERATION_REPORT_SIGN);
        CompetitionSceneSubjectOperationState materialState =
                selectStateForCredential(scheduleCredential, CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE);
        CompetitionSceneSubjectOperationState waitingState =
                selectStateForCredential(scheduleCredential, CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN);

        CompetitionSceneScheduleActionGroup group = new CompetitionSceneScheduleActionGroup();
        group.setScheduleId(scheduleCredential.getScheduleId());
        group.setTargetCredentialId(scheduleCredential.getCredentialId());
        group.setScheduleName(scheduleName);
        group.setScheduleTime(scheduleTimeText(scheduleCredential));
        group.setScheduleLocation(scheduleLocationText(scheduleCredential));
        group.setReportStatus(doneStatus(reportState, scheduleCredential.getReportStatus()));
        group.setMaterialStatus(doneStatus(materialState, scheduleCredential.getMaterialStatus()));
        group.setWaitingStatus(doneStatus(waitingState, scheduleCredential.getWaitingStatus()));
        group.setReportTime(formatStateTime(reportState, scheduleCredential.getReportTime()));
        group.setMaterialTime(formatStateTime(materialState, scheduleCredential.getMaterialTime()));
        group.setWaitingTime(formatStateTime(waitingState, scheduleCredential.getWaitingTime()));

        List<CompetitionSceneScanAction> actions = new ArrayList<>();
        if (includeActions && hasAbility(scheduleCredential, "report")) {
            actions.add(buildScheduleOperationAction(scheduleCredential,
                    CompetitionSceneConstants.OPERATION_REPORT_SIGN,
                    scheduleName,
                    reportState));
        }
        if (includeActions && hasAbility(scheduleCredential, "material")) {
            actions.add(buildScheduleOperationAction(scheduleCredential,
                    CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE,
                    scheduleName,
                    materialState));
        }
        if (includeActions && hasAbility(scheduleCredential, "waiting")) {
            actions.add(buildScheduleOperationAction(scheduleCredential,
                    CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN,
                    scheduleName,
                    waitingState));
        }
        group.setActions(actions);
        return group;
    }

    private CompetitionSceneScanAction buildScheduleOperationAction(CompetitionSceneCredential scheduleCredential,
                                                                    String operationType,
                                                                    String scheduleName,
                                                                    CompetitionSceneSubjectOperationState state) {
        boolean done = isStateDone(state) || isLegacyOperationDone(scheduleCredential, operationType);
        String actionLabel;
        String doneMessage;
        String pendingMessage;
        if (CompetitionSceneConstants.OPERATION_REPORT_SIGN.equals(operationType)) {
            actionLabel = scheduleName + " 赛场报到";
            doneMessage = "已完成赛场报到";
            pendingMessage = "确认该队员完成 " + scheduleName + " 赛场报到";
        } else if (CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE.equals(operationType)) {
            actionLabel = scheduleName + " 赛场领取资料";
            doneMessage = "已完成赛场资料领取";
            pendingMessage = "确认该人员完成 " + scheduleName + " 赛场资料领取";
        } else {
            actionLabel = scheduleName + " 候场确认";
            doneMessage = "已完成候场确认";
            pendingMessage = "确认该人员进入 " + scheduleName + " 候场";
        }
        CompetitionSceneScanAction action = buildConfirmAction(operationType,
                actionLabel,
                done,
                doneMessage,
                pendingMessage);
        action.setScheduleId(scheduleCredential.getScheduleId());
        action.setTargetCredentialId(scheduleCredential.getCredentialId());
        action.setScheduleName(scheduleName);
        action.setScheduleTime(scheduleTimeText(scheduleCredential));
        action.setScheduleLocation(scheduleLocationText(scheduleCredential));
        return action;
    }

    private String scheduleTimeText(CompetitionSceneCredential scheduleCredential) {
        return firstNotEmpty(
                formatRange(scheduleCredential.getWaitingStartTime(), scheduleCredential.getWaitingEndTime()),
                formatRange(scheduleCredential.getContestStartTime(), scheduleCredential.getContestEndTime()),
                formatRange(scheduleCredential.getReportStartTime(), scheduleCredential.getReportEndTime()));
    }

    private String scheduleLocationText(CompetitionSceneCredential scheduleCredential) {
        return firstNotEmpty(
                joinText(scheduleCredential.getWaitingLocation(), scheduleCredential.getWaitingGroupName()),
                joinText(scheduleCredential.getContestLocation(), scheduleCredential.getContestRoom()),
                scheduleCredential.getReportLocation(),
                scheduleCredential.getMaterialLocation());
    }

    private String doneStatus(CompetitionSceneSubjectOperationState state, String legacyStatus) {
        return isStateDone(state) || CompetitionSceneConstants.DONE_YES.equals(legacyStatus)
                ? CompetitionSceneConstants.DONE_YES
                : CompetitionSceneConstants.DONE_NO;
    }

    private String formatStateTime(CompetitionSceneSubjectOperationState state, Date fallbackTime) {
        Date time = state != null && state.getOperationTime() != null ? state.getOperationTime() : fallbackTime;
        if (time == null) {
            return null;
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(time);
    }

    private boolean isLegacyOperationDone(CompetitionSceneCredential credential, String operationType) {
        if (CompetitionSceneConstants.OPERATION_REPORT_SIGN.equals(operationType)) {
            return CompetitionSceneConstants.DONE_YES.equals(credential.getReportStatus());
        }
        if (CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE.equals(operationType)) {
            return CompetitionSceneConstants.DONE_YES.equals(credential.getMaterialStatus());
        }
        if (CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN.equals(operationType)) {
            return CompetitionSceneConstants.DONE_YES.equals(credential.getWaitingStatus());
        }
        return false;
    }

    private CompetitionSceneScanAction findAction(CompetitionSceneVerifyResult result,
                                                  CompetitionSceneVerifyReq req,
                                                  String operationType) {
        if (result == null || result.getAvailableActions() == null || StringUtils.isEmpty(operationType)) {
            return null;
        }
        Long targetCredentialId = req == null ? null : req.getTargetCredentialId();
        Long scheduleId = req == null ? null : req.getScheduleId();
        for (CompetitionSceneScanAction action : result.getAvailableActions()) {
            if (!Objects.equals(action.getActionType(), operationType)) {
                continue;
            }
            if (targetCredentialId != null && !Objects.equals(action.getTargetCredentialId(), targetCredentialId)) {
                continue;
            }
            if (scheduleId != null && !Objects.equals(action.getScheduleId(), scheduleId)) {
                continue;
            }
            if (targetCredentialId == null && scheduleId == null) {
                return action;
            }
            return action;
        }
        return null;
    }

    private CompetitionSceneCredential resolveConfirmCredential(CompetitionSceneVerifyReq req,
                                                                CompetitionSceneCredential scannedCredential,
                                                                String operationType) {
        if (scannedCredential == null
                || !CompetitionSceneConstants.SCOPE_TYPE_COMPETITION.equals(resolveScopeType(scannedCredential))) {
            return scannedCredential;
        }
        Long targetCredentialId = req == null ? null : req.getTargetCredentialId();
        Long scheduleId = req == null ? null : req.getScheduleId();
        boolean hasScheduleSelector = targetCredentialId != null || scheduleId != null;
        if (!isWaitingOperation(operationType) && !hasScheduleSelector) {
            return scannedCredential;
        }
        List<CompetitionSceneCredential> scheduleCredentials = selectRelatedScheduleCredentials(scannedCredential);
        if (scheduleCredentials.isEmpty()) {
            return null;
        }
        for (CompetitionSceneCredential item : scheduleCredentials) {
            if (targetCredentialId != null && Objects.equals(item.getCredentialId(), targetCredentialId)) {
                return item;
            }
        }
        for (CompetitionSceneCredential item : scheduleCredentials) {
            if (scheduleId != null && Objects.equals(item.getScheduleId(), scheduleId)) {
                return item;
            }
        }
        return scheduleCredentials.size() == 1 ? scheduleCredentials.get(0) : null;
    }

    private List<CompetitionSceneCredential> selectRelatedScheduleCredentials(CompetitionSceneCredential credential) {
        List<CompetitionSceneCredential> result = new ArrayList<>();
        if (credential == null || credential.getCompetitionSeriesId() == null) {
            return result;
        }

        boolean hasSubjectIdentity = StringUtils.isNotEmpty(credential.getSubjectType())
                && StringUtils.isNotEmpty(credential.getSubjectCode());
        boolean teamSubject = CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(credential.getSubjectType());
        if (hasSubjectIdentity) {
            CompetitionSceneCredential query = buildRelatedCredentialQuery(credential);
            query.setSubjectType(credential.getSubjectType());
            query.setSubjectCode(credential.getSubjectCode());
            addRelatedScheduleCredentials(result, query);
        }

        if (!teamSubject && credential.getUserId() != null) {
            CompetitionSceneCredential query = buildRelatedCredentialQuery(credential);
            query.setUserId(credential.getUserId());
            addRelatedScheduleCredentials(result, query);
        }
        if (!teamSubject && credential.getMemberId() != null) {
            CompetitionSceneCredential query = buildRelatedCredentialQuery(credential);
            query.setMemberId(credential.getMemberId());
            addRelatedScheduleCredentials(result, query);
        }

        // 个人核心证必须按稳定个人身份关联。按 teamCode 查询会把同队其他成员的赛场证
        // 一并带入，既造成同赛场重复，也可能让工作人员误操作队友证件。
        // 团队主体或完全没有个人身份的历史数据才允许使用团队编码兜底。
        boolean hasPersonalIdentity = hasSubjectIdentity
                || credential.getUserId() != null
                || credential.getMemberId() != null;
        if (StringUtils.isNotEmpty(credential.getTeamCode())
                && ((teamSubject && result.isEmpty()) || (!teamSubject && !hasPersonalIdentity))) {
            CompetitionSceneCredential query = buildRelatedCredentialQuery(credential);
            query.setTeamCode(credential.getTeamCode());
            addRelatedScheduleCredentials(result, query);
        }
        return retainOneCredentialPerSchedule(result);
    }

    private CompetitionSceneCredential buildRelatedCredentialQuery(CompetitionSceneCredential credential) {
        CompetitionSceneCredential query = new CompetitionSceneCredential();
        query.setCompetitionSeriesId(credential.getCompetitionSeriesId());
        query.setCredentialStatus(CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE);
        query.setCredentialType(credential.getCredentialType());
        return query;
    }

    private List<CompetitionSceneCredential> retainOneCredentialPerSchedule(
            List<CompetitionSceneCredential> credentials) {
        List<CompetitionSceneCredential> result = new ArrayList<>();
        if (credentials == null) {
            return result;
        }
        for (CompetitionSceneCredential credential : credentials) {
            if (!isScheduleCredential(credential) || containsSchedule(result, credential.getScheduleId())) {
                continue;
            }
            result.add(credential);
        }
        return result;
    }

    private boolean containsSchedule(List<CompetitionSceneCredential> credentials, Long scheduleId) {
        if (scheduleId == null || credentials == null) {
            return false;
        }
        for (CompetitionSceneCredential credential : credentials) {
            if (Objects.equals(credential.getScheduleId(), scheduleId)) {
                return true;
            }
        }
        return false;
    }

    private void addRelatedScheduleCredentials(List<CompetitionSceneCredential> result,
                                               CompetitionSceneCredential query) {
        List<CompetitionSceneCredential> list = credentialMapper.selectCompetitionSceneCredentialList(query);
        if (list == null) {
            return;
        }
        for (CompetitionSceneCredential item : list) {
            if (!isScheduleCredential(item) || containsCredential(result, item.getCredentialId())) {
                continue;
            }
            result.add(item);
        }
    }

    private boolean isScheduleCredential(CompetitionSceneCredential credential) {
        return credential != null
                && credential.getScheduleId() != null
                && !CompetitionSceneConstants.SCOPE_TYPE_COMPETITION.equals(resolveScopeType(credential))
                && CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE.equals(credential.getCredentialStatus());
    }

    private String scheduleTitle(CompetitionSceneCredential credential) {
        if (credential == null) {
            return "赛场证件";
        }
        String snapshotScheduleName = snapshotScheduleName(credential);
        CompetitionSceneSchedule schedule = null;
        if (StringUtils.isEmpty(snapshotScheduleName) && credential.getScheduleId() != null) {
            schedule = scheduleMapper.selectCompetitionSceneScheduleById(credential.getScheduleId());
        }
        return firstNotEmpty(
                snapshotScheduleName,
                schedule == null ? null : schedule.getScheduleName(),
                credential.getCredentialName(),
                joinNonEmpty(" / ",
                        credential.getCompetitionStageName(),
                        credential.getCompetitionTrackName(),
                        credential.getSecondLevelName()),
                joinNonEmpty(" / ", credential.getContestLocation(), credential.getContestRoom()),
                "赛场证件");
    }

    private String snapshotScheduleName(CompetitionSceneCredential credential) {
        if (credential == null || StringUtils.isEmpty(credential.getCredentialSnapshotJson())) {
            return null;
        }
        try {
            JSONObject snapshot = JSON.parseObject(credential.getCredentialSnapshotJson());
            if (snapshot == null) {
                return null;
            }
            JSONObject schedule = snapshot.getJSONObject("schedule");
            return schedule == null ? null : schedule.getString("scheduleName");
        } catch (Exception ignored) {
            return null;
        }
    }

    private String formatRange(Date start, Date end) {
        if (start == null && end == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (start != null && end != null) {
            return formatter.format(start) + " - " + formatter.format(end);
        }
        return formatter.format(start == null ? end : start);
    }

    private String joinText(String... values) {
        return joinNonEmpty(" / ", values);
    }

    private String joinNonEmpty(String separator, String... values) {
        StringBuilder builder = new StringBuilder();
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.isEmpty(value)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(value.trim());
        }
        return builder.length() == 0 ? null : builder.toString();
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

    private boolean hasRole(List<String> roles, String role) {
        if (roles == null || StringUtils.isEmpty(role)) {
            return false;
        }
        for (String item : roles) {
            if (Objects.equals(item, role)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasGeneralStaffRole(List<String> roles) {
        return hasRole(roles, CompetitionSceneConstants.TARGET_ROLE_STAFF)
                || hasRole(roles, CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF)
                || hasRole(roles, CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF);
    }

    private boolean hasVolunteerRole(List<String> roles) {
        return hasRole(roles, CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER);
    }

    private boolean canVolunteerOperateSchedule(OperatorContext operatorContext,
                                                CompetitionSceneCredential scheduleCredential) {
        return operatorContext != null
                && hasVolunteerRole(operatorContext.roles)
                && isScheduleCredential(scheduleCredential)
                && containsLong(operatorContext.volunteerScheduleIds, scheduleCredential.getScheduleId());
    }

    private boolean containsLong(List<Long> values, Long value) {
        if (values == null || value == null) {
            return false;
        }
        for (Long item : values) {
            if (Objects.equals(item, value)) {
                return true;
            }
        }
        return false;
    }

    private boolean isParticipantSideTarget(String targetRole) {
        return CompetitionSceneConstants.TARGET_ROLE_CAPTAIN.equals(targetRole)
                || CompetitionSceneConstants.TARGET_ROLE_MEMBER.equals(targetRole)
                || CompetitionSceneConstants.TARGET_ROLE_TEACHER.equals(targetRole)
                || CompetitionSceneConstants.TARGET_ROLE_EXPERT.equals(targetRole);
    }

    private CompetitionSceneSubjectOperationState selectStateForCredential(CompetitionSceneCredential credential,
                                                                           String operationType) {
        OperationSubjectContext context = resolveOperationSubject(null, credential, operationType);
        if (context == null) {
            return null;
        }
        return operationStateService.selectDoneOperationState(context.query);
    }

    private OperationSubjectContext resolveOperationSubject(CompetitionSceneVerifyReq req,
                                                            CompetitionSceneCredential credential,
                                                            String operationType) {
        String stateOperationType = resolveStateOperationType(operationType);
        if (StringUtils.isEmpty(stateOperationType) || credential == null || credential.getCompetitionSeriesId() == null) {
            return null;
        }
        boolean scheduleScoped = isScheduleScopedOperation(credential, stateOperationType);
        if (CompetitionSceneConstants.STATE_OPERATION_WAITING.equals(stateOperationType) && !scheduleScoped) {
            return null;
        }

        OperationSubjectContext context = new OperationSubjectContext();
        context.operationType = stateOperationType;
        context.scopeType = scheduleScoped
                ? CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE
                : CompetitionSceneConstants.SCOPE_TYPE_COMPETITION;
        context.scopeRefId = scheduleScoped
                ? credential.getScheduleId()
                : credential.getCompetitionSeriesId();
        context.subjectType = resolveStateSubjectType(credential, stateOperationType);
        context.subjectCode = resolveStateSubjectCode(req, credential, context.subjectType, stateOperationType);
        if (StringUtils.isEmpty(context.subjectType) || StringUtils.isEmpty(context.subjectCode)) {
            return null;
        }
        context.delegateCredential = resolveDelegateCredential(req, credential, stateOperationType);
        fillDelegateInfo(req, credential, context);
        context.query = new CompetitionSceneSubjectOperationStateQuery();
        context.query.setCompetitionSeriesId(credential.getCompetitionSeriesId());
        context.query.setScopeType(context.scopeType);
        context.query.setScopeRefId(context.scopeRefId);
        context.query.setSubjectType(context.subjectType);
        context.query.setSubjectCode(context.subjectCode);
        context.query.setOperationType(context.operationType);
        return context;
    }

    private boolean isScheduleScopedOperation(CompetitionSceneCredential credential, String stateOperationType) {
        return credential != null
                && credential.getScheduleId() != null
                && CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE.equals(resolveScopeType(credential))
                && (CompetitionSceneConstants.STATE_OPERATION_REPORT.equals(stateOperationType)
                || CompetitionSceneConstants.STATE_OPERATION_MATERIAL.equals(stateOperationType)
                || CompetitionSceneConstants.STATE_OPERATION_WAITING.equals(stateOperationType));
    }

    private CompetitionSceneSubjectOperationState buildOperationState(CompetitionSceneCredential credential,
                                                                      String operationType,
                                                                      OperationSubjectContext context,
                                                                      Date now) {
        CompetitionSceneSubjectOperationState state = new CompetitionSceneSubjectOperationState();
        state.setCompetitionSeriesId(credential.getCompetitionSeriesId());
        state.setScopeType(context.scopeType);
        state.setScopeRefId(context.scopeRefId);
        state.setSubjectType(context.subjectType);
        state.setSubjectCode(context.subjectCode);
        state.setOperationType(context.operationType);
        state.setOperationStatus(CompetitionSceneConstants.STATE_STATUS_DONE);
        state.setOperationTime(now);
        state.setCredentialId(credential.getCredentialId());
        state.setOperatorUserId(currentUserId());
        state.setOperatorName(currentUsername());
        if (CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE.equals(operationType)) {
            state.setDelegateUserId(context.delegateUserId);
            state.setDelegateName(context.delegateName);
            state.setDelegateCredentialId(context.delegateCredentialId);
            state.setDelegateRelation(context.delegateRelation);
        }
        state.setCreateBy(currentUsername());
        state.setUpdateBy(currentUsername());
        state.setCreateTime(now);
        state.setUpdateTime(now);
        state.setDeleted(CompetitionSceneConstants.STATE_DELETED_NO);
        return state;
    }

    private String resolveStateOperationType(String operationType) {
        String baseOperationType = baseOperationType(operationType);
        if (CompetitionSceneConstants.OPERATION_REPORT_SIGN.equals(baseOperationType)) {
            return CompetitionSceneConstants.STATE_OPERATION_REPORT;
        }
        if (CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE.equals(baseOperationType)) {
            return CompetitionSceneConstants.STATE_OPERATION_MATERIAL;
        }
        if (CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN.equals(baseOperationType)) {
            return CompetitionSceneConstants.STATE_OPERATION_WAITING;
        }
        return null;
    }

    private String baseOperationType(String operationType) {
        if (CompetitionSceneConstants.OPERATION_CANCEL_REPORT_SIGN.equals(operationType)) {
            return CompetitionSceneConstants.OPERATION_REPORT_SIGN;
        }
        if (CompetitionSceneConstants.OPERATION_CANCEL_MATERIAL_RECEIVE.equals(operationType)) {
            return CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE;
        }
        if (CompetitionSceneConstants.OPERATION_CANCEL_WAITING_CHECK_IN.equals(operationType)) {
            return CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN;
        }
        return operationType;
    }

    private boolean isCancelOperation(String operationType) {
        return CompetitionSceneConstants.OPERATION_CANCEL_REPORT_SIGN.equals(operationType)
                || CompetitionSceneConstants.OPERATION_CANCEL_MATERIAL_RECEIVE.equals(operationType)
                || CompetitionSceneConstants.OPERATION_CANCEL_WAITING_CHECK_IN.equals(operationType);
    }

    private boolean isWaitingOperation(String operationType) {
        return CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN.equals(baseOperationType(operationType));
    }

    private String cancelSuccessMessage(String operationType) {
        if (CompetitionSceneConstants.OPERATION_CANCEL_REPORT_SIGN.equals(operationType)) {
            return "取消报到成功";
        }
        if (CompetitionSceneConstants.OPERATION_CANCEL_MATERIAL_RECEIVE.equals(operationType)) {
            return "取消领取资料成功";
        }
        if (CompetitionSceneConstants.OPERATION_CANCEL_WAITING_CHECK_IN.equals(operationType)) {
            return "取消候场成功";
        }
        return "取消成功";
    }

    private String resolveStateSubjectType(CompetitionSceneCredential credential, String stateOperationType) {
        if (CompetitionSceneConstants.STATE_OPERATION_MATERIAL.equals(stateOperationType)) {
            return CompetitionSceneConstants.SUBJECT_TYPE_USER;
        }
        if (CompetitionSceneConstants.SUBJECT_TYPE_USER.equals(credential.getSubjectType())
                || CompetitionSceneConstants.SUBJECT_TYPE_EXPERT.equals(credential.getSubjectType())
                || CompetitionSceneConstants.SUBJECT_TYPE_STAFF.equals(credential.getSubjectType())
                || CompetitionSceneConstants.SUBJECT_TYPE_VIP.equals(credential.getSubjectType())
                || CompetitionSceneConstants.SUBJECT_TYPE_TEMP.equals(credential.getSubjectType())) {
            return credential.getSubjectType();
        }
        if (CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(credential.getSubjectType())
                || (CompetitionSceneConstants.DIMENSION_TEAM.equals(credential.getConfigDimension())
                && StringUtils.isNotEmpty(credential.getTeamCode()))) {
            return CompetitionSceneConstants.SUBJECT_TYPE_TEAM;
        }
        return CompetitionSceneConstants.SUBJECT_TYPE_USER;
    }

    private String resolveStateSubjectCode(CompetitionSceneVerifyReq req,
                                           CompetitionSceneCredential credential,
                                           String subjectType,
                                           String stateOperationType) {
        if (CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(subjectType)) {
            return StringUtils.isNotEmpty(credential.getSubjectCode()) ? credential.getSubjectCode() : credential.getTeamCode();
        }
        if (CompetitionSceneConstants.STATE_OPERATION_MATERIAL.equals(stateOperationType)) {
            Long subjectUserId = req != null && req.getSubjectUserId() != null ? req.getSubjectUserId() : credential.getUserId();
            if (subjectUserId != null) {
                return String.valueOf(subjectUserId);
            }
            if (CompetitionSceneConstants.SUBJECT_TYPE_USER.equals(credential.getSubjectType())
                    && StringUtils.isNotEmpty(credential.getSubjectCode())) {
                return credential.getSubjectCode();
            }
            return credential.getMemberId() == null ? null : "MEMBER:" + credential.getMemberId();
        }
        if (credential.getUserId() != null) {
            return String.valueOf(credential.getUserId());
        }
        if (StringUtils.isNotEmpty(credential.getSubjectCode())) {
            return credential.getSubjectCode();
        }
        return credential.getMemberId() == null ? null : "MEMBER:" + credential.getMemberId();
    }

    private CompetitionSceneCredential resolveDelegateCredential(CompetitionSceneVerifyReq req,
                                                                 CompetitionSceneCredential credential,
                                                                 String stateOperationType) {
        if (!CompetitionSceneConstants.STATE_OPERATION_MATERIAL.equals(stateOperationType) || req == null) {
            return null;
        }
        String raw = StringUtils.isNotEmpty(req.getDelegateCredentialToken())
                ? req.getDelegateCredentialToken() : req.getDelegateQrContent();
        String token = normalizeContent(raw);
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        CompetitionSceneCredential delegate = credentialMapper.selectCompetitionSceneCredentialByToken(token);
        if (delegate == null) {
            throw new IllegalArgumentException("代领人证件不存在或二维码无效");
        }
        if (!CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE.equals(delegate.getCredentialStatus())) {
            throw new IllegalArgumentException("代领人证件不是有效状态");
        }
        if (StringUtils.isEmpty(credential.getTeamCode())
                && delegate.getUserId() != null
                && !Objects.equals(delegate.getUserId(), credential.getUserId())) {
            throw new IllegalArgumentException("个人资料不支持他人代领");
        }
        if (StringUtils.isNotEmpty(credential.getTeamCode())
                && StringUtils.isNotEmpty(delegate.getTeamCode())
                && !Objects.equals(credential.getTeamCode(), delegate.getTeamCode())) {
            throw new IllegalArgumentException("代领人与被领取人不属于同一团队");
        }
        if (StringUtils.isNotEmpty(credential.getTeamCode()) && StringUtils.isEmpty(delegate.getTeamCode())) {
            throw new IllegalArgumentException("代领人缺少团队信息，不能代领");
        }
        return delegate;
    }

    private void fillDelegateInfo(CompetitionSceneVerifyReq req,
                                  CompetitionSceneCredential credential,
                                  OperationSubjectContext context) {
        if (!CompetitionSceneConstants.STATE_OPERATION_MATERIAL.equals(context.operationType)) {
            return;
        }
        CompetitionSceneCredential delegate = context.delegateCredential;
        if (delegate == null) {
            context.delegateUserId = req != null && req.getSubjectUserId() != null ? req.getSubjectUserId() : credential.getUserId();
            context.delegateName = StringUtils.isNotEmpty(credential.getUserName())
                    ? credential.getUserName() : (req == null ? null : req.getReceiverName());
            context.delegatePhone = StringUtils.isNotEmpty(credential.getPhone())
                    ? credential.getPhone() : (req == null ? null : req.getReceiverPhone());
            context.delegateIdSuffix = StringUtils.isNotEmpty(credential.getIdCardSuffix())
                    ? credential.getIdCardSuffix() : (req == null ? null : req.getReceiverIdSuffix());
            context.delegateCredentialId = credential.getCredentialId();
            context.delegateRelation = CompetitionSceneConstants.DELEGATE_RELATION_SELF;
            return;
        }
        context.delegateUserId = delegate.getUserId();
        context.delegateName = delegate.getUserName();
        context.delegatePhone = delegate.getPhone();
        context.delegateIdSuffix = delegate.getIdCardSuffix();
        context.delegateCredentialId = delegate.getCredentialId();
        context.delegateRelation = Objects.equals(String.valueOf(delegate.getUserId()), context.subjectCode)
                ? CompetitionSceneConstants.DELEGATE_RELATION_SELF
                : CompetitionSceneConstants.DELEGATE_RELATION_TEAM_MEMBER;
    }

    private boolean isStateDone(CompetitionSceneSubjectOperationState state) {
        return state != null && CompetitionSceneConstants.STATE_STATUS_DONE.equals(state.getOperationStatus());
    }

    private boolean hasAbility(CompetitionSceneCredential credential, String abilityName) {
        CompetitionSceneCredentialAbility ability = resolveAbility(credential);
        if ("report".equals(abilityName)) {
            return Boolean.TRUE.equals(ability.getReport());
        }
        if ("material".equals(abilityName)) {
            return Boolean.TRUE.equals(ability.getMaterial());
        }
        if ("waiting".equals(abilityName)) {
            return Boolean.TRUE.equals(ability.getWaiting());
        }
        if ("review".equals(abilityName)) {
            return Boolean.TRUE.equals(ability.getReview());
        }
        if ("resourceReservation".equals(abilityName)) {
            return Boolean.TRUE.equals(ability.getResourceReservation());
        }
        if ("vipAccess".equals(abilityName)) {
            return Boolean.TRUE.equals(ability.getVipAccess());
        }
        return false;
    }

    private CompetitionSceneCredentialAbility resolveAbility(CompetitionSceneCredential credential) {
        if (credential == null) {
            return new CompetitionSceneCredentialAbility();
        }
        if (StringUtils.isNotEmpty(credential.getAbilityJson())) {
            try {
                CompetitionSceneCredentialAbility ability =
                        JSON.parseObject(credential.getAbilityJson(), CompetitionSceneCredentialAbility.class);
                if (ability != null) {
                    return ability;
                }
            } catch (Exception ignored) {
                return buildDefaultAbility(credential);
            }
        }
        return buildDefaultAbility(credential);
    }

    private CompetitionSceneCredentialAbility buildDefaultAbility(CompetitionSceneCredential credential) {
        CompetitionSceneCredentialAbility ability = new CompetitionSceneCredentialAbility();
        String credentialType = credential == null ? null : credential.getCredentialType();
        String scopeType = resolveScopeType(credential);
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_PARTICIPANT.equals(credentialType)
                || CompetitionSceneConstants.CREDENTIAL_TYPE_COMPETITOR.equals(credentialType)) {
            ability.setReport(true);
            ability.setMaterial(true);
            if (CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE.equals(scopeType)) {
                ability.setWaiting(true);
                ability.setResourceReservation(true);
            }
        } else if (CompetitionSceneConstants.CREDENTIAL_TYPE_TEACHER.equals(credentialType)) {
            ability.setReport(true);
        } else if (CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credentialType)) {
            ability.setReport(true);
            ability.setReview(true);
        } else if (CompetitionSceneConstants.CREDENTIAL_TYPE_VIP.equals(credentialType)) {
            ability.setReport(true);
            ability.setVipAccess(true);
        }
        return ability;
    }

    private boolean isExpertReviewTarget(String targetRole) {
        return CompetitionSceneConstants.TARGET_ROLE_CAPTAIN.equals(targetRole)
                || CompetitionSceneConstants.TARGET_ROLE_MEMBER.equals(targetRole);
    }

    private String resolveCredentialRole(CompetitionSceneCredential credential, boolean allowUnknown) {
        if (credential == null) {
            return allowUnknown ? CompetitionSceneConstants.TARGET_ROLE_UNKNOWN : CompetitionSceneConstants.TARGET_ROLE_MEMBER;
        }
        String normalizedRole = normalizeTargetRole(credential.getCompetitionRoleName(), true);
        if (StringUtils.isNotEmpty(normalizedRole)) {
            return normalizedRole;
        }
        String credentialType = credential.getCredentialType();
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
                || CompetitionSceneConstants.CREDENTIAL_TYPE_COMPETITOR.equals(credentialType)) {
            return CompetitionSceneConstants.TARGET_ROLE_MEMBER;
        }
        return allowUnknown ? CompetitionSceneConstants.TARGET_ROLE_UNKNOWN : CompetitionSceneConstants.TARGET_ROLE_MEMBER;
    }

    private String normalizeTargetRole(String role, boolean allowUnknown) {
        if (StringUtils.isEmpty(role)) {
            return allowUnknown ? null : CompetitionSceneConstants.TARGET_ROLE_MEMBER;
        }
        String value = role.trim();
        String upperValue = value.toUpperCase();
        switch (upperValue) {
            case CompetitionSceneConstants.TARGET_ROLE_TEACHER:
            case "GUIDE_TEACHER":
            case "LEADER_TEACHER":
                return CompetitionSceneConstants.TARGET_ROLE_TEACHER;
            case CompetitionSceneConstants.TARGET_ROLE_MEMBER:
            case "STUDENT":
            case "PLAYER":
                return CompetitionSceneConstants.TARGET_ROLE_MEMBER;
            case CompetitionSceneConstants.TARGET_ROLE_EXPERT:
            case "JUDGE":
                return CompetitionSceneConstants.TARGET_ROLE_EXPERT;
            case CompetitionSceneConstants.TARGET_ROLE_CAPTAIN:
            case "LEADER":
                return CompetitionSceneConstants.TARGET_ROLE_CAPTAIN;
            case CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF:
                return CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF;
            case CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF:
                return CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF;
            case CompetitionSceneConstants.TARGET_ROLE_STAFF:
                return CompetitionSceneConstants.TARGET_ROLE_STAFF;
            case CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER:
                return CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER;
            default:
                break;
        }
        switch (value) {
            case "教师":
            case "指导教师":
            case "指导老师":
            case "带队老师":
                return CompetitionSceneConstants.TARGET_ROLE_TEACHER;
            case "队员":
            case "学生":
            case "选手":
            case "参赛选手":
            case "成员":
                return CompetitionSceneConstants.TARGET_ROLE_MEMBER;
            case "专家":
            case "评委":
                return CompetitionSceneConstants.TARGET_ROLE_EXPERT;
            case "队长":
            case "负责人":
                return CompetitionSceneConstants.TARGET_ROLE_CAPTAIN;
            case "发资料工作人员":
            case "资料工作人员":
            case "资料员":
            case "发资料人员":
                return CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF;
            case "签到工作人员":
            case "签到员":
                return CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF;
            case "工作人员":
            case "现场工作人员":
                return CompetitionSceneConstants.TARGET_ROLE_STAFF;
            case "志愿者":
            case "赛场志愿者":
                return CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER;
            default:
                return allowUnknown ? null : CompetitionSceneConstants.TARGET_ROLE_MEMBER;
        }
    }

    private String roleLabel(String role) {
        if (CompetitionSceneConstants.TARGET_ROLE_TEACHER.equals(role)) {
            return "教师";
        }
        if (CompetitionSceneConstants.TARGET_ROLE_MEMBER.equals(role)) {
            return "队员";
        }
        if (CompetitionSceneConstants.TARGET_ROLE_EXPERT.equals(role)) {
            return "专家";
        }
        if (CompetitionSceneConstants.TARGET_ROLE_CAPTAIN.equals(role)) {
            return "队长";
        }
        if (CompetitionSceneConstants.TARGET_ROLE_MATERIAL_STAFF.equals(role)) {
            return "资料工作人员";
        }
        if (CompetitionSceneConstants.TARGET_ROLE_CHECKIN_STAFF.equals(role)) {
            return "签到工作人员";
        }
        if (CompetitionSceneConstants.TARGET_ROLE_STAFF.equals(role)) {
            return "现场工作人员";
        }
        if (CompetitionSceneConstants.TARGET_ROLE_VOLUNTEER.equals(role)) {
            return "志愿者";
        }
        return "未配置";
    }

    private String checkSchedule(CompetitionSceneCredential credential, CompetitionSceneVerifyResult result) {
        if (!CompetitionSceneConstants.CREDENTIAL_STATUS_EFFECTIVE.equals(credential.getCredentialStatus())) {
            fillFail(result, "证件不是有效状态");
            return CompetitionSceneConstants.CHECK_RESULT_FAIL;
        }
        Date now = DateUtils.getNowDate();
        if (credential.getValidTo() != null && credential.getValidTo().before(now)) {
            fillFail(result, "证件已过期");
            return CompetitionSceneConstants.CHECK_RESULT_FAIL;
        }
        if (CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE.equals(resolveScopeType(credential))) {
            if (credential.getScheduleId() == null) {
                fillFail(result, "赛场级证件缺少赛场安排");
                return CompetitionSceneConstants.CHECK_RESULT_FAIL;
            }
            CompetitionSceneSchedule schedule = scheduleMapper.selectCompetitionSceneScheduleById(credential.getScheduleId());
            if (schedule == null || !CompetitionSceneConstants.STATUS_NORMAL.equals(schedule.getStatus())) {
                fillFail(result, "赛场安排已停用或不存在");
                return CompetitionSceneConstants.CHECK_RESULT_FAIL;
            }
        }
        return CompetitionSceneConstants.CHECK_RESULT_PASS;
    }

    private ApplyCheckResult checkApply(CompetitionSceneCredential credential) {
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credential.getCredentialType())
                || CompetitionSceneConstants.CREDENTIAL_TYPE_STAFF.equals(credential.getCredentialType())) {
            return ApplyCheckResult.skip();
        }
        if (shouldSkipApplyCheckByTargetSource(credential)) {
            return ApplyCheckResult.skip();
        }
        if (credential.getMemberId() != null) {
            CompetitionApplyInfo applyInfo =
                    competitionApplyInfoMapper.selectCompetitionApplyInfoByMemberId(credential.getMemberId());
            if (!isApplyConsistent(credential, applyInfo)) {
                return ApplyCheckResult.fail("报名信息不一致或报名状态无效", buildApplySnapshotJson(applyInfo));
            }
            return ApplyCheckResult.pass(buildApplySnapshotJson(applyInfo));
        }
        if (StringUtils.isNotEmpty(credential.getTeamCode())) {
            List<CompetitionApplyInfo> applyInfoList =
                    competitionApplyInfoMapper.selectCompetitionApplyTeamCode(credential.getTeamCode());
            if (applyInfoList != null) {
                for (CompetitionApplyInfo applyInfo : applyInfoList) {
                    if (isApplyConsistent(credential, applyInfo)) {
                        return ApplyCheckResult.pass(buildApplySnapshotJson(applyInfo));
                    }
                }
                return ApplyCheckResult.fail("团队报名信息不一致或报名状态无效", null);
            }
        }
        return ApplyCheckResult.fail("未找到可匹配的报名信息", null);
    }

    private boolean shouldSkipApplyCheckByTargetSource(CompetitionSceneCredential credential) {
        if (credential == null) {
            return false;
        }
        if (CompetitionSceneConstants.ISSUE_CHANNEL_COMPETITION_DIRECT.equals(credential.getIssueChannel())
                || CompetitionSceneConstants.ISSUE_CHANNEL_MANUAL.equals(credential.getIssueChannel())
                || CompetitionSceneConstants.ISSUE_CHANNEL_IMPORT.equals(credential.getIssueChannel())) {
            return true;
        }
        if (StringUtils.isEmpty(credential.getCredentialSnapshotJson())) {
            return false;
        }
        try {
            JSONObject snapshot = JSON.parseObject(credential.getCredentialSnapshotJson());
            String targetSource = snapshot.getString("targetSource");
            JSONObject target = snapshot.getJSONObject("target");
            if (StringUtils.isEmpty(targetSource) && target != null) {
                targetSource = target.getString("targetSource");
            }
            return CompetitionSceneConstants.TARGET_SOURCE_MANUAL.equals(targetSource)
                    || CompetitionSceneConstants.TARGET_SOURCE_IMPORT.equals(targetSource);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isApplyConsistent(CompetitionSceneCredential credential, CompetitionApplyInfo applyInfo) {
        if (applyInfo == null) {
            return false;
        }
        if (!CompetitionSceneConstants.DEL_FLAG_NORMAL.equals(applyInfo.getDelFlag())) {
            return false;
        }
        if (!Constants.CHECK_PASS.equals(applyInfo.getCheckStatus())) {
            return false;
        }
        if (!DictConstant.PAID.equals(applyInfo.getPayStatus())) {
            return false;
        }
        if (!Objects.equals(credential.getCompetitionSeriesId(), applyInfo.getCompetitionSeriesId())) {
            return false;
        }
        if (StringUtils.isNotEmpty(credential.getCompetitionTrackId())
                && !Objects.equals(credential.getCompetitionTrackId(), applyInfo.getCompetitionTrackId())) {
            return false;
        }
        return StringUtils.isEmpty(credential.getSecondLevelCode())
                || Objects.equals(credential.getSecondLevelCode(), applyInfo.getSecondLevelCode());
    }

    private String checkIdentity(CompetitionSceneVerifyReq req, CompetitionSceneCredential credential) {
        if (CompetitionSceneConstants.CREDENTIAL_TYPE_EXPERT.equals(credential.getCredentialType())) {
            return CompetitionSceneConstants.CHECK_RESULT_SKIP;
        }
        if (StringUtils.isEmpty(credential.getUserName()) && StringUtils.isEmpty(credential.getTeamName())) {
            return CompetitionSceneConstants.CHECK_RESULT_FAIL;
        }
        String operationType = resolveOperationType(req);
        if (!CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE.equals(baseOperationType(operationType))
                && req != null
                && StringUtils.isNotEmpty(req.getReceiverIdSuffix())
                && StringUtils.isNotEmpty(credential.getIdCardSuffix())
                && !Objects.equals(req.getReceiverIdSuffix(), credential.getIdCardSuffix())) {
            return CompetitionSceneConstants.CHECK_RESULT_FAIL;
        }
        return CompetitionSceneConstants.CHECK_RESULT_PASS;
    }

    private boolean isDuplicate(CompetitionSceneCredential credential, String operationType) {
        String baseOperationType = baseOperationType(operationType);
        if (CompetitionSceneConstants.OPERATION_REPORT_SIGN.equals(baseOperationType)) {
            return CompetitionSceneConstants.DONE_YES.equals(credential.getReportStatus());
        }
        if (CompetitionSceneConstants.OPERATION_MATERIAL_RECEIVE.equals(baseOperationType)) {
            return CompetitionSceneConstants.DONE_YES.equals(credential.getMaterialStatus());
        }
        if (CompetitionSceneConstants.OPERATION_WAITING_CHECK_IN.equals(baseOperationType)) {
            return CompetitionSceneConstants.DONE_YES.equals(credential.getWaitingStatus());
        }
        return false;
    }

    private String resolveOperationType(CompetitionSceneVerifyReq req) {
        if (req == null || StringUtils.isEmpty(req.getOperationType())) {
            return CompetitionSceneConstants.OPERATION_VERIFY;
        }
        return req.getOperationType().trim().toUpperCase();
    }

    private String normalizeToken(CompetitionSceneVerifyReq req) {
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
                    String qrContent = jsonObject.getString("qrContent");
                    return normalizeContent(qrContent);
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

    private void fillFail(CompetitionSceneVerifyResult result, String message) {
        result.setOperationResult(CompetitionSceneConstants.OPERATION_RESULT_FAIL);
        result.setResultMessage(message);
        result.setDuplicate(false);
    }

    private void fillDuplicate(CompetitionSceneVerifyResult result,
                               CompetitionSceneCredential credential,
                               CompetitionSceneVerifyReq req,
                               CompetitionSceneSubjectOperationState existed) {
        CompetitionSceneCredential latest = credential == null ? null
                : credentialMapper.selectCompetitionSceneCredentialById(credential.getCredentialId());
        if (latest == null) {
            latest = credential;
        }
        result.setCredential(latest);
        fillOperationMatrix(req, latest, result);
        if (CompetitionSceneConstants.STATE_OPERATION_REPORT.equals(existed.getOperationType())) {
            result.setReportState(existed);
        } else if (CompetitionSceneConstants.STATE_OPERATION_MATERIAL.equals(existed.getOperationType())) {
            result.setMaterialState(existed);
        } else if (CompetitionSceneConstants.STATE_OPERATION_WAITING.equals(existed.getOperationType())) {
            result.setWaitingState(existed);
        }
        result.setDuplicate(true);
        result.setOperationResult(CompetitionSceneConstants.OPERATION_RESULT_DUPLICATE);
        result.setResultMessage("alreadyDone：重复操作，证件状态未变更");
        writeLog(req, latest, result, CompetitionSceneConstants.OPERATION_STAGE_CONFIRM, null);
    }

    private String resolveScopeType(CompetitionSceneCredential credential) {
        if (credential == null) {
            return CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE;
        }
        if (StringUtils.isNotEmpty(credential.getScopeType())) {
            return credential.getScopeType();
        }
        return credential.getScheduleId() == null
                ? CompetitionSceneConstants.SCOPE_TYPE_COMPETITION
                : CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE;
    }

    private Long writeLog(CompetitionSceneVerifyReq req,
                          CompetitionSceneCredential credential,
                          CompetitionSceneVerifyResult result,
                          String stage,
                          String applySnapshotJson) {
        CompetitionSceneOperationLog log = new CompetitionSceneOperationLog();
        if (credential != null) {
            log.setCredentialId(credential.getCredentialId());
            log.setScheduleId(credential.getScheduleId());
            log.setTargetId(credential.getTargetId());
            log.setCompetitionSeriesId(credential.getCompetitionSeriesId());
            log.setCredentialNo(credential.getCredentialNo());
            log.setCredentialToken(credential.getCredentialToken());
            log.setTeamCode(credential.getTeamCode());
            log.setTeamName(credential.getTeamName());
            log.setMemberId(credential.getMemberId());
            log.setUserId(credential.getUserId());
            log.setUserName(credential.getUserName());
            log.setIdCardSuffix(credential.getIdCardSuffix());
            log.setCompetitionTrackId(credential.getCompetitionTrackId());
            log.setCompetitionTrackName(credential.getCompetitionTrackName());
            log.setSecondLevelCode(credential.getSecondLevelCode());
            log.setSecondLevelName(credential.getSecondLevelName());
        } else if (req != null) {
            log.setCredentialToken(normalizeToken(req));
        }
        log.setOperationType(resolveOperationType(req));
        log.setOperationStage(stage);
        log.setOperationResult(result == null ? CompetitionSceneConstants.OPERATION_RESULT_EXCEPTION : result.getOperationResult());
        log.setResultMessage(result == null ? null : result.getResultMessage());
        log.setApplyCheckResult(result == null ? null : result.getApplyCheckResult());
        log.setScheduleCheckResult(result == null ? null : result.getScheduleCheckResult());
        log.setIdentityCheckResult(result == null ? null : result.getIdentityCheckResult());
        if (req != null) {
            log.setReceiverName(req.getReceiverName());
            log.setReceiverPhone(req.getReceiverPhone());
            log.setReceiverIdSuffix(req.getReceiverIdSuffix());
            log.setOperatorOpenId(req.getOperatorOpenId());
            log.setOperatorPhone(req.getOperatorPhone());
            log.setScanIp(req.getScanIp());
            log.setDeviceInfo(req.getDeviceInfo());
            log.setRemark(normalizeRemark(req.getRemark()));
            log.setRequestPayload(JSON.toJSONString(req));
        }
        log.setOperatorUserId(currentUserId());
        log.setOperatorName(currentUsername());
        log.setOperationTime(DateUtils.getNowDate());
        log.setResponsePayload(result == null ? null : JSON.toJSONString(result));
        log.setApplySnapshotJson(applySnapshotJson);
        log.setCreateBy(currentUsername());
        log.setCreateTime(DateUtils.getNowDate());
        log.setUpdateBy(currentUsername());
        log.setUpdateTime(DateUtils.getNowDate());
        log.setVersion(0L);
        log.setDelFlag(CompetitionSceneConstants.DEL_FLAG_NORMAL);
        operationLogMapper.insertCompetitionSceneOperationLog(log);
        return log.getLogId();
    }

    private String normalizeRemark(String remark) {
        if (StringUtils.isEmpty(remark)) {
            return null;
        }
        String value = remark.trim();
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return value.length() > 500 ? value.substring(0, 500) : value;
    }

    private String buildApplySnapshotJson(CompetitionApplyInfo applyInfo) {
        if (applyInfo == null) {
            return null;
        }
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("memberId", applyInfo.getMemberId());
        snapshot.put("competitionSeriesId", applyInfo.getCompetitionSeriesId());
        snapshot.put("userId", applyInfo.getUserId());
        snapshot.put("teamCode", applyInfo.getTeamCode());
        snapshot.put("teamName", applyInfo.getTeamName());
        snapshot.put("userName", applyInfo.getUserName());
        snapshot.put("phone", applyInfo.getPhone());
        snapshot.put("schoolName", applyInfo.getSchoolName());
        snapshot.put("competitionRoleName", applyInfo.getCompetitionRoleName());
        snapshot.put("competitionTrackId", applyInfo.getCompetitionTrackId());
        snapshot.put("competitionTrackName", applyInfo.getCompetitionTrackName());
        snapshot.put("secondLevelCode", applyInfo.getSecondLevelCode());
        snapshot.put("secondLevelName", applyInfo.getSecondLevelName());
        snapshot.put("checkStatus", applyInfo.getCheckStatus());
        snapshot.put("payStatus", applyInfo.getPayStatus());
        snapshot.put("delFlag", applyInfo.getDelFlag());
        return JSON.toJSONString(snapshot);
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

    private static class OperatorContext {
        private CompetitionSceneCredential credential;
        private String primaryRole;
        private final List<String> roles = new ArrayList<>();
        private final List<Long> volunteerScheduleIds = new ArrayList<>();
    }

    private static class ActionMatrix {
        private final List<CompetitionSceneScanAction> actions = new ArrayList<>();
        private final List<CompetitionSceneScanAction> competitionActions = new ArrayList<>();
        private final List<CompetitionSceneScheduleActionGroup> scheduleActionGroups = new ArrayList<>();

        private void addAction(CompetitionSceneScanAction action) {
            if (action != null) {
                actions.add(action);
            }
        }

        private void addCompetitionAction(CompetitionSceneScanAction action) {
            if (action != null) {
                competitionActions.add(action);
                actions.add(action);
            }
        }

        private void addScheduleGroup(CompetitionSceneScheduleActionGroup group) {
            if (group == null) {
                return;
            }
            scheduleActionGroups.add(group);
            if (group.getActions() != null) {
                actions.addAll(group.getActions());
            }
        }
    }

    private static class OperationSubjectContext {
        private String operationType;
        private String scopeType;
        private Long scopeRefId;
        private String subjectType;
        private String subjectCode;
        private CompetitionSceneCredential delegateCredential;
        private Long delegateUserId;
        private String delegateName;
        private String delegatePhone;
        private String delegateIdSuffix;
        private Long delegateCredentialId;
        private String delegateRelation;
        private CompetitionSceneSubjectOperationStateQuery query;
    }

    private static class ApplyCheckResult {
        private final String checkResult;
        private final String message;
        private final String applySnapshotJson;

        private ApplyCheckResult(String checkResult, String message, String applySnapshotJson) {
            this.checkResult = checkResult;
            this.message = message;
            this.applySnapshotJson = applySnapshotJson;
        }

        private static ApplyCheckResult pass(String applySnapshotJson) {
            return new ApplyCheckResult(CompetitionSceneConstants.CHECK_RESULT_PASS, "报名信息一致", applySnapshotJson);
        }

        private static ApplyCheckResult fail(String message, String applySnapshotJson) {
            return new ApplyCheckResult(CompetitionSceneConstants.CHECK_RESULT_FAIL, message, applySnapshotJson);
        }

        private static ApplyCheckResult skip() {
            return new ApplyCheckResult(CompetitionSceneConstants.CHECK_RESULT_SKIP, "无需校验报名信息", null);
        }
    }
}
