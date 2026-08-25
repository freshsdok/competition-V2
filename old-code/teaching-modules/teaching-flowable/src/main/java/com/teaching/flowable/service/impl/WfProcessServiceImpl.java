package com.teaching.flowable.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teaching.common.core.JsonUtils;
import com.teaching.common.core.constant.SecurityConstants;
import com.teaching.common.core.domain.R;
import com.teaching.common.core.exception.ServiceException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.PageUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.flowable.common.constant.ProcessConstants;
import com.teaching.flowable.common.constant.TaskConstants;
import com.teaching.flowable.common.enums.ProcessStatus;
import com.teaching.flowable.core.FormConf;
import com.teaching.flowable.core.domain.ProcessQuery;
import com.teaching.flowable.core.domain.model.PageQuery;
import com.teaching.flowable.core.page.TableDataInfo;
import com.teaching.flowable.domain.WfDeployForm;
import com.teaching.flowable.domain.WfReport;
import com.teaching.flowable.domain.WfReportNode;
import com.teaching.flowable.domain.dto.WfMetaInfoDto;
import com.teaching.flowable.domain.vo.*;
import com.teaching.flowable.factory.FlowServiceFactory;
import com.teaching.flowable.flow.FlowableUtils;
import com.teaching.flowable.mapper.WfCopyMapper;
import com.teaching.flowable.mapper.WfDeployFormMapper;
import com.teaching.flowable.mapper.WfReportMapper;
import com.teaching.flowable.service.IOperationFlowService;
import com.teaching.flowable.service.IWfProcessService;
import com.teaching.flowable.service.IWfTaskService;
import com.teaching.flowable.utils.ModelUtils;
import com.teaching.flowable.utils.ProcessUtils;
import com.teaching.flowable.utils.TaskUtils;
import com.teaching.system.api.OrderService;
import com.teaching.system.api.RemoteUserService;
import com.teaching.system.api.domain.OperationFlow;
import com.teaching.system.api.domain.SysRole;
import com.teaching.system.api.domain.SysUser;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricActivityInstanceQuery;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.engine.task.Comment;
import org.flowable.identitylink.api.IdentityLinkInfo;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Duration;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author KonBAI
 * @createTime 2022/3/24 18:57
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WfProcessServiceImpl extends FlowServiceFactory implements IWfProcessService {

    @Autowired
    private RemoteUserService remoteUserService;
    private final IWfTaskService wfTaskService;
    private final WfDeployFormMapper deployFormMapper;
    private final WfReportMapper reportMapper;
    private final HistoryService historyService;
    private final IOperationFlowService operationFlowService;

    @Autowired
    private OrderService orderService;

    /**
     * 流程定义列表
     *
     * @param pageQuery 分页参数
     * @return 流程定义分页列表数据
     */
    @Override
    public TableDataInfo<WfDefinitionVo> selectPageStartProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfDefinitionVo> page = new Page<>();
        // 流程定义列表数据查询
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .active()
                //.processDefinitionTenantId("1")
                .orderByProcessDefinitionKey()
                .asc();

        List<ProcessDefinition> list = processDefinitionQuery.list();
        List<ProcessDefinition> collect = list.stream().filter(e -> "1".equals(e.getDescription())).collect(Collectors.toList());
        List<ProcessDefinition> definitionList = PageUtils.paginate(collect, pageQuery.getPageNum(), pageQuery.getPageSize());
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(processDefinitionQuery, processQuery);
        long pageTotal = processDefinitionQuery.count();
        if (pageTotal <= 0) {
            return TableDataInfo.build();
        }
        //int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        //List<ProcessDefinition> definitionList = processDefinitionQuery.listPage(offset, pageQuery.getPageSize());

        List<WfDefinitionVo> definitionVoList = new ArrayList<>();
        for (ProcessDefinition processDefinition : definitionList) {
            String deploymentId = processDefinition.getDeploymentId();
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
            WfDefinitionVo vo = new WfDefinitionVo();
            vo.setDefinitionId(processDefinition.getId());
            vo.setProcessKey(processDefinition.getKey());
            vo.setProcessName(processDefinition.getName());
            vo.setVersion(processDefinition.getVersion());
            vo.setDeploymentId(processDefinition.getDeploymentId());
            vo.setSuspended(processDefinition.isSuspended());
            // 流程定义时间
            vo.setCategory(deployment.getCategory());
            vo.setDeploymentTime(deployment.getDeploymentTime());
            String key = processDefinition.getKey();
            Model model = repositoryService.createModelQuery().modelKey(key).latestVersion().singleResult();
            WfMetaInfoDto metaInfo = JsonUtils.parseObject(model.getMetaInfo(), WfMetaInfoDto.class);
            vo.setDescription("");
            if (metaInfo != null) {
                vo.setDescription(metaInfo.getDescription());
            }
            definitionVoList.add(vo);
        }
       /* page.setRecords(definitionVoList);
        page.setTotal(pageTotal);
        return TableDataInfo.build(page);*/
        page.setTotal(collect.size());
        page.setRecords(definitionVoList);
        return TableDataInfo.build(page);
    }

    @Override
    public WfDefinitionVo selectStartProcess(ProcessQuery processQuery) {
        if (StringUtils.isBlank(processQuery.getTeamCode()) || StringUtils.isBlank(processQuery.getCategory())) {
            throw new RuntimeException("团队编码和流程类型不能为空");
        }
        //一个团队三种流程同时只能发起一个流程，只能有一个是进行中的
        List<OperationFlow> operationFlow = operationFlowService.selectOperationFlowList(new OperationFlow(processQuery.getTeamCode(), null, "running"));
        if (CollectionUtils.isNotEmpty(operationFlow)) {
            throw new RuntimeException("当前团队存在进行中的流程，请勿重复发起");
        }
        //流程定义列表数据查询
        return getWfDefinitionVoByProcessQuery(processQuery);
    }

    public WfDefinitionVo getWfDefinitionVoByProcessQuery(ProcessQuery processQuery) {
        //流程定义列表数据查询
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .active()
                .orderByProcessDefinitionKey()
                .asc();
        ProcessUtils.buildProcessSearch(processDefinitionQuery, processQuery);
        List<ProcessDefinition> list = processDefinitionQuery.list();
        ProcessDefinition processDefinition = list.stream().filter(e -> "1".equals(e.getDescription())).findFirst().orElse(null);
        if (processDefinition != null) {
            String deploymentId = processDefinition.getDeploymentId();
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
            WfDefinitionVo vo = new WfDefinitionVo();
            vo.setDefinitionId(processDefinition.getId());
            vo.setProcessKey(processDefinition.getKey());
            vo.setProcessName(processDefinition.getName());
            vo.setVersion(processDefinition.getVersion());
            vo.setDeploymentId(processDefinition.getDeploymentId());
            vo.setSuspended(processDefinition.isSuspended());
            // 流程定义时间
            vo.setCategory(deployment.getCategory());
            vo.setDeploymentTime(deployment.getDeploymentTime());
            String key = processDefinition.getKey();
            Model model = repositoryService.createModelQuery().modelKey(key).latestVersion().singleResult();
            WfMetaInfoDto metaInfo = JsonUtils.parseObject(model.getMetaInfo(), WfMetaInfoDto.class);
            vo.setDescription("");
            if (metaInfo != null) {
                vo.setDescription(metaInfo.getDescription());
            }
            return vo;
        }
        return null;
    }

    @Override
    public List<WfDefinitionVo> selectStartProcessList(ProcessQuery processQuery) {
        // 流程定义列表数据查询
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .active()
                .orderByProcessDefinitionKey()
                .asc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(processDefinitionQuery, processQuery);

        List<ProcessDefinition> definitionList = processDefinitionQuery.list();

        List<WfDefinitionVo> definitionVoList = new ArrayList<>();
        for (ProcessDefinition processDefinition : definitionList) {
            String deploymentId = processDefinition.getDeploymentId();
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
            WfDefinitionVo vo = new WfDefinitionVo();
            vo.setDefinitionId(processDefinition.getId());
            vo.setProcessKey(processDefinition.getKey());
            vo.setProcessName(processDefinition.getName());
            vo.setVersion(processDefinition.getVersion());
            vo.setDeploymentId(processDefinition.getDeploymentId());
            vo.setSuspended(processDefinition.isSuspended());
            // 流程定义时间
            vo.setCategory(deployment.getCategory());
            vo.setDeploymentTime(deployment.getDeploymentTime());
            definitionVoList.add(vo);
        }
        return definitionVoList;
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageOwnProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery()
                .includeProcessVariables()
                .startedBy(TaskUtils.getUserId())
                .orderByProcessInstanceStartTime()
                .desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(historicProcessInstanceQuery, processQuery);
        if (StringUtils.isNotBlank(processQuery.getIssueType())) {
            historicProcessInstanceQuery.variableValueEquals(ProcessConstants.ISSUE_TYPE, processQuery.getIssueType());
        }
        if (StringUtils.isNotBlank(processQuery.getUrgentLevel())) {
            historicProcessInstanceQuery.variableValueEquals(ProcessConstants.URGENT_LEVEL, processQuery.getUrgentLevel());
        }
        if (StringUtils.isNotBlank(processQuery.getTraceabilityCode())) {
            historicProcessInstanceQuery.variableValueLike(ProcessConstants.TRACEBILITY_CODE, "%" + processQuery.getTraceabilityCode() + "%");
        }
        if (StringUtils.isNotBlank(processQuery.getState())) {
            historicProcessInstanceQuery.variableValueEquals(ProcessConstants.PROCESS_STATUS_KEY, processQuery.getState());
        }
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<HistoricProcessInstance> historicProcessInstances = historicProcessInstanceQuery
                .listPage(offset, pageQuery.getPageSize());
        page.setTotal(historicProcessInstanceQuery.count());
        List<WfTaskVo> taskVoList = new ArrayList<>();
        for (HistoricProcessInstance hisIns : historicProcessInstances) {
            List<WfProcNodeVo> wfProcNodeVos = historyProcNodeList(hisIns);
            WfTaskVo taskVo = new WfTaskVo();
            // 获取流程状态
            HistoricVariableInstance processStatusVariable = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(hisIns.getId())
                    .variableName(ProcessConstants.PROCESS_STATUS_KEY)
                    .singleResult();
            String processStatus = null;
            if (ObjectUtil.isNotNull(processStatusVariable)) {
                processStatus = Convert.toStr(processStatusVariable.getValue());
            }
            // 兼容旧流程
            if (processStatus == null) {
                processStatus = ObjectUtil.isNull(hisIns.getEndTime()) ? ProcessStatus.RUNNING.getStatus() : ProcessStatus.COMPLETED.getStatus();
            }
            taskVo.setProcessStatus(processStatus);
            taskVo.setCreateTime(hisIns.getStartTime());
            taskVo.setFinishTime(hisIns.getEndTime());
            taskVo.setProcInsId(hisIns.getId());

            // 计算耗时
            if (Objects.nonNull(hisIns.getEndTime())) {
                taskVo.setDuration(DateUtils.getDatePoor(hisIns.getEndTime(), hisIns.getStartTime()));
            } else {
                taskVo.setDuration(DateUtils.getDatePoor(DateUtils.getNowDate(), hisIns.getStartTime()));
            }
            // 获取流程实例
            HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(hisIns.getId())
                    .includeProcessVariables()
                    .singleResult();
            List<WfProcNodeVo> wfProcNodeVoss = historyProcNodeList(historicProcIns);
            //获取当前节点审核人
            WfProcNodeVo wfProcNodeVo = wfProcNodeVoss.get(0);
            if (wfProcNodeVo.getAssigneeName() == null) {
                wfProcNodeVo.setAssigneeName("");
            }
            taskVo.setAssigneeName(wfProcNodeVo.getAssigneeName());

            // 流程部署实例信息
            Deployment deployment = repositoryService.createDeploymentQuery()
                    .deploymentId(hisIns.getDeploymentId()).singleResult();

            taskVo.setDeployId(hisIns.getDeploymentId());
            taskVo.setProcDefId(hisIns.getProcessDefinitionId());
            taskVo.setProcDefName(hisIns.getProcessDefinitionName());
            taskVo.setProcDefVersion(hisIns.getProcessDefinitionVersion());
            taskVo.setCategory(deployment.getCategory());
            // 当前所处流程
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(hisIns.getId()).includeIdentityLinks().list();
            if (CollUtil.isNotEmpty(taskList)) {
                String collect = taskList.stream().map(Task::getTaskDefinitionKey).filter(StringUtils::isNotEmpty).collect(Collectors.joining(","));
                taskVo.setTaskDefKey(collect);
                String join = taskList.stream()
                        .flatMap(task -> task.getIdentityLinks().stream())
                        .map(IdentityLinkInfo::getUserId)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.joining(","));
                taskVo.setAssigneeIds(join);
                taskVo.setTaskName(taskList.stream().map(Task::getName).filter(StringUtils::isNotEmpty).collect(Collectors.joining(",")));
            }
            Long userId = Long.parseLong(hisIns.getStartUserId());
           /* String nickName = userService.selectNickNameById(userId);
            taskVo.setStartUserName(nickName);*/
            taskVo.setStartUserId(userId);
            Map<String, Object> processVariables = hisIns.getProcessVariables();
            taskVo.setIssueType(MapUtil.getStr(processVariables, ProcessConstants.ISSUE_TYPE));
            taskVo.setUrgentLevel(MapUtil.getStr(processVariables, "urgentLevel"));
            taskVo.setTraceabilityCode(MapUtil.getStr(processVariables, "traceabilityCode"));
            taskVo.setContent(MapUtil.getStr(processVariables, "content"));
            taskVo.setCancelFlag(wfProcNodeVos.size() <= 2);
            taskVoList.add(taskVo);
        }
        page.setRecords(taskVoList);
        return TableDataInfo.build(page);
    }

    @Override
    public List<WfTaskVo> selectOwnProcessList(ProcessQuery processQuery) {
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery()
                .startedBy(TaskUtils.getUserId())
                .orderByProcessInstanceStartTime()
                .desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(historicProcessInstanceQuery, processQuery);
        List<HistoricProcessInstance> historicProcessInstances = historicProcessInstanceQuery.list();
        List<WfTaskVo> taskVoList = new ArrayList<>();
        for (HistoricProcessInstance hisIns : historicProcessInstances) {
            WfTaskVo taskVo = new WfTaskVo();
            taskVo.setCreateTime(hisIns.getStartTime());
            taskVo.setFinishTime(hisIns.getEndTime());
            taskVo.setProcInsId(hisIns.getId());

            // 计算耗时
            if (Objects.nonNull(hisIns.getEndTime())) {
                taskVo.setDuration(DateUtils.getDatePoor(hisIns.getEndTime(), hisIns.getStartTime()));
            } else {
                taskVo.setDuration(DateUtils.getDatePoor(DateUtils.getNowDate(), hisIns.getStartTime()));
            }
            // 流程部署实例信息
            Deployment deployment = repositoryService.createDeploymentQuery()
                    .deploymentId(hisIns.getDeploymentId()).singleResult();
            taskVo.setDeployId(hisIns.getDeploymentId());
            taskVo.setProcDefId(hisIns.getProcessDefinitionId());
            taskVo.setProcDefName(hisIns.getProcessDefinitionName());
            taskVo.setProcDefVersion(hisIns.getProcessDefinitionVersion());
            taskVo.setCategory(deployment.getCategory());
            // 当前所处流程
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(hisIns.getId()).includeIdentityLinks().list();
            if (CollUtil.isNotEmpty(taskList)) {
                taskVo.setTaskName(taskList.stream().map(Task::getName).filter(StringUtils::isNotEmpty).collect(Collectors.joining(",")));
            }
            taskVoList.add(taskVo);
        }
        return taskVoList;
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageTodoProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        TaskQuery taskQuery = taskService.createTaskQuery()
                .active()
                .includeProcessVariables()
                .processVariableValueEquals(ProcessConstants.PROCESS_STATUS_KEY, ProcessStatus.RUNNING.getStatus())
                .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
                .taskCandidateOrAssigned(TaskUtils.getUserId())
                .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        page.setTotal(taskQuery.count());
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<Task> taskList = taskQuery.listPage(offset, pageQuery.getPageSize());
        List<WfTaskVo> flowList = new ArrayList<>();
        for (Task task : taskList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(task.getId());
            flowTask.setTaskDefKey(task.getTaskDefinitionKey());
            flowTask.setCreateTime(task.getCreateTime());
            flowTask.setProcDefId(task.getProcessDefinitionId());
            flowTask.setTaskName(task.getName());
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
            R<SysUser> userInfoById = remoteUserService.getUserInfoById(userId, SecurityConstants.INNER);
            if (R.isSuccess(userInfoById) && userInfoById.getData() != null) {
                flowTask.setStartUserName(userInfoById.getData().getNickName());
            }
            flowTask.setStartUserId(userId);

            // 流程变量
            Map<String, Object> processVariables = task.getProcessVariables();
            flowTask.setIssueType(MapUtil.getStr(processVariables, ProcessConstants.ISSUE_TYPE));
            flowTask.setUrgentLevel(MapUtil.getStr(processVariables, ProcessConstants.URGENT_LEVEL));
            flowTask.setTraceabilityCode(MapUtil.getStr(processVariables, ProcessConstants.TRACEBILITY_CODE));
            flowTask.setProcessStatus(MapUtil.getStr(processVariables, ProcessConstants.PROCESS_STATUS_KEY));
            flowTask.setContent(MapUtil.getStr(processVariables, "content"));
            flowList.add(flowTask);
        }
        page.setRecords(flowList);
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageTodoProcessList2(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        TaskQuery taskQuery = taskService.createTaskQuery()
                .active()
                .includeProcessVariables()
                .processVariableValueEquals(ProcessConstants.PROCESS_STATUS_KEY, ProcessStatus.RUNNING.getStatus())
                .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
                .taskCandidateOrAssigned(TaskUtils.getUserId())
                .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        List<Task> list = taskQuery.list();
        String loginNickName = SecurityUtils.getLoginUser().getSysUser().getNickName();
        List<WfTaskVo> flowList = new ArrayList<>();
        for (Task task : list) {
            HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .includeProcessVariables()
                    .singleResult();
            List<WfProcNodeVo> wfProcNodeVos = historyProcNodeList(historicProcIns);
            WfProcNodeVo wfProcNodeVo = wfProcNodeVos.get(0);
            String assigneeName = wfProcNodeVo.getAssigneeName();
            if (Arrays.asList(assigneeName.split(",")).contains(loginNickName)) {
                WfTaskVo flowTask = new WfTaskVo();
                // 当前流程信息
                flowTask.setTaskId(task.getId());
                flowTask.setTaskDefKey(task.getTaskDefinitionKey());
                flowTask.setCreateTime(task.getCreateTime());
                flowTask.setProcDefId(task.getProcessDefinitionId());
                flowTask.setTaskName(task.getName());
                // 流程定义信息
                ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionId(task.getProcessDefinitionId())
                        .singleResult();
                flowTask.setDeployId(pd.getDeploymentId());
                flowTask.setProcDefName(pd.getName());
                flowTask.setProcDefVersion(pd.getVersion());
                flowTask.setProcInsId(task.getProcessInstanceId());

                // 流程发起人信息
                HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(task.getProcessInstanceId())
                        .singleResult();
                Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
                /*String nickName = userService.selectNickNameById(userId);
                flowTask.setStartUserName(nickName);*/
                flowTask.setStartUserId(userId);

                // 流程变量
                Map<String, Object> processVariables = task.getProcessVariables();
                flowTask.setIssueType(MapUtil.getStr(processVariables, ProcessConstants.ISSUE_TYPE));
                flowTask.setUrgentLevel(MapUtil.getStr(processVariables, ProcessConstants.URGENT_LEVEL));
                flowTask.setTraceabilityCode(MapUtil.getStr(processVariables, ProcessConstants.TRACEBILITY_CODE));
                flowTask.setProcessStatus(MapUtil.getStr(processVariables, ProcessConstants.PROCESS_STATUS_KEY));
                flowTask.setContent(MapUtil.getStr(processVariables, "content"));
                flowList.add(flowTask);
            }
        }
        if (CollectionUtils.isEmpty(flowList)) {
            return TableDataInfo.build();
        }
        List<WfTaskVo> paginate = PageUtils.paginate(flowList, pageQuery.getPageNum(), pageQuery.getPageSize());
        page.setTotal(flowList.size());
        page.setRecords(paginate);
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageWaitingProcessList2(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        TaskQuery taskQuery = taskService.createTaskQuery()
                .active()
                .includeProcessVariables()
                .processVariableValueEquals(ProcessConstants.PROCESS_STATUS_KEY, ProcessStatus.WAITING.getStatus())
                .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
                .taskCandidateOrAssigned(TaskUtils.getUserId())
                .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        List<Task> list = taskQuery.list();
        String loginNickName = SecurityUtils.getLoginUser().getSysUser().getNickName();
        List<WfTaskVo> flowList = new ArrayList<>();
        for (Task task : list) {
            HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .includeProcessVariables()
                    .singleResult();
            List<WfProcNodeVo> wfProcNodeVos = historyProcNodeList(historicProcIns);
            WfProcNodeVo wfProcNodeVo = wfProcNodeVos.get(0);
            String assigneeName = wfProcNodeVo.getAssigneeName();
            if (Arrays.asList(assigneeName.split(",")).contains(loginNickName)) {
                WfTaskVo flowTask = new WfTaskVo();
                // 当前流程信息
                flowTask.setTaskId(task.getId());
                flowTask.setTaskDefKey(task.getTaskDefinitionKey());
                flowTask.setCreateTime(task.getCreateTime());
                flowTask.setProcDefId(task.getProcessDefinitionId());
                flowTask.setTaskName(task.getName());
                // 流程定义信息
                ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionId(task.getProcessDefinitionId())
                        .singleResult();
                flowTask.setDeployId(pd.getDeploymentId());
                flowTask.setProcDefName(pd.getName());
                flowTask.setProcDefVersion(pd.getVersion());
                flowTask.setProcInsId(task.getProcessInstanceId());

                // 流程发起人信息
                HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(task.getProcessInstanceId())
                        .singleResult();
                Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
                R<SysUser> userInfoById = remoteUserService.getUserInfoById(userId, SecurityConstants.INNER);
                if (R.isSuccess(userInfoById) && userInfoById.getData() != null) {
                    flowTask.setStartUserName(userInfoById.getData().getNickName());
                }
                flowTask.setStartUserId(userId);

                // 流程变量
                Map<String, Object> processVariables = task.getProcessVariables();
                flowTask.setIssueType(MapUtil.getStr(processVariables, ProcessConstants.ISSUE_TYPE));
                flowTask.setUrgentLevel(MapUtil.getStr(processVariables, ProcessConstants.URGENT_LEVEL));
                flowTask.setTraceabilityCode(MapUtil.getStr(processVariables, ProcessConstants.TRACEBILITY_CODE));
                flowTask.setProcessStatus(MapUtil.getStr(processVariables, ProcessConstants.PROCESS_STATUS_KEY));
                flowTask.setContent(MapUtil.getStr(processVariables, "content"));
                flowList.add(flowTask);
            }
        }
        if (CollectionUtils.isEmpty(flowList)) {
            return TableDataInfo.build();
        }
        List<WfTaskVo> paginate = PageUtils.paginate(flowList, pageQuery.getPageNum(), pageQuery.getPageSize());
        page.setTotal(flowList.size());
        page.setRecords(paginate);
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageWaitingProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        TaskQuery taskQuery = taskService.createTaskQuery()
                .active()
                .includeProcessVariables()
                .processVariableValueEquals(ProcessConstants.PROCESS_STATUS_KEY, ProcessStatus.WAITING.getStatus())
                .taskCandidateOrAssigned(TaskUtils.getUserId())
                .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
                .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        if (StringUtils.isNotBlank(processQuery.getOperationType())) {
            taskQuery.processVariableValueEquals("operationType", processQuery.getOperationType());
        }
        page.setTotal(taskQuery.count());
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<Task> taskList = taskQuery.listPage(offset, pageQuery.getPageSize());
        List<WfTaskVo> flowList = new ArrayList<>();
        for (Task task : taskList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(task.getId());
            flowTask.setTaskDefKey(task.getTaskDefinitionKey());
            flowTask.setCreateTime(task.getCreateTime());
            flowTask.setProcDefId(task.getProcessDefinitionId());
            flowTask.setTaskName(task.getName());
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
            R<SysUser> userInfoById = remoteUserService.getUserInfoById(userId, SecurityConstants.INNER);
            if (R.isSuccess(userInfoById) && userInfoById.getData() != null) {
                flowTask.setStartUserName(userInfoById.getData().getNickName());
            }
            flowTask.setStartUserId(userId);

            // 流程变量
            Map<String, Object> processVariables = task.getProcessVariables();
            flowTask.setOperationType(MapUtil.getStr(processVariables, "operationType"));
            flowTask.setIssueType(MapUtil.getStr(processVariables, ProcessConstants.ISSUE_TYPE));
            flowTask.setUrgentLevel(MapUtil.getStr(processVariables, ProcessConstants.URGENT_LEVEL));
            flowTask.setTraceabilityCode(MapUtil.getStr(processVariables, ProcessConstants.TRACEBILITY_CODE));
            flowTask.setProcessStatus(MapUtil.getStr(processVariables, ProcessConstants.PROCESS_STATUS_KEY));
            flowTask.setContent(MapUtil.getStr(processVariables, "content"));
            flowList.add(flowTask);
        }
        page.setRecords(flowList);
        return TableDataInfo.build(page);
    }

    @Override
    public Map<String, Object> selectPageTodoProcessListIntegration(String workno) {
        Map<String, Object> map = new HashMap<>(7);
        if (StringUtils.isBlank(workno)) {
            map.put("code", "error");
            map.put("msg", "wokno不能为空！");
            map.put("total", 0);
            map.put("data", null);
            return map;
        }
        try {
            Set<String> str = new HashSet<>();
            /*List<Map<String, String>> maps = userMapper.selectUserDeptIdAndRuleIdByUserId(workno);
            for (Map<String, String> sMap : maps) {
                String deptId = MapUtil.getStr(sMap, "deptId");
                String roleId = MapUtil.getStr(sMap, "roleId");
                str.add("DEPT" + deptId);
                str.add("ROLE" + roleId);
            }*/
            TaskQuery taskQuery = taskService.createTaskQuery()
                    .active()
                    .includeProcessVariables()
                    .taskCandidateOrAssigned(workno)
                    .taskCandidateGroupIn(str)
                    .orderByTaskCreateTime().desc();
            // 构建搜索条件
            List<Task> taskList = taskQuery.list();
            map.put("code", "success");
            map.put("msg", "ok");
            map.put("total", taskList.size());
            map.put("data", taskList);
        } catch (Exception e) {
            map.put("code", "error");
            map.put("msg", e.getMessage());
            map.put("total", 0);
            map.put("data", null);
        }
        return map;
    }

    @Override
    public List<WfTaskVo> selectTodoProcessList(ProcessQuery processQuery) {
        TaskQuery taskQuery = taskService.createTaskQuery()
                .active()
                .includeProcessVariables()
                .taskCandidateOrAssigned(TaskUtils.getUserId())
                .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
                .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        List<Task> taskList = taskQuery.list();
        List<WfTaskVo> taskVoList = new ArrayList<>();
        for (Task task : taskList) {
            WfTaskVo taskVo = new WfTaskVo();
            // 当前流程信息
            taskVo.setTaskId(task.getId());
            taskVo.setTaskDefKey(task.getTaskDefinitionKey());
            taskVo.setCreateTime(task.getCreateTime());
            taskVo.setProcDefId(task.getProcessDefinitionId());
            taskVo.setTaskName(task.getName());
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            taskVo.setDeployId(pd.getDeploymentId());
            taskVo.setProcDefName(pd.getName());
            taskVo.setProcDefVersion(pd.getVersion());
            taskVo.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
          /*  String nickName = userService.selectNickNameById(userId);
            taskVo.setStartUserName(nickName);*/
            taskVo.setStartUserId(userId);

            taskVoList.add(taskVo);
        }
        return taskVoList;
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageClaimProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        TaskQuery taskQuery = taskService.createTaskQuery()
                .active()
                .includeProcessVariables()
                .taskCandidateUser(TaskUtils.getUserId())
                .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
                .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        page.setTotal(taskQuery.count());
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<Task> taskList = taskQuery.listPage(offset, pageQuery.getPageSize());
        List<WfTaskVo> flowList = new ArrayList<>();
        for (Task task : taskList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(task.getId());
            flowTask.setTaskDefKey(task.getTaskDefinitionKey());
            flowTask.setCreateTime(task.getCreateTime());
            flowTask.setProcDefId(task.getProcessDefinitionId());
            flowTask.setTaskName(task.getName());
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();

            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
            /*String nickName = userService.selectNickNameById(userId);
            flowTask.setStartUserName(nickName);*/
            flowTask.setStartUserId(userId);

            Map<String, Object> processVariables = task.getProcessVariables();
            flowTask.setDeptName(MapUtil.getStr(processVariables, "deptName"));
            flowTask.setDeptNameNew(MapUtil.getStr(processVariables, "deptNameNew"));
            flowTask.setAccountName(processVariables.containsKey("accountName") ? MapUtil.getStr(processVariables, "accountName") : MapUtil.getStr(processVariables, "newAccountName"));
            flowList.add(flowTask);
        }
        page.setRecords(flowList);
        return TableDataInfo.build(page);
    }

    @Override
    public List<WfTaskVo> selectClaimProcessList(ProcessQuery processQuery) {
        TaskQuery taskQuery = taskService.createTaskQuery()
                .active()
                .includeProcessVariables()
                .taskCandidateUser(TaskUtils.getUserId())
                .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
                .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        List<Task> taskList = taskQuery.list();
        List<WfTaskVo> flowList = new ArrayList<>();
        for (Task task : taskList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(task.getId());
            flowTask.setTaskDefKey(task.getTaskDefinitionKey());
            flowTask.setCreateTime(task.getCreateTime());
            flowTask.setProcDefId(task.getProcessDefinitionId());
            flowTask.setTaskName(task.getName());
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
           /* String nickName = userService.selectNickNameById(userId);
            flowTask.setStartUserName(nickName);*/
            flowTask.setStartUserId(userId);

            flowList.add(flowTask);
        }
        return flowList;
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageFinishedProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
                .includeProcessVariables()
                .finished()
                .taskAssignee(TaskUtils.getUserId())
                .orderByHistoricTaskInstanceEndTime()
                .desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskInstanceQuery, processQuery);
        if (StringUtils.isNotBlank(processQuery.getOperationType())) {
            taskInstanceQuery.processVariableValueEquals("operationType", processQuery.getOperationType());
        }
        //去重
        List<HistoricTaskInstance> list = taskInstanceQuery.list();
        ArrayList<HistoricTaskInstance> collect = list.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(
                                () -> new TreeSet<>(Comparator.comparing(HistoricTaskInstance::getProcessInstanceId))
                        ), ArrayList::new
                )
        );
        //排序
        List<HistoricTaskInstance> sortedList = collect.stream()
                .sorted(Comparator.comparing(HistoricTaskInstance::getEndTime).reversed())
                .collect(Collectors.toList());
        //分页
        List<HistoricTaskInstance> paginate = PageUtils.paginate(sortedList, pageQuery.getPageNum(), pageQuery.getPageSize());
        List<WfTaskVo> hisTaskList = new ArrayList<>();
        for (HistoricTaskInstance histTask : paginate) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(histTask.getId());
            // 审批人员信息
            flowTask.setCreateTime(histTask.getCreateTime());
            flowTask.setFinishTime(histTask.getEndTime());
            flowTask.setDuration(DateUtil.formatBetween(histTask.getDurationInMillis(), BetweenFormatter.Level.SECOND));
            flowTask.setProcDefId(histTask.getProcessDefinitionId());
            flowTask.setTaskDefKey(histTask.getTaskDefinitionKey());
            flowTask.setTaskName(histTask.getName());

            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(histTask.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(histTask.getProcessInstanceId());
            flowTask.setHisProcInsId(histTask.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(histTask.getProcessInstanceId())
                    .singleResult();
            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
            R<SysUser> userInfoById = remoteUserService.getUserInfoById(userId, SecurityConstants.INNER);
            if (R.isSuccess(userInfoById) && userInfoById.getData() != null) {
                flowTask.setStartUserName(userInfoById.getData().getNickName());
            }
            flowTask.setStartUserId(userId);

            // 流程变量
            Map<String, Object> processVariables = histTask.getProcessVariables();
            flowTask.setOperationType(MapUtil.getStr(processVariables, "operationType"));
            flowTask.setIssueType(MapUtil.getStr(processVariables, ProcessConstants.ISSUE_TYPE));
            flowTask.setUrgentLevel(MapUtil.getStr(processVariables, ProcessConstants.URGENT_LEVEL));
            flowTask.setTraceabilityCode(MapUtil.getStr(processVariables, ProcessConstants.TRACEBILITY_CODE));
            flowTask.setContent(MapUtil.getStr(processVariables, "content"));
            flowTask.setProcessStatus(MapUtil.getStr(processVariables, ProcessConstants.PROCESS_STATUS_KEY));
            hisTaskList.add(flowTask);
        }
        page.setTotal(sortedList.size());
        page.setRecords(hisTaskList);
        return TableDataInfo.build(page);
    }


    @Override
    public List<WfTaskVo> selectFinishedProcessList(ProcessQuery processQuery) {
        HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
                .includeProcessVariables()
                .finished()
                .taskAssignee(TaskUtils.getUserId())
                .orderByHistoricTaskInstanceEndTime()
                .desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskInstanceQuery, processQuery);
        List<HistoricTaskInstance> historicTaskInstanceList = taskInstanceQuery.list();
        List<WfTaskVo> hisTaskList = new ArrayList<>();
        for (HistoricTaskInstance histTask : historicTaskInstanceList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(histTask.getId());
            // 审批人员信息
            flowTask.setCreateTime(histTask.getCreateTime());
            flowTask.setFinishTime(histTask.getEndTime());
            flowTask.setDuration(DateUtil.formatBetween(histTask.getDurationInMillis(), BetweenFormatter.Level.SECOND));
            flowTask.setProcDefId(histTask.getProcessDefinitionId());
            flowTask.setTaskDefKey(histTask.getTaskDefinitionKey());
            flowTask.setTaskName(histTask.getName());

            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(histTask.getProcessDefinitionId())
                    .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(histTask.getProcessInstanceId());
            flowTask.setHisProcInsId(histTask.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(histTask.getProcessInstanceId())
                    .singleResult();
            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
           /* String nickName = userService.selectNickNameById(userId);
            flowTask.setStartUserName(nickName);*/
            flowTask.setStartUserId(userId);

            // 流程变量
            flowTask.setProcVars(histTask.getProcessVariables());

            hisTaskList.add(flowTask);
        }
        return hisTaskList;
    }

    @Override
    public FormConf selectFormContent(String definitionId, String deployId, String procInsId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definitionId);
        if (ObjectUtil.isNull(bpmnModel)) {
            throw new RuntimeException("获取流程设计失败！");
        }
        String name = bpmnModel.getProcess(null).getName();
        StartEvent startEvent = ModelUtils.getStartEvent(bpmnModel);
        WfDeployForm deployForm = deployFormMapper.selectOne(new LambdaQueryWrapper<WfDeployForm>()
                .eq(WfDeployForm::getDeployId, deployId)
                .eq(WfDeployForm::getFormKey, startEvent.getFormKey())
                .eq(WfDeployForm::getNodeKey, startEvent.getId()));
        Map<String, Object> formModel = JsonUtils.parseObject(deployForm.getContent(), Map.class);
        Map<String, Object> formModelMobile = JsonUtils.parseObject(deployForm.getContentMobile(), Map.class);
        if (null == formModel || formModel.isEmpty()) {
            throw new RuntimeException("获取流程表单失败！");
        }
        FormConf formConf = new FormConf();
        formConf.setProcessName(name);
        formConf.setFormBtns(false);
        formConf.setFormModel(formModel);
        formConf.setFormModelMobile(formModelMobile);
        if (ObjectUtil.isNotEmpty(procInsId)) {
            // 获取流程实例
            HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(procInsId)
                    .includeProcessVariables()
                    .singleResult();
            formConf.setFormData(historicProcIns.getProcessVariables());
        }
        return formConf;
    }

    /**
     * 根据流程定义ID启动流程实例
     *
     * @param procDefId 流程定义Id
     * @param variables 流程变量
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startProcessByDefId(String procDefId, Map<String, Object> variables) {
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(procDefId).singleResult();
            variables.put("", "");
            startProcess(processDefinition, variables);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("流程启动错误");
        }
    }

    /**
     * 启动流程
     *
     * @param variables
     * @param category
     */
    @Override
    public void startProcess(Map<String, Object> variables, String category, String teamCode) {
        if (StringUtils.isBlank(category) || StringUtils.isBlank(teamCode)) {
            throw new ServiceException("流程分类及团队编码不能为空");
        }
        ProcessQuery processQuery = new ProcessQuery();
        processQuery.setCategory(category);
        WfDefinitionVo wfDefinitionVoByProcessQuery = getWfDefinitionVoByProcessQuery(processQuery);
        if (wfDefinitionVoByProcessQuery == null) {
            throw new ServiceException("此分类的流程不存在");
        }
        //一个团队三种流程同时只能发起一个流程，只能有一个是进行中的
        List<OperationFlow> operationFlow = operationFlowService.selectOperationFlowList(new OperationFlow(teamCode, null, "running"));
        if (CollectionUtils.isNotEmpty(operationFlow)) {
            throw new RuntimeException("当前团队存在进行中的流程，请勿重复发起");
        }
        startProcessByDefId(wfDefinitionVoByProcessQuery.getDefinitionId(), variables);
    }

    /**
     * 通过DefinitionKey启动流程
     *
     * @param procDefKey 流程定义Key
     * @param variables  扩展参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startProcessByDefKey(String procDefKey, Map<String, Object> variables) {
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(procDefKey).latestVersion().singleResult();
            startProcess(processDefinition, variables);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("流程启动错误");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcessByIds(String[] instanceIds) {
        List<String> ids = Arrays.asList(instanceIds);
        // 校验流程是否结束
        long activeInsCount = runtimeService.createProcessInstanceQuery()
                .processInstanceIds(new HashSet<>(ids)).active().count();
        if (activeInsCount > 0) {
            throw new ServiceException("不允许删除进行中的流程实例");
        }
        // 删除历史流程实例
        historyService.bulkDeleteHistoricProcessInstances(ids);
    }

    /**
     * 读取xml文件
     *
     * @param processDefId 流程定义ID
     */
    @Override
    public String queryBpmnXmlById(String processDefId) {
        InputStream inputStream = repositoryService.getProcessModel(processDefId);
        try {
            return IoUtil.readUtf8(inputStream);
        } catch (IORuntimeException exception) {
            throw new RuntimeException("加载xml文件异常");
        }
    }

    /**
     * 流程详情信息
     *
     * @param procInsId 流程实例ID
     * @param taskId    任务ID
     * @return
     */
    @Override
    public WfDetailVo queryProcessDetail(String procInsId, String taskId, Long userId, String changeStatusFlag) {
        WfDetailVo detailVo = new WfDetailVo();
        detailVo.setProcInsId(procInsId);
        // 获取流程实例
        HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(procInsId)
                .includeProcessVariables()
                .singleResult();
        if (StringUtils.isNotBlank(taskId)) {
            HistoricTaskInstance taskIns = historyService.createHistoricTaskInstanceQuery()
                    .taskId(taskId)
                    .includeIdentityLinks()
                    .includeProcessVariables()
                    .includeTaskLocalVariables()
                    .singleResult();
            if (taskIns == null) {
                throw new ServiceException("没有可办理的任务！");
            }
            detailVo.setTaskFormData(currTaskFormData(historicProcIns.getDeploymentId(), taskIns));
        }
        Map<String, Object> processVariables = historicProcIns.getProcessVariables();
        String status = MapUtil.getStr(processVariables, ProcessConstants.PROCESS_STATUS_KEY);
        if (StringUtils.isNotBlank(changeStatusFlag) && status.equals(ProcessStatus.WAITING.getStatus())) {
            RuntimeService runtimeService = processEngine.getRuntimeService();
            runtimeService.setVariable(procInsId, ProcessConstants.PROCESS_STATUS_KEY, ProcessStatus.RUNNING.getStatus());
            //第一次响应时长
            Duration duration = Duration.between(historicProcIns.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            runtimeService.setVariable(procInsId, ProcessConstants.FIRST_RESPONSE_TIME, duration.toMinutes());
        }
        // 获取Bpmn模型信息
        InputStream inputStream = repositoryService.getProcessModel(historicProcIns.getProcessDefinitionId());
        String bpmnXmlStr = StrUtil.utf8Str(IoUtil.readBytes(inputStream, false));
        BpmnModel bpmnModel = ModelUtils.getBpmnModel(bpmnXmlStr);
        detailVo.setBpmnXml(bpmnXmlStr);
        detailVo.setHistoryProcNodeList(historyProcNodeList(historicProcIns));
        detailVo.setProcessFormList(processFormList(bpmnModel, historicProcIns));
        detailVo.setFlowViewer(getFlowViewer(bpmnModel, procInsId));
        //退费流程查询订单信息
        try {
            Object refundOrderObj = detailVo.getProcessFormList().get(0).getFormData().get("refundOrderId");
            if (refundOrderObj != null) {

                R<Map<String, Object>> payAndRefundOrderInfo = orderService.getPayAndRefundOrderInfo(Long.parseLong(refundOrderObj.toString()), SecurityConstants.INNER);
                if (R.isSuccess(payAndRefundOrderInfo)) {
                    detailVo.setOrderInfos(payAndRefundOrderInfo.getData());
                }
            }
        } catch (Exception e) {
            log.error("退费流程查询订单信息异常：{}", e.getMessage());
        }
        return detailVo;
    }

    /**
     * 启动流程实例
     */
    private void startProcess(ProcessDefinition procDef, Map<String, Object> variables) {
        if (ObjectUtil.isNotNull(procDef) && procDef.isSuspended()) {
            throw new ServiceException("流程已被挂起，请先激活流程");
        }
        // 设置流程发起人Id到流程中
        String userIdStr = TaskUtils.getUserId();
        String startId = MapUtils.getString(variables, "ApplicantId");
        identityService.setAuthenticatedUserId(StringUtils.isNotBlank(startId) ? startId : userIdStr);
        variables.put(BpmnXMLConstants.ATTRIBUTE_EVENT_START_INITIATOR, StringUtils.isNotBlank(startId) ? startId : userIdStr);
        // 设置流程状态为进行中
        variables.put(ProcessConstants.PROCESS_STATUS_KEY, ProcessStatus.WAITING.getStatus());
        // 发起流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(procDef.getId(), variables);
        // 第一个用户任务为发起人，则自动完成任务
        wfTaskService.startFirstTask(processInstance, variables);
    }


    /**
     * 获取流程变量
     *
     * @param taskId 任务ID
     * @return 流程变量
     */
    private Map<String, Object> getProcessVariables(String taskId) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
                .includeProcessVariables()
                .finished()
                .taskId(taskId)
                .singleResult();
        if (Objects.nonNull(historicTaskInstance)) {
            return historicTaskInstance.getProcessVariables();
        }
        return taskService.getVariables(taskId);
    }

    /**
     * 获取当前任务流程表单信息
     */
    private FormConf currTaskFormData(String deployId, HistoricTaskInstance taskIns) {
        WfDeployFormVo deployFormVo = deployFormMapper.selectVoOne(new LambdaQueryWrapper<WfDeployForm>()
                .eq(WfDeployForm::getDeployId, deployId)
                .eq(WfDeployForm::getFormKey, taskIns.getFormKey())
                .eq(WfDeployForm::getNodeKey, taskIns.getTaskDefinitionKey()));
        if (ObjectUtil.isNotEmpty(deployFormVo)) {
            FormConf currTaskFormData = new FormConf();
            Map<String, Object> formModel = JsonUtils.parseObject(deployFormVo.getContent(), Map.class);
            Map<String, Object> formModelMobile = JsonUtils.parseObject(deployFormVo.getContentMobile(), Map.class);
            if (null != formModel && !formModel.isEmpty()) {
                currTaskFormData.setFormBtns(false);
                currTaskFormData.setFormModel(formModel);
                currTaskFormData.setFormData(taskIns.getTaskLocalVariables());
                currTaskFormData.setFormModelMobile(formModelMobile);
                return currTaskFormData;
            }
        }
        return null;
    }

    /**
     * 获取历史流程表单信息
     */
    private List<FormConf> processFormList(BpmnModel bpmnModel, HistoricProcessInstance historicProcIns) {
        List<FormConf> procFormList = new ArrayList<>();

        List<HistoricActivityInstance> activityInstanceList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(historicProcIns.getId()).finished()
                .activityTypes(CollUtil.newHashSet(BpmnXMLConstants.ELEMENT_EVENT_START, BpmnXMLConstants.ELEMENT_TASK_USER))
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();
        List<String> processFormKeys = new ArrayList<>();
        Set<String> formKeys = new HashSet<>();
        for (HistoricActivityInstance activityInstance : activityInstanceList) {
            if (StringUtils.isNotBlank(activityInstance.getDeleteReason())) {
                continue;
            }
            // 获取当前节点流程元素信息
            FlowElement flowElement = ModelUtils.getFlowElementById(bpmnModel, activityInstance.getActivityId());
            // 获取当前节点表单Key
            String formKey = ModelUtils.getFormKey(flowElement);
            if (formKey == null || formKeys.contains(formKey)) {
                continue;
            }
            formKeys.add(formKey);
            boolean localScope = Convert.toBool(ModelUtils.getElementAttributeValue(flowElement, ProcessConstants.PROCESS_FORM_LOCAL_SCOPE), false);
            Map<String, Object> variables;
            if (localScope) {
                // 查询任务节点参数，并转换成Map
                variables = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(historicProcIns.getId())
                        .taskId(activityInstance.getTaskId())
                        .list()
                        .stream()
                        .collect(Collectors.toMap(HistoricVariableInstance::getVariableName, i -> Optional.ofNullable(i.getValue()).orElse(""), (v1, v2) -> v1));
//                        .collect(Collectors.toMap(HistoricVariableInstance::getVariableName, HistoricVariableInstance::getValue));
            } else {
                if (processFormKeys.contains(formKey)) {
                    continue;
                }
                variables = historicProcIns.getProcessVariables();
                processFormKeys.add(formKey);
            }
            String deptId = MapUtil.getStr(variables, "deptId");

            Long[] deptIds = getDeptIds(deptId);
            variables.put("deptId", deptIds);
            variables.put("oldDeptId", deptIds);
            String newDeptId = MapUtil.getStr(variables, "newDeptId");
            if (StringUtils.isNotBlank(newDeptId)) {
                variables.put("newDeptId", getDeptIds(newDeptId));
            }


            // 非节点表单此处查询结果可能有多条，只获取第一条信息
            List<WfDeployFormVo> formInfoList = deployFormMapper.selectVoList(new LambdaQueryWrapper<WfDeployForm>()
                    .eq(WfDeployForm::getDeployId, historicProcIns.getDeploymentId())
                    .eq(WfDeployForm::getFormKey, formKey)
                    .eq(localScope, WfDeployForm::getNodeKey, flowElement.getId()));

            //@update by Brath：避免空集合导致的NULL空指针
            WfDeployFormVo formInfo = formInfoList.stream().findFirst().orElse(null);

            if (ObjectUtil.isNotNull(formInfo)) {
                // 旧数据 formInfo.getFormName() 为 null
                String formName = Optional.ofNullable(formInfo.getFormName()).orElse(StringUtils.EMPTY);
                String title = localScope ? formName.concat("(" + flowElement.getName() + ")") : formName;
                FormConf formConf = new FormConf();
                Map<String, Object> formModel = JsonUtils.parseObject(formInfo.getContent(), Map.class);
                Map<String, Object> formModelMobile = JsonUtils.parseObject(formInfo.getContentMobile(), Map.class);
                if (null != formModel && !formModel.isEmpty()) {
                    formConf.setTitle(title);
                    formConf.setDisabled(true);
                    formConf.setFormBtns(false);
                    formConf.setFormModel(formModel);
                    formConf.setFormModelMobile(formModelMobile);
                    formConf.setFormData(variables);
                    procFormList.add(formConf);
                }
            }
        }
        return procFormList;
    }

    public Long[] getDeptIds(String deptId) {
        List<Long> longList = new ArrayList<>();
        if (StringUtils.isBlank(deptId)) {
            return longList.toArray(new Long[0]);
        }
        /*String endStr = RecordNumber.returnLastDeptIdStr(deptId);
        SysDept dept = new SysDept();
        dept.setIsDataAuth("0");
        dept.setDeptInd("1");
        List<SysDept> depts = deptService.GetDepartmentTree(dept);
        Set<Long> deptsIds = depts.stream()
                .map(SysDept::getDeptId)
                .collect(Collectors.toSet());
        longList.add(Long.valueOf(endStr));
        boolean flag = true;
        long temp = Long.parseLong(endStr);
        while (flag) {
            Long temp1 = temp;
            List<SysDept> collect = depts.stream().filter(m -> m.getDeptId().equals(temp1)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                Long parentId = collect.get(0).getParentId();
                if (deptsIds.contains(parentId)) {
                    if (parentId == 0) {
                        break;
                    }
                    longList.add(parentId);
                }
                temp = parentId;
            } else {
                flag = false;
            }
        }
        Collections.reverse(longList);*/
        return longList.toArray(new Long[0]);
    }

    @Deprecated
    private void buildStartFormData(HistoricProcessInstance historicProcIns, Process process, String deployId, List<FormConf> procFormList) {
        procFormList = procFormList == null ? new ArrayList<>() : procFormList;
        HistoricActivityInstance startInstance = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(historicProcIns.getId())
                .activityId(historicProcIns.getStartActivityId())
                .singleResult();
        StartEvent startEvent = (StartEvent) process.getFlowElement(startInstance.getActivityId());
        WfDeployFormVo startFormInfo = deployFormMapper.selectVoOne(new LambdaQueryWrapper<WfDeployForm>()
                .eq(WfDeployForm::getDeployId, deployId)
                .eq(WfDeployForm::getFormKey, startEvent.getFormKey())
                .eq(WfDeployForm::getNodeKey, startEvent.getId()));
        if (ObjectUtil.isNotNull(startFormInfo)) {
            FormConf formConf = new FormConf();
            Map<String, Object> formModel = JsonUtils.parseObject(startFormInfo.getContent(), Map.class);
            if (null != formModel && !formModel.isEmpty()) {
                formConf.setTitle(startEvent.getName());
                formConf.setDisabled(true);
                formConf.setFormBtns(false);
                formConf.setFormModel(formModel);
                formConf.setFormData(historicProcIns.getProcessVariables());
                procFormList.add(formConf);
            }
        }
    }

    @Deprecated
    private void buildUserTaskFormData(String procInsId, String deployId, Process process, List<FormConf> procFormList) {
        procFormList = procFormList == null ? new ArrayList<>() : procFormList;
        List<HistoricActivityInstance> activityInstanceList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(procInsId).finished()
                .activityType(BpmnXMLConstants.ELEMENT_TASK_USER)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();
        for (HistoricActivityInstance instanceItem : activityInstanceList) {
            UserTask userTask = (UserTask) process.getFlowElement(instanceItem.getActivityId(), true);
            String formKey = userTask.getFormKey();
            if (formKey == null) {
                continue;
            }
            // 查询任务节点参数，并转换成Map
            Map<String, Object> variables = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(procInsId)
                    .taskId(instanceItem.getTaskId())
                    .list()
                    .stream()
                    .collect(Collectors.toMap(HistoricVariableInstance::getVariableName, HistoricVariableInstance::getValue));
            WfDeployFormVo deployFormVo = deployFormMapper.selectVoOne(new LambdaQueryWrapper<WfDeployForm>()
                    .eq(WfDeployForm::getDeployId, deployId)
                    .eq(WfDeployForm::getFormKey, formKey)
                    .eq(WfDeployForm::getNodeKey, userTask.getId()));
            if (ObjectUtil.isNotNull(deployFormVo)) {
                FormConf formConf = new FormConf();
                Map<String, Object> formModel = JsonUtils.parseObject(deployFormVo.getContent(), Map.class);
                if (null != formModel && !formModel.isEmpty()) {
                    formConf.setTitle(userTask.getName());
                    formConf.setDisabled(true);
                    formConf.setFormBtns(false);
                    formConf.setFormModel(formModel);
                    formConf.setFormData(variables);
                    procFormList.add(formConf);
                }
            }
        }
    }

    /**
     * 获取历史任务信息列表
     */
    private List<WfProcNodeVo> historyProcNodeList(HistoricProcessInstance historicProcIns) {
        //表单中的单位
        Map<String, Object> processVariables = historicProcIns.getProcessVariables();
//        String endStr = RecordNumber.returnLastDeptIdStr(deptId1);
        String procInsId = historicProcIns.getId();
        List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(procInsId)
                .activityTypes(CollUtil.newHashSet(BpmnXMLConstants.ELEMENT_EVENT_START, BpmnXMLConstants.ELEMENT_EVENT_END, BpmnXMLConstants.ELEMENT_TASK_USER))
                .orderByHistoricActivityInstanceStartTime().desc()
                .orderByHistoricActivityInstanceEndTime().desc()
                .list();

        List<Comment> commentList = taskService.getProcessInstanceComments(procInsId);

        List<WfProcNodeVo> elementVoList = new ArrayList<>();
        for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
            WfProcNodeVo elementVo = new WfProcNodeVo();
            elementVo.setProcDefId(activityInstance.getProcessDefinitionId());
            elementVo.setActivityId(activityInstance.getActivityId());
            elementVo.setActivityName(activityInstance.getActivityName());
            elementVo.setActivityType(activityInstance.getActivityType());
            elementVo.setCreateTime(activityInstance.getStartTime());
            elementVo.setEndTime(activityInstance.getEndTime());
            if (ObjectUtil.isNotNull(activityInstance.getDurationInMillis())) {
                elementVo.setDuration(DateUtil.formatBetween(activityInstance.getDurationInMillis(), BetweenFormatter.Level.SECOND));
            }

            if (BpmnXMLConstants.ELEMENT_EVENT_START.equals(activityInstance.getActivityType())) {
                if (ObjectUtil.isNotNull(historicProcIns)) {
                    Long userId = Long.parseLong(historicProcIns.getStartUserId());
                    R<SysUser> userInfoById = remoteUserService.getUserInfoById(userId, SecurityConstants.INNER);
                    if (R.isSuccess(userInfoById) && userInfoById.getData() != null) {
                        elementVo.setAssigneeId(userId);
                        elementVo.setAssigneeName(userInfoById.getData().getNickName());
                    }
                }
            } else if (BpmnXMLConstants.ELEMENT_TASK_USER.equals(activityInstance.getActivityType())) {
                if (StringUtils.isNotBlank(activityInstance.getAssignee())) {
                    Long userId = Long.parseLong(activityInstance.getAssignee());
                    R<SysUser> userInfoById = remoteUserService.getUserInfoById(userId, SecurityConstants.INNER);
                    if (R.isSuccess(userInfoById) && userInfoById.getData() != null) {
                        elementVo.setAssigneeName(userInfoById.getData().getNickName());
                    }
                    elementVo.setAssigneeId(userId);
                } else {
                    // 展示审批人员
                    List<HistoricIdentityLink> linksForTask = historyService.getHistoricIdentityLinksForTask(activityInstance.getTaskId());
                    StringBuilder stringBuilder = new StringBuilder();
                    StringBuilder nickNameByRoleIdStr = new StringBuilder();
                    for (HistoricIdentityLink identityLink : linksForTask) {
                        if ("candidate".equals(identityLink.getType())) {

                            if (StringUtils.isNotBlank(identityLink.getUserId())) {
                                Long userId = Long.parseLong(identityLink.getUserId());
                                R<SysUser> userInfoById = remoteUserService.getUserInfoById(userId, SecurityConstants.INNER);
                                if (R.isSuccess(userInfoById) && userInfoById.getData() != null) {
                                    stringBuilder.append(userInfoById.getData().getNickName()).append(",");
                                }
                            }
                            if (StringUtils.isNotBlank(identityLink.getGroupId())) {
                                if (identityLink.getGroupId().startsWith(TaskConstants.ROLE_GROUP_PREFIX)) {
                                    Long roleId = Long.parseLong(StringUtils.stripStart(identityLink.getGroupId(), TaskConstants.ROLE_GROUP_PREFIX).split("-")[0]);
                                    R<SysRole> roleInfoById = remoteUserService.getRoleInfoById(roleId, SecurityConstants.INNER);
                                    if (R.isSuccess(roleInfoById) && roleInfoById.getData() != null) {
                                        stringBuilder.append(roleInfoById.getData().getRoleName()).append(",");
                                    }
                                    if (StringUtils.isBlank(activityInstance.getAssignee())) {
                                        //Long deptId = Long.parseLong(StringUtils.stripStart(identityLink.getGroupId(), TaskConstants.ROLE_GROUP_PREFIX).split("-")[1]);
//                                        Long deptIdByUserRoldAndDeptId = deptService.getDeptIdByUserRoldAndDeptId(deptId, roleId);
                                        //Long maxRole = MapUtil.getLong(processVariables, "roleLevel");
                                       /* Long deptIdByUserRoldAndDeptId = deptService.getDeptIdByUserRoldAndDeptIdAndMaxRole(deptId, maxRole,roleId);
                                        user.setDeptId(deptIdByUserRoldAndDeptId);
                                        SysUser user = new SysUser();
                                        user.setRoleId(roleId);
                                        List<SysUser> sysUsers = userMapper.selectAllocatedList(user);
                                        List<String> nickNameByRoleId = sysUsers.stream().map(SysUser::getNickName).collect(Collectors.toList());
                                        String join = String.join(",", nickNameByRoleId);
                                        nickNameByRoleIdStr.append(join).append(",");*/
                                    }

                                } else if (identityLink.getGroupId().startsWith(TaskConstants.DEPT_GROUP_PREFIX)) {
                                    Long deptId = Long.parseLong(StringUtils.stripStart(identityLink.getGroupId(), TaskConstants.DEPT_GROUP_PREFIX));
                                    /*SysDept dept = deptService.selectDeptById(deptId);
                                    stringBuilder.append(dept.getDeptName()).append(",");*/
                                } else if (identityLink.getGroupId().startsWith(TaskConstants.POST_GROUP_PREFIX)) {
                                }
                            }
                        }
                    }
                    if (StringUtils.isNotBlank(stringBuilder)) {
                        elementVo.setCandidate(stringBuilder.substring(0, stringBuilder.length() - 1));
                        elementVo.setAssigneeName(stringBuilder.substring(0, stringBuilder.length() - 1));
                    }
                    if (StringUtils.isNotBlank(nickNameByRoleIdStr)) {
                        elementVo.setAssigneeName(nickNameByRoleIdStr.substring(0, nickNameByRoleIdStr.length() - 1));
                    }
                }
                // 获取意见评论内容
                if (CollUtil.isNotEmpty(commentList)) {
                    List<Comment> comments = new ArrayList<>();
                    for (Comment comment : commentList) {

                        if (comment.getTaskId().equals(activityInstance.getTaskId())) {
                            comments.add(comment);
                        }
                    }
                    elementVo.setCommentList(comments);
                }
            }
            elementVoList.add(elementVo);
        }
        return elementVoList;
    }

    /**
     * 获取流程执行过程
     *
     * @param procInsId
     * @return
     */
    private WfViewerVo getFlowViewer(BpmnModel bpmnModel, String procInsId) {
        // 构建查询条件
        HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(procInsId);
        List<HistoricActivityInstance> allActivityInstanceList = query.list();
        if (CollUtil.isEmpty(allActivityInstanceList)) {
            return new WfViewerVo();
        }
        // 查询所有已完成的元素
        List<HistoricActivityInstance> finishedElementList = allActivityInstanceList.stream()
                .filter(item -> ObjectUtil.isNotNull(item.getEndTime())).collect(Collectors.toList());
        // 所有已完成的连线
        Set<String> finishedSequenceFlowSet = new HashSet<>();
        // 所有已完成的任务节点
        Set<String> finishedTaskSet = new HashSet<>();
        finishedElementList.forEach(item -> {
            if (BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW.equals(item.getActivityType())) {
                finishedSequenceFlowSet.add(item.getActivityId());
            } else {
                finishedTaskSet.add(item.getActivityId());
            }
        });
        // 查询所有未结束的节点
        Set<String> unfinishedTaskSet = allActivityInstanceList.stream()
                .filter(item -> ObjectUtil.isNull(item.getEndTime()))
                .map(HistoricActivityInstance::getActivityId)
                .collect(Collectors.toSet());
        // DFS 查询未通过的元素集合
        Set<String> rejectedSet = FlowableUtils.dfsFindRejects(bpmnModel, unfinishedTaskSet, finishedSequenceFlowSet, finishedTaskSet);
        return new WfViewerVo(finishedTaskSet, finishedSequenceFlowSet, unfinishedTaskSet, rejectedSet);
    }

    @Resource
    protected RuntimeService runtimeService;

    @Override
    public TableDataInfo<WfTaskVo> queryPageRList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery()
                .includeProcessVariables()
                .orderByStartTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(processInstanceQuery, processQuery);
        if (StringUtils.isNotBlank(processQuery.getOperationType())) {
            processInstanceQuery.variableValueEquals("operationType", processQuery.getOperationType());
        }
        if (processQuery.getStartDeptId() != null) {
            processInstanceQuery.variableValueEquals(ProcessConstants.DEPT_ID, processQuery.getStartDeptId());
        }
        if (StringUtils.isNotBlank(processQuery.getProcInsId())) {
            processInstanceQuery.processInstanceId(processQuery.getProcInsId());
        }
        if (StringUtils.isNotBlank(processQuery.getStartUserId())) {
            R<SysUser> userResult = remoteUserService.getUserInfoByNickName(processQuery.getStartUserId().trim(), SecurityConstants.INNER);
            if (R.isSuccess(userResult) && userResult.getData() != null) {
                processInstanceQuery.startedBy(userResult.getData().getUserId()+"");
            }
        }
        if (StringUtils.isNotBlank(processQuery.getUrgentLevel())) {
            processInstanceQuery.variableValueEquals(ProcessConstants.URGENT_LEVEL, processQuery.getUrgentLevel());
        }
        if (StringUtils.isNotBlank(processQuery.getIssueType())) {
            processInstanceQuery.variableValueEquals(ProcessConstants.ISSUE_TYPE, processQuery.getIssueType());
        }
        if (StringUtils.isNotBlank(processQuery.getTraceabilityCode())) {
            processInstanceQuery.variableValueLike(ProcessConstants.TRACEBILITY_CODE, "%" + processQuery.getTraceabilityCode() + "%");
        }
        if (StringUtils.isNotBlank(processQuery.getState())) {
            processInstanceQuery.variableValueEquals(ProcessConstants.PROCESS_STATUS_KEY, processQuery.getState());
        }
        String processName = processQuery.getProcessName();
        if (StringUtils.isNotBlank(processName)) {
            processInstanceQuery.processDefinitionName(processName);
        }
        Map<String, Object> params = processQuery.getParams();
        if (params.get("beginTime") != null && params.get("endTime") != null) {
            processInstanceQuery.startedAfter(DateUtil.beginOfDay(DateUtils.parseDate(params.get("beginTime"))));
            processInstanceQuery.startedBefore(DateUtil.endOfDay(DateUtils.parseDate(params.get("endTime"))));
        }
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<ProcessInstance> processInstances = processInstanceQuery
                .listPage(offset, pageQuery.getPageSize());
        page.setTotal(processInstanceQuery.count());
        List<WfTaskVo> taskVoList = new ArrayList<>();
        for (ProcessInstance runIns : processInstances) {
            WfTaskVo taskVo = new WfTaskVo();
            taskVo.setSuspended(runIns.isSuspended());
            // 获取流程状态
            HistoricVariableInstance processStatusVariable = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(runIns.getId())
                    .variableName(ProcessConstants.PROCESS_STATUS_KEY)
                    .singleResult();
            String processStatus = null;
            if (ObjectUtil.isNotNull(processStatusVariable)) {
                processStatus = Convert.toStr(processStatusVariable.getValue());
            }
            taskVo.setProcessStatus(processStatus);
            taskVo.setCreateTime(runIns.getStartTime());
            taskVo.setProcInsId(runIns.getId());
            // 计算耗时
            taskVo.setDuration(DateUtils.getDatePoor(DateUtils.getNowDate(), runIns.getStartTime()));
            // 流程部署实例信息
            Deployment deployment = repositoryService.createDeploymentQuery()
                    .deploymentId(runIns.getDeploymentId()).singleResult();
            taskVo.setDeployId(runIns.getDeploymentId());
            taskVo.setProcDefId(runIns.getProcessDefinitionId());
            taskVo.setProcDefName(runIns.getProcessDefinitionName());
            taskVo.setProcDefVersion(runIns.getProcessDefinitionVersion());
            taskVo.setCategory(deployment.getCategory());
            // 当前所处流程
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(runIns.getId()).includeIdentityLinks().list();

            // 获取流程实例
            HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(runIns.getId())
                    .includeProcessVariables()
                    .singleResult();
            if (ObjectUtil.isNotNull(historicProcIns)) {

                List<WfProcNodeVo> wfProcNodeVos = historyProcNodeList(historicProcIns);
                //获取当前节点审核人
                WfProcNodeVo wfProcNodeVo = wfProcNodeVos.get(0);
                if (wfProcNodeVo.getAssigneeName() == null) {
                    wfProcNodeVo.setAssigneeName("");
                }
                taskVo.setAssigneeName(wfProcNodeVo.getAssigneeName());
            }
            if (CollUtil.isNotEmpty(taskList)) {
                taskVo.setTaskName(taskList.stream().map(Task::getName).filter(StringUtils::isNotEmpty).collect(Collectors.joining(",")));
                taskVo.setTaskId(taskList.stream().map(Task::getId).filter(StringUtils::isNotEmpty).collect(Collectors.joining(",")));
            }
            Map<String, Object> processVariables = runIns.getProcessVariables();
            taskVo.setOperationType(MapUtil.getStr(processVariables, "operationType"));
            taskVo.setTraceabilityCode(MapUtil.getStr(processVariables, ProcessConstants.TRACEBILITY_CODE));
            taskVo.setUrgentLevel(MapUtil.getStr(processVariables, ProcessConstants.URGENT_LEVEL));
            taskVo.setIssueType(MapUtil.getStr(processVariables, ProcessConstants.ISSUE_TYPE));
            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(runIns.getId())
                    .singleResult();

            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
            R<SysUser> userInfoById = remoteUserService.getUserInfoById(userId, SecurityConstants.INNER);
            if (R.isSuccess(userInfoById) && userInfoById.getData() != null) {
                taskVo.setStartUserName(userInfoById.getData().getNickName());
            }
            taskVo.setStartUserId(userId);
            //Long deptId = MapUtil.getLong(processVariables, ProcessConstants.DEPT_ID);
           /* SysDept dept = deptService.selectDeptById(deptId);
            taskVo.setStartDeptName(dept.getcOrgFullnm());*/
            taskVoList.add(taskVo);
        }
        page.setRecords(taskVoList);
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<WfTaskVo> queryPageFList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        HistoricProcessInstanceQuery finished = historyService.createHistoricProcessInstanceQuery()
                .includeProcessVariables().finished()
                .orderByProcessInstanceEndTime()
                .orderByProcessInstanceStartTime()
                .desc();
        // 构建搜索条件
        if (StringUtils.isNotBlank(processQuery.getStartUserId())) {
            R<SysUser> userResult = remoteUserService.getUserInfoByNickName(processQuery.getStartUserId().trim(), SecurityConstants.INNER);
            if (R.isSuccess(userResult) && userResult.getData() != null) {
                processQuery.setStartUserId(userResult.getData().getUserId()+"");
            }
        }
        ProcessUtils.buildProcessSearch(finished, processQuery);
        if (StringUtils.isNotBlank(processQuery.getProcInsId())) {
            finished.processInstanceId(processQuery.getProcInsId());
        }
        if (StringUtils.isNotBlank(processQuery.getState())) {
            finished.variableValueEquals(ProcessConstants.PROCESS_STATUS_KEY, processQuery.getState());
        }
        if (StringUtils.isNotBlank(processQuery.getOperationType())) {
            finished.variableValueEquals("operationType", processQuery.getOperationType());
        }
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<HistoricProcessInstance> historicProcessInstances = finished
                .listPage(offset, pageQuery.getPageSize());
        page.setTotal(finished.count());
        List<WfTaskVo> taskVoList = new ArrayList<>();
        for (HistoricProcessInstance finishIns : historicProcessInstances) {
            WfTaskVo taskVo = new WfTaskVo();
            // 获取流程状态
            HistoricVariableInstance processStatusVariable = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(finishIns.getId())
                    .variableName(ProcessConstants.PROCESS_STATUS_KEY)
                    .singleResult();
            String processStatus = null;
            if (ObjectUtil.isNotNull(processStatusVariable)) {
                processStatus = Convert.toStr(processStatusVariable.getValue());
            }
            // 兼容旧流程
            if (processStatus == null) {
                processStatus = ObjectUtil.isNull(finishIns.getEndTime()) ? ProcessStatus.RUNNING.getStatus() : ProcessStatus.COMPLETED.getStatus();
            }
            taskVo.setProcessStatus(processStatus);
            taskVo.setCreateTime(finishIns.getStartTime());
            taskVo.setFinishTime(finishIns.getEndTime());
            taskVo.setProcInsId(finishIns.getId());

            // 计算耗时
            if (Objects.nonNull(finishIns.getEndTime())) {
                taskVo.setDuration(DateUtils.getDatePoor(finishIns.getEndTime(), finishIns.getStartTime()));
            } else {
                taskVo.setDuration(DateUtils.getDatePoor(DateUtils.getNowDate(), finishIns.getStartTime()));
            }
            // 流程部署实例信息
            Deployment deployment = repositoryService.createDeploymentQuery()
                    .deploymentId(finishIns.getDeploymentId()).singleResult();
            taskVo.setDeployId(finishIns.getDeploymentId());
            taskVo.setProcDefId(finishIns.getProcessDefinitionId());
            taskVo.setProcDefName(finishIns.getProcessDefinitionName());
            taskVo.setProcDefVersion(finishIns.getProcessDefinitionVersion());
            taskVo.setCategory(deployment.getCategory());
            // 当前所处流程
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(finishIns.getId()).includeIdentityLinks().list();
            if (CollUtil.isNotEmpty(taskList)) {
                taskVo.setTaskName(taskList.stream().map(Task::getName).filter(StringUtils::isNotEmpty).collect(Collectors.joining(",")));
            }
            Map<String, Object> processVariables = finishIns.getProcessVariables();
            taskVo.setOperationType(MapUtil.getStr(processVariables, "operationType"));
            taskVo.setTraceabilityCode(MapUtil.getStr(processVariables, ProcessConstants.TRACEBILITY_CODE));
            taskVo.setUrgentLevel(MapUtil.getStr(processVariables, ProcessConstants.URGENT_LEVEL));
            taskVo.setIssueType(MapUtil.getStr(processVariables, ProcessConstants.ISSUE_TYPE));
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(finishIns.getId())
                    .singleResult();

            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
            R<SysUser> userInfoById = remoteUserService.getUserInfoById(userId, SecurityConstants.INNER);
            if (R.isSuccess(userInfoById) && userInfoById.getData() != null) {
                taskVo.setStartUserName(userInfoById.getData().getNickName());
            }
            taskVo.setStartUserId(userId);
            Long deptId = MapUtil.getLong(processVariables, ProcessConstants.DEPT_ID);
           /* SysDept dept = deptService.selectDeptById(deptId);
            taskVo.setStartDeptName(dept.getcOrgFullnm());*/
            taskVoList.add(taskVo);
        }
        page.setRecords(taskVoList);
        return TableDataInfo.build(page);
    }

    /**
     * 获取流转记录中的某些值
     *
     * @param procInsId
     * @return
     */
    public List<Map<String, String>> getLifeCycle(String procInsId) {
        List<Map<String, String>> result = new ArrayList<>();
        // 获取流程实例
        HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(procInsId)
                .includeProcessVariables()
                .singleResult();
        if (ObjectUtil.isNull(historicProcIns)) {
            return result;
        }
        String processDefinitionName = historicProcIns.getProcessDefinitionName();
        List<WfProcNodeVo> wfProcNodeVos = historyProcNodeList(historicProcIns);
        List<WfProcNodeVo> collect = wfProcNodeVos.stream().sorted(Comparator.comparing(WfProcNodeVo::getEndTime)).collect(Collectors.toList());
        for (WfProcNodeVo wfProcNodeVo : collect) {
            Map<String, String> map = new HashMap<>();
            //角色activityName、姓名assigneeName、时间endTime、意见fullMessage
            //endEvent:结束，startEvent:开始
            String activityType = wfProcNodeVo.getActivityType();
            if (!"endEvent".equals(activityType)) {
                map.put("processName", processDefinitionName);
                String activityName = wfProcNodeVo.getActivityName();
                map.put("activityName", !"startEvent".equals(activityType) ? activityName : "发起流程");
                String assigneeName = wfProcNodeVo.getAssigneeName();
                map.put("assigneeName", assigneeName);
                Date endTime = wfProcNodeVo.getEndTime();
                map.put("endTime", DateUtils.dateTimeFormatr(endTime, "yyyy-MM-dd HH:mm:ss"));
                String fullMessage = !"startEvent".equals(activityType) ? wfProcNodeVo.getCommentList().get(0).getFullMessage() : "-";
                map.put("fullMessage", fullMessage);
                result.add(map);
            }
        }
        return result;
    }

    private final WfCopyMapper baseMapper;


    /**
     * 催办
     *
     * @param assigneeIds 指定的人员id
     * @param procInsId   流程id
     * @param taskDefKey  当前节点
     */
    @Override
    public int hasten(String assigneeIds, String procInsId, String taskDefKey) {
        /*SysMessagePush sysMessagePush = sysMessagePushMapper.selectMsgByFlowIdAndNodeId(new SysMessagePush(procInsId, taskDefKey));
        if (ObjectUtil.isNotNull(sysMessagePush)) {
            return -1;
        }*/
        List<String> userPhoneList = new ArrayList<>();
        Set<String> userNameList = new HashSet<>();
        Set<Long> userIdList = new HashSet<>();
        if (StringUtils.isNotBlank(assigneeIds)) {
            String[] split = StringUtils.split(assigneeIds, ",");
            userIdList = Arrays.stream(split)
                    .map(String::trim)
                    .filter(StringUtils::isNumeric)
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        } else {
            List<Task> list = taskService.createTaskQuery().processInstanceId(procInsId)
                    .includeProcessVariables()
                    .includeIdentityLinks().list();
            if (CollectionUtils.isNotEmpty(list)) {
                for (Task task : list) {
                    List<HistoricIdentityLink> linksForTask = historyService.getHistoricIdentityLinksForTask(task.getId());
                    Map<String, Object> variables = taskService.getVariables(task.getId());
                    for (HistoricIdentityLink identityLink : linksForTask) {
                        if (StringUtils.isNotBlank(identityLink.getUserId())) {
                            Long userId = Long.parseLong(identityLink.getUserId());
                            userIdList.add(userId);
                        } else if (StringUtils.isNotBlank(identityLink.getGroupId())) {
                            if (identityLink.getGroupId().startsWith(TaskConstants.ROLE_GROUP_PREFIX)) {
                                Long roleId = Long.parseLong(StringUtils.stripStart(identityLink.getGroupId(), TaskConstants.ROLE_GROUP_PREFIX).split("-")[0]);
                                Long deptId = MapUtil.getLong(variables, "deptId");
                                Long maxRole = MapUtil.getLong(variables, "roleLevel");
                               /* Long deptIdByUserRoldAndDeptId = deptService.getDeptIdByUserRoldAndDeptIdAndMaxRole(deptId, maxRole,roleId);
                                SysUser user = new SysUser();
                                user.setRoleId(roleId);
                                user.setDeptId(deptIdByUserRoldAndDeptId);
                                List<SysUser> sysUsers = userMapper.selectAllocatedList(user);
                                Set<Long> collect = sysUsers.stream().map(SysUser::getUserId).collect(Collectors.toSet());
                                userIdList.addAll(collect);*/
                            } else if (identityLink.getGroupId().startsWith(TaskConstants.DEPT_GROUP_PREFIX)) {
                                Long deptId = Long.parseLong(StringUtils.stripStart(identityLink.getGroupId(), TaskConstants.DEPT_GROUP_PREFIX));
                                /*List<SysUser> sysUsers = userMapper.selectUserByDeptId(deptId);
                                Set<Long> collect = sysUsers.stream().map(SysUser::getUserId).collect(Collectors.toSet());
                                userIdList.addAll(collect);*/
                            }
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isEmpty(userIdList)) {
            return -2;
        }
        /*List<SysUser> sysUsers = userMapper.selectUserByIds(new ArrayList<>(userIdList));
        String deptId = "";
        for (int i = 0; i < sysUsers.size(); i++) {
            SysUser sysUser = sysUsers.get(i);
            if (i == 0) {
                deptId = sysUser.getDeptId() + "";
            }
            userPhoneList.add(sysUser.getPhonenumber());
            userNameList.add(sysUser.getNickName());
        }
        CompletableFuture<Integer> hasten = messagePushService.returnSendSysMessagePush(Arrays.asList(deptId),
                userPhoneList, userIdList, userNameList, "hasten", procInsId, taskDefKey);
        try {
            return hasten.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
        return 1;
    }

    @Override
    public List<Map<String, String>> getFlowModel() {
        return reportMapper.selectFlowModel();
    }

    @Override
    public TableDataInfo<WfReport> getReportList1(WfReport report, PageQuery pageQuery) {
        Page<WfReport> page = new Page<>();
        List<WfReport> wfFormVos = reportMapper.selectReportDetail(report);
        if (CollectionUtils.isEmpty(wfFormVos)) {
            return TableDataInfo.build();
        }
        List<WfReport> paginate = PageUtils.paginate(wfFormVos, pageQuery.getPageNum(), pageQuery.getPageSize());
        for (WfReport wfReport : paginate) {
            List<WfReportNode> nodeList = wfReport.getNodeList();
            nodeList.forEach(node -> {
                node.setDurationFormat(DateUtil.formatBetween(Long.parseLong(node.getDuration()), BetweenFormatter.Level.SECOND));
            });
        }
        page.setTotal(wfFormVos.size());
        page.setRecords(paginate);
        int size = wfFormVos.stream().max(Comparator.comparingInt(e -> e.getNodeList().size())).get().getNodeList().size();
        TableDataInfo<WfReport> build = TableDataInfo.build(page);
        Map<String, Object> sizeMap = new HashMap<>();
        sizeMap.put("maxSize", size);
//        build.setOtherInfos(sizeMap);
        return build;
    }

    @Override
    public List<WfReport> getReportList(WfReport report) {
        List<WfReport> wfFormVos = reportMapper.selectReportList(report);
        wfFormVos.forEach(wfReport -> {
            wfReport.setLcVersion("V" + wfReport.getLcVersion());
            wfReport.setDurationFormat(DateUtil.formatBetween(Long.parseLong(wfReport.getDuration()), BetweenFormatter.Level.SECOND));
            wfReport.setDurationMinute(TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(wfReport.getDuration())) + "");
        });
        return wfFormVos;
    }

    //新增数据
    private void fillResult(Map<String, Object> resultTemp, String deptName, long count) {
        resultTemp.put("deptName", deptName);
        resultTemp.put("startedCount", count);
    }

}
