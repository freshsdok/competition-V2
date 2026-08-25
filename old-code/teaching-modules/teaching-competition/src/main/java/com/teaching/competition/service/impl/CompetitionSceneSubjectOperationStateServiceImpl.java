package com.teaching.competition.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.competition.contant.CompetitionSceneConstants;
import com.teaching.competition.domain.CompetitionSceneCredential;
import com.teaching.competition.domain.CompetitionSceneSubjectOperationState;
import com.teaching.competition.domain.CompetitionSceneSubjectOperationStateQuery;
import com.teaching.competition.mapper.CompetitionSceneSubjectOperationStateMapper;
import com.teaching.competition.service.ICompetitionSceneSubjectOperationStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 赛事现场主体操作状态Service业务层处理。
 */
@Service
public class CompetitionSceneSubjectOperationStateServiceImpl implements ICompetitionSceneSubjectOperationStateService {

    @Autowired
    private CompetitionSceneSubjectOperationStateMapper stateMapper;

    @Override
    public CompetitionSceneSubjectOperationState selectDoneOperationState(CompetitionSceneSubjectOperationStateQuery query) {
        if (query == null) {
            return null;
        }
        query.setOperationStatus(CompetitionSceneConstants.STATE_STATUS_DONE);
        query.setDeleted(CompetitionSceneConstants.STATE_DELETED_NO);
        return stateMapper.selectDoneOperationState(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionSceneSubjectOperationState insertDoneOperationStateIfAbsent(CompetitionSceneSubjectOperationState state) {
        normalizeState(state);
        CompetitionSceneSubjectOperationStateQuery query = new CompetitionSceneSubjectOperationStateQuery();
        query.setCompetitionSeriesId(state.getCompetitionSeriesId());
        query.setScopeType(state.getScopeType());
        query.setScopeRefId(state.getScopeRefId());
        query.setSubjectType(state.getSubjectType());
        query.setSubjectCode(state.getSubjectCode());
        query.setOperationType(state.getOperationType());
        CompetitionSceneSubjectOperationState existed = selectDoneOperationState(query);
        if (existed != null) {
            return existed;
        }
        int inserted = stateMapper.insertDoneOperationStateIfAbsent(state);
        if (inserted <= 0) {
            return selectDoneOperationState(query);
        }
        return selectDoneOperationState(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelDoneOperationState(CompetitionSceneSubjectOperationStateQuery query) {
        if (query == null) {
            return 0;
        }
        query.setOperationStatus(CompetitionSceneConstants.STATE_STATUS_DONE);
        query.setDeleted(CompetitionSceneConstants.STATE_DELETED_NO);
        return stateMapper.cancelDoneOperationState(query, currentUsername());
    }

    @Override
    public void updateLastLogId(Long stateId, Long lastLogId) {
        if (stateId != null && lastLogId != null) {
            stateMapper.updateOperationStateLastLogId(stateId, lastLogId, currentUsername());
        }
    }

    @Override
    public void fillCredentialOperationStates(List<CompetitionSceneCredential> credentials) {
        if (credentials == null || credentials.isEmpty()) {
            return;
        }
        for (CompetitionSceneCredential credential : credentials) {
            fillCredentialOperationState(credential, CompetitionSceneConstants.STATE_OPERATION_REPORT);
            fillCredentialOperationState(credential, CompetitionSceneConstants.STATE_OPERATION_MATERIAL);
            fillCredentialOperationState(credential, CompetitionSceneConstants.STATE_OPERATION_WAITING);
        }
    }

    private void fillCredentialOperationState(CompetitionSceneCredential credential, String operationType) {
        CompetitionSceneSubjectOperationStateQuery query = buildQuery(credential, operationType);
        if (query == null) {
            return;
        }
        CompetitionSceneSubjectOperationState state = selectDoneOperationState(query);
        if (state == null) {
            return;
        }
        if (CompetitionSceneConstants.STATE_OPERATION_REPORT.equals(operationType)) {
            credential.setReportStateStatus(state.getOperationStatus());
            credential.setReportStateTime(state.getOperationTime());
            credential.setReportStatus(CompetitionSceneConstants.DONE_YES);
            credential.setReportTime(state.getOperationTime());
        } else if (CompetitionSceneConstants.STATE_OPERATION_MATERIAL.equals(operationType)) {
            credential.setMaterialStateStatus(state.getOperationStatus());
            credential.setMaterialStateTime(state.getOperationTime());
            credential.setMaterialDelegateName(state.getDelegateName());
            credential.setMaterialDelegateRelation(state.getDelegateRelation());
            credential.setMaterialStatus(CompetitionSceneConstants.DONE_YES);
            credential.setMaterialTime(state.getOperationTime());
            if (StringUtils.isNotEmpty(state.getDelegateName())) {
                credential.setMaterialReceiverName(state.getDelegateName());
            }
        } else if (CompetitionSceneConstants.STATE_OPERATION_WAITING.equals(operationType)) {
            credential.setWaitingStateStatus(state.getOperationStatus());
            credential.setWaitingStateTime(state.getOperationTime());
            credential.setWaitingStatus(CompetitionSceneConstants.DONE_YES);
            credential.setWaitingTime(state.getOperationTime());
        }
    }

    private CompetitionSceneSubjectOperationStateQuery buildQuery(CompetitionSceneCredential credential, String operationType) {
        if (credential == null || credential.getCompetitionSeriesId() == null) {
            return null;
        }
        CompetitionSceneSubjectOperationStateQuery query = new CompetitionSceneSubjectOperationStateQuery();
        query.setCompetitionSeriesId(credential.getCompetitionSeriesId());
        query.setOperationType(operationType);
        if (CompetitionSceneConstants.STATE_OPERATION_WAITING.equals(operationType)) {
            Long scheduleId = credential.getScheduleId();
            if (scheduleId == null) {
                return null;
            }
            query.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_SCHEDULE);
            query.setScopeRefId(scheduleId);
        } else {
            query.setScopeType(CompetitionSceneConstants.SCOPE_TYPE_COMPETITION);
            query.setScopeRefId(credential.getCompetitionSeriesId());
        }
        String subjectType = resolveSubjectType(credential, operationType);
        String subjectCode = resolveSubjectCode(credential, subjectType, operationType);
        if (StringUtils.isEmpty(subjectType) || StringUtils.isEmpty(subjectCode)) {
            return null;
        }
        query.setSubjectType(subjectType);
        query.setSubjectCode(subjectCode);
        return query;
    }

    private String resolveSubjectType(CompetitionSceneCredential credential, String operationType) {
        if (CompetitionSceneConstants.STATE_OPERATION_MATERIAL.equals(operationType)) {
            return CompetitionSceneConstants.SUBJECT_TYPE_USER;
        }
        if (CompetitionSceneConstants.SUBJECT_TYPE_USER.equals(credential.getSubjectType())
                || CompetitionSceneConstants.SUBJECT_TYPE_EXPERT.equals(credential.getSubjectType())
                || CompetitionSceneConstants.SUBJECT_TYPE_STAFF.equals(credential.getSubjectType())
                || CompetitionSceneConstants.SUBJECT_TYPE_VIP.equals(credential.getSubjectType())
                || CompetitionSceneConstants.SUBJECT_TYPE_TEMP.equals(credential.getSubjectType())) {
            return credential.getSubjectType();
        }
        if (CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(credential.getSubjectType())) {
            return CompetitionSceneConstants.SUBJECT_TYPE_TEAM;
        }
        if (CompetitionSceneConstants.DIMENSION_TEAM.equals(credential.getConfigDimension())
                && StringUtils.isNotEmpty(credential.getTeamCode())) {
            return CompetitionSceneConstants.SUBJECT_TYPE_TEAM;
        }
        return CompetitionSceneConstants.SUBJECT_TYPE_USER;
    }

    private String resolveSubjectCode(CompetitionSceneCredential credential, String subjectType, String operationType) {
        if (CompetitionSceneConstants.SUBJECT_TYPE_TEAM.equals(subjectType)) {
            return StringUtils.isNotEmpty(credential.getSubjectCode()) ? credential.getSubjectCode() : credential.getTeamCode();
        }
        if (CompetitionSceneConstants.STATE_OPERATION_MATERIAL.equals(operationType)) {
            if (credential.getUserId() != null) {
                return String.valueOf(credential.getUserId());
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

    private void normalizeState(CompetitionSceneSubjectOperationState state) {
        Date now = DateUtils.getNowDate();
        state.setOperationStatus(CompetitionSceneConstants.STATE_STATUS_DONE);
        if (state.getOperationTime() == null) {
            state.setOperationTime(now);
        }
        state.setDeleted(CompetitionSceneConstants.STATE_DELETED_NO);
        if (state.getCreateTime() == null) {
            state.setCreateTime(now);
        }
        state.setUpdateTime(now);
        if (StringUtils.isEmpty(state.getCreateBy())) {
            state.setCreateBy(currentUsername());
        }
        state.setUpdateBy(currentUsername());
    }

    private String currentUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception e) {
            return "system";
        }
    }
}
