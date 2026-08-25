package com.teaching.flowable.utils;

import cn.hutool.core.date.DateUtil;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.flowable.common.constant.ProcessConstants;
import com.teaching.flowable.common.constant.TaskConstants;
import com.teaching.flowable.core.domain.ProcessQuery;
import org.flowable.common.engine.api.query.Query;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;

import java.util.Map;

/**
 * 流程工具类
 *
 * @author konbai
 * @since 2022/12/11 03:35
 */
public class ProcessUtils {

    public static void buildProcessSearch(Query<?, ?> query, ProcessQuery process) {
        if (query instanceof ProcessDefinitionQuery) {
            buildProcessDefinitionSearch((ProcessDefinitionQuery) query, process);
        } else if (query instanceof TaskQuery) {
            buildTaskSearch((TaskQuery) query, process);
        } else if (query instanceof HistoricTaskInstanceQuery) {
            buildHistoricTaskInstanceSearch((HistoricTaskInstanceQuery) query, process);
        } else if (query instanceof HistoricProcessInstanceQuery) {
            buildHistoricProcessInstanceSearch((HistoricProcessInstanceQuery) query, process);
        }
    }

    /**
     * 构建流程定义搜索
     */
    public static void buildProcessDefinitionSearch(ProcessDefinitionQuery query, ProcessQuery process) {
        // 流程标识
        if (StringUtils.isNotBlank(process.getProcessKey())) {
            query.processDefinitionKeyLike("%" + process.getProcessKey() + "%");
        }
        // 流程名称
        if (StringUtils.isNotBlank(process.getProcessName())) {
            query.processDefinitionNameLike("%" + process.getProcessName() + "%");
        }
        // 流程分类
        if (StringUtils.isNotBlank(process.getCategory())) {
            query.processDefinitionCategory(process.getCategory());
        }
        // 流程状态
        if (StringUtils.isNotBlank(process.getState())) {
            if (SuspensionState.ACTIVE.toString().equals(process.getState())) {
                query.active();
            } else if (SuspensionState.SUSPENDED.toString().equals(process.getState())) {
                query.suspended();
            }
        }
    }

    /**
     * 构建任务搜索
     */
    public static void buildTaskSearch(TaskQuery query, ProcessQuery process) {
        Map<String, Object> params = process.getParams();
        if (StringUtils.isNotBlank(process.getProcessKey())) {
            query.processDefinitionKeyLike("%" + process.getProcessKey() + "%");
        }
        if (StringUtils.isNotBlank(process.getProcessName())) {
            query.processDefinitionNameLike("%" + process.getProcessName() + "%");
        }
        if (process.getStartDeptId()!=null) {
            query.processVariableValueEquals(ProcessConstants.DEPT_ID,process.getStartDeptId());
        }
        if (StringUtils.isNotBlank(process.getIssueType())) {
            query.processVariableValueEquals(ProcessConstants.ISSUE_TYPE,process.getIssueType());
        }
        if (StringUtils.isNotBlank(process.getUrgentLevel())) {
            query.processVariableValueEquals(ProcessConstants.URGENT_LEVEL, process.getUrgentLevel());
        }
        if (StringUtils.isNotBlank(process.getTraceabilityCode())) {
            query.processVariableValueLike(ProcessConstants.TRACEBILITY_CODE, "%" + process.getTraceabilityCode() + "%");
        }
        if(StringUtils.isNotBlank(process.getStartUserId())){
            query.processVariableValueEquals(TaskConstants.PROCESS_INITIATOR, process.getStartUserId());
        }
        if (params.get("beginTime") != null && params.get("endTime") != null) {
            query.taskCreatedAfter(DateUtil.beginOfDay(DateUtils.parseDate(params.get("beginTime"))));
            query.taskCreatedBefore(DateUtil.endOfDay(DateUtils.parseDate(params.get("endTime"))));
        }
    }

    private static void buildHistoricTaskInstanceSearch(HistoricTaskInstanceQuery query, ProcessQuery process) {
        Map<String, Object> params = process.getParams();
        if (StringUtils.isNotBlank(process.getProcessKey())) {
            query.processDefinitionKeyLike("%" + process.getProcessKey() + "%");
        }
        if (StringUtils.isNotBlank(process.getProcessName())) {
            query.processDefinitionNameLike("%" + process.getProcessName() + "%");
        }
        if (StringUtils.isNotBlank(process.getIssueType())) {
            query.processVariableValueEquals(ProcessConstants.ISSUE_TYPE,process.getIssueType());
        }
        if (StringUtils.isNotBlank(process.getUrgentLevel())) {
            query.processVariableValueEquals(ProcessConstants.URGENT_LEVEL, process.getUrgentLevel());
        }
        if (StringUtils.isNotBlank(process.getTraceabilityCode())) {
            query.processVariableValueLike(ProcessConstants.TRACEBILITY_CODE, "%" + process.getTraceabilityCode() + "%");
        }
        if (StringUtils.isNotBlank(process.getStartUserId())) {
            query.processVariableValueEquals(TaskConstants.PROCESS_INITIATOR, process.getStartUserId());
        }
        if (params.get("beginTime") != null && params.get("endTime") != null) {
            query.taskCompletedAfter(DateUtil.beginOfDay(DateUtils.parseDate(params.get("beginTime"))));
            query.taskCompletedBefore(DateUtil.endOfDay(DateUtils.parseDate(params.get("endTime"))));
        }
    }

    /**
     * 构建历史流程实例搜索
     */
    public static void buildHistoricProcessInstanceSearch(HistoricProcessInstanceQuery query, ProcessQuery process) {
        Map<String, Object> params = process.getParams();
        // 流程标识
        if (StringUtils.isNotBlank(process.getProcessKey())) {
            query.processDefinitionKey(process.getProcessKey());
        }
        // 流程名称
        if (StringUtils.isNotBlank(process.getProcessName())) {
            query.processDefinitionName(process.getProcessName());
        }
        // 流程名称
        if (StringUtils.isNotBlank(process.getCategory())) {
            query.processDefinitionCategory(process.getCategory());
        }
        if (StringUtils.isNotBlank(process.getStartUserId())) {
            query.startedBy(process.getStartUserId());
        }
        if (process.getStartDeptId()!=null) {
            query.variableValueEquals(ProcessConstants.DEPT_ID,process.getStartDeptId());
        }
        if (StringUtils.isNotBlank(process.getIssueType())) {
            query.variableValueEquals(ProcessConstants.ISSUE_TYPE,process.getIssueType());
        }
        if (StringUtils.isNotBlank(process.getUrgentLevel())) {
            query.variableValueEquals(ProcessConstants.URGENT_LEVEL, process.getUrgentLevel());
        }
        if (StringUtils.isNotBlank(process.getTraceabilityCode())) {
            query.variableValueLike(ProcessConstants.TRACEBILITY_CODE, "%" + process.getTraceabilityCode() + "%");
        }
        if (StringUtils.isNotBlank(process.getStartUserId())) {
            query.variableValueEquals(TaskConstants.PROCESS_INITIATOR, process.getStartUserId());
        }
        if (params.get("beginTime") != null && params.get("endTime") != null) {
            query.startedAfter(DateUtil.beginOfDay(DateUtils.parseDate(params.get("beginTime"))));
            query.startedBefore(DateUtil.endOfDay(DateUtils.parseDate(params.get("endTime"))));
        }
    }

}
